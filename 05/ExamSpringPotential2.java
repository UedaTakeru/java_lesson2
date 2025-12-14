public class ExamSpringPotential2 {
    public static void main(String[] args) {

        if (args.length < 7) {
            System.out.println("Usage: java ExamSpringPotential2 m k x0 y0 vx0 vy0 stopTime");
            System.out.println("  m        : 質量[kg]");
            System.out.println("  k        : バネ定数[N/m]");
            System.out.println("  x0       : 初期位置x[m]");
            System.out.println("  y0       : 初期位置y[m]");
            System.out.println("  vx0      : 初速度vx[m/s]");
            System.out.println("  vy0      : 初速度vy[m/s]");
            System.out.println("  stopTime : 計算終了時刻[s]");
            return;
        }

        // コマンドライン引数
        double m    = Double.parseDouble(args[0]); // 質量
        double k    = Double.parseDouble(args[1]); // バネ定数
        double x    = Double.parseDouble(args[2]); // 位置 x
        double y    = Double.parseDouble(args[3]); // 位置 y
        double vx   = Double.parseDouble(args[4]); // 速度 vx
        double vy   = Double.parseDouble(args[5]); // 速度 vy
        double stop = Double.parseDouble(args[6]); // 計算終了時刻

        // シミュレーション条件
        double t  = 0.0;      // 時刻
        double dt = 0.001;    // 時間刻み
        double h  = 0.001;    // 数値微分用の刻み
        int step  = 0;

        System.out.println("programmed by 上田剛瑠");
        System.out.println("m=" + m + " k=" + k +
                           " x0=" + x + " y0=" + y +
                           " vx0=" + vx + " vy0=" + vy +
                           " stopTime=" + stop);
        System.out.println("------------------------------------------------------------");
        System.out.println("   T[s]     x[m]     y[m]    vx[m/s]   vy[m/s]");

        while (t <= stop + 1.0e-12) {

            // ポテンシャルエネルギー U(x, y) = 1/2 * k * (x^2 + y^2)
            double uxph_y  = 0.5 * k * ((x + h) * (x + h) + y * y); // U(x+h, y)
            double uxmh_y  = 0.5 * k * ((x - h) * (x - h) + y * y); // U(x-h, y)
            double ux_yph  = 0.5 * k * (x * x + (y + h) * (y + h)); // U(x, y+h)
            double ux_ymh  = 0.5 * k * (x * x + (y - h) * (y - h)); // U(x, y-h)

            // 中心差分で力を計算
            // fx = -∂U/∂x ≈ - (U(x+h,y) - U(x-h,y)) / (2h)
            // fy = -∂U/∂y ≈ - (U(x,y+h) - U(x,y-h)) / (2h)
            double fx = - (uxph_y - uxmh_y) / (2.0 * h);
            double fy = - (ux_yph - ux_ymh) / (2.0 * h);

            // 100ステップごとに出力（小数3桁）
            if (step % 100 == 0) {
                System.out.printf(
                    "%7.3f  %7.3f  %7.3f  %8.3f  %8.3f%n",
                    t, x, y, vx, vy
                );
            }

            // 数値シミュレーション（オイラー法）
            double ax = fx / m;
            double ay = fy / m;

            vx = vx + ax * dt;
            vy = vy + ay * dt;

            x  = x + vx * dt;
            y  = y + vy * dt;

            t  = t + dt;
            step++;
        }
    }
}
