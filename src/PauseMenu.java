import java.awt.*;

/**
 * Renders the in-game pause overlay with a centered dark panel,
 * "PAUSED" header, and navigable menu options.
 *
 * <p>The currently selected option is highlighted in yellow with a right-arrow
 * indicator. All layout is computed relative to the GamePanel size so it
 * remains correct at any window resolution.</p>
 */
public class PauseMenu {

    private static final int PANEL_W   = 340;
    private static final int OPTION_H  = 52;
    private static final int CORNER_R  = 18;

    private final GamePanel gp;

    /**
     * Creates a PauseMenu bound to the given GamePanel.
     *
     * @param gp the active game panel whose state is displayed
     */
    public PauseMenu(GamePanel gp) {
        this.gp = gp;
    }

    /**
     * Draws the full pause overlay onto the supplied Graphics2D context.
     * Should be called from within {@code paintComponent}.
     *
     * @param g2 graphics context to paint onto
     */
    public void draw(Graphics2D g2) {
        int sw = gp.getWidth();
        int sh = gp.getHeight();

        // Semi-transparent full-screen dim
        g2.setColor(new Color(0, 0, 0, 195));
        g2.fillRect(0, 0, sw, sh);

        String[] options = gp.getPauseOptions();
        int panelH = 100 + options.length * OPTION_H + 24;
        int panelX = (sw - PANEL_W) / 2;
        int panelY = (sh - panelH) / 2;

        // Panel background
        g2.setColor(new Color(12, 20, 30, 245));
        g2.fillRoundRect(panelX, panelY, PANEL_W, panelH, CORNER_R, CORNER_R);

        // Panel border
        g2.setColor(new Color(255, 255, 255, 80));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(panelX, panelY, PANEL_W, panelH, CORNER_R, CORNER_R);

        // "PAUSED" header
        g2.setFont(new Font("SansSerif", Font.BOLD, 34));
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        String header = "PAUSED";
        g2.drawString(header, panelX + (PANEL_W - fm.stringWidth(header)) / 2, panelY + 56);

        // Divider line
        g2.setColor(new Color(255, 255, 255, 40));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(panelX + 24, panelY + 68, panelX + PANEL_W - 24, panelY + 68);

        // Menu options
        int selected = gp.getPauseOptionIndex();
        g2.setFont(new Font("SansSerif", Font.BOLD, 19));

        for (int i = 0; i < options.length; i++) {
            int optY = panelY + 82 + i * OPTION_H;

            if (i == selected) {
                // Highlight row
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillRoundRect(panelX + 12, optY + 4, PANEL_W - 24, OPTION_H - 8, 10, 10);

                g2.setColor(new Color(255, 220, 60));
                g2.setFont(new Font("SansSerif", Font.BOLD, 19));
                fm = g2.getFontMetrics();
                String arrow = "\u25B6  " + options[i];
                g2.drawString(arrow, panelX + (PANEL_W - fm.stringWidth(arrow)) / 2,
                        optY + OPTION_H / 2 + fm.getAscent() / 2 - 2);
            } else {
                g2.setColor(new Color(210, 210, 210));
                g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
                fm = g2.getFontMetrics();
                g2.drawString(options[i], panelX + (PANEL_W - fm.stringWidth(options[i])) / 2,
                        optY + OPTION_H / 2 + fm.getAscent() / 2 - 2);
            }
        }

        // Hint at the bottom
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(new Color(160, 160, 160));
        fm = g2.getFontMetrics();
        String hint = "W / S to navigate   \u2022   Enter to select";
        g2.drawString(hint, panelX + (PANEL_W - fm.stringWidth(hint)) / 2,
                panelY + panelH - 10);
    }
}
