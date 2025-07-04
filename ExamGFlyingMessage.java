import java.awt.*;
import javax.swing.*;

public class ExamGFlyingMessage extends JPanel implements Runnable {
    String message;
    int fontSize = 10;
    Thread thread;

    public ExamGFlyingMessage(String message) {
        this.message = message;
        setPreferredSize(new Dimension(400, 300));
        thread = new Thread(this);
        thread.start();
    }

    public void run() {
        while (true) {
            fontSize += 2;
            if (fontSize > 100) fontSize = 10;
            repaint();
            try { Thread.sleep(50); } catch (InterruptedException e) {}
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setFont(new Font("Serif", Font.BOLD, fontSize));
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(message)) / 2;
        int y = (getHeight() + fm.getAscent()) / 2;
        g.setColor(Color.BLACK);
        g.drawString(message, x, y);
    }

    public static void main(String[] args) {
        String message = args.length > 0 ? args[0] : "Hello, World!";
        JFrame frame = new JFrame("ExamGFlyingMessage");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ExamGFlyingMessage(message));
        frame.pack();
        frame.setVisible(true);
    }
} 