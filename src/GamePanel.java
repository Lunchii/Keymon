import java.awt.BasicStroke;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * World gameplay panel that handles exploration, battle encounters, score tracking, and level
 * progression for the typing adventure.
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
public class GamePanel extends JPanel {
    private static final int TILE_SIZE = 32;

    private final int[][] map;
    private final Player player;
    private final List<Trainer> trainers;
    private Trainer activeBattleTrainer;
    private final List<TerrainObjects> terrainObjects;
    private final Random random = new Random();

    private final CardLayout cardLayout;
    private final JPanel container;
    private final Main mainApp;
    private final String currentUsername;
    private final boolean tutorialMode;
    private final int currentLevelId;
    private final String currentMapFile;

    private final TimeManager timeManager = new TimeManager(0);
    private final UIManager uiManager;

    private BufferedImage tallGrassSprite;

    private List<Pokemon> party = new ArrayList<>();
    private ItemBag itemBag = ItemBag.starterBag();
    private int activePartyIndex;

    private boolean inBattle;
    private boolean showingPartyMenu;
    private boolean showingBagMenu;
    private boolean showSpottedMessage;
    private boolean tutorialBattleExplained;
    private boolean levelEnded;
    private boolean levelFailed;
    private boolean sessionRecorded;
    private boolean isPaused;
    private boolean isStat;
    private boolean showHud = true;

    private final String[] pauseOptions = {"RESUME", "RESTART STAGE", "VIEW STATS", "QUIT TO HUB"};
    private int pauseOptionIndex;

    private int lastTileRow = -1;
    private int lastTileCol = -1;

    private int levelScore;
    private int bestScore;
    private int wordsTyped;
    private int correctChars;
    private int totalTypedChars;
    private int errorCount;
    private int comboStreak;
    private int longestStreak;
    private int powerupsUsed;
    private int battlesWon;
    private double currentWpm;
    private double peakWpm;
    private long levelStartMillis;

    private Point endTilePoint;
    private String statusBanner = "";
    private long statusBannerUntil;

    public GamePanel(CardLayout layout, JPanel container, Player player, int[][] map, List<Trainer> trainers,
                     List<TerrainObjects> terrainObjects, String username, Main mainApp) {
        this(layout, container, player, map, trainers, terrainObjects, username, mainApp, false, 1, "maps/level1_map.csv");
    }

    public GamePanel(CardLayout layout, JPanel container, Player player, int[][] map, List<Trainer> trainers,
                     List<TerrainObjects> terrainObjects, String username, Main mainApp, boolean tutorialMode) {
        this(layout, container, player, map, trainers, terrainObjects, username, mainApp, tutorialMode, 1,
            tutorialMode ? "maps/tutorial_map.csv" : "maps/level1_map.csv");
    }

    public GamePanel(CardLayout layout, JPanel container, Player player, int[][] map, List<Trainer> trainers,
                     List<TerrainObjects> terrainObjects, String username, Main mainApp, boolean tutorialMode,
                     int levelId) {
        this(layout, container, player, map, trainers, terrainObjects, username, mainApp, tutorialMode, levelId,
            levelId == 2 ? "maps/level2_map.csv" : levelId == 3 ? "maps/level3_map.csv" : "maps/level1_map.csv");
    }

    public GamePanel(CardLayout layout, JPanel container, Player player, int[][] map, List<Trainer> trainers,
                     List<TerrainObjects> terrainObjects, String username, Main mainApp, boolean tutorialMode,
                     int levelId, String mapFile) {
        this.cardLayout = layout;
        this.container = container;
        this.player = player;
        this.map = map;
        this.trainers = trainers != null ? trainers : new ArrayList<>();
        this.terrainObjects = terrainObjects;
        this.currentUsername = username == null ? "" : username;
        this.mainApp = mainApp;
        this.tutorialMode = tutorialMode;
        this.currentLevelId = WordRepository.sanitizeLevel(levelId);
        this.currentMapFile = mapFile == null || mapFile.isBlank() ? defaultMapFile(this.currentLevelId) : mapFile;
        this.uiManager = new UIManager(this);
        this.bestScore = tutorialMode ? 0 : ProfileStore.loadProfile(this.currentUsername).highScore;
        this.levelStartMillis = System.currentTimeMillis();
        this.endTilePoint = findEndTilePoint();

        this.player.setTerrainObjects(terrainObjects);
        this.player.setTrainers(this.trainers);
        loadTallGrassSprite();
        if (!tutorialMode) {
            MusicPlayer.play("level" + this.currentLevelId);
        }

        setBackground(Color.BLACK);
        setFocusable(true);
        setLayout(null);
        buildButtons();

        new InputHandler(this);
        timeManager.resume();
        installMouseSupport();
        startGameLoop();
    }

    public List<Pokemon> getParty() {
        return party;
    }

    public void setParty(List<Pokemon> party) {
        this.party = party == null ? new ArrayList<>() : party;
        this.activePartyIndex = 0;
    }

    public ItemBag getItemBag() {
        return itemBag;
    }

    public void setItemBag(ItemBag itemBag) {
        this.itemBag = itemBag == null ? ItemBag.starterBag() : itemBag;
    }

    public int getActivePartyIndex() {
        return activePartyIndex;
    }

    public void setActivePartyIndex(int index) {
        if (index >= 0 && index < party.size()) {
            activePartyIndex = index;
        }
    }

