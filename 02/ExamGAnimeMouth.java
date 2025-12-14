import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ExamGAnimeMouth extends JPanel implements Runnable {
    boolean open = true;
    Thread thread;

    public ExamGAnimeMouth() {
        setPreferredSize(new Dimension(200, 200));
        thread = new Thread(this);
        thread.start();
    }

    public void run() {
        while (true) {
            open = !open;
            repaint();
            try { Thread.sleep(500); } catch (InterruptedException e) {}
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.PINK);
        g.fillOval(50, 50, 100, 100); // 顔
        g.setColor(Color.BLACK);
        if (open) {
            g.fillArc(80, 120, 40, 20, 0, -180); // 開いた口
        } else {
            g.drawLine(80, 130, 120, 130); // 閉じた口
        }
    }

    public static void main(String[] args) {
        System.out.println("programmed by 上田剛瑠");
        JFrame frame = new JFrame("ExamGAnimeMouth");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ExamGAnimeMouth());
        frame.pack();
        frame.setVisible(true);
    }
} 