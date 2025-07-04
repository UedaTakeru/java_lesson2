public class ExamTenThread {
    public static void main(String[] args) {
        System.out.println("programmed by 上田剛瑠");
        MyThread[] threads = new MyThread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new MyThread();
            threads[i].start();
        }
    }
}

class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println(getName());
    }
} 