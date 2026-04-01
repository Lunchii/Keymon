import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Random;

import javax.swing.*;

public class GamePanel extends JPanel {
    private int[][] map;
    private Player player;
    private Trainer trainer;
    private List<TerrainObjects> terrainObjects;
    private final int TILE_SIZE = 32;
    private int lastTileRow = -1;
    private int lastTileCol = -1;
    private final Random random = new Random();
    // Added for screen switching and saving
    private CardLayout cardLayout;
    private JPanel container;
    private String currentUsername; // Added to identify the user session

    private boolean isPaused = false;
    private int pauseOptionIndex = 0;
    private String[] pauseOptions = { "RESUME", "OPTIONS", "STATS", "QUIT TO MENU" };

    private boolean isStat = false;

    private TimeManager timeManager = new TimeManager(0);

    // Modified Constructor to accept data from Main.java (including static trainer
    // and terrain objects)
    // Addition: Added 'String username' to the parameter list
    public GamePanel(CardLayout layout, JPanel container, Player player, int[][] map, Trainer trainer,
                     List<TerrainObjects> terrainObjects, String username) {
        this.cardLayout = layout;
        this.container = container;
        this.player = player; // Use the player instance passed from Main.java
        this.map = map;
        this.trainer = trainer;
        this.terrainObjects = terrainObjects;
        this.currentUsername = username; // Store the session username
        this.player.setTerrainObjects(terrainObjects);

        this.setBackground(Color.BLACK);
        this.setFocusable(true);

        new InputHandler(this);
        timeManager.resume();

        // Inside GamePanel constructor
        this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                if (isPaused && !isStat) {
                    handleMouseHover(e.getX(), e.getY());
                }
            }
        });

        this.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (isPaused) {
                    handlePauseSelection(); // This already uses pauseOptionIndex
                }
            }
        });

        // Inside GamePanel constructor
        ActionListener taskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (player != null) {
                    if (isPaused)
                        return;
                    player.update(); // Move the player

                    // Check for entering tall grass when player finishes moving to a new tile
                    if (!player.isMoving) {
                        int currentCol = player.x / TILE_SIZE;
                        int currentRow = player.y / TILE_SIZE;

                        if (currentRow >= 0 && currentRow < map.length &&
                            currentCol >= 0 && currentCol < map[0].length) {

                            if (currentRow != lastTileRow || currentCol != lastTileCol) {
                                lastTileRow = currentRow;
                                lastTileCol = currentCol;

                                // Prototype: every tile has a chance for an encounter
                                maybeStartBattle();
                            }
                        }
                    }
                }
                repaint(); // Redraw the screen
            }
        };

        // 16ms delay = ~60 frames per second
        Timer timer = new Timer(16, taskPerformer);
        timer.start();
    }

    // Simple prototype typing battle triggered when stepping on a new tile
    private void maybeStartBattle() {
        // 20% chance to start a battle each time you step into tall grass
        if (random.nextDouble() > 0.2) {
            return;
        }

        String[] words = {"keyboard", "typing", "monster", "learning", "practice",
                          "accuracy", "student", "teacher", "dragon", "forest"};
        String target = words[random.nextInt(words.length)];

        // Switch to a dedicated battle screen that looks like a Pokémon battle
        BattlePanel battle = new BattlePanel(cardLayout, container, this, target);
        container.add(battle, "BATTLE");
        cardLayout.show(container, "BATTLE");
    }

    private void setupInputs() {
        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke("W"), "move_up");
        am.put("move_up", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!player.isMoving) {
                    player.targetY -= TILE_SIZE;
                    player.isMoving = true;
                    player.setDirection(200, 35, 52, 14, 29);
                    player.startAnimation();
                }
            }
        });
        im.put(KeyStroke.getKeyStroke("A"), "move_left");
        am.put("move_left", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!player.isMoving) {
                    player.targetX -= TILE_SIZE;
                    player.isMoving = true;
                    player.setDirection(76, 33, 48, 15, 32);
                    player.startAnimation();
                }
            }
        });
        im.put(KeyStroke.getKeyStroke("S"), "move_down");
        am.put("move_down", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!player.isMoving) {
                    player.targetY += TILE_SIZE;
                    player.isMoving = true;
                    player.setDirection(10, 35, 50, 14, 29);
                    player.startAnimation();
                }
            }
        });
        im.put(KeyStroke.getKeyStroke("D"), "move_right");
        am.put("move_right", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!player.isMoving) {
                    player.targetX += TILE_SIZE;
                    player.isMoving = true;
                    player.setDirection(140, 33, 48, 15, 32);
                    player.startAnimation();
                }
            }
        });
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isStat() {
        return isStat;
    }

    public Player getPlayer() {
        return player;
    }

    // Helper for menu navigation
    public void navigateMenuUp() {
        pauseOptionIndex = (pauseOptionIndex > 0) ? pauseOptionIndex - 1 : pauseOptions.length - 1;
        repaint();
    }

    public void navigateMenuDown() {
        pauseOptionIndex = (pauseOptionIndex < pauseOptions.length - 1) ? pauseOptionIndex + 1 : 0;
        repaint();
    }

    public void togglePause() {
        // If we are currently looking at Stats, Escape should go back to the Pause menu
        if (isStat) {
            isStat = false;
            repaint();
            return;
        }

        // Normal Pause/Unpause logic
        isPaused = !isPaused;

        if (isPaused) {
            timeManager.pause();
        } else {
            timeManager.resume();
        }

        pauseOptionIndex = 0; // Reset menu selection
        repaint();
    }

    public void handlePauseSelection() {
        // If we are currently looking at stats, ENTER should take us back
        if (isStat) {
            isStat = false;
            repaint();
            return;
        }

        if (pauseOptionIndex == 0)
            togglePause(); // Resume
        else if (pauseOptionIndex == 1)
            System.out.println("Options...");
        else if (pauseOptionIndex == 2) {
            isStat = true; // Open Stats
            repaint();
        } else if (pauseOptionIndex == 3) {
            // Addition: Save game data before returning to the menu
            SaveManager.saveGame(currentUsername, player.x, player.y, player.getSpriteRow());
            isPaused = false;
            isStat = false;
            timeManager.pause();
            cardLayout.show(container, "MENU");
        }
    }

    public TimeManager getTimeManager() {
        return timeManager;
    }

    public boolean getIsStat() {
        return isStat;
    }

    public String[] getPauseOptions() {
        return pauseOptions;
    }

    public int getPauseOptionIndex() {
        return pauseOptionIndex;
    }

    public int getTileSize() {
        return TILE_SIZE;
    }

    private void handleMouseHover(int mouseX, int mouseY) {
        int menuX = getWidth() / 2 - 100;
        int menuWidth = 200; // Approximate width of your text area
        int startY = 220; // Just above the first option
        int optionHeight = 50;

        // Check if mouse is within the horizontal bounds of the menu
        if (mouseX >= menuX && mouseX <= menuX + menuWidth) {
            for (int i = 0; i < pauseOptions.length; i++) {
                int top = startY + (i * optionHeight);
                int bottom = top + optionHeight;

                if (mouseY >= top && mouseY <= bottom) {
                    if (pauseOptionIndex != i) {
                        pauseOptionIndex = i;
                        repaint(); // Update the yellow highlight
                    }
                    return;
                }
            }
        }
    }

    private void drawMap(Graphics2D g2d) {
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[row].length; col++) {
                if (map[row][col] == 0)
                    g2d.setColor(new Color(34, 139, 34));

                int x = col * TILE_SIZE;
                int y = row * TILE_SIZE;
                g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);

                g2d.setColor(new Color(0, 100, 0));
                g2d.drawRect(x, y, TILE_SIZE, TILE_SIZE);
            }
        }

        // Draw Terrain Objects
        if (terrainObjects != null) {
            for (TerrainObjects obj : terrainObjects) {
                obj.draw(g2d);
            }
        }

        // Draw Trainer (static object)
        if (trainer != null) {
            trainer.draw(g2d);
        }
    }

    private UIManager uiManager = new UIManager(this);

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Loop through the 64x64 array
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[row].length; col++) {

                int tile = map[row][col];

                // 0 = regular grass (lighter), 1 = encounter grass (yellow)
                if (tile == 0) {
                    g2d.setColor(new Color(60, 170, 60));
                } else if (tile == 1) {
                    g2d.setColor(new Color(230, 230, 80)); // yellow patch where monsters may appear
                } else {
                    g2d.setColor(new Color(60, 170, 60));
                }

                int x = col * TILE_SIZE;
                int y = row * TILE_SIZE;

                g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);

                // Draw grid lines so you can see the tiles
                g2d.setColor(new Color(0, 100, 0));
                g2d.drawRect(x, y, TILE_SIZE, TILE_SIZE);
            }
        }

        // Draw Player
        player.draw(g);

        uiManager.draw(g2d);
    }

}