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
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 2. Setup Font Math
        FontMetrics metrics = g2.getFontMetrics(getFont());
        String text = getText();
        int x = (getWidth() - metrics.stringWidth(text)) / 2;
        int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();

        // 3. Draw White Outline (Stroke)
        int size = 2; // Thickness of the outline
        g2.setColor(Color.WHITE);
        for (int i = -size; i <= size; i++) {
            for (int j = -size; j <= size; j++) {
                if (i != 0 || j != 0)
                    g2.drawString(text, x + i, y + j);
            }
        }

        // 4. Draw Main Black Text
        g2.setColor(getForeground());
        g2.drawString(text, x, y);

        // 5. Draw Border if Selected (Optional but recommended)
        if (getBorder() != null && !(getBorder() instanceof javax.swing.border.EmptyBorder)) {
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(4));
            g2.drawRect(2, 2, getWidth() - 4, getHeight() - 4);
        }
    }
}