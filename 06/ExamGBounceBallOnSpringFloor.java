import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * ExamGBounceBallOnSpringFloor
 *
 * ダンパ付きバネでできた床の上で球を投げるシミュレーション。
 * - 床は y = 0
 * - y - r >= 0 のとき：重力のみ
 * - y - r < 0 のとき：重力 + バネの復元力 + 減衰力
 *
 * programmed by 上田剛瑠
 */
public class ExamGBounceBallOnSpringFloor extends JFrame {

    // デフォルト値
    private static final double DEFAULT_M   = 1.0;   // 質量
    private static final double DEFAULT_K   = 50.0;  // バネ定数
    private static final double DEFAULT_D   = 1.0;   // 減衰係数
    private static final double DEFAULT_R   = 0.2;   // 球の半径
    private static final double DEFAULT_X0  = 0.0;   // 初期位置x
    private static final double DEFAULT_Y0  = 2.0;   // 初期位置y
    private static final double DEFAULT_VX0 = 2.0;   // 初速度vx
    private static final double DEFAULT_VY0 = 0.0;   // 初速度vy
    private static final double DEFAULT_G   = -9.8;  // 重力加速度（上向きを正とする座標系）
    private static final double DEFAULT_DT  = 0.01;  // 時間刻み
    private static final double DEFAULT_STOP = 10.0; // シミュレーション終了時刻

    private final SimulationPanel simPanel;

    private final JTextField mField;
    private final JTextField kField;
    private final JTextField dField;
    private final JTextField rField;
    private final JTextField x0Field;
    private final JTextField y0Field;
    private final JTextField vx0Field;
    private final JTextField vy0Field;
    private final JTextField gField;
    private final JTextField dtField;
    private final JTextField stopField;

    private final JButton startButton;
    private final JButton stopButton;

