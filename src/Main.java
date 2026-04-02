import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class Main {

    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private static final int TILE_SIZE = 32;

    private PlayerScreen playerScreen;
    private StatsPage statsPage;
    private GamePanel tutorialGamePanel;

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
        TutorialPanel tutorialPanel = new TutorialPanel(cardLayout, mainContainer, this);

        // Initialize PlayerScreen with references for navigation
        playerScreen = new PlayerScreen(cardLayout, mainContainer, this);
        // Initialize StatsPage for standalone viewing (Isaac's request)
        statsPage = new StatsPage(this);

        // 2. Register panels in the CardLayout container
        mainContainer.add(menuPanel, "MENU");
        mainContainer.add(loginPanel, "LOGIN");
        mainContainer.add(playerScreen, "PLAYER_SCREEN");
        mainContainer.add(highscorePanel, "HIGHSCORE");
        mainContainer.add(tutorialPanel, "TUTORIAL");
        mainContainer.add(statsPage, "STATS_PAGE");

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

    /** Called by PlayerScreen — show stats screen. */
    public void showStatsPage(String username) {
        statsPage.setUserData(username);
        cardLayout.show(mainContainer, "STATS_PAGE");
    }

    /** Called by the "START GAME" button in PlayerScreen — show team select. */
    public void startGame(String username) {
        mainContainer.add(new LevelSelectPanel(mainContainer, this, username), "LEVEL_SELECT");
        cardLayout.show(mainContainer, "LEVEL_SELECT");
    }

    /** Called by LevelSelectPanel — launch selected map. */
    public void startGame(String username, String mapFile) {
        int levelId = getLevelIdFromMap(mapFile);
        SaveManager.PlayerData data = SaveManager.loadGame(username, levelId);
        List<Pokemon> team = SaveManager.loadParty(username);
        if (team == null || team.isEmpty()) {
            team = new ArrayList<>();
            List<Pokemon> pool = Pokemon.getPool();
            for (int i = 0; i < Math.min(3, pool.size()); i++) {
                Pokemon t = pool.get(i);
                Pokemon p = new Pokemon(t.name, t.maxHp, t.attack, t.spritePath);
                p.loadSprite();
                team.add(p);
            }
        }

        MapLoader loader = new MapLoader(mapFile);
        Trainer trainer = loader.getTrainer();
        Player player = new Player(data.x, data.y, data.spriteRow, TILE_SIZE);

        GamePanel gamePanel = new GamePanel(cardLayout, mainContainer, player,
            loader.getWorldMap(), trainer, loader.getTerrainObjects(), username, this, false, levelId);
        gamePanel.setParty(team);
        mainContainer.add(gamePanel, "PLAY GAME");

        System.out.println("Session started for user: " + username + " on map " + mapFile);

        cardLayout.show(mainContainer, "PLAY GAME");
        gamePanel.requestFocusInWindow();
    }

    private int getLevelIdFromMap(String mapFile) {
        if ("level2_map.csv".equalsIgnoreCase(mapFile)) return 2;
        if ("level3_map.csv".equalsIgnoreCase(mapFile)) return 3;
        return 1;
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

    public void startTutorial() {
        MapLoader tutorialLoader = new MapLoader("tutorial_map.csv");
        Trainer trainer = tutorialLoader.getTrainer();

        if (tutorialGamePanel != null) {
            mainContainer.remove(tutorialGamePanel);
        }

        Player player = new Player(3 * TILE_SIZE, 3 * TILE_SIZE, 0, TILE_SIZE);
        tutorialGamePanel = new GamePanel(cardLayout, mainContainer, player, tutorialLoader.getWorldMap(),
                trainer, tutorialLoader.getTerrainObjects(), "", this, true);
        List<Pokemon> tutorialParty = new java.util.ArrayList<>();
        tutorialParty.add(Pokemon.random());
        tutorialGamePanel.setParty(tutorialParty);

        mainContainer.add(tutorialGamePanel, "TUTORIAL_GAME");
        mainContainer.revalidate();
        mainContainer.repaint();

        System.out.println("Tutorial started.");
        cardLayout.show(mainContainer, "TUTORIAL_GAME");
        tutorialGamePanel.requestFocusInWindow();
    }

    /**
     * Utility method to return to the main menu.
     */
    public void returnToMenu() {
        cardLayout.show(mainContainer, "MENU");
    }

    private static class TutorialMap {
        int[][] worldMap;
        Trainer trainer;
        java.util.List<TerrainObjects> terrainObjects;
    }
}
