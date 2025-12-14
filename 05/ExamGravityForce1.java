public class ExamGravityForce1 {
    public static void main(String[] args) {

        if (args.length < 4) {
            System.out.println("Usage: java ExamGravityForce1 m y0 vy0 stopTime");
            System.out.println("  m         : 質量[kg]");
            System.out.println("  y0        : 初期位置[m]");
            System.out.println("  vy0       : 初速度[m/s]");
            System.out.println("  stopTime  : 計算終了時刻[s]");
            return;
        }

        // コマンドライン引数の読み込み
        double m       = Double.parseDouble(args[0]); // 質量
        double y       = Double.parseDouble(args[1]); // 位置（シミュレーション用）
        double vy      = Double.parseDouble(args[2]); // 速度（シミュレーション用）
        double stop    = Double.parseDouble(args[3]); // 計算終了時刻

        // 理論値計算用に初期値を保持
        double y0  = y;
        double vy0 = vy;

        // シミュレーション条件
        double t   = 0.0;      // 時刻
        double dt  = 0.001;    // 時間刻み
        double g   = -9.8;     // 重力加速度（上向きを正とする）
        double fy  = m * g;    // 一定の重力（力としては常に一定）

        int step = 0;

        System.out.println("programmed by 上田剛瑠");
        System.out.println("m=" + m + " y0=" + y0 + " vy0=" + vy0 + " stopTime=" + stop);
        System.out.println("------------------------------------------------------------");
        System.out.println("   T[s]     y_calc[m]   vy_calc[m/s]   y_theory[m]   vy_theory[m/s]");

        while (t <= stop + 1.0e-12) {

            // 理論値の計算
            double vyTheory = vy0 + g * t;
            double yTheory  = y0 + vy0 * t + 0.5 * g * t * t;

            // 100ステップごとに結果を出力（小数4桁）
            if (step % 100 == 0) {
                System.out.printf(
                    "%8.4f   %10.4f   %12.4f   %10.4f   %14.4f%n",
                    t, y, vy, yTheory, vyTheory
                );
            }

            // 数値シミュレーション (オイラー法)
            vy = vy + (fy / m) * dt;  // 加速度 = fy/m
            y  = y + vy * dt;         // 位置更新
            t  = t + dt;              // 時刻更新

            step++;
        }
    }
}
