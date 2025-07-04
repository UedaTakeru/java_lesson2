import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ExamGBouncingBall extends JPanel implements Runnable {
    int x, y, vx, vy, radius = 10;
    Thread thread;

    public ExamGBouncingBall(int x, int y, int vx, int vy) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        setPreferredSize(new Dimension(400, 300));
        thread = new Thread(this);
        thread.start();
    }

    public void run() {
        while (true) {
            x += vx;
            y += vy;
            if (x < radius || x > getWidth() - radius) vx = -vx;
            if (y < radius || y > getHeight() - radius) vy = -vy;
            repaint();
            try { Thread.sleep(50); } catch (InterruptedException e) {}
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLUE);
        g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    public static void main(String[] args) {
        int x = args.length > 0 ? Integer.parseInt(args[0]) : 100;
        int y = args.length > 1 ? Integer.parseInt(args[1]) : 100;
        int vx = args.length > 2 ? Integer.parseInt(args[2]) : 3;
        int vy = args.length > 3 ? Integer.parseInt(args[3]) : 4;
        JFrame frame = new JFrame("ExamGBouncingBall");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ExamGBouncingBall(x, y, vx, vy));
        frame.pack();
        frame.setVisible(true);
    }
}