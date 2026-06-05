import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class IconGenerator {

    public static void main(String[] args) throws Exception {
        String outDir = args.length > 0 ? args[0] : "icon.iconset";
        File iconset = new File(outDir);
        iconset.mkdirs();

        // Standard macOS iconset sizes
        int[][] specs = {
            {16, 1}, {16, 2},
            {32, 1}, {32, 2},
            {64, 1}, {64, 2},
            {128, 1}, {128, 2},
            {256, 1}, {256, 2},
            {512, 1}, {512, 2}
        };

        for (int[] spec : specs) {
            int logicalSize = spec[0];
            int scale = spec[1];
            int pixelSize = logicalSize * scale;
            BufferedImage img = createIcon(pixelSize);
            String name = scale == 1
                ? "icon_" + logicalSize + "x" + logicalSize + ".png"
                : "icon_" + logicalSize + "x" + logicalSize + "@2x.png";
            ImageIO.write(img, "PNG", new File(iconset, name));
            System.out.println("Generated: " + name + " (" + pixelSize + "px)");
        }

        System.out.println("Done -> " + iconset.getAbsolutePath());
    }

    static BufferedImage createIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        float s = size / 512f;

        // ── Background: Spring Boot green rounded square ──────────────────────
        Color springGreen   = new Color(0x6D, 0xB3, 0x3F);
        Color springDark    = new Color(0x4A, 0x7C, 0x29);
        Color springLeaf    = new Color(0x3B, 0x6B, 0x1E);

        int arc = (int)(100 * s);
        g.setColor(springGreen);
        g.fillRoundRect(0, 0, size, size, arc, arc);

        // Subtle darker gradient ring at edge
        GradientPaint edge = new GradientPaint(0, 0, springDark, size, size, springGreen);
        g.setPaint(edge);
        g.setStroke(new BasicStroke(6 * s));
        g.drawRoundRect(0, 0, size - 1, size - 1, arc, arc);

        // ── Spring Boot leaf (bottom-right accent) ────────────────────────────
        drawLeaf(g, size, s, springLeaf);

        // ── Clock face ────────────────────────────────────────────────────────
        int faceMargin = (int)(55 * s);
        int faceSize   = size - 2 * faceMargin;
        int fx = faceMargin;
        int fy = faceMargin;
        int cx = size / 2;
        int cy = size / 2;
        int fr = faceSize / 2;

        // Shadow
        g.setColor(new Color(0, 0, 0, 50));
        g.fillOval(fx + (int)(4*s), fy + (int)(6*s), faceSize, faceSize);

        // White face
        g.setColor(Color.WHITE);
        g.fillOval(fx, fy, faceSize, faceSize);

        // Face border
        g.setColor(new Color(0xE0, 0xE0, 0xE0));
        g.setStroke(new BasicStroke(3 * s));
        g.drawOval(fx, fy, faceSize, faceSize);

        // ── Hour/minute tick marks ────────────────────────────────────────────
        for (int i = 0; i < 60; i++) {
            double angle = Math.toRadians(i * 6 - 90);
            boolean isHour = (i % 5 == 0);
            float tickLen  = isHour ? 18 * s : 9 * s;
            float tickW    = isHour ? 5 * s : 2.5f * s;
            float outerR   = fr * 0.91f;
            float innerR   = outerR - tickLen;

            int x1 = (int)(cx + Math.cos(angle) * outerR);
            int y1 = (int)(cy + Math.sin(angle) * outerR);
            int x2 = (int)(cx + Math.cos(angle) * innerR);
            int y2 = (int)(cy + Math.sin(angle) * innerR);

            g.setColor(isHour ? new Color(0x33, 0x33, 0x33) : new Color(0xAA, 0xAA, 0xAA));
            g.setStroke(new BasicStroke(tickW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(x1, y1, x2, y2);
        }

        // ── Hour hand (pointing ~10) ──────────────────────────────────────────
        double hourAngle = Math.toRadians(10 * 30 + (2 / 60.0) * 30 - 90); // 10:02
        drawHand(g, cx, cy, hourAngle, fr * 0.52f, 14 * s, new Color(0x22, 0x22, 0x22));

        // ── Minute hand (pointing ~2) ─────────────────────────────────────────
        double minAngle = Math.toRadians(2 * 6 - 90); // 2 minutes
        drawHand(g, cx, cy, minAngle, fr * 0.73f, 10 * s, new Color(0x22, 0x22, 0x22));

        // ── Second hand (red, pointing ~35s) ─────────────────────────────────
        double secAngle = Math.toRadians(35 * 6 - 90);
        // counterbalance
        int secTipX  = (int)(cx + Math.cos(secAngle) * fr * 0.82f);
        int secTipY  = (int)(cy + Math.sin(secAngle) * fr * 0.82f);
        int secTailX = (int)(cx - Math.cos(secAngle) * fr * 0.22f);
        int secTailY = (int)(cy - Math.sin(secAngle) * fr * 0.22f);
        g.setColor(new Color(0xE5, 0x39, 0x35));
        g.setStroke(new BasicStroke(4 * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(secTailX, secTailY, secTipX, secTipY);

        // ── Center cap ───────────────────────────────────────────────────────
        int capR = (int)(11 * s);
        g.setColor(new Color(0x22, 0x22, 0x22));
        g.fillOval(cx - capR, cy - capR, capR * 2, capR * 2);
        g.setColor(new Color(0xE5, 0x39, 0x35));
        int redCapR = (int)(5 * s);
        g.fillOval(cx - redCapR, cy - redCapR, redCapR * 2, redCapR * 2);

        g.dispose();
        return img;
    }

    // Tapered clock hand
    static void drawHand(Graphics2D g, int cx, int cy, double angle, float length,
                         float width, Color color) {
        int tipX = (int)(cx + Math.cos(angle) * length);
        int tipY = (int)(cy + Math.sin(angle) * length);
        int baseX = (int)(cx - Math.cos(angle) * length * 0.12);
        int baseY = (int)(cy - Math.sin(angle) * length * 0.12);
        g.setColor(color);
        g.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(baseX, baseY, tipX, tipY);
    }

    // Spring Boot leaf in bottom-right area
    static void drawLeaf(Graphics2D g, int size, float s, Color leafColor) {
        // Draw a stylized Spring leaf: two curved lobes forming a leaf with a stem
        int leafCx = (int)(size * 0.76);
        int leafCy = (int)(size * 0.76);
        int leafR  = (int)(72 * s);

        g.setColor(new Color(leafColor.getRed(), leafColor.getGreen(), leafColor.getBlue(), 200));

        // Outer circle (the Spring coil circle)
        g.setStroke(new BasicStroke(9 * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawOval(leafCx - leafR, leafCy - leafR, leafR * 2, leafR * 2);

        // Leaf shape inside: two bezier lobes
        Path2D leaf = new Path2D.Float();
        // Start at tip (top of leaf)
        float tipX = leafCx;
        float tipY = leafCy - leafR * 0.55f;
        // Right lobe
        float rCtrl1X = leafCx + leafR * 0.55f;
        float rCtrl1Y = leafCy - leafR * 0.55f;
        float rCtrl2X = leafCx + leafR * 0.55f;
        float rCtrl2Y = leafCy + leafR * 0.15f;
        float bottomX = leafCx;
        float bottomY = leafCy + leafR * 0.55f;
        // Left lobe
        float lCtrl1X = leafCx - leafR * 0.55f;
        float lCtrl1Y = leafCy + leafR * 0.15f;
        float lCtrl2X = leafCx - leafR * 0.55f;
        float lCtrl2Y = leafCy - leafR * 0.55f;

        leaf.moveTo(tipX, tipY);
        leaf.curveTo(rCtrl1X, rCtrl1Y, rCtrl2X, rCtrl2Y, bottomX, bottomY);
        leaf.curveTo(lCtrl1X, lCtrl1Y, lCtrl2X, lCtrl2Y, tipX, tipY);
        leaf.closePath();

        g.setColor(new Color(leafColor.getRed(), leafColor.getGreen(), leafColor.getBlue(), 220));
        g.fill(leaf);

        // Leaf midrib (vein)
        g.setColor(new Color(springGreenR(), springGreenG(), springGreenB(), 160));
        g.setStroke(new BasicStroke(3.5f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine((int)tipX, (int)tipY, (int)bottomX, (int)bottomY);
    }

    static int springGreenR() { return 0x6D; }
    static int springGreenG() { return 0xB3; }
    static int springGreenB() { return 0x3F; }
}
