import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
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
    private BufferedImage tallGrassSprite;
    private boolean inBattle = false;
    private boolean showingPartyMenu = false;
    private boolean showingBagMenu = false;

    // Trainer spotted overlay
    private boolean showSpottedMessage = false;

    // Party
    private List<Pokemon> party = new ArrayList<>();
    private int activePartyIndex = 0;

    // Item bag
    private ItemBag itemBag = ItemBag.starterBag();

    // Added for screen switching and saving
    private CardLayout cardLayout;
    private JPanel container;
    private Main mainApp;
    private String currentUsername; // Added to identify the user session
    private boolean tutorialMode = false;
    private int currentLevelId = 1;
    private boolean tutorialBattleExplained = false;
    private boolean levelEnded = false;
    private Point endTilePoint;

    private boolean isPaused = false;
    private int pauseOptionIndex = 0;
    private String[] pauseOptions = { "RESUME", "OPTIONS", "STATS", "QUIT TO MENU" };
    private boolean isStat = false;
    private TimeManager timeManager = new TimeManager(0);

    public GamePanel(CardLayout layout, JPanel container, Player player, int[][] map, Trainer trainer,
                     List<TerrainObjects> terrainObjects, String username, Main mainApp) {
        this(layout, container, player, map, trainer, terrainObjects, username, mainApp, false, 1);
    }

    public GamePanel(CardLayout layout, JPanel container, Player player, int[][] map, Trainer trainer,
                     List<TerrainObjects> terrainObjects, String username, Main mainApp, boolean tutorialMode) {
        this(layout, container, player, map, trainer, terrainObjects, username, mainApp, tutorialMode, 1);
    }

    public GamePanel(CardLayout layout, JPanel container, Player player, int[][] map, Trainer trainer,
                     List<TerrainObjects> terrainObjects, String username, Main mainApp, boolean tutorialMode,
                     int levelId) {
        this.cardLayout = layout;
        this.container = container;
        this.player = player;
        this.map = map;
        this.trainer = trainer;
        this.terrainObjects = terrainObjects;
        this.currentUsername = username;
        this.mainApp = mainApp;
        this.tutorialMode = tutorialMode;
        this.currentLevelId = Math.max(1, Math.min(3, levelId));
        this.player.setTerrainObjects(terrainObjects);
        this.endTilePoint = findEndTilePoint();

        try {
            tallGrassSprite = ImageIO.read(new File("tall_grass.png"));
        } catch (Exception e) {
            System.err.println("Warning: Could not load tall_grass.png");
        }

        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.setLayout(null);

        // Party button — always visible in top-right corner
        JButton partyBtn = new JButton("PARTY  [P]");
        partyBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        partyBtn.setBackground(new Color(30, 90, 50));
        partyBtn.setForeground(Color.WHITE);
        partyBtn.setFocusPainted(false);
        partyBtn.setBorderPainted(false);
        partyBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        partyBtn.setBounds(10, 10, 130, 34);
        partyBtn.addActionListener(e -> { togglePartyMenu(); this.requestFocusInWindow(); });
        this.add(partyBtn);

        JButton escBtn = new JButton("MENU  [ESC]");
        escBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        escBtn.setBackground(new Color(70, 50, 20));
        escBtn.setForeground(Color.WHITE);
        escBtn.setFocusPainted(false);
        escBtn.setBorderPainted(false);
        escBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        escBtn.setBounds(150, 10, 130, 34);
        escBtn.addActionListener(e -> { togglePause(); this.requestFocusInWindow(); });
        this.add(escBtn);

        JButton bagBtn = new JButton("BAG  [B]");
        bagBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        bagBtn.setBackground(new Color(80, 55, 120));
        bagBtn.setForeground(Color.WHITE);
        bagBtn.setFocusPainted(false);
        bagBtn.setBorderPainted(false);
        bagBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bagBtn.setBounds(290, 10, 130, 34);
        bagBtn.addActionListener(e -> { toggleBagMenu(); this.requestFocusInWindow(); });
        this.add(bagBtn);

        new InputHandler(this);
        timeManager.resume();

        this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                if (isPaused && !isStat) handleMouseHover(e.getX(), e.getY());
            }
        });

        this.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (isPaused) handlePauseSelection();
            }
        });

        ActionListener taskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (player != null) {
                    if (isPaused)
                        return;
                    player.update(); // Move the player
                    int playerCol = player.x / TILE_SIZE;
                    int playerRow = player.y / TILE_SIZE;
                    if (playerRow != lastTileRow || playerCol != lastTileCol) {
                        lastTileRow = playerRow;
                        lastTileCol = playerCol;
                        checkTrainerLoS(playerCol, playerRow);
                        if (!inBattle) {
                            maybeStartBattle();
                        }
                    }
                    checkLevelEnd();
                }
                repaint();
            }
        };

        Timer timer = new Timer(16, taskPerformer);
        timer.start();
    }

    // ── Party ───────────────────────────────────────────────────────────────

    public List<Pokemon> getParty() { return party; }

    public void setParty(List<Pokemon> party) {
        this.party = party;
        activePartyIndex = 0;
    }

    public int getActivePartyIndex() { return activePartyIndex; }

    public ItemBag getItemBag() { return itemBag; }

    public void setActivePartyIndex(int i) {
        if (i >= 0 && i < party.size()) activePartyIndex = i;
    }

    // ── Party menu ──────────────────────────────────────────────────────────

    public boolean isShowingPartyMenu() { return showingPartyMenu; }
    public boolean isShowingBagMenu() { return showingBagMenu; }

    public void togglePartyMenu() {
        if (!showingPartyMenu) showingBagMenu = false;
        showingPartyMenu = !showingPartyMenu;
        repaint();
    }

    public void toggleBagMenu() {
        if (!showingBagMenu) showingPartyMenu = false;
        showingBagMenu = !showingBagMenu;
        repaint();
    }

    // ── Trainer line-of-sight ────────────────────────────────────────────────

    private void checkTrainerLoS(int playerCol, int playerRow) {
        if (trainer == null || inBattle) return;
        if (!trainer.canSeePlayer(playerCol, playerRow)) return;
        if (party.isEmpty()) return;

        inBattle = true;
        launchBattle(Pokemon.random());
    }

    public void onTrainerDefeated() {
        if (trainer != null) trainer.setDefeated(true);
    }

    // ── Battle trigger ──────────────────────────────────────────────────────

    private void maybeStartBattle() {
        int col = player.x / TILE_SIZE;
        int row = player.y / TILE_SIZE;
        if (row < 0 || row >= map.length || col < 0 || col >= map[0].length) return;
        int tile = map[row][col];
        if (tile != 9 && tile != 10) return;
        if (tile == 9 && random.nextDouble() > 0.2) return;
        if (party.isEmpty()) return; // need a Pokemon to battle

        inBattle = true;
        launchBattle(Pokemon.random());
    }

    // ── Battle launcher (with tutorial popup) ──────────────────────────────

    private void launchBattle(Pokemon wild) {
        if (tutorialMode && !tutorialBattleExplained) {
            tutorialBattleExplained = true;

            JPanel overlay = new JPanel(new GridBagLayout());
            overlay.setBackground(new Color(0, 0, 0, 200));
            overlay.setOpaque(false);

            JPanel box = new JPanel();
            box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
            box.setBackground(new Color(10, 10, 10));
            box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 3),
                BorderFactory.createEmptyBorder(24, 32, 24, 32)
            ));

            JLabel title = new JLabel("\u2694  HOW TO BATTLE");
            title.setFont(new Font("SansSerif", Font.BOLD, 22));
            title.setForeground(Color.WHITE);
            title.setAlignmentX(Component.CENTER_ALIGNMENT);

            String[] infoLines = {
                "A wild Keymon appeared!",
                "Press ATTACK to start your turn.",
                "You have 10 seconds to type as many words as you can.",
                "Each correct word deals damage based on your Keymon's attack.",
                "The enemy will attack you back after your turn.",
                "Win by reducing the enemy HP to 0.",
                "You can also SWITCH your active Keymon or RUN away."
            };

            box.add(title);
            box.add(Box.createVerticalStrut(16));
            for (String text : infoLines) {
                JLabel l = new JLabel(text);
                l.setFont(new Font("SansSerif", Font.PLAIN, 14));
                l.setForeground(Color.WHITE);
                l.setAlignmentX(Component.CENTER_ALIGNMENT);
                box.add(l);
                box.add(Box.createVerticalStrut(6));
            }
            box.add(Box.createVerticalStrut(10));

            JButton acceptBtn = new JButton("GOT IT, START BATTLE");
            acceptBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
            acceptBtn.setBackground(new Color(60, 150, 60));
            acceptBtn.setForeground(Color.WHITE);
            acceptBtn.setFocusPainted(false);
            acceptBtn.setBorderPainted(false);
            acceptBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            acceptBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            acceptBtn.addActionListener(e -> {
                remove(overlay);
                revalidate();
                repaint();
                startBattlePanel(wild);
            });
            box.add(acceptBtn);

            overlay.add(box);
            setLayout(null);
            overlay.setBounds(0, 0, getWidth(), getHeight());
            add(overlay);
            setComponentZOrder(overlay, 0);
            revalidate();
            repaint();
        } else {
            startBattlePanel(wild);
        }
    }

    private void startBattlePanel(Pokemon wild) {
        BattlePanel battle = new BattlePanel(cardLayout, container, this, wild, party);
        container.add(battle, "BATTLE");
        cardLayout.show(container, "BATTLE");
        battle.requestFocusInWindow();
    }

    // ── State getters/setters ───────────────────────────────────────────────

    public boolean isPaused()      { return isPaused; }
    public boolean isInBattle()    { return inBattle; }
    public void setInBattle(boolean b) { inBattle = b; }
    public boolean isTutorialMode(){ return tutorialMode; }
    public boolean isStat()      { return isStat; }
    public Player getPlayer()    { return player; }
    public int getTileSize()     { return TILE_SIZE; }
    public String[] getPauseOptions()  { return pauseOptions; }
    public int getPauseOptionIndex()   { return pauseOptionIndex; }
    public TimeManager getTimeManager(){ return timeManager; }
    public boolean getIsStat()   { return isStat; }

    public void navigateMenuUp() {
        pauseOptionIndex = (pauseOptionIndex > 0) ? pauseOptionIndex - 1 : pauseOptions.length - 1;
        repaint();
    }

    public void navigateMenuDown() {
        pauseOptionIndex = (pauseOptionIndex < pauseOptions.length - 1) ? pauseOptionIndex + 1 : 0;
        repaint();
    }

    public void togglePause() {
        if (isStat) { isStat = false; repaint(); return; }
        isPaused = !isPaused;
        if (isPaused) timeManager.pause(); else timeManager.resume();
        pauseOptionIndex = 0;
        repaint();
    }

    public void handlePauseSelection() {
        if (isStat) { isStat = false; repaint(); return; }
        if (pauseOptionIndex == 0) {
            togglePause();
        } else if (pauseOptionIndex == 1) {
            System.out.println("Options...");
        } else if (pauseOptionIndex == 2) {
            isStat = true;
            repaint();
        } else if (pauseOptionIndex == 3) {
            if (!tutorialMode) {
                SaveManager.saveGame(currentUsername, currentLevelId, player.x, player.y,
                        player.getSpriteRow(), 0, currentLevelId, party);
            }

            isPaused = false;
            isStat = false;
            timeManager.pause();

            if (tutorialMode) {
                mainApp.returnToMenu();
            } else {
                mainApp.showPlayerScreen(currentUsername);
            }
        }
    }

    private void handleMouseHover(int mouseX, int mouseY) {
        int menuX = getWidth() / 2 - 100;
        int menuWidth = 200;
        int startY = 220;
        int optionHeight = 50;
        if (mouseX >= menuX && mouseX <= menuX + menuWidth) {
            for (int i = 0; i < pauseOptions.length; i++) {
                int top = startY + (i * optionHeight);
                int bottom = top + optionHeight;
                if (mouseY >= top && mouseY <= bottom) {
                    if (pauseOptionIndex != i) { pauseOptionIndex = i; repaint(); }
                    return;
                }
            }
        }
    }

    private void checkLevelEnd() {
        if (levelEnded || player.isMoving)
            return;

        int col = player.x / TILE_SIZE;
        int row = player.y / TILE_SIZE;
        if (row < 0 || row >= map.length || col < 0 || col >= map[row].length)
            return;

        if (map[row][col] == 5) {
            levelEnded = true;
            timeManager.pause();
            JOptionPane.showMessageDialog(this, "Level complete!", "Success", JOptionPane.INFORMATION_MESSAGE);
            if (tutorialMode) {
                cardLayout.show(container, "TUTORIAL");
            } else {
                SaveManager.saveGame(currentUsername, currentLevelId, player.x, player.y,
                        player.getSpriteRow(), 0, currentLevelId, party);
                SaveManager.markLevelCompleted(currentUsername, currentLevelId);
                mainApp.showPlayerScreen(currentUsername);
            }
        }
    }

    private void drawTutorialBubbles(Graphics2D g2d) {
        if (trainer != null) {
            int bubbleX = trainer.getX() + 50;
            int bubbleY = trainer.getY() - 60;
            drawBubble(g2d, bubbleX, bubbleY, "This is a trainer. Walking in front of him will start a battle.");
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            int arrowX = trainer.getX() + 16;
            int arrowY = trainer.getY() - 8;
            g2d.drawLine(bubbleX + 20, bubbleY + 45, arrowX, arrowY);
            g2d.drawLine(arrowX, arrowY, arrowX - 6, arrowY - 6);
            g2d.drawLine(arrowX, arrowY, arrowX + 6, arrowY - 6);
        }

        if (endTilePoint != null) {
            int bubbleX = endTilePoint.x + 60;
            int bubbleY = endTilePoint.y - 40;
            drawBubble(g2d, bubbleX, bubbleY, "This is an end tile. It ends the level.");
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            int arrowX = endTilePoint.x + TILE_SIZE / 2;
            int arrowY = endTilePoint.y + TILE_SIZE / 2;
            g2d.drawLine(bubbleX + 20, bubbleY + 35, arrowX, arrowY);
            g2d.drawLine(arrowX, arrowY, arrowX - 6, arrowY - 6);
            g2d.drawLine(arrowX, arrowY, arrowX + 6, arrowY - 6);
        }
    }

    private void drawBubble(Graphics2D g2d, int x, int y, String text) {
        int width = 300;
        int height = 60;
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRoundRect(x, y, width, height, 16, 16);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x, y, width, height, 16, 16);

        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        drawStringWrapped(g2d, text, x + 12, y + 18, width - 24, 16);
    }

    private void drawStringWrapped(Graphics2D g2d, String text, int x, int y, int maxWidth, int lineHeight) {
        FontMetrics fm = g2d.getFontMetrics();
        String[] words = text.split(" ");
        String line = "";
        for (String word : words) {
            String test = line.isEmpty() ? word : line + " " + word;
            if (fm.stringWidth(test) > maxWidth) {
                g2d.drawString(line, x, y);
                line = word;
                y += lineHeight;
            } else {
                line = test;
            }
        }
        if (!line.isEmpty()) {
            g2d.drawString(line, x, y);
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

    private void drawMap(Graphics2D g2d) {
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[row].length; col++) {
                int x = col * TILE_SIZE;
                int y = row * TILE_SIZE;
                int tile = map[row][col];

                g2d.setColor(new Color(60, 170, 60));
                g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);

                if (tile == 5) {
                    g2d.setColor(Color.BLACK);
                    g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                }

                if (tile == 9 && tallGrassSprite != null) {
                    g2d.drawImage(tallGrassSprite, x, y, TILE_SIZE, TILE_SIZE, null);
                }

                if (tile == 10) {
                    g2d.setColor(new Color(200, 35, 35));
                    g2d.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                }

                g2d.setColor(new Color(0, 100, 0));
                g2d.drawRect(x, y, TILE_SIZE, TILE_SIZE);
            }
        }

        // Draw terrain objects and trainer
        if (terrainObjects != null)
            for (TerrainObjects obj : terrainObjects) obj.draw(g2d);
        if (trainer != null)
            trainer.draw(g2d);

        player.draw(g2d);
        uiManager.draw(g2d);

        // Draw party overlay if open
        if (showingPartyMenu) drawPartyOverlay(g2d);
        if (showingBagMenu) drawBagOverlay(g2d);

        // Draw trainer spotted overlay
        if (showSpottedMessage) drawSpottedOverlay(g2d);
    }

    private void drawSpottedOverlay(Graphics2D g2) {
        int w = getWidth(), h = getHeight();
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, w, h);

        // Exclamation box
        int bw = 340, bh = 80;
        int bx = (w - bw) / 2, by = h / 2 - 60;
        g2.setColor(new Color(220, 60, 60));
        g2.fillRoundRect(bx, by, bw, bh, 16, 16);
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(bx, by, bw, bh, 16, 16);

        g2.setFont(new Font("SansSerif", Font.BOLD, 28));
        FontMetrics fm = g2.getFontMetrics();
        String line1 = "! A Trainer spotted you !";
        g2.drawString(line1, bx + (bw - fm.stringWidth(line1)) / 2, by + 50);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 15));
        fm = g2.getFontMetrics();
        String line2 = "Prepare for battle!";
        g2.setColor(new Color(255, 220, 220));
        g2.drawString(line2, bx + (bw - fm.stringWidth(line2)) / 2, by + bh + 26);
    }

    private void drawPartyOverlay(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();

        // Semi-transparent backdrop
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, w, h);

        // Panel
        int panelW = 530, panelH = 360;
        int panelX = (w - panelW) / 2;
        int panelY = (h - panelH) / 2;

        g2.setColor(new Color(15, 50, 35, 240));
        g2.fillRoundRect(panelX, panelY, panelW, panelH, 20, 20);
        g2.setColor(new Color(100, 200, 130));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(panelX, panelY, panelW, panelH, 20, 20);

        // Title
        g2.setFont(new Font("SansSerif", Font.BOLD, 20));
        g2.setColor(Color.WHITE);
        g2.drawString("YOUR PARTY  (P to close)", panelX + 20, panelY + 30);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(new Color(180, 220, 180));
        g2.drawString("Active: " + (party.isEmpty() ? "—" : party.get(activePartyIndex).name), panelX + 20, panelY + 50);

        // Single column, 3 slots
        int slotW = 480, slotH = 90;
        int startX = panelX + 20;
        int startY = panelY + 65;
        int gapY = 10;

        for (int i = 0; i < 3; i++) {
            int col = 0;
            int row = i;
            int sx = startX + col * slotW;
            int sy = startY + row * (slotH + gapY);

            if (i < party.size()) {
                Pokemon p = party.get(i);
                boolean isActive = (i == activePartyIndex);

                // Slot background
                g2.setColor(isActive ? new Color(40, 100, 60) : new Color(20, 60, 40));
                g2.fillRoundRect(sx, sy, slotW, slotH, 10, 10);
                g2.setColor(isActive ? new Color(100, 220, 140) : new Color(60, 120, 80));
                g2.setStroke(new BasicStroke(isActive ? 2f : 1f));
                g2.drawRoundRect(sx, sy, slotW, slotH, 10, 10);

                // Sprite
                Image spr = p.getSprite();
                if (spr != null) g2.drawImage(spr, sx + 4, sy + 4, 72, 72, null);

                // Name
                g2.setFont(new Font("SansSerif", Font.BOLD, 14));
                g2.setColor(p.isFainted() ? new Color(200, 80, 80) : Color.WHITE);
                g2.drawString(p.name + (p.isFainted() ? "  [FAINTED]" : ""), sx + 82, sy + 20);

                // HP bar
                int barX = sx + 82, barY = sy + 30, barW = 148, barH = 10;
                g2.setColor(new Color(10, 30, 20));
                g2.fillRoundRect(barX, barY, barW, barH, 4, 4);
                float ratio = (float) p.currentHp / p.maxHp;
                g2.setColor(ratio > 0.5f ? new Color(60, 210, 100) : ratio > 0.25f ? new Color(230, 200, 50) : new Color(220, 70, 70));
                g2.fillRoundRect(barX, barY, (int)(barW * Math.max(0, ratio)), barH, 4, 4);

                // HP numbers
                g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
                g2.setColor(Color.WHITE);
                g2.drawString(p.currentHp + " / " + p.maxHp, barX, sy + 56);
                g2.drawString("ATK " + p.attack, barX + 90, sy + 56);
            } else {
                // Empty slot
                g2.setColor(new Color(15, 40, 28));
                g2.fillRoundRect(sx, sy, slotW, slotH, 10, 10);
                g2.setColor(new Color(40, 70, 50));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(sx, sy, slotW, slotH, 10, 10);
                g2.setFont(new Font("SansSerif", Font.ITALIC, 13));
                g2.setColor(new Color(80, 120, 90));
                g2.drawString("Empty", sx + 100, sy + 55);
            }
        }
    }

    private void drawBagOverlay(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, w, h);

        int panelW = 560, panelH = 340;
        int panelX = (w - panelW) / 2;
        int panelY = (h - panelH) / 2;

        g2.setColor(new Color(30, 20, 55, 240));
        g2.fillRoundRect(panelX, panelY, panelW, panelH, 20, 20);
        g2.setColor(new Color(170, 140, 220));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(panelX, panelY, panelW, panelH, 20, 20);

        g2.setFont(new Font("SansSerif", Font.BOLD, 20));
        g2.setColor(Color.WHITE);
        g2.drawString("ITEM BAG  (B to close)", panelX + 20, panelY + 32);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(new Color(215, 205, 235));
        g2.drawString("Items are used during battles. This screen shows what you have available.", panelX + 20, panelY + 54);

        int cardX = panelX + 20;
        int cardY = panelY + 76;
        int cardW = panelW - 40;
        int cardH = 52;
        int gap = 12;

        Item[] items = Item.values();
        for (int i = 0; i < items.length; i++) {
            Item item = items[i];
            int y = cardY + i * (cardH + gap);
            int count = itemBag.getCount(item);

            g2.setColor(item.color.darker());
            g2.fillRoundRect(cardX, y, cardW, cardH, 14, 14);
            g2.setColor(new Color(255, 255, 255, 90));
            g2.drawRoundRect(cardX, y, cardW, cardH, 14, 14);

            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.setColor(Color.WHITE);
            g2.drawString(item.displayName + "  x" + count, cardX + 16, y + 22);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g2.setColor(new Color(240, 240, 240));
            g2.drawString(item.description, cardX + 16, y + 40);
        }
    }
}
