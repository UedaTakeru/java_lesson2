import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * ExamGSpringWithForce1
 *
 * 1次元バネ + ダンパ + 外力（強制振動）のシミュレーション。
 *
 * 運動方程式：
 *   m d^2x/dt^2 = -k x - c dx/dt + F0 sin(ω t)
 *
 * 上部：x(t) のグラフ（時間-変位）
 * 下部：左の壁に固定されたバネ + 質点のアニメーション
 *
 * programmed by 上田剛瑠
 */
public class ExamGSpringWithForce1 extends JFrame {

    // デフォルト値
    private static final double DEFAULT_M    = 1.0;   // 質量
    private static final double DEFAULT_K    = 10.0;  // バネ定数
    private static final double DEFAULT_C    = 0.5;   // 減衰係数
    private static final double DEFAULT_F0   = 1.0;   // 外力の振幅
    private static final double DEFAULT_OMEGA= 2.0;   // 外力の角振動数
    private static final double DEFAULT_X0   = 0.0;   // 初期変位
    private static final double DEFAULT_V0   = 0.0;   // 初期速度
    private static final double DEFAULT_DT   = 0.005; // 時間刻み
    private static final double DEFAULT_STOP = 20.0;  // シミュレーション終了時刻

    // 入力フィールド
    private final JTextField mField;
    private final JTextField kField;
    private final JTextField cField;
    private final JTextField f0Field;
    private final JTextField omegaField;
    private final JTextField x0Field;
    private final JTextField v0Field;
    private final JTextField dtField;
    private final JTextField stopField;

    private final JButton startButton;
    private final JButton stopButton;

    private final SimulationPanel simPanel;

