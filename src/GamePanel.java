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
    // Added for screen switching and saving
    private CardLayout cardLayout;
    private JPanel container;

    // Modified Constructor to accept data from Main.java (including static trainer
    // and terrain objects)
    public GamePanel(CardLayout layout, JPanel container, Player player, int[][] map, Trainer trainer,
            List<TerrainObjects> terrainObjects) {
        this.cardLayout = layout;
        this.container = container;
        this.player = player; // Use the player instance passed from Main.java
        this.map = map;
        this.trainer = trainer;
        this.terrainObjects = terrainObjects;
        this.player.setTerrainObjects(terrainObjects);

        this.setBackground(Color.BLACK);
        this.setFocusable(true);

        // Add a button to save and go back to menu
        JButton backButton = new JButton("Save & Back");
        backButton.addActionListener(e -> {
            // Save current state before switching screens
            SaveManager.saveGame(player.x, player.y, player.getSpriteRow());
            cardLayout.show(container, "MENU");
        });
        this.add(backButton);

        setupInputs();

        // Inside GamePanel constructor
        ActionListener taskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (player != null) {
                    player.update(); // Move the player
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
                if (!player.isMoving) {
                    int newY = player.targetY - TILE_SIZE;
                    if (player.isPositionWalkable(player.targetX, newY)) {
                        player.targetY = newY;
                        player.isMoving = true;
                        player.setDirection(200, 35, 52, 14, 29);
                        player.startAnimation();
                    }
                }
            }
        });
        im.put(KeyStroke.getKeyStroke("A"), "move_left");
        am.put("move_left", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!player.isMoving) {
                    int newX = player.targetX - TILE_SIZE;
                    if (player.isPositionWalkable(newX, player.targetY)) {
                        player.targetX = newX;
                        player.isMoving = true;
                        player.setDirection(76, 33, 48, 15, 32);
                        player.startAnimation();
                    }
                }
            }
        });
        im.put(KeyStroke.getKeyStroke("S"), "move_down");
        am.put("move_down", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!player.isMoving) {
                    int newY = player.targetY + TILE_SIZE;
                    if (player.isPositionWalkable(player.targetX, newY)) {
                        player.targetY = newY;
                        player.isMoving = true;
                        player.setDirection(10, 35, 50, 14, 29);
                        player.startAnimation();
                    }
                }
            }
        });
        im.put(KeyStroke.getKeyStroke("D"), "move_right");
        am.put("move_right", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!player.isMoving) {
                    int newX = player.targetX + TILE_SIZE;
                    if (player.isPositionWalkable(newX, player.targetY)) {
                        player.targetX = newX;
                        player.isMoving = true;
                        player.setDirection(140, 33, 48, 15, 32);
                        player.startAnimation();
                    }
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Loop through the 64x64 array
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[row].length; col++) {

                // For now, 0 = Green (Grass)
                if (map[row][col] == 0) {
                    g2d.setColor(new Color(34, 139, 34));
                }

                int x = col * TILE_SIZE;
                int y = row * TILE_SIZE;

                g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);

                // Draw grid lines so you can see the tiles
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

        // Draw Player
        player.draw(g);
    }
}