    public boolean isShowingPartyMenu() {
        return showingPartyMenu;
    }

    public boolean isShowingBagMenu() {
        return showingBagMenu;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isInBattle() {
        return inBattle;
    }

    public void setInBattle(boolean inBattle) {
        this.inBattle = inBattle;
    }

    public boolean isTutorialMode() {
        return tutorialMode;
    }

    public boolean isStat() {
        return isStat;
    }

    public Player getPlayer() {
        return player;
    }

    public int getTileSize() {
        return TILE_SIZE;
    }

    public String[] getPauseOptions() {
        return pauseOptions;
    }

    public int getPauseOptionIndex() {
        return pauseOptionIndex;
    }

    public TimeManager getTimeManager() {
        return timeManager;
    }

    public boolean getIsStat() {
        return isStat;
    }

    public int getCurrentLevelId() {
        return currentLevelId;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public String getCurrentMapFile() {
        return currentMapFile;
    }

    public int getLevelScore() {
        return levelScore;
    }

    public int getBestScore() {
        return bestScore;
    }

    public int getWordsTyped() {
        return wordsTyped;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public double getCurrentWpm() {
        return currentWpm;
    }

    public double getPeakWpm() {
        return peakWpm;
    }

    public int getComboStreak() {
        return comboStreak;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public int getPowerupsUsed() {
        return powerupsUsed;
    }

    public String getStageName() {
        return WordRepository.getStageName(currentLevelId);
    }

    public String getStageDescription() {
        return WordRepository.getStageDescription(currentLevelId);
    }

    public String getStatusBanner() {
        return statusBanner;
    }

    public double getAccuracyPercent() {
        if (totalTypedChars <= 0) {
            return 100.0;
        }
        return (100.0 * correctChars) / totalTypedChars;
    }

    public void togglePartyMenu() {
        if (!showingPartyMenu) {
            showingBagMenu = false;
        }
        showingPartyMenu = !showingPartyMenu;
        repaint();
    }

    public void toggleBagMenu() {
        if (!showingBagMenu) {
            showingPartyMenu = false;
        }
        showingBagMenu = !showingBagMenu;
        repaint();
    }

    public void toggleHud() {
        showHud = !showHud;
        repaint();
    }

    public void navigateMenuUp() {
        pauseOptionIndex = pauseOptionIndex > 0 ? pauseOptionIndex - 1 : pauseOptions.length - 1;
        repaint();
    }

    public void navigateMenuDown() {
        pauseOptionIndex = pauseOptionIndex < pauseOptions.length - 1 ? pauseOptionIndex + 1 : 0;
        repaint();
    }

    public void togglePause() {
        if (levelEnded || levelFailed) {
            return;
        }
        if (isStat) {
            isStat = false;
            repaint();
            return;
        }
        isPaused = !isPaused;
        if (isPaused) {
            timeManager.pause();
        } else {
            timeManager.resume();
        }
        pauseOptionIndex = 0;
        repaint();
    }

    public void handlePauseSelection() {
        if (isStat) {
            isStat = false;
            repaint();
            return;
        }

        switch (pauseOptionIndex) {
            case 0:
                togglePause();
                break;
            case 1:
                restartStage();
                break;
            case 2:
                isStat = true;
                repaint();
                break;
            case 3:
                quitToHub();
                break;
            default:
                break;
        }
    }

    public void onTrainerDefeated() {
        if (activeBattleTrainer != null) {
            activeBattleTrainer.setDefeated(true);
            activeBattleTrainer = null;
        }
    }

    public void recordTypedCharacter(boolean correct) {
        if (levelEnded || levelFailed) {
            return;
        }

        totalTypedChars++;
        if (correct) {
            correctChars++;
        } else {
            errorCount++;
            comboStreak = 0;
        }

        updateLiveWpm();
    }

    public void recordCompletedWord(String word, int scoreAwarded) {
        if (word == null || word.isBlank()) {
            return;
        }

        wordsTyped++;
        comboStreak++;
        longestStreak = Math.max(longestStreak, comboStreak);
        levelScore += Math.max(0, scoreAwarded);
        bestScore = Math.max(bestScore, levelScore);
        updateLiveWpm();
        peakWpm = Math.max(peakWpm, currentWpm);
        setStatusBanner("Clean word! +" + scoreAwarded + " score");
    }

    public void notePowerupUsed(Item item) {
        powerupsUsed++;
        if (item != null) {
            setStatusBanner(item.displayName + " activated.");
        }
    }

    public void awardBattleVictory(Pokemon defeatedPokemon, boolean trainerBattle) {
        if (levelEnded || levelFailed) {
            return;
        }

        battlesWon++;
        int bonus = trainerBattle ? 180 : 95;
        if (defeatedPokemon != null) {
            bonus += defeatedPokemon.attack + (defeatedPokemon.maxHp / 5);
        }
        levelScore += bonus;
        bestScore = Math.max(bestScore, levelScore);

        String reward = maybeAwardItem(trainerBattle);
        String suffix = reward.isEmpty() ? "" : " " + reward;
        setStatusBanner("Battle won! +" + bonus + " score." + suffix);
    }

    public void handleLevelFailure(String reason) {
        if (levelFailed || levelEnded) {
            return;
        }

        levelFailed = true;
        inBattle = false;
        isPaused = false;
        isStat = false;
        timeManager.pause();
        persistSession(false, currentLevelId);

        if (tutorialMode) {
            JOptionPane.showMessageDialog(this,
                reason + "\nTry the tutorial stage again when you're ready.",
                "Tutorial Over",
                JOptionPane.WARNING_MESSAGE);
            cardLayout.show(container, "TUTORIAL");
            return;
        }

        String summary = reason + "\n\nScore: " + levelScore
            + "\nWords typed: " + wordsTyped
            + "\nAccuracy: " + String.format("%.1f%%", getAccuracyPercent())
            + "\nPeak WPM: " + String.format("%.1f", peakWpm);

        Object[] options = {"Retry Stage", "Player Hub"};
        int choice = JOptionPane.showOptionDialog(this, summary, "Game Over",
            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            restartStage();
        } else {
            mainApp.showPlayerScreen(currentUsername);
        }
    }

    public void setStatusBanner(String message) {
        this.statusBanner = message == null ? "" : message;
        this.statusBannerUntil = System.currentTimeMillis() + 2400L;
        repaint();
    }

    private void buildButtons() {
        addActionButton("PARTY  [P]", new Color(30, 90, 50), 10, event -> {
            togglePartyMenu();
            requestFocusInWindow();
        });
        addActionButton("MENU  [ESC]", new Color(70, 50, 20), 150, event -> {
            togglePause();
            requestFocusInWindow();
        });
        addActionButton("BAG  [B]", new Color(80, 55, 120), 290, event -> {
            toggleBagMenu();
            requestFocusInWindow();
        });
    }

    private void addActionButton(String text, Color background, int x, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBounds(x, 10, 130, 34);
        button.addActionListener(listener);
        add(button);
    }

    private void installMouseSupport() {
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent event) {
                if (isPaused && !isStat) {
                    handleMouseHover(event.getX(), event.getY());
                }
            }
        });

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent event) {
                if (isPaused) {
                    handlePauseSelection();
                }
            }
        });
    }

    private void startGameLoop() {
        Timer timer = new Timer(16, (ActionEvent event) -> {
            if (!isPaused && !levelEnded && !levelFailed && player != null) {
                player.update();
                int playerCol = (int)player.x / TILE_SIZE;
                int playerRow = (int)player.y / TILE_SIZE;
                if (playerRow != lastTileRow || playerCol != lastTileCol) {
                    lastTileRow = playerRow;
                    lastTileCol = playerCol;
                    checkTrainerLineOfSight(playerCol, playerRow);
                    if (!inBattle) {
                        maybeStartWildBattle();
                    }
                    checkPokeballPickup();
                }
                if (!inBattle) {
                    checkLevelEnd();
                }
            }

            if (!statusBanner.isEmpty() && System.currentTimeMillis() > statusBannerUntil) {
                statusBanner = "";
            }
            repaint();
        });
        timer.start();
    }

    private void checkPokeballPickup() {
        if (terrainObjects == null || player == null) {
            return;
        }

        int playerX = (int)player.x;
        int playerY = (int)player.y;

        for (int i = terrainObjects.size() - 1; i >= 0; i--) {
            TerrainObjects obj = terrainObjects.get(i);
            if (obj.getTileValue() == 3 && obj.collidesWith(playerX, playerY)) {
                Item[] items = Item.values();
                Item earned = items[random.nextInt(items.length)];
                itemBag.add(earned, 1);

                statusBanner = "Picked up " + earned.displayName + "!";
                statusBannerUntil = System.currentTimeMillis() + 2000;
                
                int pickedCol = obj.getX() / TILE_SIZE;
                int pickedRow = obj.getY() / TILE_SIZE;
                ProfileStore.recordPokeballPickup(currentUsername, currentLevelId, pickedCol, pickedRow);

                terrainObjects.remove(i);
            }
        }
    }

    private void loadTallGrassSprite() {
        try {
            tallGrassSprite = ImageIO.read(new File("images/tall_grass.png"));
        } catch (Exception exception) {
            System.err.println("Warning: Could not load tall_grass.png");
        }
    }

    private void checkTrainerLineOfSight(int playerCol, int playerRow) {
        if (inBattle || party.isEmpty() || trainers == null) return;
        for (Trainer t : trainers) {
            if (t.canSeePlayer(playerCol, playerRow)) {
                activeBattleTrainer = t;
                inBattle = true;
                showSpottedMessage = true;
                List<Pokemon> team = copyTeam(t.getTeam());
                Timer timer = new Timer(900, event -> {
                    ((Timer) event.getSource()).stop();
                    showSpottedMessage = false;
                    startBattleSequence(team, true);
                });
                timer.setRepeats(false);
                timer.start();
                return;
            }
        }
    }

    private void maybeStartWildBattle() {
        int col = (int)player.x / TILE_SIZE;
        int row = (int)player.y / TILE_SIZE;
        if (row < 0 || row >= map.length || col < 0 || col >= map[row].length || party.isEmpty()) {
            return;
        }

        int tile = map[row][col];
        if (tile != 9 && tile != 10) {
            return;
        }

        double encounterChance = tile == 10 ? 0.45 : 0.20;
        if (random.nextDouble() > encounterChance) {
            return;
        }

        inBattle = true;
        startBattleSequence(null, false);
    }

    private void startBattleSequence(List<Pokemon> trainerTeam, boolean trainerBattle) {
        if (tutorialMode && !tutorialBattleExplained) {
            tutorialBattleExplained = true;
            showTutorialBattlePopup(trainerTeam, trainerBattle);
            return;
        }
        openBattlePanel(trainerTeam, trainerBattle);
    }

    private void showTutorialBattlePopup(List<Pokemon> trainerTeam, boolean trainerBattle) {
        JPanel overlay = new JPanel(new java.awt.GridBagLayout());
        overlay.setBackground(new Color(0, 0, 0, 210));
        overlay.setOpaque(true);

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(new Color(12, 18, 22));
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 3),
            BorderFactory.createEmptyBorder(24, 32, 24, 32)
        ));

        JLabel title = new JLabel("HOW BATTLES WORK");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(title);
        box.add(Box.createVerticalStrut(16));

        String[] lines = {
            "Type the highlighted word before the timer runs out.",
            "Correct letters glow green as you build the word.",
            "Mistakes reset your combo streak, so accuracy matters.",
            "Long streaks earn bigger score bonuses.",
            "Use BAG powerups for extra time, healing, or stronger attacks.",
            "Reach the dark exit tile after surviving the route."
        };

        for (String line : lines) {
            JLabel label = new JLabel(line);
            label.setFont(new Font("SansSerif", Font.PLAIN, 14));
            label.setForeground(Color.WHITE);
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            box.add(label);
            box.add(Box.createVerticalStrut(6));
        }

        JButton startButton = new JButton("START BATTLE");
        startButton.setFont(new Font("SansSerif", Font.BOLD, 15));
        startButton.setBackground(new Color(60, 150, 60));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setBorderPainted(false);
        startButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.addActionListener(event -> {
            remove(overlay);
            revalidate();
            repaint();
            openBattlePanel(trainerTeam, trainerBattle);
        });
        box.add(Box.createVerticalStrut(12));
        box.add(startButton);

        overlay.add(box);
        overlay.setBounds(0, 0, Math.max(1, getWidth()), Math.max(1, getHeight()));
        add(overlay);
        setComponentZOrder(overlay, 0);
        revalidate();
        repaint();
    }

    private void openBattlePanel(List<Pokemon> trainerTeam, boolean trainerBattle) {
        Pokemon opponent = trainerBattle && trainerTeam != null && !trainerTeam.isEmpty()
            ? trainerTeam.get(0)
            : Pokemon.random();

        for (Component component : container.getComponents()) {
            if (component instanceof BattlePanel) {
                container.remove(component);
            }
        }

        BattlePanel battlePanel = new BattlePanel(cardLayout, container, this, opponent, party);
        if (trainerBattle && trainerTeam != null && !trainerTeam.isEmpty()) {
            battlePanel.setTrainerBattle(trainerTeam);
        }

        container.add(battlePanel, "BATTLE");
        // Stop player movement when entering battle
        player.moveUp = false;
        player.moveDown = false;
        player.moveLeft = false;
        player.moveRight = false;
        cardLayout.show(container, "BATTLE");
        battlePanel.requestFocusInWindow();
    }

    private void handleMouseHover(int mouseX, int mouseY) {
        int menuX = getWidth() / 2 - 120;
        int menuWidth = 240;
        int startY = 220;
        int optionHeight = 52;
        if (mouseX >= menuX && mouseX <= menuX + menuWidth) {
            for (int i = 0; i < pauseOptions.length; i++) {
                int top = startY + (i * optionHeight);
                int bottom = top + optionHeight;
                if (mouseY >= top && mouseY <= bottom) {
                    if (pauseOptionIndex != i) {
                        pauseOptionIndex = i;
                        repaint();
                    }
                    return;
                }
            }
        }
    }

    private void restartStage() {
        isPaused = false;
        isStat = false;
        timeManager.pause();
        if (tutorialMode) {
            mainApp.startTutorial();
        } else {
            mainApp.startGame(currentUsername, currentMapFile);
        }
    }

    private void quitToHub() {
        isPaused = false;
        isStat = false;
        timeManager.pause();

        if (tutorialMode) {
            mainApp.returnToMenu();
            return;
        }

        persistSession(false, currentLevelId);
        JOptionPane.showMessageDialog(this,
            "Progress saved.\nYou can continue from the player hub.",
            "Saved",
            JOptionPane.INFORMATION_MESSAGE);
        mainApp.showPlayerScreen(currentUsername);
    }

    private void updateLiveWpm() {
        long elapsedMillis = Math.max(1L, System.currentTimeMillis() - levelStartMillis);
        double elapsedMinutes = elapsedMillis / 60000.0;
        currentWpm = elapsedMinutes <= 0.0 ? 0.0 : wordsTyped / elapsedMinutes;
        peakWpm = Math.max(peakWpm, currentWpm);
    }

    private void checkLevelEnd() {
        if (levelEnded || player.isMoving) {
            return;
        }

        int col = (int)player.x / TILE_SIZE;
        int row = (int)player.y / TILE_SIZE;
        if (row < 0 || row >= map.length || col < 0 || col >= map[row].length) {
            return;
        }

        if (map[row][col] == 5) {
            levelEnded = true;
            timeManager.pause();
            levelScore += WordRepository.levelCompletionBonus(currentLevelId);
            bestScore = Math.max(bestScore, levelScore);

            if (tutorialMode) {
                JOptionPane.showMessageDialog(this,
                    "Tutorial clear!\nYou reached the goal tile and learned the battle flow.",
                    "Tutorial Complete",
                    JOptionPane.INFORMATION_MESSAGE);
                cardLayout.show(container, "TUTORIAL");
                return;
            }

            int nextLevel = Math.min(3, currentLevelId + 1);
            ProfileStore.markLevelCompleted(currentUsername, currentLevelId);
            persistSession(true, nextLevel);

            String summary = "Stage cleared: " + WordRepository.getStageName(currentLevelId)
                + "\nScore: " + levelScore
                + "\nWords typed: " + wordsTyped
                + "\nAccuracy: " + String.format("%.1f%%", getAccuracyPercent())
                + "\nPeak WPM: " + String.format("%.1f", peakWpm)
                + "\nBest score: " + bestScore;

            Object[] options = currentLevelId < 3
                ? new Object[]{"Next Stage", "Replay Stage", "Player Hub"}
                : new Object[]{"Replay Stage", "Player Hub"};

            int choice = JOptionPane.showOptionDialog(this, summary, "Stage Complete",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

            if (currentLevelId < 3 && choice == 0) {
                mainApp.startGame(currentUsername, defaultMapFile(nextLevel));
            } else if ((currentLevelId < 3 && choice == 1) || (currentLevelId == 3 && choice == 0)) {
                mainApp.startGame(currentUsername, currentMapFile);
            } else {
                mainApp.showPlayerScreen(currentUsername);
            }
        }
    }

    private void persistSession(boolean wonLevel, int progressLevel) {
        if (tutorialMode || sessionRecorded || currentUsername.isBlank()) {
            return;
        }

        ProfileStore.recordLevelSummary(currentUsername, new ProfileStore.SessionStats(
            currentLevelId,
            levelScore,
            currentWpm,
            peakWpm,
            correctChars,
            totalTypedChars,
            errorCount,
            wordsTyped,
            Math.max(1L, (System.currentTimeMillis() - levelStartMillis) / 1000L),
            longestStreak,
            powerupsUsed,
            wonLevel
        ));

        ProfileStore.saveProgress(currentUsername, currentLevelId, (int)player.x, (int)player.y,
            player.getSpriteRow(), bestScore, progressLevel, party, itemBag);
        sessionRecorded = true;
    }

    private String maybeAwardItem(boolean trainerBattle) {
        if (tutorialMode) {
            return "";
        }

        double chance = trainerBattle ? 1.0 : 0.45;
        if (random.nextDouble() > chance) {
            return "";
        }

        Item reward = Item.values()[random.nextInt(Item.values().length)];
        itemBag.add(reward, 1);
        return "Found " + reward.displayName + " x1!";
    }

    private List<Pokemon> copyTeam(List<Pokemon> original) {
        List<Pokemon> copy = new ArrayList<>();
        if (original == null) {
            return copy;
        }
        for (Pokemon pokemon : original) {
            Pokemon cloned = pokemon.copy();
            cloned.loadSprite();
            copy.add(cloned);
        }
        return copy;
    }

    private void drawTutorialBubbles(Graphics2D graphics) {
        if (trainers != null && !trainers.isEmpty()) {
            Trainer t = trainers.get(0);
            int bubbleX = t.getX() + 52;
            int bubbleY = t.getY() - 70;
            drawBubble(graphics, bubbleX, bubbleY,
                "Trainer battles are tougher. Beat the trainer team to clear the lane.");
            drawBubbleArrow(graphics, bubbleX + 22, bubbleY + 50, t.getX() + 16, t.getY() - 8);
        }

        if (endTilePoint != null) {
            int bubbleX = endTilePoint.x + 58;
            int bubbleY = endTilePoint.y - 46;
            drawBubble(graphics, bubbleX, bubbleY,
                "The dark exit tile finishes the level after you survive the route.");
            drawBubbleArrow(graphics, bubbleX + 22, bubbleY + 42,
                endTilePoint.x + TILE_SIZE / 2, endTilePoint.y + TILE_SIZE / 2);
        }
    }

    private void drawBubble(Graphics2D graphics, int x, int y, String text) {
        int width = 320;
        int height = 66;
        graphics.setColor(new Color(0, 0, 0, 210));
        graphics.fillRoundRect(x, y, width, height, 16, 16);
        graphics.setColor(Color.WHITE);
        graphics.setStroke(new BasicStroke(2));
        graphics.drawRoundRect(x, y, width, height, 16, 16);

        graphics.setFont(new Font("Arial", Font.BOLD, 12));
        drawStringWrapped(graphics, text, x + 12, y + 18, width - 24, 16);
    }

    private void drawBubbleArrow(Graphics2D graphics, int fromX, int fromY, int toX, int toY) {
        graphics.setColor(Color.WHITE);
        graphics.setStroke(new BasicStroke(2));
        graphics.drawLine(fromX, fromY, toX, toY);
        graphics.drawLine(toX, toY, toX - 6, toY - 6);
        graphics.drawLine(toX, toY, toX + 6, toY - 6);
    }

    private void drawStringWrapped(Graphics2D graphics, String text, int x, int y, int maxWidth, int lineHeight) {
        FontMetrics metrics = graphics.getFontMetrics();
        String[] words = text.split(" ");
        String line = "";
        for (String word : words) {
            String test = line.isEmpty() ? word : line + " " + word;
            if (metrics.stringWidth(test) > maxWidth) {
                graphics.drawString(line, x, y);
                line = word;
                y += lineHeight;
            } else {
                line = test;
            }
        }
        if (!line.isEmpty()) {
            graphics.drawString(line, x, y);
        }
    }

    private Point findEndTilePoint() {
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[row].length; col++) {
                if (map[row][col] == 5) {
                    return new Point(col * TILE_SIZE, row * TILE_SIZE);
                }
            }
        }
        return null;
    }

    private String defaultMapFile(int levelId) {
        switch (WordRepository.sanitizeLevel(levelId)) {
            case 2:
                return "maps/level2_map.csv";
            case 3:
                return "maps/level3_map.csv";
            default:
                return "maps/level1_map.csv";
        }
    }

    private int getCameraX() {
        int mapPixelWidth = map.length > 0 ? map[0].length * TILE_SIZE : 0;
        int screenWidth = getWidth();
        if (mapPixelWidth <= screenWidth) return 0;
        int camX = (int)player.x + TILE_SIZE / 2 - screenWidth / 2;
        return Math.max(0, Math.min(camX, mapPixelWidth - screenWidth));
    }

    private int getCameraY() {
        int mapPixelHeight = map.length * TILE_SIZE;
        int screenHeight = getHeight();
        if (mapPixelHeight <= screenHeight) return 0;
        int camY = (int)player.y + TILE_SIZE / 2 - screenHeight / 2;
        return Math.max(0, Math.min(camY, mapPixelHeight - screenHeight));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        try {
            drawMap(g2);
        } finally {
            g2.dispose();
        }
    }

    private void drawMap(Graphics2D graphics) {
        Color baseGround = stageBaseColor();
        Color gridColor = stageGridColor();
        Color exitColor = stageExitColor();
        Color battlePatchColor = stageBattlePatchColor();

        // Fill screen background first (screen space)
        graphics.setPaint(new GradientPaint(0, 0, baseGround.brighter(), getWidth(), getHeight(), baseGround.darker()));
        graphics.fillRect(0, 0, getWidth(), getHeight());

        // Apply camera translation for world-space rendering
        int camX = getCameraX();
        int camY = getCameraY();
        graphics.translate(-camX, -camY);

        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[row].length; col++) {
                int x = col * TILE_SIZE;
                int y = row * TILE_SIZE;
                int tile = map[row][col];

                graphics.setColor(tile == 10 ? battlePatchColor : baseGround);
                graphics.fillRect(x, y, TILE_SIZE, TILE_SIZE);

                if (tile == 5) {
                    graphics.setColor(exitColor);
                    graphics.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                }

                if ((tile == 9 || tile == 10) && tallGrassSprite != null) {
                    graphics.drawImage(tallGrassSprite, x, y, TILE_SIZE, TILE_SIZE, null);
                }

                graphics.setColor(gridColor);
                graphics.drawRect(x, y, TILE_SIZE, TILE_SIZE);
            }
        }

        if (terrainObjects != null) {
            for (TerrainObjects object : terrainObjects) {
                object.draw(graphics);
            }
        }

        if (trainers != null) {
            for (Trainer t : trainers) {
                t.draw(graphics);
            }
        }

        player.draw(graphics);

        if (tutorialMode) {
            drawTutorialBubbles(graphics);
        }

        // Reset to screen space for HUD and overlays
        graphics.translate(camX, camY);

        if (showHud) {
            drawHud(graphics);
        } else {
            drawExpandButton(graphics);
        }
        uiManager.draw(graphics);

        if (showingPartyMenu) {
            drawPartyOverlay(graphics);
        }
        if (showingBagMenu) {
            drawBagOverlay(graphics);
        }
        if (showSpottedMessage) {
            drawSpottedOverlay(graphics);
        }
    }

    private void drawHud(Graphics2D graphics) {
        int hudX = getWidth() - 320;
        int hudY = 16;
        int hudW = 296;
        int hudH = 154;

        graphics.setColor(new Color(8, 15, 26, 210));
        graphics.fillRoundRect(hudX, hudY, hudW, hudH, 18, 18);
        graphics.setColor(new Color(255, 255, 255, 70));
        graphics.setStroke(new BasicStroke(2f));
        graphics.drawRoundRect(hudX, hudY, hudW, hudH, 18, 18);

        // Draw "press T to hide" text in top right corner
        graphics.setColor(new Color(255, 255, 255, 180));
        graphics.setFont(new Font("SansSerif", Font.PLAIN, 12));
        FontMetrics metrics = graphics.getFontMetrics();
        String hideText = "Press T to Hide";
        int textX = getWidth() - metrics.stringWidth(hideText) - 40;
        int textY = 40;
        graphics.drawString(hideText, textX, textY);

        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("SansSerif", Font.BOLD, 19));
        graphics.drawString(getStageName(), hudX + 18, hudY + 28);

        graphics.setFont(new Font("SansSerif", Font.PLAIN, 13));
        graphics.setColor(new Color(220, 230, 245));
        graphics.drawString(getStageDescription(), hudX + 18, hudY + 48);

        graphics.setFont(new Font("SansSerif", Font.BOLD, 15));
        graphics.setColor(Color.WHITE);
        graphics.drawString("Score: " + levelScore, hudX + 18, hudY + 76);
        graphics.drawString("Best: " + bestScore, hudX + 156, hudY + 76);
        graphics.drawString("WPM: " + String.format("%.1f", currentWpm), hudX + 18, hudY + 98);
        graphics.drawString("Accuracy: " + String.format("%.1f%%", getAccuracyPercent()), hudX + 156, hudY + 98);
        graphics.drawString("Words: " + wordsTyped, hudX + 18, hudY + 120);
        graphics.drawString("Combo: x" + Math.max(1, comboStreak), hudX + 156, hudY + 120);
        graphics.drawString("Items: " + totalItemsInBag(), hudX + 18, hudY + 142);
        graphics.drawString("Battles: " + battlesWon, hudX + 156, hudY + 142);

        if (!statusBanner.isEmpty()) {
            graphics.setColor(new Color(0, 0, 0, 180));
            graphics.fillRoundRect(20, getHeight() - 58, 410, 36, 14, 14);
            graphics.setColor(new Color(255, 255, 255, 85));
            graphics.drawRoundRect(20, getHeight() - 58, 410, 36, 14, 14);
            graphics.setColor(Color.WHITE);
            graphics.setFont(new Font("SansSerif", Font.BOLD, 14));
            graphics.drawString(statusBanner, 34, getHeight() - 35);
        }
    }

    private int totalItemsInBag() {
        int total = 0;
        for (Integer count : itemBag.getAll().values()) {
            total += count;
        }
        return total;
    }

    private void drawExpandButton(Graphics2D graphics) {
        int buttonX = getWidth() - 200;
        int buttonY = 16;
        int buttonW = 180;
        int buttonH = 40;

        graphics.setColor(new Color(8, 15, 26, 210));
        graphics.fillRoundRect(buttonX, buttonY, buttonW, buttonH, 12, 12);
        graphics.setColor(new Color(255, 255, 255, 70));
        graphics.setStroke(new BasicStroke(2f));
        graphics.drawRoundRect(buttonX, buttonY, buttonW, buttonH, 12, 12);

        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics metrics = graphics.getFontMetrics();
        String text = "Press T to expand";
        int textX = buttonX + (buttonW - metrics.stringWidth(text)) / 2;
        int textY = buttonY + (buttonH + metrics.getAscent() - metrics.getDescent()) / 2;
        graphics.drawString(text, textX, textY);
    }

    private void drawSpottedOverlay(Graphics2D graphics) {
        int width = getWidth();
        int height = getHeight();
        graphics.setColor(new Color(0, 0, 0, 165));
        graphics.fillRect(0, 0, width, height);

        int boxW = 360;
        int boxH = 88;
        int boxX = (width - boxW) / 2;
        int boxY = height / 2 - 60;
        graphics.setColor(new Color(220, 60, 60));
        graphics.fillRoundRect(boxX, boxY, boxW, boxH, 16, 16);
        graphics.setColor(Color.WHITE);
        graphics.setStroke(new BasicStroke(3f));
        graphics.drawRoundRect(boxX, boxY, boxW, boxH, 16, 16);

        graphics.setFont(new Font("SansSerif", Font.BOLD, 28));
        FontMetrics metrics = graphics.getFontMetrics();
        String line1 = "A Trainer Spotted You!";
        graphics.drawString(line1, boxX + (boxW - metrics.stringWidth(line1)) / 2, boxY + 52);

        graphics.setFont(new Font("SansSerif", Font.PLAIN, 15));
        metrics = graphics.getFontMetrics();
        String line2 = "Get ready for a tougher typing battle.";
        graphics.setColor(new Color(255, 230, 230));
        graphics.drawString(line2, boxX + (boxW - metrics.stringWidth(line2)) / 2, boxY + boxH + 24);
    }

    private void drawPartyOverlay(Graphics2D graphics) {
        int width = getWidth();
        int height = getHeight();
        graphics.setColor(new Color(0, 0, 0, 185));
        graphics.fillRect(0, 0, width, height);

        int panelW = 540;
        int panelH = 370;
        int panelX = (width - panelW) / 2;
        int panelY = (height - panelH) / 2;

        graphics.setColor(new Color(15, 50, 35, 242));
        graphics.fillRoundRect(panelX, panelY, panelW, panelH, 20, 20);
        graphics.setColor(new Color(100, 200, 130));
        graphics.setStroke(new BasicStroke(2f));
        graphics.drawRoundRect(panelX, panelY, panelW, panelH, 20, 20);

        graphics.setFont(new Font("SansSerif", Font.BOLD, 20));
        graphics.setColor(Color.WHITE);
        graphics.drawString("YOUR PARTY  (P to close)", panelX + 20, panelY + 30);
        graphics.setFont(new Font("SansSerif", Font.PLAIN, 12));
        graphics.setColor(new Color(180, 220, 180));
        String activeText = party.isEmpty() ? "-" : party.get(activePartyIndex).name;
        graphics.drawString("Active: " + activeText, panelX + 20, panelY + 50);

        int slotW = 488;
        int slotH = 90;
        int startX = panelX + 20;
        int startY = panelY + 68;
        int gapY = 12;

        for (int i = 0; i < 3; i++) {
            int slotY = startY + i * (slotH + gapY);
            if (i < party.size()) {
                Pokemon pokemon = party.get(i);
                boolean active = i == activePartyIndex;

                graphics.setColor(active ? new Color(40, 100, 60) : new Color(20, 60, 40));
                graphics.fillRoundRect(startX, slotY, slotW, slotH, 10, 10);
                graphics.setColor(active ? new Color(100, 220, 140) : new Color(60, 120, 80));
                graphics.setStroke(new BasicStroke(active ? 2f : 1f));
                graphics.drawRoundRect(startX, slotY, slotW, slotH, 10, 10);

                Image sprite = pokemon.getSprite();
                if (sprite != null) {
                    graphics.drawImage(sprite, startX + 4, slotY + 4, 72, 72, null);
                }

                graphics.setFont(new Font("SansSerif", Font.BOLD, 14));
                graphics.setColor(pokemon.isFainted() ? new Color(200, 80, 80) : Color.WHITE);
                graphics.drawString(pokemon.name + (pokemon.isFainted() ? "  [FAINTED]" : ""),
                    startX + 84, slotY + 20);

                int barX = startX + 84;
                int barY = slotY + 32;
                int barW = 150;
                int barH = 10;
                graphics.setColor(new Color(10, 30, 20));
                graphics.fillRoundRect(barX, barY, barW, barH, 4, 4);
                float ratio = (float) pokemon.currentHp / Math.max(1, pokemon.maxHp);
                graphics.setColor(ratio > 0.5f ? new Color(60, 210, 100) : ratio > 0.25f
                    ? new Color(230, 200, 50) : new Color(220, 70, 70));
                graphics.fillRoundRect(barX, barY, (int) (barW * Math.max(0, ratio)), barH, 4, 4);
                graphics.setFont(new Font("SansSerif", Font.PLAIN, 12));
                graphics.setColor(Color.WHITE);
                graphics.drawString(pokemon.currentHp + " / " + pokemon.maxHp, barX, slotY + 58);
                graphics.drawString("ATK " + pokemon.attack, barX + 96, slotY + 58);
            } else {
                graphics.setColor(new Color(15, 40, 28));
                graphics.fillRoundRect(startX, slotY, slotW, slotH, 10, 10);
                graphics.setColor(new Color(40, 70, 50));
                graphics.drawRoundRect(startX, slotY, slotW, slotH, 10, 10);
                graphics.setFont(new Font("SansSerif", Font.ITALIC, 13));
                graphics.setColor(new Color(80, 120, 90));
                graphics.drawString("Empty", startX + 100, slotY + 55);
            }
        }
    }

    private void drawBagOverlay(Graphics2D graphics) {
        int width = getWidth();
        int height = getHeight();
        graphics.setColor(new Color(0, 0, 0, 180));
        graphics.fillRect(0, 0, width, height);

        int panelW = 560;
        int panelH = 340;
        int panelX = (width - panelW) / 2;
        int panelY = (height - panelH) / 2;

        graphics.setColor(new Color(30, 20, 55, 240));
        graphics.fillRoundRect(panelX, panelY, panelW, panelH, 20, 20);
        graphics.setColor(new Color(170, 140, 220));
        graphics.setStroke(new BasicStroke(2f));
        graphics.drawRoundRect(panelX, panelY, panelW, panelH, 20, 20);

        graphics.setFont(new Font("SansSerif", Font.BOLD, 20));
        graphics.setColor(Color.WHITE);
        graphics.drawString("ITEM BAG  (B to close)", panelX + 20, panelY + 32);
        graphics.setFont(new Font("SansSerif", Font.PLAIN, 12));
        graphics.setColor(new Color(215, 205, 235));
        graphics.drawString("Battle powerups are earned by winning fights and help on harder stages.",
            panelX + 20, panelY + 54);

        int cardX = panelX + 20;
        int cardY = panelY + 76;
        int cardW = panelW - 40;
        int cardH = 52;
        int gap = 12;

        for (int i = 0; i < Item.values().length; i++) {
            Item item = Item.values()[i];
            int y = cardY + i * (cardH + gap);
            int count = itemBag.getCount(item);

            graphics.setColor(item.color.darker());
            graphics.fillRoundRect(cardX, y, cardW, cardH, 14, 14);
            graphics.setColor(new Color(255, 255, 255, 90));
            graphics.drawRoundRect(cardX, y, cardW, cardH, 14, 14);

            graphics.setFont(new Font("SansSerif", Font.BOLD, 16));
            graphics.setColor(Color.WHITE);
            graphics.drawString(item.displayName + "  x" + count, cardX + 16, y + 22);

            graphics.setFont(new Font("SansSerif", Font.PLAIN, 13));
            graphics.setColor(new Color(240, 240, 240));
            graphics.drawString(item.description, cardX + 16, y + 40);
        }
    }

    private Color stageBaseColor() {
        switch (currentLevelId) {
            case 2:
                return new Color(56, 94, 72);
            case 3:
                return new Color(87, 106, 145);
            default:
                return new Color(88, 176, 88);
        }
    }

    private Color stageGridColor() {
        switch (currentLevelId) {
            case 2:
                return new Color(35, 70, 55);
            case 3:
                return new Color(60, 82, 118);
            default:
                return new Color(42, 122, 42);
        }
    }

    private Color stageExitColor() {
        switch (currentLevelId) {
            case 2:
                return new Color(35, 20, 45);
            case 3:
                return new Color(20, 24, 46);
            default:
                return new Color(28, 28, 28);
        }
    }

    private Color stageBattlePatchColor() {
        switch (currentLevelId) {
            case 2:
                return new Color(128, 94, 68);
            case 3:
                return new Color(142, 124, 82);
            default:
                return new Color(173, 122, 70);
        }
    }
}
