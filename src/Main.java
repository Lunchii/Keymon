import java.awt.*;
import java.util.List;
import javax.swing.*;

public class Main {

    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private static final int TILE_SIZE = 32;

    private PlayerScreen playerScreen;
    private StatsPage statsPage;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().initGUI());
    }

    private void initGUI() {
        frame = new JFrame("Keymon");
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        MenuPanel menuPanel        = new MenuPanel(cardLayout, mainContainer);
        LoginPanel loginPanel      = new LoginPanel(mainContainer, this);
        HighscorePanel highscorePanel = new HighscorePanel(cardLayout, mainContainer);
        playerScreen = new PlayerScreen(cardLayout, mainContainer, this);
        statsPage    = new StatsPage(this);

        mainContainer.add(menuPanel,       "MENU");
        mainContainer.add(loginPanel,      "LOGIN");
        mainContainer.add(playerScreen,    "PLAYER_SCREEN");
        mainContainer.add(highscorePanel,  "HIGHSCORE");
        mainContainer.add(statsPage,       "STATS_PAGE");

        frame.add(mainContainer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        cardLayout.show(mainContainer, "MENU");
    }

    /** Called by LoginPanel — show player screen (stats + start button). */
    public void showPlayerScreen(String username) {
        playerScreen.setUserData(username);
        cardLayout.show(mainContainer, "PLAYER_SCREEN");
    }

    /** Called by the "START GAME" button in PlayerScreen — show team select. */
    public void startGame(String username) {
        try { mainContainer.remove(mainContainer.getComponentCount() - 1); } catch (Exception ignored) {}
        mainContainer.add(new TeamSelectPanel(mainContainer, this, username), "TEAM_SELECT");
        cardLayout.show(mainContainer, "TEAM_SELECT");
    }

    /** Called by TeamSelectPanel after team confirmed — launch game. */
    public void startGame(String username, List<Pokemon> team) {
        SaveManager.PlayerData data = SaveManager.loadGame(username);

        MapLoader loader  = new MapLoader("map.csv");
        Trainer trainer   = loader.getTrainer();
        Player player     = new Player(data.x, data.y, data.spriteRow, TILE_SIZE);

        GamePanel gamePanel = new GamePanel(cardLayout, mainContainer, player,
                loader.getWorldMap(), trainer, loader.getTerrainObjects(), username, this);
        gamePanel.setParty(team);
        mainContainer.add(gamePanel, "PLAY GAME");

        System.out.println("Session started for user: " + username);

        cardLayout.show(mainContainer, "PLAY GAME");
        gamePanel.requestFocusInWindow();
    }

    /** Called by the "VIEW STATS" button in PlayerScreen. */
    public void showStatsPage(String username) {
        statsPage.setUserData(username);
        cardLayout.show(mainContainer, "STATS_PAGE");
    }

    public void returnToMenu() {
        cardLayout.show(mainContainer, "MENU");
    }
}
