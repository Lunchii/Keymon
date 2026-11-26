import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * Application entry point and top-level screen coordinator.
 *
 * <p>
 * Creates the {@link JFrame} and a {@link CardLayout} container that holds
 * every named screen. Screen transitions are initiated by calling the public
 * {@code show*()} / {@code start*()} methods from child panels, keeping all
 * navigation logic in one place.
 * </p>
 *
 * <p>
 * Card names:
 * <ul>
 * <li>{@code "MENU"} – {@link MenuPanel}</li>
 * <li>{@code "LOGIN"} – {@link LoginPanel}</li>
 * <li>{@code "PLAYER_SCREEN"} – {@link PlayerScreen}</li>
 * <li>{@code "LEVEL_SELECT"} – {@link LevelSelectPanel} (created on
 * demand)</li>
 * <li>{@code "PLAY GAME"} – {@link GamePanel} (created on demand)</li>
 * <li>{@code "TUTORIAL_GAME"} – {@link GamePanel} (tutorial, created on
 * demand)</li>
 * <li>{@code "STATS_PAGE"} – {@link StatsPage}</li>
 * <li>{@code "HIGHSCORE"} – {@link HighscorePanel}</li>
 * <li>{@code "TUTORIAL"} – {@link InstructionPanel}</li>
 * <li>{@code "PARENTAL"} – {@link ParentalControlPanel}</li>
 * </ul>
 * </p>
 * 
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
public class Main {

    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private static final int TILE_SIZE = 32;

    private PlayerScreen playerScreen;
    private StatsPage statsPage;
    private GamePanel tutorialGamePanel;
    private GamePanel activeGamePanel;
    private LevelSelectPanel levelSelectPanel;

    /** Application entry point — launches the GUI on the Swing event thread. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().initGUI());
    }

    private void initGUI() {
        frame = new JFrame("Keymon");
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        MenuPanel menuPanel = new MenuPanel(cardLayout, mainContainer);
        LoginPanel loginPanel = new LoginPanel(mainContainer, this);
        HighscorePanel highscorePanel = new HighscorePanel(cardLayout, mainContainer);
        InstructionPanel tutorialPanel = new InstructionPanel(cardLayout, mainContainer, this);
        ParentalControlPanel parentalControlPanel = new ParentalControlPanel(cardLayout, mainContainer, this);
        ParentStatsPanel adminStatsPanel = new ParentStatsPanel(cardLayout, mainContainer, this);

        // Just create instances of your other files
        mainContainer.add(new MenuPanel(cardLayout, mainContainer), "MENU");
        mainContainer.add(new GamePanel(cardLayout, mainContainer, player, loader.getWorldMap()), "PLAY GAME");
        PlayerScreen playerScreen = new PlayerScreen(cardLayout, mainContainer, player);
        mainContainer.add(playerScreen, "PLAYER_SCREEN");
        mainContainer.add(new LoginPanel(cardLayout, mainContainer), "LOGIN");

        frame.add(mainContainer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        cardLayout.show(mainContainer, "MENU");
        MusicPlayer.play("menu");
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
        if (levelSelectPanel != null) {
            mainContainer.remove(levelSelectPanel);
        }
        levelSelectPanel = new LevelSelectPanel(mainContainer, this, username);
        mainContainer.add(levelSelectPanel, "LEVEL_SELECT");
        cardLayout.show(mainContainer, "LEVEL_SELECT");
    }

    public void continueGame(String username) {
        ProfileStore.ProfileData profile = ProfileStore.loadProfile(username);
        int targetLevel = Math.max(1, Math.min(profile.unlockedLevel, profile.currentLevel));
        startGame(username, mapFileForLevel(targetLevel));
    }

    /** Called by LevelSelectPanel — launch selected map. */
    public void startGame(String username, String mapFile) {
        int levelId = getLevelIdFromMap(mapFile);
        ProfileStore.PlayerProgress data = ProfileStore.loadProgress(username, levelId);
        List<Pokemon> team = ProfileStore.loadParty(username);
        if (team == null || team.isEmpty()) {
            team = buildStarterTeam();
        }
        healParty(team);
        ItemBag itemBag = ProfileStore.loadItemBag(username);

        MapLoader loader = new MapLoader(mapFile);
        int[][] worldMap = loader.getWorldMap();
        List<Trainer> trainers = loader.getTrainers();

        List<TerrainObjects> terrainObjects = loader.getTerrainObjects();
        terrainObjects.removeIf(obj -> obj.getTileValue() == 3 &&
            ProfileStore.hasCollectedPokeball(username, levelId, obj.getX() / TILE_SIZE, obj.getY() / TILE_SIZE));

        Point spawnPoint = resolveSpawnPoint(worldMap, terrainObjects, trainers, data.x, data.y);
        Player player = new Player(spawnPoint.x, spawnPoint.y, sanitizeSpriteRow(data.spriteRow), TILE_SIZE);

        if (activeGamePanel != null) {
            mainContainer.remove(activeGamePanel);
        }

        GamePanel gamePanel = new GamePanel(cardLayout, mainContainer, player,
            worldMap, trainers, terrainObjects, username, this, false, levelId, mapFile);
        gamePanel.setParty(team);
        gamePanel.setItemBag(itemBag);
        activeGamePanel = gamePanel;
        mainContainer.add(gamePanel, "PLAY GAME");

        cardLayout.show(mainContainer, "PLAY GAME");
        gamePanel.requestFocusInWindow();
    }

