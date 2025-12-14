public class ExamSpringPotentialWithDamper1 {
    public static void main(String[] args) {

        if (args.length < 6) {
            System.out.println("Usage: java ExamSpringPotentialWithDamper1 m k C x0 v0 stopTime");
            System.out.println("  m        : 質量[kg]");
            System.out.println("  k        : バネ定数[N/m]");
            System.out.println("  C        : 減衰係数[N·s/m]");
            System.out.println("  x0       : 初期位置[m]");
            System.out.println("  v0       : 初速度[m/s]");
            System.out.println("  stopTime : 計算終了時刻[s]");
            return;
        }

        // コマンドライン引数
        double m       = Double.parseDouble(args[0]); // 質量
        double k       = Double.parseDouble(args[1]); // バネ定数
        double c       = Double.parseDouble(args[2]); // 減衰係数
        double x       = Double.parseDouble(args[3]); // 位置
        double v       = Double.parseDouble(args[4]); // 速度
        double stop    = Double.parseDouble(args[5]); // 計算終了時刻

        // シミュレーション条件
        double t   = 0.0;      // 時刻
        double dt  = 0.001;    // 時間刻み
        double h   = 0.001;    // 数値微分用の刻み
        int step   = 0;

        System.out.println("programmed by 上田剛瑠");
        System.out.println("m=" + m + " k=" + k + " C=" + c +
                           " x0=" + x + " v0=" + v + " stopTime=" + stop);
        System.out.println("------------------------------------------------------------");
        System.out.println("   T[s]       x[m]       v[m/s]   Fspring[N]   Fdamper[N]   Ftotal[N]");

        while (t <= stop + 1.0e-12) {

            // ポテンシャルエネルギー U(x) = 1/2 k x^2
            double up = 0.5 * k * (x + h) * (x + h); // U(x + h)
            double um = 0.5 * k * (x - h) * (x - h); // U(x - h)

            // バネの力（中心差分）Fspring(x) ≒ - (U(x+h) - U(x-h)) / (2h)
            double fSpring = - (up - um) / (2.0 * h);

            // ダンパの力 Fdamper = -C * v
            double fDamper = -c * v;

            // 合力
            double fTotal = fSpring + fDamper;

            // 100ステップごとに出力（小数4桁）
            if (step % 100 == 0) {
                System.out.printf(
                    "%8.4f   %8.4f   %10.4f   %10.4f   %10.4f   %10.4f%n",
                    t, x, v, fSpring, fDamper, fTotal
                );
            }

            // 数値シミュレーション（オイラー法）
            double a = fTotal / m;  // 加速度
            v = v + a * dt;         // 速度更新
            x = x + v * dt;         // 位置更新
            t = t + dt;             // 時刻更新

            step++;
        }
    }
}
