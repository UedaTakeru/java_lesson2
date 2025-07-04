import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ExamGBouncingBall3 extends JPanel implements Runnable, ActionListener {
    int x, y, vx, vy, radius = 10;
    Thread thread;
    boolean running = false;
    JButton startBtn, stopBtn;

    public ExamGBouncingBall3(int x, int y, int vx, int vy) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        setPreferredSize(new Dimension(400, 300));
        startBtn = new JButton("start");
        stopBtn = new JButton("stop");
        startBtn.addActionListener(this);
        stopBtn.addActionListener(this);
        this.add(startBtn);
        this.add(stopBtn);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startBtn) {
            if (!running) {
                running = true;
                thread = new Thread(this);
                thread.start();
            }
        } else if (e.getSource() == stopBtn) {
            running = false;
        }
    }

    public void run() {
        while (running) {
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
        JFrame frame = new JFrame("ExamGBouncingBall3");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ExamGBouncingBall3(x, y, vx, vy));
        frame.pack();
        frame.setVisible(true);
    }
} 