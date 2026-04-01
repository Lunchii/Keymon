import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.*;

public class GamePanel extends JPanel {
    private int[][] map;
    private Player player;
    private Trainer trainer;
    private List<TerrainObjects> terrainObjects;
    private final int TILE_SIZE = 32;

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
                }
                repaint(); // Redraw the screen
            }
        };

        // 16ms delay = ~60 frames per second
        Timer timer = new Timer(16, taskPerformer);
        timer.start();
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

        drawMap(g2d);
        player.draw(g);

        uiManager.draw(g2d);
    }

}