import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ExamGHypotrochoid extends JFrame implements ActionListener, Runnable {
    // UI部品
    private JButton btnStart;
    private JLabel lblRc;
    private JTextField tfRot, tfRm, tfRd;
    private DrawPanel drawPanel;
    // アニメーション用
    private Thread th;
    private volatile boolean running = false;
    // パラメータ
    private final double rc = 1.0; // 定円半径は1固定
    private double rm = 0.5; // 動円半径
    private double rd = 0.5; // 描画点半径
    private int rot = 3;     // 回転数
    // 名前
    private final String myName = "上田 剛瑠"; // ←ここを自分の名前に変更

    public ExamGHypotrochoid() {
        setTitle("内トロコイド（ハイポトロコイド）");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(null);
        // UIパネル
        JPanel p = new JPanel(new FlowLayout());
        btnStart = new JButton("開始");
        btnStart.addActionListener(this);
        lblRc = new JLabel("定円の半径=1");
        tfRot = new JTextField("3", 3);
        tfRm = new JTextField("0.5", 4);
        tfRd = new JTextField("0.5", 4);
        p.add(btnStart);
        p.add(lblRc);
        p.add(new JLabel("回転数"));
        p.add(tfRot);
        p.add(new JLabel("動円半径(rm)"));
        p.add(tfRm);
        p.add(new JLabel("描画点半径(rd)"));
        p.add(tfRd);
        add(p, BorderLayout.NORTH);
        // 描画パネル
        drawPanel = new DrawPanel();
        add(drawPanel, BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnStart && !running) {
            try {
                rot = Integer.parseInt(tfRot.getText());
                rm = Double.parseDouble(tfRm.getText());
                rd = Double.parseDouble(tfRd.getText());
            } catch (Exception ex) {
                rot = 3; rm = 0.5; rd = 0.5;
            }
            drawPanel.reset();
            running = true;
            th = new Thread(this);
            th.start();
        }
    }

    public void run() {
        double theta = 0.0;
        double dtheta = 0.01;
        double end = 2 * Math.PI * rot;
        drawPanel.setParams(rc, rm, rd, rot);
        while (theta <= end && running) {
            // ハイポトロコイド座標計算
            double R = rc - rm;
            double x = R * Math.cos(theta) + rd * Math.cos(R * theta / rm);
            double y = R * Math.sin(theta) - rd * Math.sin(R * theta / rm);
            drawPanel.updateTrace(x, y, theta);
            try { Thread.sleep(10); } catch (InterruptedException ex) {}
            theta += dtheta;
        }
        running = false;
    }

    // 描画用パネル
    class DrawPanel extends JPanel {
        private Image BGimg;
        private Graphics2D BGg;
        private int cx, cy, scale;
        private double rc, rm, rd;
        private int rot;
        private double curX, curY, curTheta;
        public DrawPanel() {
            setBackground(Color.white);
        }
        public void setParams(double rc, double rm, double rd, int rot) {
            this.rc = rc; this.rm = rm; this.rd = rd; this.rot = rot;
            Dimension d = getSize();
            cx = d.width / 2;
            cy = d.height / 2 + 30;
            scale = (int)(Math.min(d.width, d.height) * 0.35);
        }
        public void reset() {
            Dimension d = getSize();
            BGimg = createImage(d.width, d.height);
            BGg = (Graphics2D)BGimg.getGraphics();
            BGg.setColor(Color.white);
            BGg.fillRect(0, 0, d.width, d.height);
            repaint();
        }
        public void updateTrace(double x, double y, double theta) {
            int px = cx + (int)(x * scale);
            int py = cy - (int)(y * scale);
            if (curTheta > 0) {
                int prevX = cx + (int)(curX * scale);
                int prevY = cy - (int)(curY * scale);
                BGg.setColor(Color.red);
                BGg.drawLine(prevX, prevY, px, py);
            }
            curX = x; curY = y; curTheta = theta;
            repaint();
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (BGimg == null) reset();
            g.drawImage(BGimg, 0, 0, this);
            // 定円
            g.setColor(Color.gray);
            g.drawOval(cx - (int)(rc * scale), cy - (int)(rc * scale), (int)(rc * scale * 2), (int)(rc * scale * 2));
            // 動円
            double mx = (rc - rm) * Math.cos(curTheta);
            double my = (rc - rm) * Math.sin(curTheta);
            int mcx = cx + (int)(mx * scale);
            int mcy = cy - (int)(my * scale);
            g.setColor(Color.blue);
            g.drawOval(mcx - (int)(rm * scale), mcy - (int)(rm * scale), (int)(rm * scale * 2), (int)(rm * scale * 2));
            // 動径
            int px = cx + (int)(curX * scale);
            int py = cy - (int)(curY * scale);
            g.setColor(Color.green);
            g.drawLine(mcx, mcy, px, py);
            // 描画点
            g.setColor(Color.red);
            g.fillOval(px - 3, py - 3, 7, 7);
            // 名前表示
            g.setColor(Color.black);
            g.drawString(myName, 10, getHeight() - 10);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ExamGHypotrochoid f = new ExamGHypotrochoid();
            f.setVisible(true);
        });
    }
} 