    public ExamGBounceBallOnSpringFloor() {
        super("ExamGBounceBallOnSpringFloor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        simPanel = new SimulationPanel();

        // --- コントロールパネル ---
        JPanel controlPanel = new JPanel(new GridLayout(0, 4, 4, 4));

        controlPanel.add(new JLabel("m"));
        mField = new JTextField(Double.toString(DEFAULT_M), 6);
        controlPanel.add(mField);

        controlPanel.add(new JLabel("k"));
        kField = new JTextField(Double.toString(DEFAULT_K), 6);
        controlPanel.add(kField);

        controlPanel.add(new JLabel("d"));
        dField = new JTextField(Double.toString(DEFAULT_D), 6);
        controlPanel.add(dField);

        controlPanel.add(new JLabel("r"));
        rField = new JTextField(Double.toString(DEFAULT_R), 6);
        controlPanel.add(rField);

        controlPanel.add(new JLabel("x0"));
        x0Field = new JTextField(Double.toString(DEFAULT_X0), 6);
        controlPanel.add(x0Field);

        controlPanel.add(new JLabel("y0"));
        y0Field = new JTextField(Double.toString(DEFAULT_Y0), 6);
        controlPanel.add(y0Field);

        controlPanel.add(new JLabel("vx0"));
        vx0Field = new JTextField(Double.toString(DEFAULT_VX0), 6);
        controlPanel.add(vx0Field);

        controlPanel.add(new JLabel("vy0"));
        vy0Field = new JTextField(Double.toString(DEFAULT_VY0), 6);
        controlPanel.add(vy0Field);

        controlPanel.add(new JLabel("g"));
        gField = new JTextField(Double.toString(DEFAULT_G), 6);
        controlPanel.add(gField);

        controlPanel.add(new JLabel("dt"));
        dtField = new JTextField(Double.toString(DEFAULT_DT), 6);
        controlPanel.add(dtField);

        controlPanel.add(new JLabel("stopTime"));
        stopField = new JTextField(Double.toString(DEFAULT_STOP), 6);
        controlPanel.add(stopField);

        // --- ボタン ---
        startButton = new JButton("Start");
        stopButton  = new JButton("Stop");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        JPanel topPanel = new JPanel(new BorderLayout(4, 4));
        topPanel.add(controlPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(simPanel, BorderLayout.CENTER);

        // --- イベントハンドラ ---
        startButton.addActionListener(e -> {
            double m   = parseDouble(mField,   DEFAULT_M);
            double k   = parseDouble(kField,   DEFAULT_K);
            double d   = parseDouble(dField,   DEFAULT_D);
            double r   = parseDouble(rField,   DEFAULT_R);
            double x0  = parseDouble(x0Field,  DEFAULT_X0);
            double y0  = parseDouble(y0Field,  DEFAULT_Y0);
            double vx0 = parseDouble(vx0Field, DEFAULT_VX0);
            double vy0 = parseDouble(vy0Field, DEFAULT_VY0);
            double g   = parseDouble(gField,   DEFAULT_G);
            double dt  = parseDouble(dtField,  DEFAULT_DT);
            double stop = parseDouble(stopField, DEFAULT_STOP);

            simPanel.startSimulation(m, k, d, r, x0, y0, vx0, vy0, g, dt, stop);
        });

        stopButton.addActionListener(e -> simPanel.stopSimulation());

        pack();
        setLocationRelativeTo(null);
    }

    private double parseDouble(JTextField field, double defaultValue) {
        try {
            return Double.parseDouble(field.getText().trim());
        } catch (NumberFormatException e) {
            field.setText(Double.toString(defaultValue));
            return defaultValue;
        }
    }

    // --- シミュレーションパネル ---
    private static class SimulationPanel extends JPanel implements Runnable {

        // 物理パラメータ
        private double m, k, d, r, g;
        private double x, y, vx, vy;
        private double dt, time, stopTime;
        private double worldTop;

        private final List<Point2D.Double> trail = new ArrayList<>();
        private Thread thread;
        private volatile boolean running = false;

        public SimulationPanel() {
            setPreferredSize(new Dimension(800, 600));
            setBackground(Color.BLACK);
        }

        public synchronized void startSimulation(double m, double k, double d, double r,
                                                 double x0, double y0, double vx0, double vy0,
                                                 double g, double dt, double stopTime) {
            // 前回のスレッドを止める
            running = false;

            this.m = m;
            this.k = k;
            this.d = d;
            this.r = r;
            this.g = g;
            this.x = x0;
            this.y = y0;
            this.vx = vx0;
            this.vy = vy0;
            this.dt = dt > 0 ? dt : 0.01;
            this.time = 0.0;
            this.stopTime = stopTime > 0 ? stopTime : 10.0;

            // 表示範囲（上側）を決める
            this.worldTop = Math.max(y0 + r * 2.0, r * 4.0);

            trail.clear();
            trail.add(new Point2D.Double(x, y));

            running = true;
            thread = new Thread(this, "SpringFloorSimulation");
            thread.start();
        }

        public void stopSimulation() {
            running = false;
        }

        @Override
        public void run() {
            while (running && time <= stopTime) {
                stepPhysics();
                trail.add(new Point2D.Double(x, y));
                repaint();

                try {
                    Thread.sleep(20); // だいたい 50 fps くらい
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            running = false;
        }

        // 物理シミュレーションの1ステップ
        private void stepPhysics() {
            // 力の初期値：重力のみ
            double fx = 0.0;
            double fy = m * g; // g は負の値（下向き）

            // y - r < 0 なら床にめり込んでいる → バネ + 減衰
            if (y - r < 0.0) {
                double spring = k * (r - y); // y が下がるほど大きい上向きの力
                fy += spring;

                double dampingX = -d * vx;
                double dampingY = -d * vy;
                fx += dampingX;
                fy += dampingY;
            }

            double ax = fx / m;
            double ay = fy / m;

            vx += ax * dt;
            vy += ay * dt;
            x  += vx * dt;
            y  += vy * dt;

            time += dt;
        }

        // ワールド座標 → 画面座標 変換
        private double getScale() {
            int h = getHeight();
            int floorPixel = getFloorPixelY(h);
            double usableHeight = floorPixel - 40.0;
            if (usableHeight <= 0) usableHeight = h * 0.8;
            double top = (worldTop > 0) ? worldTop : 1.0;
            return usableHeight / top;
        }

        private int getFloorPixelY(int h) {
            return h - 50;
        }

        private int worldToScreenX(double wx) {
            int w = getWidth();
            double scale = getScale();
            double cx = w / 2.0;
            return (int) Math.round(cx + wx * scale);
        }

        private int worldToScreenY(double wy) {
            int h = getHeight();
            int floor = getFloorPixelY(h);
            double scale = getScale();
            return (int) Math.round(floor - wy * scale);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int floorY = getFloorPixelY(h);
            double scale = getScale();

            // 床の線 y=0
            g2.setColor(Color.GRAY);
            g2.drawLine(0, floorY, w, floorY);

            // 球の軌跡（赤）
            g2.setColor(Color.RED);
            Point2D.Double prev = null;
            for (Point2D.Double p : trail) {
                int sx = worldToScreenX(p.x);
                int sy = worldToScreenY(p.y);
                if (prev != null) {
                    int psx = worldToScreenX(prev.x);
                    int psy = worldToScreenY(prev.y);
                    g2.drawLine(psx, psy, sx, sy);
                }
                prev = p;
            }

            // 現在位置の球（青）
            int ballX = worldToScreenX(x);
            int ballY = worldToScreenY(y);
            int pixelR = (int) Math.max(3, Math.round(r * scale));

            g2.setColor(Color.BLUE);
            g2.fillOval(ballX - pixelR, ballY - pixelR, pixelR * 2, pixelR * 2);

            // 時刻表示
            g2.setColor(Color.WHITE);
            g2.drawString(String.format("t = %.2f s", time), 10, 20);

            // 署名
            g2.drawString("programmed by 上田剛瑠", 10, h - 10);

            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ExamGBounceBallOnSpringFloor frame = new ExamGBounceBallOnSpringFloor();
            frame.setVisible(true);
        });
    }
}
