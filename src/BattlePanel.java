import java.awt.*;
import java.util.List;
import javax.swing.*;

public class BattlePanel extends JPanel {
    private CardLayout cardLayout;
    private JPanel container;
    private GamePanel gamePanel;

    private Pokemon wildPokemon;
    private List<Pokemon> party;

    private boolean isTrainerBattle = false;
    private List<Pokemon> trainerTeam;
    private int trainerIndex = 0;

    private enum Phase { MENU, COUNTDOWN, PLAYER_TURN, ENEMY_TURN, SWITCH_SELECT, FINISHED }
    private Phase phase = Phase.MENU;
    private boolean forcedSwitch = false;

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

    private boolean attackBoostActive = false;
    private int pendingBonusSeconds = 0;

    private JTextField inputField;
    private JLabel messageLabel;
    private JLabel timerLabel;
    private JLabel feedbackLabel;
    private JButton attackButton;
    private JButton bagButton;
    private JButton switchButton;
    private JButton runButton;
    private JButton closeBagButton;

    private JPanel switchOverlay;
    private JButton[] switchSlots = new JButton[6];

    private JPanel bagOverlay;
    private JButton[] itemButtons = new JButton[Item.values().length];

    public BattlePanel(CardLayout layout, JPanel container, GamePanel gamePanel,
                       Pokemon wildPokemon, List<Pokemon> party) {
        this.cardLayout = layout;
        this.container = container;
        this.gamePanel = gamePanel;
        this.wildPokemon = wildPokemon;
        this.party = party;

        setLayout(null);
        setBackground(new Color(30, 100, 70));

        setupUI();
        setupKeyBindings();
        showMenu();
    }

    public void setTrainerBattle(List<Pokemon> trainerTeam) {
        this.isTrainerBattle = true;
        this.trainerTeam = trainerTeam;
        this.trainerIndex = 0;
    }

