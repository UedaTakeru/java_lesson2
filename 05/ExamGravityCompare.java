public class ExamGravityCompare {
    public static void main(String[] args) {

        if (args.length < 4) {
            System.out.println("Usage: java ExamGravityCompare m y0 vy0 stopTime");
            return;
        }

        // コマンドライン引数の読み込み
        double m    = Double.parseDouble(args[0]); // 物体の質量
        double y    = Double.parseDouble(args[1]); // 初期位置（シミュレーション用）
        double vy   = Double.parseDouble(args[2]); // 初速度（シミュレーション用）
        double stop = Double.parseDouble(args[3]); // 計算終了時刻

        // 理論値計算用に初期値を保持
        double y0  = y;
        double vy0 = vy;

        // シミュレーション用の変数
        double t  = 0.0;   // 時刻
        double dt = 0.1;   // 時間刻み
        double g  = -9.8;  // 重力加速度
        double fy = m * g; // 物体に加わる重力（一定）

        System.out.println("programmed by 上田剛瑠");

        while (t <= stop) {
            // 理論値の計算
            double vyTheory = vy0 + g * t;
            double yTheory  = y0 + vy0 * t + 0.5 * g * t * t;

            // 理論値の出力
            System.out.println(
                "理論値 T=" + t + " y=" + yTheory + " vy=" + vyTheory
            );

            // シミュレーション値の出力
            System.out.println(
                "計算値 T=" + t + " y=" + y + " vy=" + vy
            );

            // 数値シミュレーション（ExGravityForce1 と同じ方法）
            vy = vy + fy / m * dt; // 次時刻の速度
            y  = y + vy * dt;      // 次時刻の位置
            t  = t + dt;           // 時刻更新
        }
    }
}
