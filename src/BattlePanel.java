import java.awt.*;
import java.util.List;

import javax.swing.*;

public class BattlePanel extends JPanel {
    private CardLayout cardLayout;
    private JPanel container;
    private GamePanel gamePanel;

    private Pokemon wildPokemon;
    private List<Pokemon> party;

    // Trainer battle support
    private boolean isTrainerBattle = false;
    private List<Pokemon> trainerTeam;
    private int trainerIndex = 0;

    private enum Phase { MENU, COUNTDOWN, PLAYER_TURN, ENEMY_TURN, SWITCH_SELECT, FINISHED }
    private Phase phase = Phase.MENU;
    private boolean forcedSwitch = false;

    /** Call after construction to configure a trainer battle. */
    public void setTrainerBattle(List<Pokemon> trainerTeam) {
        this.isTrainerBattle = true;
        this.trainerTeam = trainerTeam;
        this.trainerIndex = 0;
        // wildPokemon is already trainerTeam.get(0) from constructor
    }

    private String[] wordBank = {
        "keyboard", "typing", "monster", "learning", "practice",
        "accuracy", "student", "teacher", "dragon", "forest",
        "challenge", "victory", "battle", "damage", "critical"
    };
    private String currentWord;
    private int correctWordsThisTurn = 0;
    private int secondsLeft = 10;
    private int preCountdown = 3;
    private Timer turnTimer;
    private Timer preTimer;

    // Swing components
    private JTextField inputField;
    private JLabel messageLabel;
    private JLabel timerLabel;
    private JLabel feedbackLabel;
    private JButton attackButton;
    private JButton switchButton;
    private JButton runButton;

    // Switch overlay buttons (up to 6 party slots)
    private JPanel switchOverlay;
    private JButton[] switchSlots = new JButton[6];

    public BattlePanel(CardLayout layout, JPanel container, GamePanel gamePanel,
                       Pokemon wildPokemon, List<Pokemon> party) {
        this.cardLayout   = layout;
        this.container    = container;
        this.gamePanel    = gamePanel;
        this.wildPokemon  = wildPokemon;
        this.party        = party;

        setLayout(null);
        setBackground(new Color(30, 100, 70));

        setupUI();
        showMenu();
    }

    // ── UI SETUP ─────────────────────────────────────────────────────────────

    private void setupUI() {
        // Text components – positioned dynamically in paintComponent
        messageLabel = label("A wild " + wildPokemon.name + " appeared!", Font.BOLD, 17);
        add(messageLabel);

        timerLabel = label(" ", Font.BOLD, 17);
        add(timerLabel);

        inputField = new JTextField();
        inputField.setFont(new Font("Monospaced", Font.PLAIN, 17));
        inputField.setVisible(false);
        add(inputField);

        feedbackLabel = label(" ", Font.PLAIN, 15);
        add(feedbackLabel);

        // Action buttons
        attackButton = battleBtn("⚔  ATTACK", new Color(60, 150, 220));
        attackButton.addActionListener(e -> startPlayerTurn());
        add(attackButton);

        switchButton = battleBtn("↔  SWITCH", new Color(80, 130, 80));
        switchButton.addActionListener(e -> openSwitchMenu(false));
        add(switchButton);

        runButton = battleBtn("🏃  RUN", new Color(180, 70, 60));
        runButton.addActionListener(e -> endBattle(false, true));
        add(runButton);

        inputField.addActionListener(e -> handlePlayerInput());

        // Switch overlay
        switchOverlay = new JPanel(null);
        switchOverlay.setBackground(new Color(10, 40, 25, 230));
        switchOverlay.setVisible(false);
        add(switchOverlay);

        JLabel switchTitle = new JLabel("Choose a Keymon:");
        switchTitle.setForeground(Color.WHITE);
        switchTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        switchTitle.setBounds(10, 8, 260, 24);
        switchOverlay.add(switchTitle);

        for (int i = 0; i < 6; i++) {
            final int idx = i;
            switchSlots[i] = new JButton();
            switchSlots[i].setFont(new Font("SansSerif", Font.BOLD, 13));
            switchSlots[i].setForeground(Color.WHITE);
            switchSlots[i].setFocusPainted(false);
            switchSlots[i].setBorderPainted(false);
            switchSlots[i].addActionListener(e -> selectSwitch(idx));
            switchOverlay.add(switchSlots[i]);
        }
    }

