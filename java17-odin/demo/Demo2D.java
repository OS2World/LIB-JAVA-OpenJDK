import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/** Swing + Java2D showcase: shapes, gradients, transforms, text, images, animation, charts, widgets. */
public class Demo2D {
    static class Shapes extends JPanel {
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g.setPaint(new GradientPaint(0, 0, new Color(20, 30, 80), 0, h, new Color(180, 90, 200)));
            g.fillRect(0, 0, w, h);
            // radial gradient sun
            g.setPaint(new RadialGradientPaint(new Point2D.Float(w * 0.78f, h * 0.25f), 90,
                new float[]{0f, 0.6f, 1f}, new Color[]{Color.WHITE, new Color(255, 200, 60), new Color(255, 120, 0, 0)}));
            g.fillOval((int) (w * 0.78f) - 90, (int) (h * 0.25f) - 90, 180, 180);
            // rotating star pattern
            AffineTransform old = g.getTransform();
            g.translate(w * 0.25, h * 0.5);
            for (int i = 0; i < 24; i++) {
                g.rotate(Math.PI / 12);
                g.setColor(Color.getHSBColor(i / 24f, 0.8f, 1f));
                g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Line2D.Double(10, 0, 110, 0));
                g.fill(new Ellipse2D.Double(105, -6, 12, 12));
            }
            g.setTransform(old);
            // bezier path with dashed stroke
            Path2D p = new Path2D.Double();
            p.moveTo(w * 0.45, h * 0.8);
            p.curveTo(w * 0.55, h * 0.3, w * 0.7, h * 1.0, w * 0.95, h * 0.55);
            g.setColor(new Color(255, 255, 255, 220));
            g.setStroke(new BasicStroke(4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{14f, 8f}, 0f));
            g.draw(p);
            // translucent rounded rects
            g.setStroke(new BasicStroke(2f));
            for (int i = 0; i < 4; i++) {
                g.setColor(new Color(60 + i * 50, 220 - i * 40, 120, 120));
                g.fill(new RoundRectangle2D.Double(w * 0.42 + i * 30, h * 0.08 + i * 18, 150, 80, 24, 24));
                g.setColor(Color.WHITE);
                g.draw(new RoundRectangle2D.Double(w * 0.42 + i * 30, h * 0.08 + i * 18, 150, 80, 24, 24));
            }
            // text with several fonts
            g.setFont(new Font("SansSerif", Font.BOLD, 34));
            g.setColor(Color.WHITE);
            g.drawString("Java 17 on ArcaOS", 24, 52);
            g.setFont(new Font("SansSerif", Font.PLAIN, 16));
            g.drawString("Win32 provided by Odin32 - " + System.getProperty("java.vm.name"), 26, 78);
            g.setFont(new Font("Serif", Font.ITALIC, 22));
            g.drawString("Antialiased text, gradients, transforms", 26, h - 24);
        }
    }

    static class Balls extends JPanel {
        final int N = 18;
        double[] x = new double[N], y = new double[N], dx = new double[N], dy = new double[N];
        int[] r = new int[N];
        Color[] c = new Color[N];
        int frames = 0;
        long t0 = System.nanoTime();
        double fps;

        Balls() {
            Random rnd = new Random(7);
            for (int i = 0; i < N; i++) {
                x[i] = 50 + rnd.nextInt(400); y[i] = 50 + rnd.nextInt(200);
                dx[i] = (rnd.nextDouble() - 0.5) * 8; dy[i] = (rnd.nextDouble() - 0.5) * 8;
                r[i] = 12 + rnd.nextInt(24); c[i] = Color.getHSBColor(rnd.nextFloat(), 0.7f, 0.95f);
            }
            new Timer(25, e -> { step(); repaint(); }).start();
        }

        void step() {
            int w = getWidth(), h = getHeight();
            for (int i = 0; i < N; i++) {
                x[i] += dx[i]; y[i] += dy[i];
                if (x[i] < r[i] || x[i] > w - r[i]) dx[i] = -dx[i];
                if (y[i] < r[i] || y[i] > h - r[i]) dy[i] = -dy[i];
            }
            frames++;
            long now = System.nanoTime();
            if (now - t0 > 1_000_000_000L) { fps = frames * 1e9 / (now - t0); frames = 0; t0 = now; }
        }

        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(15, 20, 35));
            g.fillRect(0, 0, getWidth(), getHeight());
            for (int i = 0; i < N; i++) {
                g.setPaint(new RadialGradientPaint(new Point2D.Double(x[i] - r[i] / 3.0, y[i] - r[i] / 3.0), r[i],
                    new float[]{0f, 1f}, new Color[]{Color.WHITE, c[i]}));
                g.fill(new Ellipse2D.Double(x[i] - r[i], y[i] - r[i], 2 * r[i], 2 * r[i]));
            }
            g.setColor(Color.GREEN);
            g.setFont(new Font("Monospaced", Font.PLAIN, 14));
            g.drawString(String.format("animation: %.1f fps", fps), 10, 20);
        }
    }

    static class Chart extends JPanel {
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), m = 40;
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, w, h);
            g.setColor(Color.LIGHT_GRAY);
            for (int i = 0; i <= 5; i++) { int yy = m + i * (h - 2 * m) / 5; g.drawLine(m, yy, w - m, yy); }
            String[] names = {"Q1", "Q2", "Q3", "Q4", "Q5", "Q6"};
            int[] val = {40, 65, 55, 90, 75, 110};
            int bw = (w - 2 * m) / names.length;
            for (int i = 0; i < val.length; i++) {
                int bh = (int) ((h - 2 * m) * val[i] / 120.0);
                int bx = m + i * bw + bw / 6, by = h - m - bh;
                g.setPaint(new GradientPaint(bx, by, new Color(70, 130, 220), bx, h - m, new Color(20, 50, 130)));
                g.fillRect(bx, by, bw * 2 / 3, bh);
                g.setColor(Color.DARK_GRAY);
                g.drawString(names[i], bx + bw / 4, h - m + 16);
                g.drawString(String.valueOf(val[i]), bx + bw / 4, by - 4);
            }
            // line overlay
            Path2D line = new Path2D.Double();
            for (int i = 0; i < val.length; i++) {
                double px = m + i * bw + bw / 2.0, py = h - m - (h - 2 * m) * (val[i] * 0.8 + 10) / 120.0;
                if (i == 0) line.moveTo(px, py); else line.lineTo(px, py);
            }
            g.setColor(new Color(220, 60, 60));
            g.setStroke(new BasicStroke(3f));
            g.draw(line);
            g.setColor(Color.BLACK);
            g.drawString("Sales (bars) vs trend (line)", m, 24);
        }
    }

    static JComponent imagePanel() {
        int W = 360, H = 240;
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < H; y++)
            for (int x = 0; x < W; x++) {
                double v = Math.sin(x / 18.0) + Math.sin(y / 11.0) + Math.sin((x + y) / 23.0) + Math.sin(Math.hypot(x - 180, y - 120) / 9.0);
                img.setRGB(x, y, Color.HSBtoRGB((float) ((v + 4) / 8), 0.85f, 1f));
            }
        JPanel p = new JPanel() {
            protected void paintComponent(Graphics g0) {
                super.paintComponent(g0);
                Graphics2D g = (Graphics2D) g0;
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(img, 10, 10, null);
                g.drawImage(img, 390, 10, 180, 120, null);              // scaled down
                AffineTransform t = AffineTransform.getRotateInstance(Math.toRadians(20), 480, 200);
                t.translate(400, 150); t.scale(0.35, 0.35);
                g.drawImage(img, t, null);                              // rotated
                g.setColor(Color.BLACK);
                g.drawString("plasma image, scaled and rotated copies", 12, 270);
            }
        };
        return p;
    }

    static JComponent widgets() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        DefaultTableModel tm = new DefaultTableModel(new Object[]{"Function", "Area", "Status"}, 0);
        String[][] rows = {{"VirtualAlloc", "kernel32", "ok"}, {"CreateIoCompletionPort", "kernel32", "ok"},
            {"getaddrinfo", "ws2_32", "ok (IPv4)"}, {"SHGetSpecialFolderPath", "shell32", "ok"},
            {"DuplicateHandle", "kernel32", "ok"}, {"AcceptEx", "mswsock", "missing"}};
        for (String[] r : rows) tm.addRow(r);
        JTable table = new JTable(tm);
        table.setAutoCreateRowSorter(true);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Odin32");
        DefaultMutableTreeNode k = new DefaultMutableTreeNode("kernel32");
        k.add(new DefaultMutableTreeNode("iocp.cpp")); k.add(new DefaultMutableTreeNode("win7compat.cpp"));
        DefaultMutableTreeNode n = new DefaultMutableTreeNode("ws2_32");
        n.add(new DefaultMutableTreeNode("addrinfo.cpp"));
        root.add(k); root.add(n); root.add(new DefaultMutableTreeNode("shell32"));
        JTree tree = new JTree(root);
        for (int i = 0; i < tree.getRowCount(); i++) tree.expandRow(i);
        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tree), new JScrollPane(table));
        sp.setDividerLocation(160);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JProgressBar pb = new JProgressBar(0, 100); pb.setStringPainted(true);
        JSlider sl = new JSlider(0, 100, 40);
        sl.addChangeListener(e -> pb.setValue(sl.getValue()));
        pb.setValue(40);
        top.add(new JLabel("Level:")); top.add(sl); top.add(pb);
        top.add(new JCheckBox("Enabled", true)); top.add(new JComboBox<>(new String[]{"Metal", "Nimbus", "Motif"}));
        JButton b = new JButton("Click me");
        JLabel lab = new JLabel("clicks: 0");
        int[] cnt = {0};
        b.addActionListener(e -> lab.setText("clicks: " + (++cnt[0])));
        top.add(b); top.add(lab);
        JTextArea ta = new JTextArea("Editable text area.\nSwing widgets rendered by Java2D on top of Odin's GDI.\n", 4, 40);
        p.add(top, BorderLayout.NORTH);
        p.add(sp, BorderLayout.CENTER);
        p.add(new JScrollPane(ta), BorderLayout.SOUTH);
        return p;
    }

    public static void main(String[] a) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JFrame f = new JFrame("Java 17 on ArcaOS - Java2D / Swing demo");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab("Java2D", new Shapes());
            tabs.addTab("Animation", new Balls());
            tabs.addTab("Chart", new Chart());
            tabs.addTab("Image", imagePanel());
            tabs.addTab("Widgets", widgets());
            String sel = System.getProperty("tab");
            if (a.length > 0) tabs.setSelectedIndex(Integer.parseInt(a[0]));
            f.add(tabs);
            JMenuBar mb = new JMenuBar();
            JMenu m = new JMenu("File");
            JMenuItem q = new JMenuItem("Exit");
            q.addActionListener(e -> System.exit(0));
            m.add(q); mb.add(m);
            f.setJMenuBar(mb);
            f.setSize(760, 520);
            f.setLocation(40, 40);
            f.setVisible(true);
        });
        // auto-exit so unattended runs never leave a process behind
        new Timer((a.length > 1 ? Integer.parseInt(a[1]) : 180) * 1000, e -> System.exit(0)) {{ setRepeats(false); }}.start();
    }
}
