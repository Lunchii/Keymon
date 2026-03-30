import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.*;

public class BattlePanel extends JPanel {
    private CardLayout cardLayout;
    private JPanel container;
    private GamePanel gamePanel;

    private Image enemySprite;
    private Image trainerSprite;

    // HP and turn state
    private static final int PLAYER_MAX_HP = 100;
    private static final int ENEMY_MAX_HP = 100;

    private int playerHp = PLAYER_MAX_HP;
    private int enemyHp = ENEMY_MAX_HP;

    private enum Phase { COUNTDOWN, PLAYER_TURN, ENEMY_TURN, FINISHED }
    private Phase phase = Phase.PLAYER_TURN;

    // Typing / timer state
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

    private JTextField inputField;
    private JLabel promptLabel;
    private JLabel feedbackLabel;
    private JLabel timerLabel;

    public BattlePanel(CardLayout layout, JPanel container, GamePanel gamePanel, String targetWord) {
        this.cardLayout = layout;
        this.container = container;
        this.gamePanel = gamePanel;

        setLayout(null);
        setBackground(new Color(40, 120, 80));

        loadSprites();
        setupUI();
        startPlayerTurn();
    }

    private void loadSprites() {
        try {
            enemySprite = ImageIO.read(new File("enemy.png"));
        } catch (Exception e) {
            System.err.println("BattlePanel: could not load enemy.png");
        }

        try {
            trainerSprite = ImageIO.read(new File("trainer.png"));
        } catch (Exception e) {
            System.err.println("BattlePanel: could not load trainer.png");
        }
    }