    private int getLevelIdFromMap(String mapFile) {
        if ("maps/level2_map.csv".equalsIgnoreCase(mapFile))
            return 2;
        if ("maps/level3_map.csv".equalsIgnoreCase(mapFile))
            return 3;
        return 1;
    }

    private String mapFileForLevel(int levelId) {
        switch (WordRepository.sanitizeLevel(levelId)) {
            case 2:
                return "maps/level2_map.csv";
            case 3:
                return "maps/level3_map.csv";
            default:
                return "maps/level1_map.csv";
        }
    }

    private Point resolveSpawnPoint(int[][] map, List<TerrainObjects> terrainObjects, List<Trainer> trainers,
                                    int savedX, int savedY) {
        if (isValidSpawn(map, terrainObjects, trainers, savedX, savedY)) {
            return new Point(savedX, savedY);
        }

        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[row].length; col++) {
                if (map[row][col] == 0) {
                    int px = col * TILE_SIZE;
                    int py = row * TILE_SIZE;
                    if (isValidSpawn(map, terrainObjects, trainers, px, py)) {
                        return new Point(px, py);
                    }
                }
            }
        }

        return new Point(0, 0);
    }

    private boolean isValidSpawn(int[][] map, List<TerrainObjects> terrainObjects, List<Trainer> trainers,
                                 int x, int y) {
        if (map == null || map.length == 0 || map[0].length == 0)
            return false;
        if (x < 0 || y < 0)
            return false;

        int col = x / TILE_SIZE;
        int row = y / TILE_SIZE;
        if (row < 0 || row >= map.length || col < 0 || col >= map[row].length)
            return false;

        if (map[row][col] == 5)
            return false;

        // Reject positions occupied by a solid terrain object (trees, rocks)
        if (terrainObjects != null) {
            for (TerrainObjects obj : terrainObjects) {
                if (obj.isSolid() && obj.collidesWith(x, y)) {
                    return false;
                }
            }
        }

        if (trainers != null) {
            for (Trainer trainer : trainers) {
                if (trainer.occupies(x, y)) {
                    return false;
                }
            }
        }

        return true;
    }

    private int sanitizeSpriteRow(int spriteRow) {
        int[] validRows = { 10, 76, 140, 200 };
        for (int validRow : validRows) {
            if (spriteRow == validRow)
                return spriteRow;
        }
        return 10;
    }

    public void startTutorial() {
        MapLoader tutorialLoader = new MapLoader("maps/tutorial_map.csv");
        List<Trainer> tutorialTrainers = tutorialLoader.getTrainers();

        if (tutorialGamePanel != null) {
            mainContainer.remove(tutorialGamePanel);
        }

        Player player = new Player(3 * TILE_SIZE, 3 * TILE_SIZE, 0, TILE_SIZE);
        tutorialGamePanel = new GamePanel(cardLayout, mainContainer, player, tutorialLoader.getWorldMap(),
                tutorialTrainers, tutorialLoader.getTerrainObjects(), "", this, true, 1, "maps/tutorial_map.csv");
        List<Pokemon> tutorialParty = new java.util.ArrayList<>();
        tutorialParty.add(Pokemon.random());
        tutorialGamePanel.setParty(tutorialParty);
        tutorialGamePanel.setItemBag(ItemBag.starterBag());

        mainContainer.add(tutorialGamePanel, "TUTORIAL_GAME");
        mainContainer.revalidate();
        mainContainer.repaint();

        cardLayout.show(mainContainer, "TUTORIAL_GAME");
        tutorialGamePanel.requestFocusInWindow();
    }

    /**
     * Utility method to return to the main menu.
     */
    public void returnToMenu() {
        cardLayout.show(mainContainer, "MENU");
    }

    private List<Pokemon> buildStarterTeam() {
        List<Pokemon> team = new ArrayList<>();
        List<Pokemon> pool = Pokemon.getPool();
        for (int i = 0; i < Math.min(3, pool.size()); i++) {
            Pokemon template = pool.get(i);
            Pokemon pokemon = new Pokemon(template.name, template.maxHp, template.attack, template.spritePath);
            pokemon.loadSprite();
            team.add(pokemon);
        }
        return team;
    }

    private void healParty(List<Pokemon> party) {
        for (Pokemon pokemon : party) {
            pokemon.currentHp = pokemon.maxHp;
        }
    }

    public void showAdminStats() {
        for (Component comp : mainContainer.getComponents()) {
            if (comp instanceof ParentStatsPanel) {
                ((ParentStatsPanel) comp).refreshData();
                break;
            }
        }
        cardLayout.show(mainContainer, "ADMIN_STATS");
    }
}
