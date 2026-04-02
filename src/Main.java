import java.awt.*;
import javax.swing.*;

public class Main {

    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private static final int TILE_SIZE = 32;

    // Keep a reference to PlayerScreen to update its data dynamically
    private PlayerScreen playerScreen;
    // Keep a reference to StatsPage to update user stats dynamically
    private StatsPage statsPage;
    private GamePanel tutorialGamePanel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().initGUI());
    }

    /**
     * Initializes the GUI and registers all persistent panels.
     */
    private void initGUI() {
        frame = new JFrame("Keymon");
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // 1. Initialize persistent panels
        MenuPanel menuPanel = new MenuPanel(cardLayout, mainContainer);
        LoginPanel loginPanel = new LoginPanel(mainContainer, this);
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

        // Show the main menu on startup
        cardLayout.show(mainContainer, "MENU");
    }

    /**
     * Switches to the intermediate Player Screen.
     * Called by LoginPanel after a successful name entry.
     */
    public void showPlayerScreen(String username) {
        // Refresh the labels and data in PlayerScreen before showing it
        playerScreen.setUserData(username);
        cardLayout.show(mainContainer, "PLAYER_SCREEN");
    }

    /**
     * Switches to the dedicated Stats page.
     * Called by the "VIEW STATS" button in PlayerScreen.
     */
    public void showStatsPage(String username) {
        // Updated: Pass the username to stats page and switch view
        statsPage.setUserData(username);
        System.out.println("Navigating to Stats Page for user: " + username);
        cardLayout.show(mainContainer, "STATS_PAGE");
    }

    /**
     * Officially starts the gameplay.
     * Called by the "START GAME" button in PlayerScreen.
     */
    public void startGame(String username) {
        // 1. Load full player data (coordinates, level, highscore)
        SaveManager.PlayerData data = SaveManager.loadGame(username);

        // 2. Load map and trainer resources
        MapLoader loader = new MapLoader("map.csv");
        Trainer trainer = loader.getTrainer();

        // 3. Initialize player using saved data
        Player player = new Player(data.x, data.y, data.spriteRow, TILE_SIZE);

        // 4. Create GamePanel dynamically to ensure fresh state for each session
        GamePanel gamePanel = new GamePanel(cardLayout, mainContainer, player, loader.getWorldMap(), trainer,
                loader.getTerrainObjects(), username, this);

        mainContainer.add(gamePanel, "PLAY GAME");

        System.out.println("Game started for user: " + username);

        // 5. Switch to gameplay and capture keyboard focus
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