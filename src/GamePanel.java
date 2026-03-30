import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.*;

public class GamePanel extends JPanel {
    private int[][] map;
    private Player player;
    private final int TILE_SIZE = 32;
    private int lastTileRow = -1;
    private int lastTileCol = -1;
    private final Random random = new Random();
    // Added for screen switching and saving
    private CardLayout cardLayout;
    private JPanel container;

    // Modified Constructor to accept data from Main.java
    public GamePanel(CardLayout layout, JPanel container, Player player, int[][] map) {
        this.cardLayout = layout;
        this.container = container;
        this.player = player; // Use the player instance passed from Main.java
        this.map = map;

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
    }
}