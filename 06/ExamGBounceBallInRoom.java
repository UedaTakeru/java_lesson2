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
 * ExamGBounceBallInRoom
 *
 * 球形の部屋（半径 1）の中で球を投げるシミュレーション。
 * 部屋の内壁はバネ＋ダンパで構成されており、
 * 球が壁にめり込んだ分だけバネの復元力と減衰力が働く。
 *
 * programmed by 上田剛瑠
 */
public class ExamGBounceBallInRoom extends JFrame {

    // デフォルト値
    private static final double DEFAULT_M   = 1.0;   // 質量
    private static final double DEFAULT_K   = 100.0; // バネ定数
    private static final double DEFAULT_C   = 1.0;   // 減衰係数
    private static final double DEFAULT_R   = 0.05;  // 球の半径
    private static final double DEFAULT_X0  = 0.1;   // 初期位置 x
    private static final double DEFAULT_Y0  = 0.0;   // 初期位置 y
    private static final double DEFAULT_VX0 = 0.0;   // 初速度 vx
    private static final double DEFAULT_VY0 = 0.5;   // 初速度 vy
    private static final double DEFAULT_DT  = 0.005; // 時間刻み
    private static final double DEFAULT_STOP = 10.0; // シミュレーション終了時刻

    private final SimulationPanel simPanel;

    private final JTextField mField;
    private final JTextField kField;
    private final JTextField cField;
    private final JTextField rField;
    private final JTextField x0Field;
    private final JTextField y0Field;
    private final JTextField vx0Field;
    private final JTextField vy0Field;
    private final JTextField dtField;
    private final JTextField stopField;

    private final JButton startButton;
    private final JButton stopButton;

    public ExamGBounceBallInRoom() {
        super("ExamGBounceBallInRoom");
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

        controlPanel.add(new JLabel("c"));
        cField = new JTextField(Double.toString(DEFAULT_C), 6);
        controlPanel.add(cField);

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
            double c   = parseDouble(cField,   DEFAULT_C);
            double r   = parseDouble(rField,   DEFAULT_R);
            double x0  = parseDouble(x0Field,  DEFAULT_X0);
            double y0  = parseDouble(y0Field,  DEFAULT_Y0);
            double vx0 = parseDouble(vx0Field, DEFAULT_VX0);
            double vy0 = parseDouble(vy0Field, DEFAULT_VY0);
            double dt  = parseDouble(dtField,  DEFAULT_DT);
            double stop = parseDouble(stopField, DEFAULT_STOP);

            simPanel.startSimulation(m, k, c, r, x0, y0, vx0, vy0, dt, stop);
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
        private static final double ROOM_RADIUS = 1.0; // 部屋の半径

        private double m, k, c, r;
        private double x, y, vx, vy;
        private double dt, time, stopTime;

        private final List<Point2D.Double> trail = new ArrayList<>();
        private Thread thread;
        private volatile boolean running = false;

        public SimulationPanel() {
            setPreferredSize(new Dimension(800, 600));
            setBackground(Color.BLACK);
        }

        public synchronized void startSimulation(double m, double k, double c, double r,
                                                 double x0, double y0, double vx0, double vy0,
                                                 double dt, double stopTime) {
            // 前回のスレッドを止める
            running = false;

            this.m = m;
            this.k = k;
            this.c = c;
            this.r = r;
            this.x = x0;
            this.y = y0;
            this.vx = vx0;
            this.vy = vy0;
            this.dt = dt > 0 ? dt : 0.005;
            this.time = 0.0;
            this.stopTime = stopTime > 0 ? stopTime : 10.0;

            trail.clear();
            trail.add(new Point2D.Double(x, y));

            running = true;
            thread = new Thread(this, "BounceBallInRoomSimulation");
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
                    Thread.sleep(20); // おおよそ 50 fps
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            running = false;
        }

        // 物理シミュレーション 1ステップ
        private void stepPhysics() {
            double fx = 0.0;
            double fy = 0.0;

            // 中心からの距離
            double d = Math.sqrt(x * x + y * y);

            // 球の外側が部屋の内壁を超えているか？
            // d + r > ROOM_RADIUS なら接触（めり込み）判定
            if (d + r > ROOM_RADIUS) {
                if (d == 0.0) {
                    // 原点ど真ん中なら、適当に法線を (1,0) とする
                    d = 1e-9;
                }
                double nx = x / d; // 外向き法線ベクトル
                double ny = y / d;

                double penetration = d + r - ROOM_RADIUS; // めり込み量（>0）

                // バネの復元力：内向き（-n 方向）
                double springMag = k * penetration;
                fx += -springMag * nx;
                fy += -springMag * ny;

                // 減衰力：法線方向の速度成分に比例して内向きに働く
                double vNormal = vx * nx + vy * ny; // 速度の法線成分
                double dampMag = c * vNormal;
                fx += -dampMag * nx;
                fy += -dampMag * ny;
            }

            // 運動方程式
            double ax = fx / m;
            double ay = fy / m;

            vx += ax * dt;
            vy += ay * dt;
            x  += vx * dt;
            y  += vy * dt;

            time += dt;
        }

        // ワールド座標 [-1,1] を画面にマッピング
        private double getScale() {
            int w = getWidth();
            int h = getHeight();
            double margin = 40.0;
            double usable = Math.min(w, h) - 2 * margin;
            if (usable <= 0) usable = Math.min(w, h) * 0.8;
            return usable / (2.0 * ROOM_RADIUS); // 半径1を中心 ±1 にマップ
        }

        private int worldToScreenX(double wx) {
            int w = getWidth();
            double cx = w / 2.0;
            double scale = getScale();
            return (int) Math.round(cx + wx * scale);
        }

        private int worldToScreenY(double wy) {
            int h = getHeight();
            double cy = h / 2.0;
            double scale = getScale();
            // y 上向きを画面座標に変換（画面は下向きが正）
            return (int) Math.round(cy - wy * scale);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            double scale = getScale();

            int cx = w / 2;
            int cy = h / 2;
            int roomPixelR = (int) Math.round(ROOM_RADIUS * scale);

            // 部屋の内壁（白い円）
            g2.setColor(Color.WHITE);
            g2.drawOval(cx - roomPixelR, cy - roomPixelR, roomPixelR * 2, roomPixelR * 2);

            // 軌跡（赤）
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

            // 球（青）
            int ballX = worldToScreenX(x);
            int ballY = worldToScreenY(y);
            int pixelR = (int) Math.max(3, Math.round(r * scale));

            g2.setColor(Color.BLUE);
            g2.fillOval(ballX - pixelR, ballY - pixelR, pixelR * 2, pixelR * 2);

            // 時刻
            g2.setColor(Color.WHITE);
            g2.drawString(String.format("t = %.2f s", time), 10, 20);
            g2.drawString("programmed by 上田剛瑠", 10, h - 10);

            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ExamGBounceBallInRoom frame = new ExamGBounceBallInRoom();
            frame.setVisible(true);
        });
    }
}
