public class ExamThreadCompany {
    public static void main(String[] args) {
        System.out.println("programmed by 上田剛瑠");
        Engineer engineer = new Engineer();
        Salesman salesman = new Salesman();
        engineer.start();
        salesman.start();
    }
}

class Engineer extends Thread {
    @Override
    public void run() {
        System.out.println("車を作っています");
    }
}

class Salesman extends Thread {
    @Override
    public void run() {
        System.out.println("車を売っています");
    }
} 