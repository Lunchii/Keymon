import java.awt.BasicStroke;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Random;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Typing battle screen used for wild encounters and trainer fights.
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
public class BattlePanel extends JPanel {
    private enum Phase { MENU, COUNTDOWN, PLAYER_TURN, ENEMY_TURN, SWITCH_SELECT, FINISHED }

    private final CardLayout cardLayout;
    private final JPanel container;
    private final GamePanel gamePanel;
    private final List<Pokemon> party;
    private final Random random = new Random();

    private Pokemon wildPokemon;
    private boolean isTrainerBattle;
    private List<Pokemon> trainerTeam;
    private int trainerIndex;
    private Phase phase = Phase.MENU;
    private boolean forcedSwitch;

    private String currentWord = "";
    private int typedWordsThisTurn;
    private int secondsLeft;
    private int preCountdown;
    private int pendingBonusSeconds;
    private int turnDamage;
    private boolean attackBoostActive;
    private boolean suppressInputEvents;
    private String lastTrackedText = "";
    private long wordStartMillis;

    private Timer turnTimer;
    private Timer preTimer;

    private JLabel statusLabel;
    private JLabel timerLabel;
    private JLabel wordLabel;
    private JLabel feedbackLabel;
    private JLabel statsLabel;
    private JTextField inputField;
    private JButton attackButton;
    private JButton bagButton;
    private JButton switchButton;
    private JButton runButton;
    private JButton closeBagButton;

