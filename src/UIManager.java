import java.awt.Graphics2D;

public class UIManager {
    private GamePanel gp;
    private StatsScreen statsScreen;
    private PauseMenu pauseMenu;

    public UIManager(GamePanel gp) {
        this.gp = gp;
        // Modified: StatsScreen constructor no longer takes gp
        this.statsScreen = new StatsScreen();
        this.pauseMenu = new PauseMenu(gp);
    }

    public void draw(Graphics2D g2d) {
        // 2. Check game states
        if (gp.isPaused()) {
            if (gp.isStat()) {
                // Update statsScreen with real-time data before drawing
                // We fetch the latest save data or create a temporary one from current player state
                SaveManager.PlayerData currentSessionData = new SaveManager.PlayerData(
                        gp.getPlayer().x,
                        gp.getPlayer().y,
                        gp.getPlayer().getSpriteRow(),
                        0, // Highscore can be handled or fetched
                        1  // Current Level
                );

                // Inject the data and draw
                statsScreen.updateData("In-Game", currentSessionData);
                statsScreen.drawPlayerStat(g2d, gp.getWidth(), gp.getHeight());
            } else {
                pauseMenu.draw(g2d);
            }
        }
    }
}