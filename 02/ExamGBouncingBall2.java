import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ExamGBouncingBall2 extends JPanel implements Runnable {
    int x1, y1, vx1, vy1, x2, y2, vx2, vy2, r = 20;
    Thread thread;

    public ExamGBouncingBall2(int x1, int y1, int vx1, int vy1, int x2, int y2, int vx2, int vy2) {
        this.x1 = x1; this.y1 = y1; this.vx1 = vx1; this.vy1 = vy1;
        this.x2 = x2; this.y2 = y2; this.vx2 = vx2; this.vy2 = vy2;
        setPreferredSize(new Dimension(400, 300));
        thread = new Thread(this);
        thread.start();
    }

    public void run() {
        while (true) {
            x1 += vx1; y1 += vy1;
            x2 += vx2; y2 += vy2;
            if (x1 < r || x1 > getWidth() - r) vx1 = -vx1;
            if (y1 < r || y1 > getHeight() - r) vy1 = -vy1;
            if (x2 < r || x2 > getWidth() - r) vx2 = -vx2;
            if (y2 < r || y2 > getHeight() - r) vy2 = -vy2;
            int dx = x1 - x2, dy = y1 - y2;
            if (dx * dx + dy * dy < (2 * r) * (2 * r)) {
                vx1 = -vx1; vy1 = -vy1;
                vx2 = -vx2; vy2 = -vy2;
            }
            repaint();
            try { Thread.sleep(50); } catch (InterruptedException e) {}
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLUE);
        g.fillOval(x1 - r, y1 - r, r * 2, r * 2);
        g.setColor(Color.RED);
        g.fillOval(x2 - r, y2 - r, r * 2, r * 2);
    }

    public static void main(String[] args) {
        System.out.println("programmed by 上田剛瑠");
        int x1 = args.length > 0 ? Integer.parseInt(args[0]) : 50;
        int y1 = args.length > 1 ? Integer.parseInt(args[1]) : 50;
        int vx1 = args.length > 2 ? Integer.parseInt(args[2]) : 2;
        int vy1 = args.length > 3 ? Integer.parseInt(args[3]) : 3;
        int x2 = args.length > 4 ? Integer.parseInt(args[4]) : 200;
        int y2 = args.length > 5 ? Integer.parseInt(args[5]) : 200;
        int vx2 = args.length > 6 ? Integer.parseInt(args[6]) : -2;
        int vy2 = args.length > 7 ? Integer.parseInt(args[7]) : -3;
        JFrame frame = new JFrame("ExamGBouncingBall2");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ExamGBouncingBall2(x1, y1, vx1, vy1, x2, y2, vx2, vy2));
        frame.pack();
        frame.setVisible(true);
    }
} 