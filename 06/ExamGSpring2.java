import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
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
 * ExamGSpring2
 *
 * 2次元のバネの動きをアニメーションで示すプログラム。
 * - バネの一端は原点に固定
 * - 質点の初期位置(x0, y0)・初速度(vx0, vy0)・質量m・バネ定数kを指定
 * - 運動方程式: F = -k (x, y)
 * - (-1, -1)〜(1, 1) の範囲に描画
 *
 * programmed by 上田剛瑠
 */
public class ExamGSpring2 extends JFrame {

    // デフォルト値
    private static final double DEFAULT_M   = 1.0;
    private static final double DEFAULT_K   = 10.0;
    private static final double DEFAULT_X0  = 0.5;
    private static final double DEFAULT_Y0  = 0.0;
    private static final double DEFAULT_VX0 = 0.0;
    private static final double DEFAULT_VY0 = 0.5;
    private static final double DEFAULT_DT  = 0.005;
    private static final double DEFAULT_STOP = 10.0;
    private static final int    DEFAULT_COILS = 12;   // バネの「山」の数
    private static final double DEFAULT_COIL_WIDTH = 0.1; // バネのふくらみ幅

    // 入力フィールド
    private final JTextField mField;
    private final JTextField kField;
    private final JTextField x0Field;
    private final JTextField y0Field;
    private final JTextField vx0Field;
    private final JTextField vy0Field;
    private final JTextField dtField;
    private final JTextField stopField;
    private final JTextField coilsField;
    private final JTextField widthField;

    private final JButton startButton;
    private final JButton stopButton;

    private final SimulationPanel simPanel;

    public ExamGSpring2() {
        super("ExamGSpring2");
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

        controlPanel.add(new JLabel("coils"));
        coilsField = new JTextField(Integer.toString(DEFAULT_COILS), 6);
        controlPanel.add(coilsField);

        controlPanel.add(new JLabel("coilWidth"));
        widthField = new JTextField(Double.toString(DEFAULT_COIL_WIDTH), 6);
        controlPanel.add(widthField);

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
            double x0  = parseDouble(x0Field,  DEFAULT_X0);
            double y0  = parseDouble(y0Field,  DEFAULT_Y0);
            double vx0 = parseDouble(vx0Field, DEFAULT_VX0);
            double vy0 = parseDouble(vy0Field, DEFAULT_VY0);
            double dt  = parseDouble(dtField,  DEFAULT_DT);
            double stop = parseDouble(stopField, DEFAULT_STOP);
            int coils   = parseInt(coilsField, DEFAULT_COILS);
            double width = parseDouble(widthField, DEFAULT_COIL_WIDTH);

            simPanel.startSimulation(m, k, x0, y0, vx0, vy0, dt, stop, coils, width);
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

    private int parseInt(JTextField field, int defaultValue) {
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException e) {
            field.setText(Integer.toString(defaultValue));
            return defaultValue;
        }
    }

    // --- シミュレーションパネル ---
    private static class SimulationPanel extends JPanel implements Runnable {

        // 物理パラメータ
        private double m, k;
        private double x, y, vx, vy;
        private double dt, time, stopTime;
        private int coils;
        private double coilWidth;

        private final List<Point2D.Double> trail = new ArrayList<>();
        private Thread thread;
        private volatile boolean running = false;

        // オフスクリーンバッファ（簡易トリプルバッファ風）
        private Image offImage;
        private Graphics2D offG;

        public SimulationPanel() {
            setPreferredSize(new Dimension(800, 600));
            setBackground(Color.BLACK);
        }