    private void setupUI() {
        timerLabel = new JLabel(" ");
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        timerLabel.setBounds(40, 40, 200, 24);
        add(timerLabel);

        // Prompt / word to type
        promptLabel = new JLabel();
        promptLabel.setForeground(Color.WHITE);
        promptLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        promptLabel.setBounds(40, 350, 900, 30);
        add(promptLabel);

        inputField = new JTextField();
        inputField.setFont(new Font("Monospaced", Font.PLAIN, 18));
        inputField.setBounds(40, 390, 400, 32);
        add(inputField);

        feedbackLabel = new JLabel(" ");
        feedbackLabel.setForeground(Color.WHITE);
        feedbackLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        feedbackLabel.setBounds(40, 430, 600, 26);
        add(feedbackLabel);

        // When player presses Enter, validate the current word
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlePlayerInput();
            }
        });

        SwingUtilities.invokeLater(() -> inputField.requestFocusInWindow());
    }

    private void handlePlayerInput() {
        if (phase != Phase.PLAYER_TURN) {
            return;
        }

        String typed = inputField.getText().trim();
        if (typed.equals(currentWord)) {
            correctWordsThisTurn++;
            feedbackLabel.setText("Nice! +" + 1 + " hit (" + correctWordsThisTurn + " total)");
        } else {
            feedbackLabel.setText("Miss! The word was: " + currentWord);
        }

        // Prepare next word
        inputField.setText("");
        pickNextWord();
    }

    private void startPlayerTurn() {
        // First do a short 3-2-1 countdown before typing starts
        phase = Phase.COUNTDOWN;
        correctWordsThisTurn = 0;
        secondsLeft = 10;
        preCountdown = 3;
        inputField.setEnabled(false);
        timerLabel.setText("Get ready: " + preCountdown);
        promptLabel.setText("PLAYER TURN IS ABOUT TO START...");

        if (preTimer != null) {
            preTimer.stop();
        }

        preTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                preCountdown--;
                if (preCountdown > 0) {
                    timerLabel.setText("Get ready: " + preCountdown);
                } else {
                    preTimer.stop();
                    beginTypingPhase();
                }
            }
        });
        preTimer.start();
    }

    private void beginTypingPhase() {
        phase = Phase.PLAYER_TURN;
        inputField.setEnabled(true);
        SwingUtilities.invokeLater(() -> inputField.requestFocusInWindow());

        pickNextWord();
        updateTimerLabel();

        if (turnTimer != null) {
            turnTimer.stop();
        }

        turnTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                secondsLeft--;
                updateTimerLabel();
                if (secondsLeft <= 0) {
                    turnTimer.stop();
                    finishPlayerTurn();
                }
            }
        });
        turnTimer.start();
    }

    private void pickNextWord() {
        currentWord = wordBank[(int) (Math.random() * wordBank.length)];
        promptLabel.setText("PLAYER: Type this word (" + secondsLeft + "s): " + currentWord);
    }

    private void updateTimerLabel() {
        timerLabel.setText("Time: " + secondsLeft);
    }

    private void finishPlayerTurn() {
        // Each correct word deals 20 damage
        int damage = correctWordsThisTurn * 20;
        enemyHp -= damage;
        if (enemyHp < 0) enemyHp = 0;
        if (damage > 0) {
            feedbackLabel.setText("You hit the enemy for " + damage + " damage!");
        } else {
            feedbackLabel.setText("You did no damage this turn.");
        }

        if (enemyHp <= 0) {
            endBattle(true);
        } else {
            startEnemyTurn();
        }
    }

    private void startEnemyTurn() {
        phase = Phase.ENEMY_TURN;
        promptLabel.setText("ENEMY TURN: The Keymon attacks!");
        inputField.setEnabled(false);

        Timer enemyTimer = new Timer(1000, e -> {
            int damage = 15;
            playerHp -= damage;
            if (playerHp < 0) playerHp = 0;
            feedbackLabel.setText("The Keymon hit you for " + damage + " damage!");

            if (playerHp <= 0) {
                endBattle(false);
            } else {
                inputField.setEnabled(true);
                inputField.setText("");
                SwingUtilities.invokeLater(() -> inputField.requestFocusInWindow());
                startPlayerTurn();
            }
        });
        enemyTimer.setRepeats(false);
        enemyTimer.start();
    }

    private void endBattle(boolean playerWon) {
        phase = Phase.FINISHED;
        inputField.setEnabled(false);

        if (playerWon) {
            feedbackLabel.setText("You defeated the Keymon!");
        } else {
            feedbackLabel.setText("You were defeated by the Keymon...");
        }

        Timer backTimer = new Timer(1500, e -> {
            cardLayout.show(container, "PLAY GAME");
            gamePanel.requestFocusInWindow();
        });
        backTimer.setRepeats(false);
        backTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int w = getWidth();
        int h = getHeight();

        // Simple battle background: two platforms
        g2d.setColor(new Color(30, 100, 70));
        g2d.fillRect(0, 0, w, h);

        // Player HP bar (top-left)
        int playerBarX = 40;
        int playerBarY = 20;
        int barWidth = 220;
        int barHeight = 16;

        float playerRatio = (float) playerHp / PLAYER_MAX_HP;
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRoundRect(playerBarX, playerBarY, barWidth, barHeight, 8, 8);
        g2d.setColor(new Color(80, 220, 120));
        g2d.fillRoundRect(playerBarX, playerBarY, (int) (barWidth * playerRatio), barHeight, 8, 8);
        g2d.setColor(Color.WHITE);
        g2d.drawRoundRect(playerBarX, playerBarY, barWidth, barHeight, 8, 8);
        g2d.drawString("PLAYER", playerBarX, playerBarY - 4);

        // Enemy HP bar (top-right)
        int enemyBarWidth = 220;
        int enemyBarX = w - enemyBarWidth - 40;
        int enemyBarY = 20;
        float enemyRatio = (float) enemyHp / ENEMY_MAX_HP;
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRoundRect(enemyBarX, enemyBarY, enemyBarWidth, barHeight, 8, 8);
        g2d.setColor(new Color(220, 80, 80));
        g2d.fillRoundRect(enemyBarX, enemyBarY, (int) (enemyBarWidth * enemyRatio), barHeight, 8, 8);
        g2d.setColor(Color.WHITE);
        g2d.drawRoundRect(enemyBarX, enemyBarY, enemyBarWidth, barHeight, 8, 8);
        g2d.drawString("ENEMY", enemyBarX, enemyBarY - 4);

        // Enemy side platform (right)
        g2d.setColor(new Color(60, 150, 90));
        g2d.fillOval(w - 260, 140, 200, 60);

        // Player side platform (left)
        g2d.fillOval(40, h - 220, 220, 70);

        // Draw enemy sprite on the right
        if (enemySprite != null) {
            g2d.drawImage(enemySprite, w - 230, 40, 192, 192, null);
        }

        // Draw trainer sprite on the left
        if (trainerSprite != null) {
            g2d.drawImage(trainerSprite, 90, h - 280, 96, 96, null);
        }

        // Simple "battle box" at bottom
        g2d.setColor(new Color(20, 60, 40));
        g2d.fillRoundRect(20, 330, w - 40, 140, 16, 16);
        g2d.setColor(Color.WHITE);
        g2d.drawRoundRect(20, 330, w - 40, 140, 16, 16);
    }
}

