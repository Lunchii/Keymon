import java.awt.*;

public class PauseMenu {

    private GamePanel gp;
    private StatsScreen ss;

    public PauseMenu(GamePanel gp) {
        this.gp = gp;
    }

    public void drawPaused(Graphics2D g2d) {

        g2d.setColor(new Color(0, 0, 0, 220));
        g2d.fillRect(0, 0, gp.getWidth(), gp.getHeight());

        if (gp.getIsStat()) {
            ss.drawPlayerStat(g2d); // Draw the cool Stats UI
        } else {
            // Draw Main Pause Menu
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 50));
            g2d.drawString("PAUSED", gp.getWidth() / 2 - 100, 150);

            g2d.setFont(new Font("Arial", Font.PLAIN, 30));
            for (int i = 0; i < gp.getPauseOptions().length; i++) {
                if (i == gp.getPauseOptionIndex()) {
                    g2d.setColor(Color.YELLOW);
                    g2d.drawString("> " + gp.getPauseOptions()[i], gp.getWidth() / 2 - 100, 250 + (i * 50));
                } else {
                    g2d.setColor(Color.WHITE);
                    g2d.drawString("  " + gp.getPauseOptions()[i], gp.getWidth() / 2 - 100, 250 + (i * 50));
                }
            }
        }
    }
}
