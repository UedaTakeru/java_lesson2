public class ExamThreadCounter {
    public static void main(String[] args) {
        System.out.println("programmed by 上田剛瑠");
        Counter counter = new Counter();
        Thread adder = new Thread(new Adder(counter));
        Thread subtracter = new Thread(new Subtracter(counter));
        adder.start();
        subtracter.start();
        try {
            adder.join();
            subtracter.join();
        } catch (InterruptedException e) {
        }
        System.out.println("counterの値=" + counter.getValue());
    }
}

class Counter {
    private Integer value = 0;
    public void increment() {
        synchronized (this) {
            value++;
        }
    }
    public void decrement() {
        synchronized (this) {
            value--;
        }
    }
    public int getValue() {
        return value;
    }
}

class Adder implements Runnable {
    private final Counter counter;
    public Adder(Counter counter) {
        this.counter = counter;
    }
    @Override
    public void run() {
        for (int i = 0; i < 100; i++) {
            counter.increment();
        }
        System.out.println("100回インクリメントしました");
    }
}

class Subtracter implements Runnable {
    private final Counter counter;
    public Subtracter(Counter counter) {
        this.counter = counter;
    }
    @Override
    public void run() {
        for (int i = 0; i < 100; i++) {
            counter.decrement();
        }
        System.out.println("100回デクリメントしました");
    }
} 