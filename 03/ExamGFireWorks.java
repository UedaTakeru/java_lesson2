/* <applet code="ExamGFireWorks.class" width="900" height="700"></applet> */

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JApplet;

public class ExamGFireWorks extends JApplet implements Runnable {

    private Thread thread = null;       // アニメーション用スレッド
    private int width, height;          // 画面サイズ
    private int cycletime = 50;         // 1フレームあたりの目標時間[ms]

    private Image workImage;            // ダブルバッファ用イメージ
    private Graphics workG;             // workImage に描画する Graphics

    private Fire[] fires;               // 花火の配列
    private int numOfFires = 30;        // 花火の本数

    private double dt = 0.1;            // シミュレーションの時間刻み[sec]
    private double launchSpeed = 60.0;  // 打ち上げ速度の基準値
    private double speedVariation = 20.0; // 打ち上げ速度のばらつき

    @Override
    public void init() {
        System.out.println("programmed by 上田剛瑠");
        // 画面サイズ取得
        width  = getWidth();
        height = getHeight();

        // ダブルバッファリング用の画像と Graphics を準備
        workImage = createImage(width, height);
        workG     = workImage.getGraphics();

        // スレッド開始
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        // 花火の初期配置
        setupFire();

        while (thread != null) {
            long start = System.currentTimeMillis();

            // 1ステップ分のシミュレーションと描画
            paintWork(workG);
            repaint();

            long time = System.currentTimeMillis() - start;
            long sleepTime = cycletime - time;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    // 無視
                }
            } else {
                System.out.println("コマ落ちがおこりました");
            }
        }
    }

    /** 花火を初期化して配列に格納する */
    private void setupFire() {
        fires = new Fire[numOfFires];

        for (int i = 0; i < numOfFires; i++) {
            Fire fire;
            int kind = i % 3;  // 0:噴水, 1:菊, 2:リング

            if (kind == 0) {
                // 噴水型花火
                FountainFire f = new FountainFire(workG, 80, width, height, dt);
                f.x = (int)(width * Math.random());
                f.y = height;  // 画面下から噴き出す
                f.color = randomColor();
                f.launchSpeed     = 80;
                f.speedVariation  = 30;
                f.duration        = 25;
                fire = f;

            } else if (kind == 1) {
                // 菊型打ち上げ花火
                KikuFire k = new KikuFire(workG, width, height, dt);
                k.setCount(40);              // 星の数
                k.x = (int)(width * Math.random());
                k.y = height;                // 地面から打ち上げ
                k.starType = "菊";
                k.setColor(randomColor());
                k.len      = 9.0;            // しだれの長さ
                k.duration = 3.0;            // 星の燃焼時間
                k.fade     = 6.0;            // 消滅までの時間

                double speed = launchSpeed + speedVariation * Math.random();
                k.launchSpeed     = speed;   // 打ち上げ速度
                k.explosionSpeed  = speed * 0.3; // 星の爆発速度
                k.countDownTime   = 10.0 * Math.random(); // 最初の打ち上げまでの待ち時間

                fire = k;

            } else {
                // 新しく考案したリング花火
                RingFire r = new RingFire(workG, width, height, dt);
                r.setPosition(
                    (int)(width * Math.random()),
                    (int)(height * 0.3 + height * 0.3 * Math.random()) // 画面中央あたり
                );
                r.setColor(randomColor());
                fire = r;
            }

            fires[i] = fire;
        }

        // 最初の点火
        for (int i = 0; i < numOfFires; i++) {
            fires[i].fire();
        }
    }

    /** ランダムな色を返すヘルパ */
    private Color randomColor() {
        int r = (int)(255.0 * Math.random());
        int g = (int)(255.0 * Math.random());
        int b = (int)(255.0 * Math.random());
        return new Color(r, g, b);
    }

    /** ダブルバッファ用イメージに 1フレーム分の描画を行う */
    private void paintWork(Graphics g) {
        // 背景を黒で塗りつぶし
        g.setColor(Color.black);
        g.fillRect(0, 0, width, height);

        // すべての花火について 1ステップ進める
        for (int i = 0; i < numOfFires; i++) {
            Fire fire = fires[i];

            // step() が 0 を返したら「停止状態」なので再点火の準備
            if (fire.step() == 0) {

                if (fire instanceof FountainFire) {
                    FountainFire ff = (FountainFire)fire;
                    ff.x = (int)(width * Math.random());
                    ff.y = height;
                    ff.color = randomColor();
                    ff.fire();

                } else if (fire instanceof KikuFire) {
                    KikuFire k = (KikuFire)fire;
                    k.x = (int)(width * Math.random());
                    k.y = height;
                    k.setColor(randomColor());

                    double speed = launchSpeed + speedVariation * Math.random();
                    k.launchSpeed     = speed;
                    k.explosionSpeed  = speed * 0.3;
                    k.countDownTime   = 10.0 * Math.random();

                    k.fire();

                } else if (fire instanceof RingFire) {
                    RingFire r = (RingFire)fire;
                    r.setPosition(
                        (int)(width * Math.random()),
                        (int)(height * 0.3 + height * 0.3 * Math.random())
                    );
                    r.setColor(randomColor());
                    r.fire();
                }
            }
        }

        // 署名（自分の名前）
        g.setColor(Color.WHITE);
        g.setFont(new Font("Serif", Font.PLAIN, 20));
        g.drawString("ExamGFireWorks - programmed by 上田剛瑠", 10, height - 20);
    }

    @Override
    public void update(Graphics g) {
        // チラつきを防ぐため、paint() だけ呼ぶ
        paint(g);
    }

    @Override
    public void paint(Graphics g) {
        // ダブルバッファの内容を画面に貼り付け
        g.drawImage(workImage, 0, 0, width, height, null);
    }

    //====================================================
    // 花火のベースクラス
    //====================================================
    static abstract class Fire {
        protected final Graphics g;
        public int x;
        public int y;
        public Color color = Color.WHITE;

        protected Fire(Graphics g) {
            this.g = g;
        }

        /** 初期化（再点火） */
        public abstract void fire();

        /** 1ステップ進める。0なら停止、1なら継続。 */
        public abstract int step();
    }

    //====================================================
    // 噴水型花火
    //====================================================
    static class FountainFire extends Fire {
        private final int maxParticles;
        private final int w;
        private final int h;
        private final double dt;
        private final ArrayList<Particle> particles = new ArrayList<>();
        public double launchSpeed = 80;
        public double speedVariation = 30;
        public double duration = 25;
        private double t = 0.0;

        public FountainFire(Graphics g, int maxParticles, int w, int h, double dt) {
            super(g);
            this.maxParticles = maxParticles;
            this.w = w;
            this.h = h;
            this.dt = dt;
        }

        @Override
        public void fire() {
            particles.clear();
            t = 0.0;
        }

        @Override
        public int step() {
            t += dt;
            // 適度に粒を追加
            if (particles.size() < maxParticles) {
                for (int i = 0; i < 5; i++) {
                    double spd = launchSpeed + (Math.random() - 0.5) * speedVariation;
                    double angle = Math.toRadians(60 + Math.random() * 60); // 上向きにばらつき
                    double vx = spd * Math.cos(angle) * 0.1;
                    double vy = -spd * Math.sin(angle) * 0.1; // 上向きなのでマイナス
                    particles.add(new Particle(x, y, vx, vy, 2.0 + Math.random()));
                }
            }

            // 描画しながら更新
            g.setColor(color);
            Iterator<Particle> it = particles.iterator();
            while (it.hasNext()) {
                Particle p = it.next();
                p.vy += 9.8 * dt * 0.4; // 重力っぽい加速度
                p.x += p.vx * dt;
                p.y += p.vy * dt;
                p.life -= dt;
                if (p.life <= 0 || p.y > h || p.x < 0 || p.x > w) {
                    it.remove();
                    continue;
                }
                g.fillOval((int)p.x, (int)p.y, 3, 3);
            }

            // 一定時間経過し、粒が無ければ終了
            return (t > duration && particles.isEmpty()) ? 0 : 1;
        }
    }

    //====================================================
    // 菊型花火
    //====================================================
    static class KikuFire extends Fire {
        private final int w;
        private final int h;
        private final double dt;
        private final ArrayList<Particle> particles = new ArrayList<>();

        public String starType = "菊";
        public double len = 9.0;
        public double duration = 3.0;
        public double fade = 6.0;
        public double launchSpeed = 60.0;
        public double explosionSpeed = 20.0;
        public double countDownTime = 0.0;
        private int count = 30;

        private enum Phase { COUNTDOWN, LAUNCH, BURST }
        private Phase phase = Phase.COUNTDOWN;
        private double timer = 0.0;
        private double vy;

        public KikuFire(Graphics g, int w, int h, double dt) {
            super(g);
            this.w = w;
            this.h = h;
            this.dt = dt;
        }

        public void setCount(int c) {
            this.count = Math.max(5, c);
        }

        public void setColor(Color c) {
            this.color = c;
        }

        @Override
        public void fire() {
            phase = Phase.COUNTDOWN;
            timer = 0.0;
            vy = -launchSpeed * 0.15; // 上向き
            particles.clear();
        }

        @Override
        public int step() {
            timer += dt;
            switch (phase) {
                case COUNTDOWN:
                    if (timer >= countDownTime) {
                        phase = Phase.LAUNCH;
                    }
                    break;
                case LAUNCH:
                    vy += 9.8 * dt * 0.4;
                    y += vy * dt;
                    g.setColor(color);
                    g.fillRect(x, y, 3, 3);
                    if (vy >= 0 || y < h * 0.2) {
                        explode();
                        phase = Phase.BURST;
                    }
                    break;
                case BURST:
                    if (particles.isEmpty()) {
                        return 0;
                    }
                    g.setColor(color);
                    Iterator<Particle> it = particles.iterator();
                    while (it.hasNext()) {
                        Particle p = it.next();
                        p.vy += 9.8 * dt * 0.4;
                        p.x += p.vx * dt;
                        p.y += p.vy * dt;
                        p.life -= dt;
                        if (p.life <= 0 || p.x < 0 || p.x > w || p.y > h) {
                            it.remove();
                            continue;
                        }
                        g.fillOval((int)p.x, (int)p.y, 3, 3);
                    }
                    break;
                default:
                    break;
            }
            return 1;
        }

        private void explode() {
            particles.clear();
            for (int i = 0; i < count; i++) {
                double angle = 2 * Math.PI * i / count;
                double spd = explosionSpeed * (0.6 + 0.4 * Math.random());
                double vx = spd * Math.cos(angle) * 0.1;
                double vyStar = spd * Math.sin(angle) * 0.1;
                particles.add(new Particle(x, y, vx, vyStar, duration + Math.random() * fade));
            }
        }
    }

    //====================================================
    // 新しく考案した花火：RingFire
    //====================================================
    /** 円形に星が広がるリング状の花火 */
    static class RingFire extends Fire {
        private int cx, cy;       // 花火の中心
        private double t = 0.0;   // 経過時間
        private double dt;        // 時間刻み
        private double duration = 3.0; // 持続時間
        private double maxRadius;      // 最大半径
        private Color baseColor = Color.WHITE;

        public RingFire(Graphics g, int w, int h, double dt) {
            super(g);
            this.dt = dt;
            this.maxRadius = Math.min(w, h) * 0.4;
        }

        public void setPosition(int x, int y) {
            this.cx = x;
            this.cy = y;
        }

        public void setColor(Color c) {
            this.baseColor = c;
        }

        @Override
        public void fire() {
            t = 0.0;  // 時間リセット
        }

        @Override
        public int step() {
            t += dt;
            if (t > duration) {
                return 0; // 終了
            }

            double progress = t / duration;       // 0.0〜1.0
            double radius   = maxRadius * progress;

            g.setColor(baseColor);

            int n = 50; // 星の数
            for (int i = 0; i < n; i++) {
                double angle = 2.0 * Math.PI * i / n;
                int px = (int)(cx + radius * Math.cos(angle));
                int py = (int)(cy - radius * Math.sin(angle)); // 上がマイナス

                g.fillOval(px - 2, py - 2, 4, 4);
            }

            return 1; // 動作中
        }
    }

    //====================================================
    // パーティクル（共通ヘルパ）
    //====================================================
    static class Particle {
        double x;
        double y;
        double vx;
        double vy;
        double life;

        Particle(double x, double y, double vx, double vy, double life) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.life = life;
        }
    }
}
