import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class GamePanel extends JPanel {
    private int[][] map;
    private Player player;
    private final int TILE_SIZE = 32;
    // Added for screen switching and saving
    private CardLayout cardLayout;
    private JPanel container;

    private boolean isPaused = false;
    private int pauseOptionIndex = 0;
    private String[] pauseOptions = { "RESUME", "OPTIONS", "STATS", "QUIT TO MENU" };

    private boolean isStat = false;

    private TimeManager timeManager = new TimeManager(0);

    // Modified Constructor to accept data from Main.java
    public GamePanel(CardLayout layout, JPanel container, Player player, int[][] map) {
        this.cardLayout = layout;
        this.container = container;
        this.player = player; // Use the player instance passed from Main.java
        this.map = map;

        this.setBackground(Color.BLACK);
        this.setFocusable(true);

        setupInputs();
        timeManager.resume();

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

    private void setupInputs() {
        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        // ESCAPE KEY - Toggles Pause
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "toggle_pause");
        am.put("toggle_pause", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                togglePause();
            }
        });

        // UP (W)
        im.put(KeyStroke.getKeyStroke("W"), "move_up");
        am.put("move_up", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (isPaused) {
                    // Menu Navigation
                    pauseOptionIndex = (pauseOptionIndex > 0) ? pauseOptionIndex - 1 : pauseOptions.length - 1;
                    repaint();
                } else if (!player.isMoving) {
                    // Player Movement
                    player.targetY -= TILE_SIZE;
                    player.isMoving = true;
                    player.setDirection(200, 35, 52, 14, 29);
                    player.startAnimation();
                }
            }
        });

        // DOWN (S)
        im.put(KeyStroke.getKeyStroke("S"), "move_down");
        am.put("move_down", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (isPaused) {
                    // Menu Navigation
                    pauseOptionIndex = (pauseOptionIndex < pauseOptions.length - 1) ? pauseOptionIndex + 1 : 0;
                    repaint();
                } else if (!player.isMoving) {
                    // Player Movement
                    player.targetY += TILE_SIZE;
                    player.isMoving = true;
                    player.setDirection(10, 35, 50, 14, 29);
                    player.startAnimation();
                }
            }
        });

        // LEFT (A) - Add pause check to prevent moving while paused
        im.put(KeyStroke.getKeyStroke("A"), "move_left");
        am.put("move_left", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!isPaused && !player.isMoving) {
                    player.targetX -= TILE_SIZE;
                    player.isMoving = true;
                    player.setDirection(76, 33, 48, 15, 32);
                    player.startAnimation();
                }
            }
        });

        // RIGHT (D) - Add pause check to prevent moving while paused
        im.put(KeyStroke.getKeyStroke("D"), "move_right");
        am.put("move_right", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!isPaused && !player.isMoving) {
                    player.targetX += TILE_SIZE;
                    player.isMoving = true;
                    player.setDirection(140, 33, 48, 15, 32);
                    player.startAnimation();
                }
            }
        });

        // ENTER (Select Pause Option)
        im.put(KeyStroke.getKeyStroke("ENTER"), "pause_select");
        am.put("pause_select", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (isPaused) {
                    handlePauseSelection();
                }
            }
        });
    }

    public void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            timeManager.pause(); // Stop clock while paused
        } else {
            timeManager.resume(); // Start clock when resuming
        }
        pauseOptionIndex = 0;
        repaint();
    }

    private void handlePauseSelection() {
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
        } else if (pauseOptionIndex == 3)
            cardLayout.show(container, "MENU"); // Quit
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
    }

    private void drawPaused(Graphics2D g2d) {

        g2d.setColor(new Color(0, 0, 0, 220));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        if (isStat) {
            drawPlayerStat(g2d); // Draw the cool Stats UI
        } else {
            // Draw Main Pause Menu
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 50));
            g2d.drawString("PAUSED", getWidth() / 2 - 100, 150);

            g2d.setFont(new Font("Arial", Font.PLAIN, 30));
            for (int i = 0; i < pauseOptions.length; i++) {
                if (i == pauseOptionIndex) {
                    g2d.setColor(Color.YELLOW);
                    g2d.drawString("> " + pauseOptions[i], getWidth() / 2 - 100, 250 + (i * 50));
                } else {
                    g2d.setColor(Color.WHITE);
                    g2d.drawString("  " + pauseOptions[i], getWidth() / 2 - 100, 250 + (i * 50));
                }
            }
        }
    }

    private void drawPlayerStat(Graphics2D g2d) {
        int x = 200;
        int y = 200;
        int w = getWidth() / 3;
        int h = getHeight() / 3;

        // 1. Draw player box
        g2d.setColor(new Color(45, 52, 71)); // Dark blue-grey
        g2d.fillRoundRect(x, y, w, h, 20, 20);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(x, y, w, h, 20, 20);

        // 2. Draw Header
        g2d.setFont(new Font("Monospaced", Font.BOLD, 35));
        g2d.drawString("PLAYER STATS:", x + 30, y + 50);
        g2d.drawLine(x + 30, y + 60, x + 350, y + 60);

        // 3. Stats Data
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 25));
        g2d.drawString("NAME: ASH", x + 30, y + 110);

        // HP Bar
        g2d.drawString("HP: ", x + 30, y + 160);
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(x + 100, y + 140, 200, 25);
        g2d.setColor(new Color(114, 186, 158)); // Greenish
        g2d.fillRect(x + 100, y + 140, 200, 25); // Set width to (currentHP/maxHP * 200)

        // MP Bar
        g2d.setColor(Color.WHITE);
        g2d.drawString("MP: ", x + 30, y + 210);
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(x + 100, y + 190, 200, 25);
        g2d.setColor(new Color(103, 146, 219)); // Blue
        g2d.fillRect(x + 100, y + 190, 100, 25); // Example 50% fill

        // -- Top right -- //
        int rx = x + 700; // Horizontal offset for right column
        int rw = 400;
        int rh = 200;

        g2d.setColor(new Color(45, 52, 71, 230));
        g2d.fillRoundRect(rx, y, rw, rh, 20, 20);
        g2d.setColor(Color.WHITE);
        g2d.drawRoundRect(rx, y, rw, rh, 20, 20);

        g2d.setFont(new Font("Monospaced", Font.BOLD, 28));
        g2d.drawString("GAME PROGRESS", rx + 30, y + 45);
        g2d.drawLine(rx + 30, y + 55, rx + 250, y + 55);

        g2d.setFont(new Font("Monospaced", Font.PLAIN, 20));
        g2d.drawString("🕒 WPM:         75 WPM", rx + 30, y + 100);
        g2d.drawString("🕒 TIME PLAYED: " + timeManager.getFormattedTime(), rx + 30, y + 150);

        // -- Bottom Right -- //
        int ry_bottom = y + 250;
        int rh_bottom = 250;

        g2d.setColor(new Color(45, 52, 71, 230));
        g2d.fillRoundRect(rx, ry_bottom, rw, rh_bottom, 20, 20);
        g2d.setColor(Color.WHITE);
        g2d.drawRoundRect(rx, ry_bottom, rw, rh_bottom, 20, 20);

        // Trainers Content
        g2d.setFont(new Font("Monospaced", Font.BOLD, 28));
        g2d.drawString("TRAINERS BEAT:", rx + 30, ry_bottom + 45);

        // Big Centered Counter
        g2d.setFont(new Font("Monospaced", Font.BOLD, 50));
        g2d.drawString("32 / 50", rx + 80, ry_bottom + 130);
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 20));
        g2d.drawString("TOTAL", rx + 160, ry_bottom + 160);

        // -- Press Enter to go back label -- //
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.ITALIC, 20));
        g2d.drawString("PRESS ENTER TO GO BACK", getWidth() / 2 - 150, getHeight() - 50);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        drawMap(g2d);
        player.draw(g);

        if (isPaused) {
            drawPaused(g2d);
        }
    }

}