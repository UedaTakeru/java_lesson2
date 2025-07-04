import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ExamGAnimeWalk extends JPanel implements Runnable {
    int step = 0;
    Thread thread;

    public ExamGAnimeWalk() {
        setPreferredSize(new Dimension(250, 250));
        thread = new Thread(this);
        thread.start();
    }

    public void run() {
        while (true) {
            step = (step + 1) % 20;
            repaint();
            try { Thread.sleep(100); } catch (InterruptedException e) {}
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int baseX = 100, baseY = 150;
        g.setColor(Color.ORANGE);
        g.fillOval(baseX, baseY - 40, 40, 40); // 頭
        g.setColor(Color.BLUE);
        g.fillRect(baseX + 10, baseY, 20, 40); // 体
        g.setColor(Color.BLACK);
        // 手
        g.drawLine(baseX + 10, baseY + 10, baseX - 10, baseY + 30 + (step % 2 == 0 ? 10 : -10));
        g.drawLine(baseX + 30, baseY + 10, baseX + 50, baseY + 30 + (step % 2 == 1 ? 10 : -10));
        // 足
        g.drawLine(baseX + 15, baseY + 40, baseX + 5, baseY + 60 + (step % 2 == 0 ? 10 : -10));
        g.drawLine(baseX + 25, baseY + 40, baseX + 35, baseY + 60 + (step % 2 == 1 ? 10 : -10));
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("ExamGAnimeWalk");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ExamGAnimeWalk());
        frame.pack();
        frame.setVisible(true);
    }
} 