    private JLabel label(String text, int style, int size) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("SansSerif", style, size));
        return l;
    }

    private JButton battleBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ── PHASE CONTROL ─────────────────────────────────────────────────────────

    private void showMenu() {
        phase = Phase.MENU;
        String activeName = party.isEmpty() ? "?" : party.get(gamePanel.getActivePartyIndex()).name;
        messageLabel.setText(activeName + "  vs  " + wildPokemon.name + "  —  What will you do?");
        timerLabel.setText(" ");
        feedbackLabel.setForeground(Color.WHITE);
        feedbackLabel.setText(" ");
        inputField.setVisible(false);
        switchOverlay.setVisible(false);
        attackButton.setVisible(true);
        switchButton.setVisible(hasSwitchOption());
        runButton.setVisible(true);
    }

    private boolean hasSwitchOption() {
        for (int i = 0; i < party.size(); i++)
            if (i != gamePanel.getActivePartyIndex() && !party.get(i).isFainted()) return true;
        return false;
    }

    // ── ATTACK (typing) ───────────────────────────────────────────────────────

    private void startPlayerTurn() {
        phase = Phase.COUNTDOWN;
        correctWordsThisTurn = 0;
        secondsLeft = 10;
        preCountdown = 3;
        hideActionButtons();
        inputField.setEnabled(false);
        inputField.setVisible(false);
        feedbackLabel.setText(" ");
        messageLabel.setText("Get ready…");
        timerLabel.setText("3");

        if (preTimer != null) preTimer.stop();
        preTimer = new Timer(1000, e -> {
            preCountdown--;
            if (preCountdown > 0) {
                timerLabel.setText(String.valueOf(preCountdown));
            } else {
                preTimer.stop();
                beginTypingPhase();
            }
        });
        preTimer.start();
    }

    private void beginTypingPhase() {
        phase = Phase.PLAYER_TURN;
        inputField.setVisible(true);
        inputField.setEnabled(true);
        inputField.setText("");
        SwingUtilities.invokeLater(() -> inputField.requestFocusInWindow());
        pickNextWord();

        if (turnTimer != null) turnTimer.stop();
        turnTimer = new Timer(1000, e -> {
            secondsLeft--;
            timerLabel.setText(secondsLeft + "s");
            if (secondsLeft <= 0) { turnTimer.stop(); finishPlayerTurn(); }
        });
        turnTimer.start();
    }

    private void pickNextWord() {
        currentWord = wordBank[(int) (Math.random() * wordBank.length)];
        messageLabel.setText("Type:  " + currentWord);
        timerLabel.setText(secondsLeft + "s");
    }

    private void handlePlayerInput() {
        if (phase != Phase.PLAYER_TURN) return;
        String typed = inputField.getText().trim();
        if (typed.equalsIgnoreCase(currentWord)) {
            correctWordsThisTurn++;
            feedbackLabel.setForeground(new Color(100, 255, 140));
            feedbackLabel.setText("Correct!  +" + correctWordsThisTurn + " hit(s) this turn");
        } else {
            feedbackLabel.setForeground(new Color(255, 120, 100));
            feedbackLabel.setText("Miss!  The word was:  " + currentWord);
        }
        inputField.setText("");
        pickNextWord();
    }

    private void finishPlayerTurn() {
        inputField.setVisible(false);
        Pokemon active = party.get(gamePanel.getActivePartyIndex());
        int damage = correctWordsThisTurn * active.attack;
        wildPokemon.currentHp = Math.max(0, wildPokemon.currentHp - damage);
        feedbackLabel.setForeground(Color.WHITE);
        feedbackLabel.setText(damage > 0 ? "You hit " + wildPokemon.name + " for " + damage + " damage!" : "You missed every word…");
        timerLabel.setText(" ");
        repaint();

        if (wildPokemon.isFainted()) {
            if (isTrainerBattle) {
                trainerIndex++;
                if (trainerIndex < trainerTeam.size()) {
                    wildPokemon = trainerTeam.get(trainerIndex);
                    messageLabel.setText("Trainer sent out " + wildPokemon.name + "!");
                    feedbackLabel.setForeground(new Color(255, 220, 100));
                    feedbackLabel.setText("The trainer has " + (trainerTeam.size() - trainerIndex) + " Keymon left!");
                    repaint();
                    new Timer(1400, e -> { ((Timer) e.getSource()).stop(); showMenu(); }).start();
                } else {
                    gamePanel.onTrainerDefeated();
                    endBattle(true, false);
                }
            } else {
                endBattle(true, false);
            }
            return;
        }
        startEnemyTurn();
    }

    // ── SWITCH ────────────────────────────────────────────────────────────────

    private void openSwitchMenu(boolean forced) {
        this.forcedSwitch = forced;
        phase = Phase.SWITCH_SELECT;
        hideActionButtons();
        inputField.setVisible(false);

        // Populate slots
        for (int i = 0; i < 6; i++) {
            if (i < party.size()) {
                Pokemon p = party.get(i);
                boolean isActive = (i == gamePanel.getActivePartyIndex());
                boolean fainted = p.isFainted();
                switchSlots[i].setText(p.name + "  " + p.currentHp + "/" + p.maxHp
                        + (isActive ? "  ◄" : "") + (fainted ? "  [X]" : ""));
                switchSlots[i].setEnabled(!isActive && !fainted);
                switchSlots[i].setBackground(fainted ? new Color(80, 40, 40) : isActive ? new Color(40, 80, 60) : new Color(40, 100, 70));
                switchSlots[i].setVisible(true);
            } else {
                switchSlots[i].setVisible(false);
            }
        }

        // Resize and reposition overlay (done in paintComponent, but set visible here)
        switchOverlay.setVisible(true);
        repaint();
    }

    private void selectSwitch(int idx) {
        if (idx >= party.size() || party.get(idx).isFainted()) return;
        if (idx == gamePanel.getActivePartyIndex()) return;

        gamePanel.setActivePartyIndex(idx);
        switchOverlay.setVisible(false);
        messageLabel.setText("Go, " + party.get(idx).name + "!");
        feedbackLabel.setText(" ");

        if (forcedSwitch) {
            // No enemy turn penalty for forced switch
            new Timer(900, e -> { ((Timer) e.getSource()).stop(); showMenu(); }).start();
        } else {
            // Voluntary switch costs the player's turn → enemy attacks
            new Timer(600, e -> { ((Timer) e.getSource()).stop(); startEnemyTurn(); }).start();
        }
    }

    // ── ENEMY TURN ────────────────────────────────────────────────────────────

    private void startEnemyTurn() {
        phase = Phase.ENEMY_TURN;
        messageLabel.setText(wildPokemon.name + " attacks!");
        feedbackLabel.setText(" ");
        timerLabel.setText(" ");

        new Timer(1100, e -> {
            ((Timer) e.getSource()).stop();
            Pokemon active = party.get(gamePanel.getActivePartyIndex());
            int damage = wildPokemon.attack;
            active.currentHp = Math.max(0, active.currentHp - damage);
            feedbackLabel.setForeground(new Color(255, 120, 100));
            feedbackLabel.setText(wildPokemon.name + " hit " + active.name + " for " + damage + " damage!");
            repaint();

            if (active.isFainted()) {
                messageLabel.setText(active.name + " fainted!");
                // Check if any party member remains
                boolean anyAlive = false;
                for (Pokemon p : party) if (!p.isFainted()) { anyAlive = true; break; }

                if (!anyAlive) {
                    new Timer(1200, ev -> { ((Timer) ev.getSource()).stop(); endBattle(false, false); }).start();
                } else {
                    new Timer(1200, ev -> { ((Timer) ev.getSource()).stop(); openSwitchMenu(true); }).start();
                }
            } else {
                new Timer(800, ev -> { ((Timer) ev.getSource()).stop(); showMenu(); }).start();
            }
        }).start();
    }

    // ── END BATTLE ────────────────────────────────────────────────────────────

    private void endBattle(boolean won, boolean ran) {
        phase = Phase.FINISHED;
        if (turnTimer != null) turnTimer.stop();
        if (preTimer  != null) preTimer.stop();
        hideActionButtons();
        inputField.setVisible(false);
        switchOverlay.setVisible(false);
        timerLabel.setText(" ");
        feedbackLabel.setForeground(Color.WHITE);
        feedbackLabel.setText(" ");

        if (ran)       messageLabel.setText("You got away safely!");
        else if (won)  messageLabel.setText("You won the battle!");
        else           messageLabel.setText("All your Keymon fainted…");

        new Timer(1600, e -> {
            ((Timer) e.getSource()).stop();
            gamePanel.setInBattle(false);
            cardLayout.show(container, "PLAY GAME");
            gamePanel.requestFocusInWindow();
        }).start();
    }

    private void hideActionButtons() {
        attackButton.setVisible(false);
        switchButton.setVisible(false);
        runButton.setVisible(false);
    }

    // ── PAINTING ──────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // Background
        g2.setColor(new Color(30, 100, 70));
        g2.fillRect(0, 0, w, h);

        // Platforms
        g2.setColor(new Color(55, 140, 85));
        g2.fillOval(w - 280, 150, 220, 65);   // enemy
        g2.fillOval(30, h - 235, 240, 72);     // player

        // Enemy sprite (top-right)
        Image enemySpr = wildPokemon.getSprite();
        if (enemySpr != null) g2.drawImage(enemySpr, w - 260, 50, 200, 200, null);

        // Player active Pokemon sprite (bottom-left)
        if (!party.isEmpty()) {
            Pokemon active = party.get(gamePanel.getActivePartyIndex());
            Image playerSpr = active.getSprite();
            if (playerSpr != null) g2.drawImage(playerSpr, 60, h - 310, 150, 150, null);
        }

        // Player party circles (3 circles bottom-right of player HP card area)
        drawPartyCircles(g2, 20, 120, party, gamePanel.getActivePartyIndex());

        // Trainer team circles (if trainer battle)
        if (isTrainerBattle && trainerTeam != null)
            drawPartyCircles(g2, w - 80, 120, trainerTeam, trainerIndex);

        // HP cards
        String playerName = party.isEmpty() ? "—" : party.get(gamePanel.getActivePartyIndex()).name;
        int playerHp  = party.isEmpty() ? 0 : party.get(gamePanel.getActivePartyIndex()).currentHp;
        int playerMax = party.isEmpty() ? 1 : party.get(gamePanel.getActivePartyIndex()).maxHp;
        drawHpCard(g2, w - 220, h - 260, playerName, playerHp, playerMax);
        drawHpCard(g2, 20, 50, wildPokemon.name, wildPokemon.currentHp, wildPokemon.maxHp);

        // Battle box
        int boxY = h - 185;
        g2.setColor(new Color(15, 50, 35, 230));
        g2.fillRoundRect(18, boxY, w - 36, 170, 18, 18);
        g2.setColor(new Color(180, 220, 180, 160));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(18, boxY, w - 36, 170, 18, 18);

        // Reposition Swing components dynamically
        int btnX  = w - 200;
        int row1Y = boxY + 14;
        int row2Y = boxY + 52;
        int row3Y = boxY + 100;

        messageLabel.setBounds(45, row1Y, btnX - 60, 30);
        timerLabel.setBounds(btnX - 50, row1Y, 100, 30);
        inputField.setBounds(45, row2Y, btnX - 80, 36);
        feedbackLabel.setBounds(45, row3Y, btnX - 60, 26);

        // Three action buttons stacked in right column
        int bh = 36, bgap = 8;
        attackButton.setBounds(btnX, boxY + 14,                160, bh);
        switchButton.setBounds(btnX, boxY + 14 + bh + bgap,   160, bh);
        runButton.setBounds(   btnX, boxY + 14 + 2*(bh+bgap), 160, bh);

        // Switch overlay (positioned over battle box, left side)
        switchOverlay.setBounds(30, boxY + 8, btnX - 50, 160);
        // Layout slots inside overlay
        int slotH = 28, slotGap = 3;
        for (int i = 0; i < 6; i++) {
            int col = i % 2, row = i / 2;
            int sw = (switchOverlay.getWidth() - 15) / 2;
            switchSlots[i].setBounds(5 + col * (sw + 5), 36 + row * (slotH + slotGap), sw, slotH);
        }
    }

    private void drawPartyCircles(Graphics2D g2, int x, int y, List<Pokemon> team, int activeIdx) {
        int r = 12, gap = 8;
        for (int i = 0; i < team.size(); i++) {
            boolean alive = !team.get(i).isFainted();
            boolean active = (i == activeIdx);
            int cx = x + i * (r * 2 + gap);

            // Fill
            if (alive) g2.setColor(active ? new Color(80, 220, 130) : new Color(60, 180, 100));
            else       g2.setColor(new Color(80, 50, 50));
            g2.fillOval(cx, y, r * 2, r * 2);

            // Border
            g2.setColor(active ? Color.WHITE : new Color(30, 100, 60));
            g2.setStroke(new BasicStroke(active ? 2.5f : 1.5f));
            g2.drawOval(cx, y, r * 2, r * 2);

            // X for fainted
            if (!alive) {
                g2.setColor(new Color(200, 80, 80));
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(cx + 4, y + 4, cx + r*2 - 4, y + r*2 - 4);
                g2.drawLine(cx + r*2 - 4, y + 4, cx + 4, y + r*2 - 4);
            }
        }
    }

    private void drawHpCard(Graphics2D g2, int x, int y, String name, int hp, int maxHp) {
        g2.setColor(new Color(15, 50, 35, 210));
        g2.fillRoundRect(x, y, 200, 58, 12, 12);
        g2.setColor(new Color(180, 220, 180, 120));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(x, y, 200, 58, 12, 12);

        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.setColor(Color.WHITE);
        g2.drawString(name, x + 10, y + 16);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.drawString(hp + " / " + maxHp, x + 140, y + 16);

        int barX = x + 10, barY = y + 24, barW = 180, barH = 12;
        g2.setColor(new Color(10, 30, 20));
        g2.fillRoundRect(barX, barY, barW, barH, 6, 6);
        float ratio = maxHp > 0 ? (float) hp / maxHp : 0;
        g2.setColor(ratio > 0.5f ? new Color(60, 210, 100) : ratio > 0.25f ? new Color(230, 200, 50) : new Color(220, 70, 70));
        g2.fillRoundRect(barX, barY, (int)(barW * Math.max(0, ratio)), barH, 6, 6);
        g2.setColor(new Color(255, 255, 255, 60));
        g2.drawRoundRect(barX, barY, barW, barH, 6, 6);
    }
}
