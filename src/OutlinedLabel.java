import java.awt.*;
import javax.swing.*;

/**
 * Custom JLabel that draws its text with an 8 px white stroke outline and a
 * black fill, suitable for overlaying large stylised text on the animated
 * menu background.
 *
 * <p>Uses a {@code GlyphVector} rather than a {@code TextLayout} so custom
 * TTF fonts render fully solid without artefacts at display sizes above 80 pt.</p>
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
class OutlinedLabel extends JLabel {

    public OutlinedLabel(String text) {
        super(text);
    }

    @Override
    protected void paintComponent(Graphics g) {
        // DO NOT call super.paintComponent(g) here
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        String text = getText();
        if (text == null || text.isEmpty())
            return;

        FontMetrics metrics = g2.getFontMetrics(getFont());
        int x = (getWidth() - metrics.stringWidth(text)) / 2;
        int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();

        // GlyphVector handles the "solidness" of custom fonts better than TextLayout
        java.awt.font.GlyphVector gv = getFont().createGlyphVector(g2.getFontRenderContext(), text);
        Shape textShape = gv.getOutline(x, y);

        // 1. Draw the Outline (White)
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(8.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(textShape);

        // 2. Fill the Center (Black)
        g2.setColor(Color.BLACK);
        g2.fill(textShape);
    }
}