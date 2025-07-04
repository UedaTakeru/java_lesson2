import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Calendar;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ExamGAnalogClock extends JPanel implements Runnable {
    Thread thread;

    public ExamGAnalogClock() {
        setPreferredSize(new Dimension(300, 300));
        thread = new Thread(this);
        thread.start();
    }

    public void run() {
        while (true) {
            repaint();
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth(), h = getHeight();
        int cx = w / 2, cy = h / 2, r = Math.min(w, h) / 2 - 10;
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.setColor(Color.BLACK);
        g.drawOval(cx - r, cy - r, r * 2, r * 2);

        Calendar now = Calendar.getInstance();
        int sec = now.get(Calendar.SECOND);
        int min = now.get(Calendar.MINUTE);
        int hour = now.get(Calendar.HOUR);

        double secAngle = Math.toRadians(sec * 6 - 90);
        double minAngle = Math.toRadians(min * 6 - 90);
        double hourAngle = Math.toRadians((hour % 12) * 30 + min * 0.5 - 90);

        // 時針
        g.drawLine(cx, cy, (int)(cx + Math.cos(hourAngle) * r * 0.5), (int)(cy + Math.sin(hourAngle) * r * 0.5));
        // 分針
        g.drawLine(cx, cy, (int)(cx + Math.cos(minAngle) * r * 0.8), (int)(cy + Math.sin(minAngle) * r * 0.8));
        // 秒針
        g.setColor(Color.RED);
        g.drawLine(cx, cy, (int)(cx + Math.cos(secAngle) * r * 0.9), (int)(cy + Math.sin(secAngle) * r * 0.9));
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("ExamGAnalogClock");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ExamGAnalogClock());
        frame.pack();
        frame.setVisible(true);
    }
} 