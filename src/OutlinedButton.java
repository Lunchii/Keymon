import java.awt.*;
import javax.swing.*;

class OutlinedButton extends JButton {
    public OutlinedButton(String text) {
        super(text);
        setContentAreaFilled(false); // We will draw the background ourselves
        setFocusPainted(false);
        setBorderPainted(false);
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        // Essential for speed and quality
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        String text = getText();
        if (text.isEmpty())
            return;

        FontMetrics metrics = g2.getFontMetrics(getFont());
        int x = (getWidth() - metrics.stringWidth(text)) / 2;
        int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();

        // 1. Create a "Shape" out of the text
        java.awt.font.TextLayout tl = new java.awt.font.TextLayout(text, getFont(), g2.getFontRenderContext());
        Shape shape = tl.getOutline(java.awt.geom.AffineTransform.getTranslateInstance(x, y));

        // 2. Draw the Outline (The "Stroke")
        g2.setColor(Color.WHITE);
        // 6.0f is the thickness. BasicStroke is MUCH faster than loops.
        g2.setStroke(new BasicStroke(6.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(shape);

        // 3. Fill the Center (Black)
        g2.setColor(getForeground());
        g2.fill(shape);
    }
}