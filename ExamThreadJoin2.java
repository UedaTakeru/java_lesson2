public class ExamThreadJoin2 {
    public static void main(String[] args) {
        System.out.println("programmed by 上田剛瑠");
        System.out.println("ExamThreadJoin　開始");
        Thread threadA = new Thread(new ThreadA());
        threadA.start();
        try {
            threadA.join();
        } catch (InterruptedException e) {
        }
        System.out.println("ExamThreadJoin　終了");
    }
}

class ThreadA implements Runnable {
    @Override
    public void run() {
        System.out.println("ThreadA 開始");
        Thread threadB = new Thread(new ThreadB());
        threadB.start();
        try {
            threadB.join();
        } catch (InterruptedException e) {
        }
        System.out.println("threadA 終了");
    }
}

class ThreadB implements Runnable {
    @Override
    public void run() {
        System.out.println("ThreadB 開始");
        System.out.println("threadB 終了");
    }
} 