    public ExamGSpringWithForce1() {
        super("ExamGSpringWithForce1");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        simPanel = new SimulationPanel();

        // ---- コントロールパネル ----
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

        controlPanel.add(new JLabel("F0"));
        f0Field = new JTextField(Double.toString(DEFAULT_F0), 6);
        controlPanel.add(f0Field);

        controlPanel.add(new JLabel("omega"));
        omegaField = new JTextField(Double.toString(DEFAULT_OMEGA), 6);
        controlPanel.add(omegaField);

        controlPanel.add(new JLabel("x0"));
        x0Field = new JTextField(Double.toString(DEFAULT_X0), 6);
        controlPanel.add(x0Field);

        controlPanel.add(new JLabel("v0"));
        v0Field = new JTextField(Double.toString(DEFAULT_V0), 6);
        controlPanel.add(v0Field);

        controlPanel.add(new JLabel("dt"));
        dtField = new JTextField(Double.toString(DEFAULT_DT), 6);
        controlPanel.add(dtField);

        controlPanel.add(new JLabel("stopTime"));
        stopField = new JTextField(Double.toString(DEFAULT_STOP), 6);
        controlPanel.add(stopField);

        // ---- ボタン ----
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

        // ---- イベントハンドラ ----
        startButton.addActionListener(e -> {
            double m     = parseDouble(mField,     DEFAULT_M);
            double k     = parseDouble(kField,     DEFAULT_K);
            double c     = parseDouble(cField,     DEFAULT_C);
            double f0    = parseDouble(f0Field,    DEFAULT_F0);
            double omega = parseDouble(omegaField, DEFAULT_OMEGA);
            double x0    = parseDouble(x0Field,    DEFAULT_X0);
            double v0    = parseDouble(v0Field,    DEFAULT_V0);
            double dt    = parseDouble(dtField,    DEFAULT_DT);
            double stop  = parseDouble(stopField,  DEFAULT_STOP);

            simPanel.startSimulation(m, k, c, f0, omega, x0, v0, dt, stop);
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

    // ---- シミュレーションパネル ----
    private static class SimulationPanel extends JPanel implements Runnable {

        // 物理パラメータ
        private double m, k, c;
        private double f0, omega;
        private double x, v;
        private double dt, time, stopTime;

        // グラフ用の履歴
        private final List<Double> timeList = new ArrayList<>();
        private final List<Double> xList    = new ArrayList<>();

        private Thread thread;
        private volatile boolean running = false;

        // オフスクリーンバッファ
        private Image offImage;
        private Graphics2D offG;

        public SimulationPanel() {
            setPreferredSize(new Dimension(800, 600));
            setBackground(Color.BLACK);
        }

        public synchronized void startSimulation(double m, double k, double c,
                                                 double f0, double omega,
                                                 double x0, double v0,
                                                 double dt, double stopTime) {
            // 前回のシミュレーションを停止
            running = false;

            this.m = m;
            this.k = k;
            this.c = c;
            this.f0 = f0;
            this.omega = omega;
            this.x = x0;
            this.v = v0;
            this.dt = dt > 0 ? dt : 0.005;
            this.time = 0.0;
            this.stopTime = stopTime > 0 ? stopTime : 20.0;

            timeList.clear();
            xList.clear();
            timeList.add(time);
            xList.add(x);

            running = true;
            thread = new Thread(this, "SpringWithForceSimulation");
            thread.start();
        }

        public void stopSimulation() {
            running = false;
        }

        @Override
        public void run() {
            while (running && time <= stopTime) {
                stepPhysics();
                timeList.add(time);
                xList.add(x);

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

        // 運動方程式の 1 ステップ（オイラー法）
        private void stepPhysics() {
            // 外力 F = F0 * sin(ω t)
            double fSpring = -k * x;
            double fDamp   = -c * v;
            double fDrive  = f0 * Math.sin(omega * time);

            double fTotal  = fSpring + fDamp + fDrive;
            double a       = fTotal / m;

            v    += a * dt;
            x    += v * dt;
            time += dt;
        }

        // オフスクリーンバッファに描画
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

            // 背景
            offG.setColor(Color.BLACK);
            offG.fillRect(0, 0, w, h);

            int margin = 40;
            int midY   = h / 2;

            // 上半分：x(t) グラフ
            int graphLeft   = margin;
            int graphRight  = w - margin;
            int graphTop    = margin;
            int graphBottom = midY - margin / 2;
            int graphWidth  = graphRight - graphLeft;
            int graphHeight = graphBottom - graphTop;

            // 軸
            offG.setColor(Color.GRAY);
            offG.drawRect(graphLeft, graphTop, graphWidth, graphHeight);
            offG.drawString("x(t)", graphLeft + 5, graphTop + 15);

            if (timeList.size() >= 2) {
                // t の最大値を stopTime としてスケーリング
                double tMax = stopTime;
                if (tMax <= 0) tMax = 1.0;

                // x のスケーリング用に、履歴から最大振幅をざっくり取得
                double maxAbsX = 0.0;
                for (double xv : xList) {
                    double ax = Math.abs(xv);
                    if (ax > maxAbsX) maxAbsX = ax;
                }
                double xRange = (maxAbsX > 0) ? maxAbsX * 1.2 : 1.0;
                double cx = graphTop + graphHeight / 2.0;

                offG.setColor(Color.GREEN);

                int n = timeList.size();
                int prevX = -1;
                int prevY = -1;
                for (int i = 0; i < n; i++) {
                    double tt = timeList.get(i);
                    double xx = xList.get(i);

                    double tx = (tt / tMax);
                    if (tx > 1.0) tx = 1.0; // はみ出し防止

                    int px = graphLeft + (int) Math.round(tx * graphWidth);
                    int py = (int) Math.round(cx - (xx / xRange) * (graphHeight / 2.0));

                    if (i > 0) {
                        offG.drawLine(prevX, prevY, px, py);
                    }
                    prevX = px;
                    prevY = py;
                }
            }

            // 下半分：バネ + 質点のアニメーション
            int animTop    = midY + margin / 2;
            int animBottom = h - margin;
            int railY      = (animTop + animBottom) / 2;

            int wallX      = margin;
            int animRight  = w - margin;
            int availableW = animRight - wallX - 80; // 質点の幅分ちょい余裕

            offG.setColor(Color.DARK_GRAY);
            // 壁
            offG.fillRect(wallX - 10, railY - 30, 10, 60);
            // レール
            offG.drawLine(wallX, railY, animRight, railY);

            // x の値を [-1, 1] に正規化（大きい振幅だとクリップ）
            double xNormRange = 1.0;
            double xn = x / xNormRange;
            if (xn > 1.0) xn = 1.0;
            if (xn < -1.0) xn = -1.0;

            int massX = wallX + (availableW / 2) + (int) Math.round(xn * (availableW / 2.0));
            int massWidth  = 40;
            int massHeight = 30;

            // バネ（壁 → 質点）
            offG.setColor(Color.WHITE);
            drawSpringPixels(offG, wallX, railY, massX - massWidth / 2, railY, 12, 10);

            // 質点
            offG.setColor(Color.BLUE);
            offG.fillRect(massX - massWidth / 2, railY - massHeight / 2, massWidth, massHeight);

            // 時刻表示など
            offG.setColor(Color.WHITE);
            offG.drawString(String.format("t = %.2f s", time), 10, h - 30);
            offG.drawString("programmed by 上田剛瑠", 10, h - 10);
        }

        // ピクセル座標でバネを描く（ジグザグポリライン）
        private void drawSpringPixels(Graphics2D g2,
                                      int x1, int y1,
                                      int x2, int y2,
                                      int coils, int ampPixels) {
            int nCoils = Math.max(3, coils);
            int totalPoints = nCoils * 2 + 1;

            double dx = x2 - x1;
            double dy = y2 - y1;
            double len = Math.sqrt(dx * dx + dy * dy);
            if (len == 0) return;

            double ex = dx / len;
            double ey = dy / len;

            // 垂直ベクトル（左右にふくらませる）
            double px = -ey;
            double py = ex;

            int[] xs = new int[totalPoints];
            int[] ys = new int[totalPoints];

            xs[0] = x1;
            ys[0] = y1;

            for (int i = 1; i < totalPoints - 1; i++) {
                double t = (double) i / (totalPoints - 1); // 0..1
                double bx = x1 + dx * t;
                double by = y1 + dy * t;

                if (i % 2 == 1) {
                    double s = (i % 4 == 1) ? +1.0 : -1.0;
                    bx += px * ampPixels * s;
                    by += py * ampPixels * s;
                }

                xs[i] = (int) Math.round(bx);
                ys[i] = (int) Math.round(by);
            }

            xs[totalPoints - 1] = x2;
            ys[totalPoints - 1] = y2;

            g2.drawPolyline(xs, ys, totalPoints);
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
            ExamGSpringWithForce1 frame = new ExamGSpringWithForce1();
            frame.setVisible(true);
        });
    }
}
