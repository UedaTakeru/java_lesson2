public class ExamThread1 {
    public static void main(String[] args) {
        Thread t1 = new Thread(new MyThread("上田剛瑠1"));
        Thread t2 = new Thread(new MyThread("上田剛瑠2"));
        t1.start();
        t2.start();
    }
}

class MyThread implements Runnable {
    private final String name;
    public MyThread(String name) {
        this.name = name;
    }
    @Override
    public void run() {
        for (int i = 1; i <= 5; i++) {
            System.out.println(name + "の" + i + "回目");
        }
    }
} 