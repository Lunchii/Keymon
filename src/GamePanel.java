import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class GamePanel extends JPanel {
    private int[][] map;
    private Player player;
    private final int TILE_SIZE = 32;
    private static final int TILE_GRASS = 0;
    private static final int TILE_WALL = 1;
    private static final int TILE_BATTLE = 2;
    // Added for screen switching and saving
    private CardLayout cardLayout;
    private JPanel container;
    private boolean saveOnBack = true;
    // While a battle is active the player cannot move.
    private boolean battleActive = false;
    private String screenName;
    private BattlePanel battlePanel;
    private int lastTileX = -1;
    private int lastTileY = -1;

    public GamePanel(CardLayout layout, JPanel container, Player player, int[][] map) {
        this(layout, container, player, map, true, "PLAY GAME", null);
    }

    public GamePanel(CardLayout layout, JPanel container, Player player, int[][] map, boolean saveOnBack) {
        this(layout, container, player, map, saveOnBack, "PLAY GAME", null);
    }

    public GamePanel(CardLayout layout, JPanel container, Player player, int[][] map, boolean saveOnBack, String screenName, BattlePanel battlePanel) {
        this.cardLayout = layout;
        this.container = container;
        this.player = player; // Use the player instance passed from Main.java
        this.map = map;
        this.saveOnBack = saveOnBack;
        this.screenName = screenName;
        this.battlePanel = battlePanel;

        this.setBackground(Color.BLACK);
        this.setFocusable(true);

        // Add a button to save and go back to menu
        JButton backButton = new JButton(saveOnBack ? "Save & Back" : "Back");
        backButton.addActionListener(e -> {
            if (saveOnBack) {
                SaveManager.saveGame(player.x, player.y, player.getSpriteRow());
            }
            cardLayout.show(container, "MENU");
        });
        this.add(backButton);

        setupInputs();

        // Inside GamePanel constructor
        ActionListener taskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (player != null) {
                    player.update(); // Move the player
                    handleTileEvents();
                }
                repaint(); // Redraw the screen
            }
        };

        // 16ms delay = ~60 frames per second
        Timer timer = new Timer(16, taskPerformer);
        timer.start();
    }

    private void setupInputs() {
        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke("W"), "move_up");
        am.put("move_up", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (battleActive || player.isMoving) {
                    return;
                }
                int currentTileX = player.x / TILE_SIZE;
                int currentTileY = player.y / TILE_SIZE;
                if (canMoveTo(currentTileX, currentTileY - 1)) {
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
                if (battleActive || player.isMoving) {
                    return;
                }
                int currentTileX = player.x / TILE_SIZE;
                int currentTileY = player.y / TILE_SIZE;
                if (canMoveTo(currentTileX - 1, currentTileY)) {
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
                if (battleActive || player.isMoving) {
                    return;
                }
                int currentTileX = player.x / TILE_SIZE;
                int currentTileY = player.y / TILE_SIZE;
                if (canMoveTo(currentTileX, currentTileY + 1)) {
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
                if (battleActive || player.isMoving) {
                    return;
                }
                int currentTileX = player.x / TILE_SIZE;
                int currentTileY = player.y / TILE_SIZE;
                if (canMoveTo(currentTileX + 1, currentTileY)) {
                    player.targetX += TILE_SIZE;
                    player.isMoving = true;
                    player.setDirection(140, 33, 48, 15, 32);
                    player.startAnimation();
                }
            }
        });
    }

    private void handleTileEvents() {
        if (battleActive) {
            return;
        }

        int tileX = player.x / TILE_SIZE;
        int tileY = player.y / TILE_SIZE;

        if (tileX == lastTileX && tileY == lastTileY) {
            return;
        }

        lastTileX = tileX;
        lastTileY = tileY;

        if (isBattleTile(tileX, tileY)) {
            // Trigger battle after the player moves onto the battle tile.
            triggerBattle(tileX, tileY);
        }
    }

    private boolean isBattleTile(int tileX, int tileY) {
        return tileX >= 0 && tileY >= 0 && tileY < map.length && tileX < map[0].length && map[tileY][tileX] == TILE_BATTLE;
    }

    private void triggerBattle(int tileX, int tileY) {
        // Enter battle state without changing the tile.
        battleActive = true;
        if (battlePanel != null) {
            battlePanel.startBattle(screenName, this);
        }
    }

    public void setBattleActive(boolean battleActive) {
        this.battleActive = battleActive;
    }

    public void returnToPreBattleTileAndResume() {
        battleActive = false;
    }

    private boolean canMoveTo(int tileX, int tileY) {
        // Return true for any tile that is not a wall.
        if (tileY < 0 || tileY >= map.length || tileX < 0 || tileX >= map[0].length) {
            return false;
        }
        return map[tileY][tileX] != TILE_WALL;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Loop through the 64x64 array
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[row].length; col++) {
                Color tileColor;
                if (map[row][col] == TILE_GRASS) {
                    tileColor = new Color(34, 139, 34);
                } else if (map[row][col] == TILE_BATTLE) {
                    tileColor = new Color(0, 100, 0);
                } else {
                    tileColor = new Color(128, 128, 128);
                }
                g2d.setColor(tileColor);

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
    }
}