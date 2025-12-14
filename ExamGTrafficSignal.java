import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ExamGTrafficSignal extends JPanel implements Runnable {
    int state = 0, timer = 0;
    Thread thread;

    public ExamGTrafficSignal() {
        setPreferredSize(new Dimension(200, 300));
        thread = new Thread(this);
        thread.start();
    }

    public void run() {
        while (true) {
            switch (state) {
                case 0: timer = 10; break; // 赤
                case 1: timer = 10; break; // 青
                case 2: timer = 2; break;  // 黄
            }
            for (int i = timer; i > 0; i--) {
                repaint();
                try { Thread.sleep(1000); } catch (InterruptedException e) {}
            }
            state = (state + 1) % 3;
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int x = 50, y = 20;
        g.setColor(Color.GRAY);
        g.fillRect(x, y, 60, 180);
        g.setColor(state == 0 ? Color.RED : Color.DARK_GRAY);
        g.fillOval(x + 10, y + 10, 40, 40);
        g.setColor(state == 1 ? Color.GREEN : Color.DARK_GRAY);
        g.fillOval(x + 10, y + 70, 40, 40);
        g.setColor(state == 2 ? Color.YELLOW : Color.DARK_GRAY);
        g.fillOval(x + 10, y + 130, 40, 40);
        // 残り時間バー
        g.setColor(Color.BLACK);
        g.drawRect(x + 55, y + 10, 5, 160);
        g.fillRect(x + 55, y + 170 - (timer * 16), 5, timer * 16);
    }

    public static void main(String[] args) {
        System.out.println("programmed by 上田剛瑠");
        JFrame frame = new JFrame("ExamGTrafficSignal");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ExamGTrafficSignal());
        frame.pack();
        frame.setVisible(true);
    }
} 