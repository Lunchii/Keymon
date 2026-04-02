import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class PlayerScreen extends JPanel {
    private Main mainApp;
    private JPanel container;
    private CardLayout cardLayout;
    private String username;
    private JLabel welcomeLabel;
    private JLabel statsSummaryLabel;
    private JTextArea detailArea;

    public PlayerScreen(CardLayout layout, JPanel container, Main mainApp) {
        this.cardLayout = layout;
        this.container = container;
        this.mainApp = mainApp;

        this.setLayout(new GridBagLayout());
        this.setBackground(new Color(34, 139, 34)); // Match grass green

        // Main Dialog Box
        JPanel dialogBox = new JPanel(new GridBagLayout());
        dialogBox.setBackground(new Color(245, 245, 245, 230));
        dialogBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.BLACK, 4),
                BorderFactory.createEmptyBorder(30, 50, 30, 50)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        // 1. Welcome Header
        welcomeLabel = new JLabel("WELCOME, TRAINER", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Monospaced", Font.BOLD, 26));
        gbc.gridy = 0;
        dialogBox.add(welcomeLabel, gbc);

        statsSummaryLabel = new JLabel("Best Score: 0 | Stage: 1 | Avg WPM: 0", SwingConstants.CENTER);
        statsSummaryLabel.setFont(new Font("Monospaced", Font.PLAIN, 18));
        gbc.gridy = 1;
        dialogBox.add(statsSummaryLabel, gbc);

        detailArea = new JTextArea(8, 28);
        detailArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setBackground(new Color(235, 235, 235));
        detailArea.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        gbc.gridy = 2;
        dialogBox.add(new JScrollPane(detailArea), gbc);

        JButton continueBtn = createStyledButton("CONTINUE JOURNEY", Color.BLACK, Color.WHITE);
        continueBtn.addActionListener(e -> mainApp.continueGame(username));
        gbc.gridy = 3;
        dialogBox.add(continueBtn, gbc);

        JButton selectLevelBtn = createStyledButton("SELECT STAGE", new Color(70, 130, 180), Color.WHITE);
        selectLevelBtn.addActionListener(e -> mainApp.startGame(username));
        gbc.gridy = 4;
        dialogBox.add(selectLevelBtn, gbc);

        JButton statsBtn = createStyledButton("VIEW FULL STATS", new Color(90, 110, 170), Color.WHITE);
        statsBtn.addActionListener(e -> mainApp.showStatsPage(username));
        gbc.gridy = 5;
        dialogBox.add(statsBtn, gbc);

        JButton logoutBtn = createStyledButton("LOGOUT", new Color(178, 34, 34), Color.WHITE);
        logoutBtn.addActionListener(e -> cardLayout.show(container, "MENU"));
        gbc.gridy = 6;
        dialogBox.add(logoutBtn, gbc);

        JButton deleteBtn = new JButton("Delete Save");
        deleteBtn.setFont(new Font("Monospaced", Font.ITALIC, 12));
        deleteBtn.setContentAreaFilled(false);
        deleteBtn.setBorderPainted(false);
        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Delete all data for " + username + "?", "Warning", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                ProfileStore.deleteAccount(username);
                cardLayout.show(container, "MENU");
            }
        });
        gbc.gridy = 7;
        dialogBox.add(deleteBtn, gbc);

        add(dialogBox);
    }

    public void setUserData(String username) {
        this.username = username;
        this.welcomeLabel.setText("WELCOME, " + username.toUpperCase());

        ProfileStore.ProfileData profile = ProfileStore.loadProfile(username);
        this.statsSummaryLabel.setText(String.format(
                "Best Score: %d | Stage: %d | Avg WPM: %.1f",
                profile.highScore,
                profile.unlockedLevel,
                profile.averageWpm
        ));

        StringBuilder builder = new StringBuilder();
        builder.append("Accuracy: ").append(String.format("%.1f%%", profile.accuracyPercent)).append('\n');
        builder.append("Peak WPM: ").append(String.format("%.1f", profile.peakWpm)).append('\n');
        builder.append("Words Typed: ").append(profile.wordsTyped).append('\n');
        builder.append("Errors: ").append(profile.errorCount).append('\n');
        builder.append("Longest Streak: ").append(profile.longestStreak).append('\n');
        builder.append("Teacher Tips:\n");
        for (String tip : profile.recommendations) {
            builder.append("- ").append(tip).append('\n');
        }
        if (!profile.achievements.isEmpty()) {
            builder.append("\nBadges:\n");
            for (String achievement : profile.achievements) {
                builder.append("* ").append(achievement).append('\n');
            }
        }
        detailArea.setText(builder.toString());
        detailArea.setCaretPosition(0);
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Monospaced", Font.BOLD, 18));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
