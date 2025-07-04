public class ExamThreadJoin {
    public static void main(String[] args) {
        System.out.println("programmed by 上田剛瑠");
        System.out.println("ExamThreadJoin　開始");
        ThreadA a = new ThreadA();
        a.start();
        try {
            a.join();
        } catch (InterruptedException e) {
        }
        System.out.println("ExamThreadJoin　終了");
    }
}

class ThreadA extends Thread {
    @Override
    public void run() {
        System.out.println("ThreadA 開始");
        ThreadB b = new ThreadB();
        b.start();
        try {
            b.join();
        } catch (InterruptedException e) {
        }
        System.out.println("threadA 終了");
    }
}

class ThreadB extends Thread {
    @Override
    public void run() {
        System.out.println("ThreadB 開始");
        System.out.println("threadB 終了");
    }
} 