        public synchronized void startSimulation(double m, double k,
                                                 double x0, double y0,
                                                 double vx0, double vy0,
                                                 double dt, double stopTime,
                                                 int coils, double coilWidth) {
            // 前回のスレッドを止める
            running = false;

            this.m = m;
            this.k = k;
            this.x = x0;
            this.y = y0;
            this.vx = vx0;
            this.vy = vy0;
            this.dt = dt > 0 ? dt : 0.005;
            this.time = 0.0;
            this.stopTime = stopTime > 0 ? stopTime : 10.0;
            this.coils = Math.max(3, coils);
            this.coilWidth = coilWidth;

            trail.clear();
            trail.add(new Point2D.Double(x, y));

            running = true;
            thread = new Thread(this, "Spring2Simulation");
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
                renderOffscreen();
                repaint();

                try {
                    Thread.sleep(20); // おおよそ 50 fps
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            running = false;
        }

        // バネ運動の物理シミュレーション 1ステップ
        private void stepPhysics() {
            // 力 F = -k x, -k y
            double fx = -k * x;
            double fy = -k * y;

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
            double margin = 60.0;
            double usable = Math.min(w, h) - 2 * margin;
            if (usable <= 0) usable = Math.min(w, h) * 0.8;
            return usable / 2.0; // [-1,1] -> 幅2
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
            return (int) Math.round(cy - wy * scale);
        }

        // バネを描画するヘルパー
        private void drawSpring(Graphics2D g2,
                                double x1, double y1,
                                double x2, double y2,
                                int coils, double width) {

            // 始点と終点をスクリーン座標に変換
            int sx1 = worldToScreenX(x1);
            int sy1 = worldToScreenY(y1);
            int sx2 = worldToScreenX(x2);
            int sy2 = worldToScreenY(y2);

            double dx = sx2 - sx1;
            double dy = sy2 - sy1;
            double len = Math.sqrt(dx * dx + dy * dy);
            if (len == 0) {
                return;
            }

            // 方向ベクトル（スクリーン座標）
            double ex = dx / len;
            double ey = dy / len;

            // 垂直ベクトル（左右にふくらませるため）
            double px = -ey;
            double py = ex;

            // 実際に使う幅（ピクセル）
            double scale = getScale();
            double wPix = width * scale;

            // 始点から終点まで coils 山分のジグザグを描く
            int totalPoints = coils * 2 + 1; // 例: coils=3 → 7点
            int[] xs = new int[totalPoints];
            int[] ys = new int[totalPoints];

            // 最初の点：原点側
            xs[0] = sx1;
            ys[0] = sy1;

            for (int i = 1; i < totalPoints - 1; i++) {
                double t = (double) i / (totalPoints - 1); // 0〜1
                double bx = sx1 + dx * t;
                double by = sy1 + dy * t;

                // 奇数番目でふくらませる
                if (i % 2 == 1) {
                    double s = (i % 4 == 1) ? +1.0 : -1.0;
                    bx += px * wPix * s;
                    by += py * wPix * s;
                }

                xs[i] = (int) Math.round(bx);
                ys[i] = (int) Math.round(by);
            }

            // 最後の点：質点側
            xs[totalPoints - 1] = sx2;
            ys[totalPoints - 1] = sy2;

            g2.drawPolyline(xs, ys, totalPoints);
        }

        // オフスクリーンバッファに描画（簡易トリプルバッファ）
        private void renderOffscreen() {
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) return;

            if (offImage == null || offImage.getWidth(null) != w || offImage.getHeight(null) != h) {
                offImage = createImage(w, h);
                if (offG != null) {
                    offG.dispose();
                }
                offG = (Graphics2D) offImage.getGraphics();
                offG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                      RenderingHints.VALUE_ANTIALIAS_ON);
            }

            // 背景塗りつぶし
            offG.setColor(Color.BLACK);
            offG.fillRect(0, 0, w, h);

            // 原点
            int cx = worldToScreenX(0.0);
            int cy = worldToScreenY(0.0);
            offG.setColor(Color.DARK_GRAY);
            offG.fillOval(cx - 4, cy - 4, 8, 8);

            // 軌跡
            offG.setColor(Color.RED);
            Point2D.Double prev = null;
            for (Point2D.Double p : trail) {
                int sx = worldToScreenX(p.x);
                int sy = worldToScreenY(p.y);
                if (prev != null) {
                    int psx = worldToScreenX(prev.x);
                    int psy = worldToScreenY(prev.y);
                    offG.drawLine(psx, psy, sx, sy);
                }
                prev = p;
            }

            // バネ（原点 → 質点）
            offG.setColor(Color.WHITE);
            drawSpring(offG, 0.0, 0.0, x, y, coils, coilWidth);

            // 質点
            offG.setColor(Color.BLUE);
            int ballX = worldToScreenX(x);
            int ballY = worldToScreenY(y);
            int ballR = 8;
            offG.fillOval(ballX - ballR, ballY - ballR, ballR * 2, ballR * 2);

            // 時刻表示
            offG.setColor(Color.WHITE);
            offG.drawString(String.format("t = %.2f s", time), 10, 20);
            offG.drawString("programmed by 上田剛瑠", 10, h - 10);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (offImage != null) {
                g.drawImage(offImage, 0, 0, null);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ExamGSpring2 frame = new ExamGSpring2();
            frame.setVisible(true);
        });
    }
}
