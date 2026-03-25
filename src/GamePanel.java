import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class GamePanel extends JPanel {
    private int[][] map;
    private Player player;
    private final int TILE_SIZE = 32;

    public GamePanel(int[][] map) {
        this.map = map;
        this.player = new Player(5, 5, TILE_SIZE); // Start at tile (5,5)
        this.setBackground(Color.BLACK);

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
                    player.targetY -= TILE_SIZE;
                    player.isMoving = true;
                    player.setDirection(200, 34, 52);
                }
            }
        });
        im.put(KeyStroke.getKeyStroke("A"), "move_left");
        am.put("move_left", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!player.isMoving) {
                    player.targetX -= TILE_SIZE;
                    player.isMoving = true;
                    player.setDirection(76, 32, 48);
                }
            }
        });
        im.put(KeyStroke.getKeyStroke("S"), "move_down");
        am.put("move_down", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!player.isMoving) {
                    player.targetY += TILE_SIZE;
                    player.isMoving = true;
                    player.setDirection(10, 34, 50);
                }
            }
        });
        im.put(KeyStroke.getKeyStroke("D"), "move_right");
        am.put("move_right", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!player.isMoving) {
                    player.targetX += TILE_SIZE;
                    player.isMoving = true;
                    player.setDirection(140, 32, 48);
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

        // Draw Player
        player.draw(g);
    }
}