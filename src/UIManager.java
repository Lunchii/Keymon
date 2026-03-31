import java.awt.Graphics2D;

public class UIManager {
    private GamePanel gp;
    private StatsScreen statsScreen;
    private PauseMenu pauseMenu;

    public UIManager(GamePanel gp) {
        this.gp = gp;
        this.statsScreen = new StatsScreen(gp);
        this.pauseMenu = new PauseMenu(gp);
    }

    public void draw(Graphics2D g2d) {

        // 2. Check game states
        if (gp.isPaused()) {
            if (gp.isStat()) {
                statsScreen.drawPlayerStat(g2d);
            } else {
                pauseMenu.drawPaused(g2d);
            }
        }
    }
}