    private JPanel switchOverlay;
    private final JButton[] switchSlots = new JButton[6];
    private JPanel bagOverlay;
    private final JButton[] itemButtons = new JButton[Item.values().length];

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
        if (trainerTeam != null && !trainerTeam.isEmpty()) {
            this.wildPokemon = trainerTeam.get(0);
        }
    }

    private void setupUI() {
        statusLabel = createLabel("A wild " + wildPokemon.name + " appeared!", Font.BOLD, 18);
        add(statusLabel);

        timerLabel = createLabel(" ", Font.BOLD, 18);
        timerLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        add(timerLabel);

        wordLabel = createLabel(" ", Font.BOLD, 24);
        add(wordLabel);

        feedbackLabel = createLabel(" ", Font.PLAIN, 15);
        add(feedbackLabel);

        statsLabel = createLabel(" ", Font.PLAIN, 13);
        add(statsLabel);

        inputField = new JTextField();
        inputField.setFont(new Font("Monospaced", Font.BOLD, 18));
        inputField.setVisible(false);
        inputField.addActionListener(event -> handlePlayerInput());
        inputField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                updateTypingPreview();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                updateTypingPreview();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                updateTypingPreview();
            }
        });
        add(inputField);

        attackButton = createBattleButton("ATTACK", new Color(60, 150, 220));
        attackButton.addActionListener(event -> startPlayerTurn());
        add(attackButton);

        bagButton = createBattleButton("BAG [B]", new Color(130, 80, 160));
        bagButton.addActionListener(event -> openBagMenu());
        add(bagButton);

        switchButton = createBattleButton("SWITCH", new Color(80, 130, 80));
        switchButton.addActionListener(event -> openSwitchMenu(false));
        add(switchButton);

        runButton = createBattleButton("RUN", new Color(180, 70, 60));
        runButton.addActionListener(event -> endBattle(false, true));
        add(runButton);

        switchOverlay = new JPanel(null);
        switchOverlay.setBackground(new Color(10, 40, 25, 242));
        switchOverlay.setVisible(false);
        add(switchOverlay);

        JLabel switchTitle = createLabel("Choose a Keymon", Font.BOLD, 16);
        switchTitle.setBounds(14, 10, 260, 24);
        switchOverlay.add(switchTitle);

        for (int i = 0; i < switchSlots.length; i++) {
            final int index = i;
            switchSlots[i] = new JButton();
            switchSlots[i].setFont(new Font("SansSerif", Font.BOLD, 13));
            switchSlots[i].setForeground(Color.WHITE);
            switchSlots[i].setFocusPainted(false);
            switchSlots[i].setBorderPainted(false);
            switchSlots[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            switchSlots[i].addActionListener(event -> selectSwitch(index));
            switchOverlay.add(switchSlots[i]);
        }

        bagOverlay = new JPanel(null);
        bagOverlay.setBackground(new Color(20, 10, 40, 245));
        bagOverlay.setVisible(false);
        add(bagOverlay);

        JLabel bagTitle = createLabel("Use an item (costs your turn)", Font.BOLD, 15);
        bagTitle.setBounds(18, 12, 320, 22);
        bagOverlay.add(bagTitle);

        JLabel bagHint = createLabel("Powerups help you survive harder stages.", Font.PLAIN, 12);
        bagHint.setForeground(new Color(220, 210, 245));
        bagHint.setBounds(18, 34, 280, 18);
        bagOverlay.add(bagHint);

        for (int i = 0; i < Item.values().length; i++) {
            final Item item = Item.values()[i];
            JButton button = new JButton();
            button.setFont(new Font("SansSerif", Font.BOLD, 13));
            button.setForeground(Color.WHITE);
            button.setBackground(item.color.darker());
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setHorizontalAlignment(SwingConstants.CENTER);
            button.setVerticalTextPosition(SwingConstants.BOTTOM);
            button.setMargin(new Insets(6, 14, 6, 14));
            button.addActionListener(event -> useItem(item));
            itemButtons[i] = button;
            bagOverlay.add(button);
        }

        closeBagButton = createBattleButton("BACK", new Color(80, 80, 80));
        closeBagButton.addActionListener(event -> showMenu());
        bagOverlay.add(closeBagButton);
    }

    private void setupKeyBindings() {
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("B"), "open_bag");
        actionMap.put("open_bag", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                if (phase == Phase.MENU && bagButton.isVisible()) {
                    openBagMenu();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "close_overlay");
        actionMap.put("close_overlay", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                if (bagOverlay.isVisible() || switchOverlay.isVisible()) {
                    showMenu();
                }
            }
        });
    }

    private JLabel createLabel(String text, int style, int size) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", style, size));
        label.setVerticalAlignment(SwingConstants.TOP);
        return label;
    }

    private JButton createBattleButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void showMenu() {
        phase = Phase.MENU;
        stopTimers();
        switchOverlay.setVisible(false);
        bagOverlay.setVisible(false);
        setBattleLabelsVisible(true);
        hideInputField();
        attackButton.setVisible(true);
        bagButton.setVisible(true);
        switchButton.setVisible(hasSwitchOption());
        runButton.setVisible(true);
        statusLabel.setText(activePokemon().name + " vs " + wildPokemon.name);
        wordLabel.setText(wrapWordHtml("Press ATTACK to start your typing turn."));
        feedbackLabel.setForeground(Color.WHITE);
        feedbackLabel.setText("Use powerups, switch Keymon, or start typing.");
        statsLabel.setText(buildStatsLine());
        timerLabel.setText(" ");
    }

    private boolean hasSwitchOption() {
        for (int i = 0; i < party.size(); i++) {
            if (i != gamePanel.getActivePartyIndex() && !party.get(i).isFainted()) {
                return true;
            }
        }
        return false;
    }

    private void startPlayerTurn() {
        phase = Phase.COUNTDOWN;
        typedWordsThisTurn = 0;
        turnDamage = 0;
        secondsLeft = WordRepository.getTurnTimeSeconds(gamePanel.getCurrentLevelId()) + pendingBonusSeconds;
        pendingBonusSeconds = 0;
        preCountdown = 3;
        hideActionButtons();
        setBattleLabelsVisible(true);
        hideInputField();
        feedbackLabel.setForeground(new Color(220, 230, 255));
        feedbackLabel.setText("Build words quickly, but protect your focus.");
        statusLabel.setText("Get ready...");
        timerLabel.setText(String.valueOf(preCountdown));
        statsLabel.setText(buildStatsLine());

        if (preTimer != null) {
            preTimer.stop();
        }
        preTimer = new Timer(1000, event -> {
            preCountdown--;
            if (preCountdown > 0) {
                timerLabel.setText(String.valueOf(preCountdown));
            } else {
                ((Timer) event.getSource()).stop();
                beginTypingPhase();
            }
        });
        preTimer.start();
    }

    private void beginTypingPhase() {
        phase = Phase.PLAYER_TURN;
        showInputField();
        feedbackLabel.setForeground(Color.WHITE);
        feedbackLabel.setText("Type the word, then press Enter.");
        pickNextWord();

        if (turnTimer != null) {
            turnTimer.stop();
        }
        turnTimer = new Timer(1000, event -> {
            secondsLeft--;
            timerLabel.setText(secondsLeft + "s");
            if (secondsLeft <= 0) {
                ((Timer) event.getSource()).stop();
                finishPlayerTurn();
            }
        });
        turnTimer.start();
    }

    private void pickNextWord() {
        currentWord = WordRepository.pickWord(gamePanel.getCurrentLevelId(), random);
        wordStartMillis = System.currentTimeMillis();
        clearInputField();
        updateWordPreview("");
        timerLabel.setText(secondsLeft + "s");
        statsLabel.setText(buildStatsLine());
    }

    private void updateTypingPreview() {
        if (suppressInputEvents || phase != Phase.PLAYER_TURN) {
            return;
        }

        String text = inputField.getText();
        int shared = Math.min(lastTrackedText.length(), text.length());
        int prefix = 0;
        while (prefix < shared && lastTrackedText.charAt(prefix) == text.charAt(prefix)) {
            prefix++;
        }

        if (text.length() > lastTrackedText.length()) {
            for (int i = prefix; i < text.length(); i++) {
                char typed = text.charAt(i);
                boolean correct = i < currentWord.length()
                    && Character.toLowerCase(typed) == Character.toLowerCase(currentWord.charAt(i));
                gamePanel.recordTypedCharacter(correct);
                if (!correct) {
                    feedbackLabel.setForeground(new Color(255, 120, 100));
                    feedbackLabel.setText("Mistake! Keep going.");
                    statsLabel.setText(buildStatsLine());
                }
            }
        }

        lastTrackedText = text;
        updateWordPreview(text);
        statsLabel.setText(buildStatsLine());
    }

    private void updateWordPreview(String typed) {
        int matched = 0;
        while (matched < typed.length() && matched < currentWord.length()
            && Character.toLowerCase(typed.charAt(matched)) == Character.toLowerCase(currentWord.charAt(matched))) {
            matched++;
        }

        String good = escapeHtml(currentWord.substring(0, matched));
        String bad = "";
        String rest = escapeHtml(currentWord.substring(Math.min(currentWord.length(), matched)));

        if (typed.length() > matched && matched < currentWord.length()) {
            bad = escapeHtml(String.valueOf(currentWord.charAt(matched)));
            rest = escapeHtml(currentWord.substring(Math.min(currentWord.length(), matched + 1)));
        }

        StringBuilder builder = new StringBuilder("<html><div style='font-size:28px;'>");
        builder.append("<span style='color:#80ff9c;'>").append(good).append("</span>");
        if (!bad.isEmpty()) {
            builder.append("<span style='color:#ff8a80;'>").append(bad).append("</span>");
        }
        builder.append("<span style='color:#ffffff;'>").append(rest).append("</span>");
        builder.append("</div></html>");
        wordLabel.setText(builder.toString());
    }

    private void handlePlayerInput() {
        if (phase != Phase.PLAYER_TURN) {
            return;
        }

        String typed = inputField.getText().trim();
        if (typed.equalsIgnoreCase(currentWord)) {
            typedWordsThisTurn++;
            int score = WordRepository.scoreWord(gamePanel.getCurrentLevelId(), currentWord,
                Math.max(1, gamePanel.getComboStreak() + 1));
            gamePanel.recordCompletedWord(currentWord, score);
            turnDamage += activePokemon().attack + currentWord.length() * gamePanel.getCurrentLevelId();

            double wordSeconds = Math.max(0.25, (System.currentTimeMillis() - wordStartMillis) / 1000.0);
            double wordWpm = 60.0 / wordSeconds;
            feedbackLabel.setForeground(new Color(100, 255, 140));
            feedbackLabel.setText("Correct! +" + score + " score | Word WPM " + String.format("%.1f", wordWpm));
            statsLabel.setText(buildStatsLine());
            pickNextWord();
        } else if (!typed.isEmpty()) {
            feedbackLabel.setForeground(new Color(255, 210, 120));
            feedbackLabel.setText("Word missed. Resetting to a new one.");
            clearInputField();
            pickNextWord();
        }
    }

    private void finishPlayerTurn() {
        hideInputField();
        int damage = turnDamage;
        boolean boosted = attackBoostActive;
        if (attackBoostActive) {
            damage *= 2;
            attackBoostActive = false;
        }

        wildPokemon.currentHp = Math.max(0, wildPokemon.currentHp - damage);
        statsLabel.setText(buildStatsLine());
        if (damage > 0) {
            feedbackLabel.setForeground(Color.WHITE);
            feedbackLabel.setText("Your words dealt " + damage + " damage" + (boosted ? " (BOOSTED)" : "") + "!");
        } else {
            feedbackLabel.setForeground(new Color(255, 210, 120));
            feedbackLabel.setText("No clean words landed this turn.");
        }
        timerLabel.setText(" ");
        repaint();

        if (wildPokemon.isFainted()) {
            Pokemon defeated = wildPokemon;
            if (isTrainerBattle) {
                trainerIndex++;
                if (trainerTeam != null && trainerIndex < trainerTeam.size()) {
                    wildPokemon = trainerTeam.get(trainerIndex);
                    statusLabel.setText("Trainer sent out " + wildPokemon.name + "!");
                    wordLabel.setText(wrapWordHtml("Another opponent jumps into the battle."));
                    feedbackLabel.setForeground(new Color(255, 220, 100));
                    feedbackLabel.setText("The trainer still has " + (trainerTeam.size() - trainerIndex) + " Keymon left.");
                    new Timer(1400, event -> {
                        ((Timer) event.getSource()).stop();
                        showMenu();
                    }).start();
                } else {
                    gamePanel.onTrainerDefeated();
                    gamePanel.awardBattleVictory(defeated, true);
                    endBattle(true, false);
                }
            } else {
                gamePanel.awardBattleVictory(defeated, false);
                endBattle(true, false);
            }
            return;
        }

        startEnemyTurn();
    }

    private void openSwitchMenu(boolean forced) {
        forcedSwitch = forced;
        phase = Phase.SWITCH_SELECT;
        hideActionButtons();
        hideInputField();
        switchOverlay.setVisible(true);
        bagOverlay.setVisible(false);
        statsLabel.setText(buildStatsLine());

        for (int i = 0; i < switchSlots.length; i++) {
            if (i < party.size()) {
                Pokemon pokemon = party.get(i);
                boolean active = i == gamePanel.getActivePartyIndex();
                boolean fainted = pokemon.isFainted();
                String activeTag = active ? " [ACTIVE]" : "";
                String faintedTag = fainted ? " [X]" : "";
                switchSlots[i].setText(pokemon.name + " " + pokemon.currentHp + "/" + pokemon.maxHp + activeTag + faintedTag);
                switchSlots[i].setEnabled(!active && !fainted);
                switchSlots[i].setBackground(fainted ? new Color(80, 40, 40)
                    : active ? new Color(40, 80, 60) : new Color(40, 100, 70));
                switchSlots[i].setVisible(true);
            } else {
                switchSlots[i].setVisible(false);
            }
        }

        statusLabel.setText(forced ? "Choose a new Keymon now!" : "Switching costs your turn.");
        wordLabel.setText(wrapWordHtml(forced ? "Your active Keymon fainted." : "Pick a teammate to tag in."));
        feedbackLabel.setForeground(Color.WHITE);
        feedbackLabel.setText(" ");
    }

    private void selectSwitch(int index) {
        if (index >= party.size() || party.get(index).isFainted() || index == gamePanel.getActivePartyIndex()) {
            return;
        }

        gamePanel.setActivePartyIndex(index);
        switchOverlay.setVisible(false);
        statusLabel.setText("Go, " + party.get(index).name + "!");
        wordLabel.setText(wrapWordHtml("Fresh energy enters the battle."));
        feedbackLabel.setForeground(Color.WHITE);
        feedbackLabel.setText(" ");
        statsLabel.setText(buildStatsLine());

        if (forcedSwitch) {
            new Timer(900, event -> {
                ((Timer) event.getSource()).stop();
                showMenu();
            }).start();
        } else {
            new Timer(700, event -> {
                ((Timer) event.getSource()).stop();
                startEnemyTurn();
            }).start();
        }
    }

    private void startEnemyTurn() {
        phase = Phase.ENEMY_TURN;
        hideActionButtons();
        hideInputField();
        switchOverlay.setVisible(false);
        bagOverlay.setVisible(false);
        statusLabel.setText(wildPokemon.name + " attacks!");
        wordLabel.setText(wrapWordHtml("Hold on while the enemy takes its turn."));
        feedbackLabel.setText(" ");
        timerLabel.setText(" ");

        new Timer(1100, event -> {
            ((Timer) event.getSource()).stop();
            Pokemon active = activePokemon();
            int damage = (int) Math.round((wildPokemon.attack + WordRepository.enemyAttackBonus(gamePanel.getCurrentLevelId())) * 1.2);
            active.currentHp = Math.max(0, active.currentHp - damage);
            feedbackLabel.setForeground(new Color(255, 120, 100));
            feedbackLabel.setText(wildPokemon.name + " hit " + active.name + " for " + damage + " damage!");
            statsLabel.setText(buildStatsLine());
            repaint();

            if (active.isFainted()) {
                boolean anyAlive = false;
                for (Pokemon pokemon : party) {
                    if (!pokemon.isFainted()) {
                        anyAlive = true;
                        break;
                    }
                }

                if (!anyAlive) {
                    failBattle("All of your Keymon fainted.");
                } else {
                    new Timer(1200, next -> {
                        ((Timer) next.getSource()).stop();
                        openSwitchMenu(true);
                    }).start();
                }
            } else {
                new Timer(800, next -> {
                    ((Timer) next.getSource()).stop();
                    showMenu();
                }).start();
            }
        }).start();
    }

    private void openBagMenu() {
        phase = Phase.MENU;
        hideActionButtons();
        hideInputField();
        switchOverlay.setVisible(false);
        bagOverlay.setVisible(true);
        statusLabel.setText("Pick a powerup");
        wordLabel.setText(wrapWordHtml("Every item helps in a different way."));
        feedbackLabel.setForeground(Color.WHITE);
        feedbackLabel.setText("Items cost your turn, so use them carefully.");
        statsLabel.setText(buildStatsLine());

        for (int i = 0; i < Item.values().length; i++) {
            Item item = Item.values()[i];
            int count = gamePanel.getItemBag().getCount(item);
            String label = "<html><div style='text-align:center; padding:6px 10px;'><b>" + item.displayName
                + " x" + count + "</b><br/>" + item.description + "</div></html>";
            itemButtons[i].setText(label);
            itemButtons[i].setEnabled(count > 0);
            itemButtons[i].setBackground(count > 0 ? item.color.darker() : new Color(50, 50, 50));
        }
    }

    private void useItem(Item item) {
        ItemBag bag = gamePanel.getItemBag();
        if (!bag.use(item)) {
            return;
        }

        gamePanel.notePowerupUsed(item);
        Pokemon active = activePokemon();
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
                effect = "Your next typing turn gets 5 extra seconds!";
                break;
            default:
                effect = "Used " + item.displayName + "!";
                break;
        }

        bagOverlay.setVisible(false);
        statusLabel.setText(item.displayName + " used");
        wordLabel.setText(wrapWordHtml(effect));
        feedbackLabel.setForeground(new Color(255, 220, 100));
        feedbackLabel.setText("The enemy attacks after item use.");
        statsLabel.setText(buildStatsLine());
        repaint();

        new Timer(900, event -> {
            ((Timer) event.getSource()).stop();
            startEnemyTurn();
        }).start();
    }

    private void failBattle(String reason) {
        phase = Phase.FINISHED;
        stopTimers();
        hideActionButtons();
        hideInputField();
        switchOverlay.setVisible(false);
        bagOverlay.setVisible(false);
        gamePanel.setInBattle(false);
        String destination = gamePanel.isTutorialMode() ? "TUTORIAL_GAME" : "PLAY GAME";
        cardLayout.show(container, destination);
        gamePanel.requestFocusInWindow();
        SwingUtilities.invokeLater(() -> gamePanel.handleLevelFailure(reason));
    }

    private void endBattle(boolean won, boolean ran) {
        phase = Phase.FINISHED;
        stopTimers();
        hideActionButtons();
        hideInputField();
        switchOverlay.setVisible(false);
        bagOverlay.setVisible(false);
        timerLabel.setText(" ");
        statsLabel.setText(buildStatsLine());

        if (ran) {
            statusLabel.setText("You got away safely!");
            wordLabel.setText(wrapWordHtml("Retreating lets you keep your progress and items."));
        } else if (won) {
            statusLabel.setText("Battle won!");
            wordLabel.setText(wrapWordHtml("The path is clear. Keep moving toward the exit tile."));
        }
        feedbackLabel.setForeground(Color.WHITE);
        feedbackLabel.setText("Returning to the route...");

        new Timer(1400, event -> {
            ((Timer) event.getSource()).stop();
            gamePanel.setInBattle(false);
            String destination = gamePanel.isTutorialMode() ? "TUTORIAL_GAME" : "PLAY GAME";
            cardLayout.show(container, destination);
            gamePanel.requestFocusInWindow();
        }).start();
    }

    private void hideActionButtons() {
        attackButton.setVisible(false);
        bagButton.setVisible(false);
        switchButton.setVisible(false);
        runButton.setVisible(false);
    }

    private void setBattleLabelsVisible(boolean visible) {
        statusLabel.setVisible(visible);
        timerLabel.setVisible(visible);
        wordLabel.setVisible(visible);
        feedbackLabel.setVisible(visible);
        statsLabel.setVisible(visible);
    }

    private void showInputField() {
        suppressInputEvents = true;
        inputField.setText("");
        suppressInputEvents = false;
        lastTrackedText = "";
        inputField.setVisible(true);
        inputField.setEnabled(true);
        SwingUtilities.invokeLater(() -> inputField.requestFocusInWindow());
    }

    private void hideInputField() {
        inputField.setEnabled(false);
        inputField.setVisible(false);
        clearInputField();
    }

    private void clearInputField() {
        suppressInputEvents = true;
        inputField.setText("");
        suppressInputEvents = false;
        lastTrackedText = "";
    }

    private void stopTimers() {
        if (turnTimer != null) {
            turnTimer.stop();
        }
        if (preTimer != null) {
            preTimer.stop();
        }
    }

    private Pokemon activePokemon() {
        return party.get(gamePanel.getActivePartyIndex());
    }

    private String buildStatsLine() {
        return "Score " + gamePanel.getLevelScore()
            + " | WPM " + String.format("%.1f", gamePanel.getCurrentWpm())
            + " | Combo x" + Math.max(1, gamePanel.getComboStreak());
    }

    private String wrapWordHtml(String text) {
        return "<html><div style='font-size:22px; color:#ffffff;'>" + escapeHtml(text) + "</div></html>";
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        Color top = battleTopColor();
        Color bottom = battleBottomColor();
        g2.setPaint(new GradientPaint(0, 0, top, 0, height, bottom));
        g2.fillRect(0, 0, width, height);

        g2.setColor(new Color(255, 255, 255, 30));
        g2.fillOval(width - 300, 140, 220, 70);
        g2.fillOval(40, height - 315, 280, 86);

        int enemyBob = (int) (Math.sin(System.currentTimeMillis() / 180.0) * 6);
        int playerBob = (int) (Math.sin(System.currentTimeMillis() / 220.0 + 1.5) * 5);

        Image enemySprite = wildPokemon.getSprite();
        if (enemySprite != null) {
            g2.drawImage(enemySprite, width - 260, 60 + enemyBob, 200, 200, null);
        }

        if (!party.isEmpty()) {
            Pokemon active = activePokemon();
            Image playerSprite = active.getSprite();
            if (playerSprite != null) {
                g2.drawImage(playerSprite, 72, height - 430 + playerBob, 190, 190, null);
            }
        }

        drawPartyCircles(g2, 20, 120, party, gamePanel.getActivePartyIndex());
        if (isTrainerBattle && trainerTeam != null) {
            drawPartyCircles(g2, width - 80, 120, trainerTeam, trainerIndex);
        }

        Pokemon active = activePokemon();
        drawHpCard(g2, width - 230, height - 355, active.name, active.currentHp, active.maxHp);
        drawHpCard(g2, 20, 50, wildPokemon.name, wildPokemon.currentHp, wildPokemon.maxHp);

        int boxY = height - 220;
        int boxH = 200;
        g2.setColor(new Color(15, 50, 35, 236));
        g2.fillRoundRect(18, boxY, width - 36, boxH, 18, 18);
        g2.setColor(new Color(180, 220, 180, 160));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(18, boxY, width - 36, boxH, 18, 18);

        int buttonX = width - 210;
        int textWidth = buttonX - 80;
        statusLabel.setBounds(45, boxY + 14, textWidth, 30);
        timerLabel.setBounds(buttonX - 60, boxY + 14, 110, 28);
        wordLabel.setBounds(45, boxY + 48, textWidth, 44);
        feedbackLabel.setBounds(45, boxY + 96, textWidth, 28);
        statsLabel.setBounds(45, boxY + 124, textWidth, 24);
        inputField.setBounds(45, boxY + 154, textWidth, 36);

        int buttonHeight = 34;
        int buttonGap = 8;
        attackButton.setBounds(buttonX, boxY + 16, 170, buttonHeight);
        bagButton.setBounds(buttonX, boxY + 16 + (buttonHeight + buttonGap), 170, buttonHeight);
        switchButton.setBounds(buttonX, boxY + 16 + 2 * (buttonHeight + buttonGap), 170, buttonHeight);
        runButton.setBounds(buttonX, boxY + 16 + 3 * (buttonHeight + buttonGap), 170, buttonHeight);

        int overlayW = buttonX - 50;
        switchOverlay.setBounds(30, boxY + 10, overlayW, boxH - 20);
        int slotW = (overlayW - 20) / 2;
        int slotH = 24;
        int slotGap = 4;
        for (int i = 0; i < switchSlots.length; i++) {
            int col = i % 2;
            int row = i / 2;
            switchSlots[i].setBounds(8 + col * (slotW + 8), 42 + row * (slotH + slotGap), slotW, slotH);
        }

        bagOverlay.setBounds(30, boxY + 10, overlayW, boxH - 20);
        int itemButtonW = (overlayW - 44) / 2;
        int itemButtonH = 44;
        for (int i = 0; i < itemButtons.length; i++) {
            int col = i % 2;
            int row = i / 2;
            itemButtons[i].setBounds(14 + col * (itemButtonW + 16), 58 + row * (itemButtonH + 12), itemButtonW, itemButtonH);
        }
        closeBagButton.setBounds(overlayW - 118, boxH - 62, 108, 30);
    }

    private void drawPartyCircles(Graphics2D graphics, int x, int y, List<Pokemon> team, int activeIndex) {
        int radius = 12;
        int gap = 8;
        for (int i = 0; i < team.size(); i++) {
            boolean alive = !team.get(i).isFainted();
            boolean active = i == activeIndex;
            int cx = x + i * (radius * 2 + gap);

            if (alive) {
                graphics.setColor(active ? new Color(80, 220, 130) : new Color(60, 180, 100));
            } else {
                graphics.setColor(new Color(80, 50, 50));
            }
            graphics.fillOval(cx, y, radius * 2, radius * 2);

            graphics.setColor(active ? Color.WHITE : new Color(30, 100, 60));
            graphics.setStroke(new BasicStroke(active ? 2.5f : 1.5f));
            graphics.drawOval(cx, y, radius * 2, radius * 2);
        }
    }

    private void drawHpCard(Graphics2D graphics, int x, int y, String name, int hp, int maxHp) {
        graphics.setColor(new Color(15, 50, 35, 210));
        graphics.fillRoundRect(x, y, 200, 58, 12, 12);
        graphics.setColor(new Color(180, 220, 180, 120));
        graphics.setStroke(new BasicStroke(1.5f));
        graphics.drawRoundRect(x, y, 200, 58, 12, 12);

        graphics.setFont(new Font("SansSerif", Font.BOLD, 13));
        graphics.setColor(Color.WHITE);
        graphics.drawString(name, x + 10, y + 16);
        graphics.setFont(new Font("SansSerif", Font.PLAIN, 12));
        graphics.drawString(hp + " / " + maxHp, x + 140, y + 16);

        int barX = x + 10;
        int barY = y + 24;
        int barW = 180;
        int barH = 12;
        graphics.setColor(new Color(10, 30, 20));
        graphics.fillRoundRect(barX, barY, barW, barH, 6, 6);
        float ratio = maxHp > 0 ? (float) hp / maxHp : 0;
        graphics.setColor(ratio > 0.5f ? new Color(60, 210, 100)
            : ratio > 0.25f ? new Color(230, 200, 50) : new Color(220, 70, 70));
        graphics.fillRoundRect(barX, barY, (int) (barW * Math.max(0, ratio)), barH, 6, 6);
        graphics.setColor(new Color(255, 255, 255, 60));
        graphics.drawRoundRect(barX, barY, barW, barH, 6, 6);
    }

    private Color battleTopColor() {
        switch (gamePanel.getCurrentLevelId()) {
            case 2:
                return new Color(34, 52, 41);
            case 3:
                return new Color(54, 76, 120);
            default:
                return new Color(38, 118, 80);
        }
    }

    private Color battleBottomColor() {
        switch (gamePanel.getCurrentLevelId()) {
            case 2:
                return new Color(14, 24, 18);
            case 3:
                return new Color(24, 30, 66);
            default:
                return new Color(18, 54, 40);
        }
    }
}
