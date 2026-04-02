import java.awt.Graphics2D;

/**
 * Draws pause-related overlays for the world gameplay screen.
 */
public class UIManager {
    private final GamePanel gp;
    private final StatsScreen statsScreen;
    private final PauseMenu pauseMenu;

    public UIManager(GamePanel gp) {
        this.gp = gp;
        this.statsScreen = new StatsScreen();
        this.pauseMenu = new PauseMenu(gp);
    }

    public void draw(Graphics2D graphics) {
        if (!gp.isPaused()) {
            return;
        }

        if (gp.isStat()) {
            statsScreen.updateData(gp);
            statsScreen.drawPlayerStat(graphics, gp.getWidth(), gp.getHeight());
        } else {
            pauseMenu.draw(graphics);
        }
    }
}
