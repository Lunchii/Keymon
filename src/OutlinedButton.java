import java.awt.*;
import javax.swing.*;

class OutlinedButton extends JButton {
    public OutlinedButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false); // Ensures the area behind the rounded corners is transparent
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Always draw the Grey Box Background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

        String text = getText();
        if (text.isEmpty())
            return;

        FontMetrics metrics = g2.getFontMetrics(getFont());
        int x = (getWidth() - metrics.stringWidth(text)) / 2;
        int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();

        java.awt.font.TextLayout tl = new java.awt.font.TextLayout(text, getFont(), g2.getFontRenderContext());
        Shape shape = tl.getOutline(java.awt.geom.AffineTransform.getTranslateInstance(x, y));

        // 2. Draw the White Outline (Stroke)
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(shape);

        g2.setColor(getForeground());
        g2.fill(shape);
    }
}