    private void setupUI() {
        messageLabel = label("A wild " + wildPokemon.name + " appeared!", Font.BOLD, 18);
        add(messageLabel);

        timerLabel = label(" ", Font.BOLD, 17);
        timerLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        add(timerLabel);

        inputField = new JTextField();
        inputField.setFont(new Font("Monospaced", Font.PLAIN, 17));
        inputField.setVisible(false);
        add(inputField);

        feedbackLabel = label(" ", Font.PLAIN, 15);
        add(feedbackLabel);

        attackButton = battleBtn("ATTACK", new Color(60, 150, 220));
        attackButton.addActionListener(e -> startPlayerTurn());
        add(attackButton);

        bagButton = battleBtn("BAG [B]", new Color(130, 80, 160));
        bagButton.addActionListener(e -> openBagMenu());
        add(bagButton);

        switchButton = battleBtn("SWITCH", new Color(80, 130, 80));
        switchButton.addActionListener(e -> openSwitchMenu(false));
        add(switchButton);

        runButton = battleBtn("RUN", new Color(180, 70, 60));
        runButton.addActionListener(e -> endBattle(false, true));
        add(runButton);

        inputField.addActionListener(e -> handlePlayerInput());

        switchOverlay = new JPanel(null);
        switchOverlay.setBackground(new Color(10, 40, 25, 242));
        switchOverlay.setVisible(false);
        add(switchOverlay);

        JLabel switchTitle = new JLabel("Choose a Keymon");
        switchTitle.setForeground(Color.WHITE);
        switchTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        switchTitle.setBounds(14, 10, 260, 24);
        switchOverlay.add(switchTitle);

        for (int i = 0; i < 6; i++) {
            final int idx = i;
            switchSlots[i] = new JButton();
            switchSlots[i].setFont(new Font("SansSerif", Font.BOLD, 13));
            switchSlots[i].setForeground(Color.WHITE);
            switchSlots[i].setFocusPainted(false);
            switchSlots[i].setBorderPainted(false);
            switchSlots[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            switchSlots[i].addActionListener(e -> selectSwitch(idx));
            switchOverlay.add(switchSlots[i]);
        }

        bagOverlay = new JPanel(null);
        bagOverlay.setBackground(new Color(20, 10, 40, 245));
        bagOverlay.setVisible(false);
        add(bagOverlay);

        JLabel bagTitle = new JLabel("Use an item (costs your turn)");
        bagTitle.setForeground(Color.WHITE);
        bagTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        bagTitle.setBounds(18, 12, 320, 22);
        bagOverlay.add(bagTitle);

        JLabel bagHint = new JLabel("4 items ready - choose one");
        bagHint.setForeground(new Color(220, 210, 245));
        bagHint.setFont(new Font("SansSerif", Font.PLAIN, 12));
        bagHint.setBounds(18, 34, 240, 18);
        bagOverlay.add(bagHint);

        Item[] items = Item.values();
        for (int i = 0; i < items.length; i++) {
            final Item item = items[i];
            JButton btn = new JButton();
            btn.setFont(new Font("SansSerif", Font.BOLD, 13));
            btn.setForeground(Color.WHITE);
            btn.setBackground(item.color.darker());
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setHorizontalAlignment(SwingConstants.CENTER);
            btn.setVerticalTextPosition(SwingConstants.BOTTOM);
            btn.setMargin(new Insets(6, 14, 6, 14));
            btn.addActionListener(e -> useItem(item));
            itemButtons[i] = btn;
            bagOverlay.add(btn);
        }

        closeBagButton = battleBtn("BACK", new Color(80, 80, 80));
        closeBagButton.addActionListener(e -> showMenu());
        bagOverlay.add(closeBagButton);
    }

    private void setupKeyBindings() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke("B"), "open_bag");
        am.put("open_bag", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (phase == Phase.MENU && bagButton.isVisible()) openBagMenu();
            }
        });

        im.put(KeyStroke.getKeyStroke("ESCAPE"), "close_overlay");
        am.put("close_overlay", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (bagOverlay.isVisible() || switchOverlay.isVisible()) showMenu();
            }
        });
    }

    private JLabel label(String text, int style, int size) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", style, size));
        label.setVerticalAlignment(SwingConstants.TOP);
        return label;
    }

    private JButton battleBtn(String text, Color bg) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void setBattleInfoVisible(boolean visible) {
        messageLabel.setVisible(visible);
        timerLabel.setVisible(visible);
        feedbackLabel.setVisible(visible);
        inputField.setVisible(visible && phase == Phase.PLAYER_TURN);
    }

    private void showMenu() {
        phase = Phase.MENU;
        String activeName = party.isEmpty() ? "?" : party.get(gamePanel.getActivePartyIndex()).name;
        messageLabel.setText(activeName + " vs " + wildPokemon.name + " - What will you do?");
        timerLabel.setText(" ");
        feedbackLabel.setForeground(Color.WHITE);
        feedbackLabel.setText("Choose an action on the right.");
        inputField.setEnabled(false);
        inputField.setText("");
        switchOverlay.setVisible(false);
        bagOverlay.setVisible(false);
        attackButton.setVisible(true);
        bagButton.setVisible(true);
        switchButton.setVisible(hasSwitchOption());
        runButton.setVisible(true);
        setBattleInfoVisible(true);
    }

    private boolean hasSwitchOption() {
        for (int i = 0; i < party.size(); i++) {
            if (i != gamePanel.getActivePartyIndex() && !party.get(i).isFainted()) return true;
        }
        return false;
    }

    private void startPlayerTurn() {
        phase = Phase.COUNTDOWN;
        correctWordsThisTurn = 0;
        secondsLeft = 10 + pendingBonusSeconds;
        pendingBonusSeconds = 0;
        preCountdown = 3;
        hideActionButtons();
        setBattleInfoVisible(true);
        inputField.setEnabled(false);
        inputField.setVisible(false);
        feedbackLabel.setText("Get ready to type as many words as you can.");
        messageLabel.setText("Battle starts in...");
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
        setBattleInfoVisible(true);
        inputField.setVisible(true);
        inputField.setEnabled(true);
        inputField.setText("");
        SwingUtilities.invokeLater(() -> inputField.requestFocusInWindow());
        pickNextWord();

        if (turnTimer != null) turnTimer.stop();
        turnTimer = new Timer(1000, e -> {
            secondsLeft--;
            timerLabel.setText(secondsLeft + "s");
            if (secondsLeft <= 0) {
                turnTimer.stop();
                finishPlayerTurn();
            }
        });
        turnTimer.start();
    }

    private void pickNextWord() {
        currentWord = wordBank[(int) (Math.random() * wordBank.length)];
        messageLabel.setText("Type: " + currentWord);
        timerLabel.setText(secondsLeft + "s");
    }

    private void handlePlayerInput() {
        if (phase != Phase.PLAYER_TURN) return;

        String typed = inputField.getText().trim();
        if (typed.equalsIgnoreCase(currentWord)) {
            correctWordsThisTurn++;
            feedbackLabel.setForeground(new Color(100, 255, 140));
            feedbackLabel.setText("Correct! Hits this turn: " + correctWordsThisTurn);
        } else {
            feedbackLabel.setForeground(new Color(255, 120, 100));
            feedbackLabel.setText("Miss! The word was: " + currentWord);
        }

        inputField.setText("");
        pickNextWord();
    }

    private void finishPlayerTurn() {
        inputField.setVisible(false);
        Pokemon active = party.get(gamePanel.getActivePartyIndex());
        int damage = correctWordsThisTurn * active.attack;
        boolean boosted = attackBoostActive;
        if (attackBoostActive) {
            damage *= 2;
            attackBoostActive = false;
        }

        wildPokemon.currentHp = Math.max(0, wildPokemon.currentHp - damage);
        feedbackLabel.setForeground(Color.WHITE);
        String damageText = damage > 0
            ? "You hit " + wildPokemon.name + " for " + damage + " damage!" + (boosted ? " (BOOSTED)" : "")
            : "You missed every word...";
        feedbackLabel.setText(damageText);
        timerLabel.setText(" ");
        repaint();

        if (wildPokemon.isFainted()) {
            if (isTrainerBattle) {
                trainerIndex++;
                if (trainerIndex < trainerTeam.size()) {
                    wildPokemon = trainerTeam.get(trainerIndex);
                    messageLabel.setText("Trainer sent out " + wildPokemon.name + "!");
                    feedbackLabel.setForeground(new Color(255, 220, 100));
                    feedbackLabel.setText("The trainer has " + (trainerTeam.size() - trainerIndex) + " Keymon left.");
                    repaint();
                    new Timer(1400, e -> {
                        ((Timer) e.getSource()).stop();
                        showMenu();
                    }).start();
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

    private void openSwitchMenu(boolean forced) {
        this.forcedSwitch = forced;
        phase = Phase.SWITCH_SELECT;
        hideActionButtons();
        setBattleInfoVisible(false);

        for (int i = 0; i < 6; i++) {
            if (i < party.size()) {
                Pokemon pokemon = party.get(i);
                boolean isActive = (i == gamePanel.getActivePartyIndex());
                boolean fainted = pokemon.isFainted();
                String activeTag = isActive ? "  [ACTIVE]" : "";
                String faintedTag = fainted ? "  [X]" : "";
                switchSlots[i].setText(pokemon.name + "  " + pokemon.currentHp + "/" + pokemon.maxHp + activeTag + faintedTag);
                switchSlots[i].setEnabled(!isActive && !fainted);
                switchSlots[i].setBackground(
                    fainted ? new Color(80, 40, 40)
                            : isActive ? new Color(40, 80, 60)
                            : new Color(40, 100, 70)
                );
                switchSlots[i].setVisible(true);
            } else {
                switchSlots[i].setVisible(false);
            }
        }

        switchOverlay.setVisible(true);
        repaint();
    }

    private void selectSwitch(int idx) {
        if (idx >= party.size() || party.get(idx).isFainted()) return;
        if (idx == gamePanel.getActivePartyIndex()) return;

        gamePanel.setActivePartyIndex(idx);
        switchOverlay.setVisible(false);
        setBattleInfoVisible(true);
        messageLabel.setText("Go, " + party.get(idx).name + "!");
        feedbackLabel.setForeground(Color.WHITE);
        feedbackLabel.setText(" ");

        if (forcedSwitch) {
            new Timer(900, e -> {
                ((Timer) e.getSource()).stop();
                showMenu();
            }).start();
        } else {
            new Timer(600, e -> {
                ((Timer) e.getSource()).stop();
                startEnemyTurn();
            }).start();
        }
    }

    private void startEnemyTurn() {
        phase = Phase.ENEMY_TURN;
        setBattleInfoVisible(true);
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

                boolean anyAlive = false;
                for (Pokemon pokemon : party) {
                    if (!pokemon.isFainted()) {
                        anyAlive = true;
                        break;
                    }
                }

                if (!anyAlive) {
                    new Timer(1200, ev -> {
                        ((Timer) ev.getSource()).stop();
                        endBattle(false, false);
                    }).start();
                } else {
                    new Timer(1200, ev -> {
                        ((Timer) ev.getSource()).stop();
                        openSwitchMenu(true);
                    }).start();
                }
            } else {
                new Timer(800, ev -> {
                    ((Timer) ev.getSource()).stop();
                    showMenu();
                }).start();
            }
        }).start();
    }

    private void endBattle(boolean won, boolean ran) {
        phase = Phase.FINISHED;
        if (turnTimer != null) turnTimer.stop();
        if (preTimer != null) preTimer.stop();

        hideActionButtons();
        switchOverlay.setVisible(false);
        bagOverlay.setVisible(false);
        setBattleInfoVisible(true);
        inputField.setEnabled(false);
        inputField.setVisible(false);
        timerLabel.setText(" ");
        feedbackLabel.setForeground(Color.WHITE);
        feedbackLabel.setText(" ");

        if (ran) {
            messageLabel.setText("You got away safely!");
        } else if (won) {
            messageLabel.setText("You won the battle!");
        } else {
            messageLabel.setText("All your Keymon fainted...");
        }

        new Timer(1600, e -> {
            ((Timer) e.getSource()).stop();
            gamePanel.setInBattle(false);
            cardLayout.show(container, "PLAY GAME");
            gamePanel.requestFocusInWindow();
        }).start();
    }

    private void openBagMenu() {
        hideActionButtons();
        phase = Phase.MENU;
        setBattleInfoVisible(false);

        ItemBag bag = gamePanel.getItemBag();
        Item[] items = Item.values();
        for (int i = 0; i < items.length; i++) {
            int count = bag.getCount(items[i]);
            String label = "<html><div style='text-align:center; padding:6px 10px;'><b>" + items[i].displayName
                + " x" + count + "</b><br/>" + items[i].description + "</div></html>";
            itemButtons[i].setText(label);
            itemButtons[i].setEnabled(count > 0);
            itemButtons[i].setBackground(count > 0 ? items[i].color.darker() : new Color(50, 50, 50));
        }

        bagOverlay.setVisible(true);
        repaint();
    }

    private void useItem(Item item) {
        ItemBag bag = gamePanel.getItemBag();
        if (!bag.use(item)) return;

        Pokemon active = party.get(gamePanel.getActivePartyIndex());
        String effect;

        switch (item) {
            case POTION:
                int heal = Math.min(30, active.maxHp - active.currentHp);
                active.currentHp += heal;
                effect = active.name + " recovered " + heal + " HP!";
                break;
            case FULL_HEAL:
                active.currentHp = active.maxHp;
                effect = active.name + " was fully restored!";
                break;
            case ATTACK_BOOST:
                attackBoostActive = true;
                effect = active.name + "'s next attack is doubled!";
                break;
            case SLOW_TIME:
                pendingBonusSeconds += 5;
                effect = "Your next attack turn gets 5 extra seconds!";
                break;
            default:
                effect = "Used " + item.displayName + "!";
                break;
        }

        bagOverlay.setVisible(false);
        setBattleInfoVisible(true);
        feedbackLabel.setForeground(new Color(255, 220, 100));
        feedbackLabel.setText(effect);
        repaint();

        new Timer(900, e -> {
            ((Timer) e.getSource()).stop();
            startEnemyTurn();
        }).start();
    }

    private void hideActionButtons() {
        attackButton.setVisible(false);
        bagButton.setVisible(false);
        switchButton.setVisible(false);
        runButton.setVisible(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        g2.setColor(new Color(30, 100, 70));
        g2.fillRect(0, 0, w, h);

        g2.setColor(new Color(55, 140, 85));
        g2.fillOval(w - 280, 150, 220, 65);
        g2.fillOval(30, h - 305, 270, 84);

        Image enemySprite = wildPokemon.getSprite();
        if (enemySprite != null) g2.drawImage(enemySprite, w - 260, 50, 200, 200, null);

        if (!party.isEmpty()) {
            Pokemon active = party.get(gamePanel.getActivePartyIndex());
            Image playerSprite = active.getSprite();
            if (playerSprite != null) g2.drawImage(playerSprite, 72, h - 430, 190, 190, null);
        }

        drawPartyCircles(g2, 20, 120, party, gamePanel.getActivePartyIndex());
        if (isTrainerBattle && trainerTeam != null) {
            drawPartyCircles(g2, w - 80, 120, trainerTeam, trainerIndex);
        }

        String playerName = party.isEmpty() ? "-" : party.get(gamePanel.getActivePartyIndex()).name;
        int playerHp = party.isEmpty() ? 0 : party.get(gamePanel.getActivePartyIndex()).currentHp;
        int playerMax = party.isEmpty() ? 1 : party.get(gamePanel.getActivePartyIndex()).maxHp;
        drawHpCard(g2, w - 230, h - 355, playerName, playerHp, playerMax);
        drawHpCard(g2, 20, 50, wildPokemon.name, wildPokemon.currentHp, wildPokemon.maxHp);

        int boxY = h - 210;
        int boxH = 190;
        g2.setColor(new Color(15, 50, 35, 236));
        g2.fillRoundRect(18, boxY, w - 36, boxH, 18, 18);
        g2.setColor(new Color(180, 220, 180, 160));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(18, boxY, w - 36, boxH, 18, 18);

        int btnX = w - 210;
        int textWidth = btnX - 75;
        int row1Y = boxY + 16;
        int row2Y = boxY + 58;
        int row3Y = boxY + 108;

        messageLabel.setBounds(45, row1Y, textWidth, 30);
        feedbackLabel.setBounds(45, row2Y, textWidth, 28);
        inputField.setBounds(45, row3Y, textWidth, 38);
        timerLabel.setBounds(btnX - 60, row1Y, 110, 28);

        int buttonHeight = 34;
        int buttonGap = 8;
        attackButton.setBounds(btnX, boxY + 16, 170, buttonHeight);
        bagButton.setBounds(btnX, boxY + 16 + (buttonHeight + buttonGap), 170, buttonHeight);
        switchButton.setBounds(btnX, boxY + 16 + 2 * (buttonHeight + buttonGap), 170, buttonHeight);
        runButton.setBounds(btnX, boxY + 16 + 3 * (buttonHeight + buttonGap), 170, buttonHeight);

        int overlayW = btnX - 50;
        switchOverlay.setBounds(30, boxY + 10, overlayW, boxH - 20);
        int slotW = (overlayW - 20) / 2;
        int slotH = 24;
        int slotGap = 4;
        for (int i = 0; i < 6; i++) {
            int col = i % 2;
            int row = i / 2;
            switchSlots[i].setBounds(8 + col * (slotW + 8), 42 + row * (slotH + slotGap), slotW, slotH);
        }

        bagOverlay.setBounds(30, boxY + 10, overlayW, boxH - 20);
        int itemBtnW = (overlayW - 44) / 2;
        int itemBtnH = 44;
        for (int i = 0; i < itemButtons.length; i++) {
            int col = i % 2;
            int row = i / 2;
            itemButtons[i].setBounds(14 + col * (itemBtnW + 16), 58 + row * (itemBtnH + 12), itemBtnW, itemBtnH);
        }
        closeBagButton.setBounds(overlayW - 126, boxH - 58, 112, 28);
    }

    private void drawPartyCircles(Graphics2D g2, int x, int y, List<Pokemon> team, int activeIdx) {
        int radius = 12;
        int gap = 8;
        for (int i = 0; i < team.size(); i++) {
            boolean alive = !team.get(i).isFainted();
            boolean active = (i == activeIdx);
            int cx = x + i * (radius * 2 + gap);

            if (alive) {
                g2.setColor(active ? new Color(80, 220, 130) : new Color(60, 180, 100));
            } else {
                g2.setColor(new Color(80, 50, 50));
            }
            g2.fillOval(cx, y, radius * 2, radius * 2);

            g2.setColor(active ? Color.WHITE : new Color(30, 100, 60));
            g2.setStroke(new BasicStroke(active ? 2.5f : 1.5f));
            g2.drawOval(cx, y, radius * 2, radius * 2);

            if (!alive) {
                g2.setColor(new Color(200, 80, 80));
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(cx + 4, y + 4, cx + radius * 2 - 4, y + radius * 2 - 4);
                g2.drawLine(cx + radius * 2 - 4, y + 4, cx + 4, y + radius * 2 - 4);
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

        int barX = x + 10;
        int barY = y + 24;
        int barW = 180;
        int barH = 12;
        g2.setColor(new Color(10, 30, 20));
        g2.fillRoundRect(barX, barY, barW, barH, 6, 6);
        float ratio = maxHp > 0 ? (float) hp / maxHp : 0;
        g2.setColor(
            ratio > 0.5f ? new Color(60, 210, 100)
                         : ratio > 0.25f ? new Color(230, 200, 50)
                                          : new Color(220, 70, 70)
        );
        g2.fillRoundRect(barX, barY, (int) (barW * Math.max(0, ratio)), barH, 6, 6);
        g2.setColor(new Color(255, 255, 255, 60));
        g2.drawRoundRect(barX, barY, barW, barH, 6, 6);
    }
}
