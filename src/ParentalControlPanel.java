import java.awt.*;
import java.util.List;
import javax.swing.*;

/**
 * Password-protected parental and teacher controls screen.
 */
public class ParentalControlPanel extends JPanel {
    private final CardLayout cardLayout;
    private final JPanel container;
    private final Main mainApp;
    private final CardLayout localCards;
    private final JPanel localContainer;
    private final DefaultListModel<String> playerListModel;
    private final JList<String> playerList;
    private final JTextArea profileArea;
    private final JTextField createUserField;
    private final JPasswordField createPasswordField;
    private final JPasswordField resetPasswordField;

    public ParentalControlPanel(CardLayout cardLayout, JPanel container, Main mainApp) {
        this.cardLayout = cardLayout;
        this.container = container;
        this.mainApp = mainApp;
        this.localCards = new CardLayout();
        this.localContainer = new JPanel(localCards);
        this.playerListModel = new DefaultListModel<>();
        this.playerList = new JList<>(playerListModel);
        this.profileArea = new JTextArea();
        this.createUserField = new JTextField();
        this.createPasswordField = new JPasswordField();
        this.resetPasswordField = new JPasswordField();

        setLayout(new BorderLayout());
        localContainer.add(buildGatePanel(), "LOCKED");
        localContainer.add(buildControlPanel(), "UNLOCKED");
        add(localContainer, BorderLayout.CENTER);
        localCards.show(localContainer, "LOCKED");
    }

    private JPanel buildGatePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(28, 34, 45));

        JPanel box = new JPanel(new GridBagLayout());
        box.setBackground(new Color(245, 245, 245, 235));
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 4),
                BorderFactory.createEmptyBorder(24, 28, 24, 28)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("PARENTAL / TEACHER CONTROLS", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 22));
        gbc.gridy = 0;
        box.add(title, gbc);

        JLabel message = new JLabel("<html><div style='text-align:center;'>Enter the teacher PIN to manage accounts,<br/>review progress, and reset the high score table.</div></html>", SwingConstants.CENTER);
        message.setFont(new Font("SansSerif", Font.PLAIN, 14));
        gbc.gridy = 1;
        box.add(message, gbc);

        JPasswordField pinField = new JPasswordField(12);
        pinField.setFont(new Font("Monospaced", Font.PLAIN, 18));
        pinField.setBorder(BorderFactory.createTitledBorder("Teacher PIN"));
        gbc.gridy = 2;
        box.add(pinField, gbc);

        JButton unlockButton = new JButton("UNLOCK CONTROLS");
        unlockButton.setFont(new Font("Monospaced", Font.BOLD, 16));
        unlockButton.addActionListener(e -> {
            if (!ProfileStore.verifyParentPin(new String(pinField.getPassword()))) {
                JOptionPane.showMessageDialog(this, "That PIN was not correct.");
                return;
            }
            refreshPlayers();
            localCards.show(localContainer, "UNLOCKED");
        });
        gbc.gridy = 3;
        box.add(unlockButton, gbc);

        JButton backButton = new JButton("BACK TO MENU");
        backButton.setFont(new Font("Monospaced", Font.BOLD, 16));
        backButton.addActionListener(e -> cardLayout.show(container, "MENU"));
        gbc.gridy = 4;
        box.add(backButton, gbc);

        panel.add(box);
        return panel;
    }

    private JPanel buildControlPanel() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(new Color(16, 28, 22));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel title = new JLabel("Teacher Dashboard");
        title.setFont(new Font("Monospaced", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        topBar.add(title, BorderLayout.WEST);

        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topButtons.setOpaque(false);

        JButton refreshButton = new JButton("REFRESH");
        refreshButton.addActionListener(e -> refreshPlayers());
        topButtons.add(refreshButton);

        JButton lockButton = new JButton("LOCK");
        lockButton.addActionListener(e -> localCards.show(localContainer, "LOCKED"));
        topButtons.add(lockButton);

        JButton menuButton = new JButton("MENU");
        menuButton.addActionListener(e -> cardLayout.show(container, "MENU"));
        topButtons.add(menuButton);

        topBar.add(topButtons, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        playerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playerList.setFont(new Font("Monospaced", Font.PLAIN, 15));
        playerList.addListSelectionListener(e -> refreshSelectedProfile());

        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setBackground(new Color(235, 240, 235));
        left.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 3),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        left.add(new JLabel("Players", SwingConstants.CENTER), BorderLayout.NORTH);
        left.add(new JScrollPane(playerList), BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Profile", buildProfileTab());
        tabs.addTab("Accounts", buildAccountTab());
        tabs.addTab("System", buildSystemTab());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, tabs);
        splitPane.setDividerLocation(220);
        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildProfileTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        profileArea.setEditable(false);
        profileArea.setLineWrap(true);
        profileArea.setWrapStyleWord(true);
        profileArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        panel.add(new JScrollPane(profileArea), BorderLayout.CENTER);

        JButton openAsPlayerButton = new JButton("OPEN SELECTED PLAYER HUB");
        openAsPlayerButton.addActionListener(e -> {
            String selected = playerList.getSelectedValue();
            if (selected != null) {
                mainApp.showPlayerScreen(selected);
            }
        });
        panel.add(openAsPlayerButton, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildAccountTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel createLabel = new JLabel("Create Account");
        createLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        gbc.gridy = 0;
        panel.add(createLabel, gbc);

        createUserField.setBorder(BorderFactory.createTitledBorder("Username"));
        gbc.gridy = 1;
        panel.add(createUserField, gbc);

        createPasswordField.setBorder(BorderFactory.createTitledBorder("Password"));
        gbc.gridy = 2;
        panel.add(createPasswordField, gbc);

        JButton createButton = new JButton("CREATE ACCOUNT");
        createButton.addActionListener(e -> createAccount());
        gbc.gridy = 3;
        panel.add(createButton, gbc);

        JLabel resetLabel = new JLabel("Reset Selected Password");
        resetLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        gbc.gridy = 4;
        panel.add(resetLabel, gbc);

        resetPasswordField.setBorder(BorderFactory.createTitledBorder("New Password"));
        gbc.gridy = 5;
        panel.add(resetPasswordField, gbc);

        JButton resetButton = new JButton("RESET PASSWORD");
        resetButton.addActionListener(e -> resetSelectedPassword());
        gbc.gridy = 6;
        panel.add(resetButton, gbc);

        JButton resetStatsButton = new JButton("RESET SELECTED STATISTICS");
        resetStatsButton.addActionListener(e -> resetSelectedStatistics());
        gbc.gridy = 7;
        panel.add(resetStatsButton, gbc);

        JButton deleteButton = new JButton("DELETE SELECTED ACCOUNT");
        deleteButton.addActionListener(e -> deleteSelectedAccount());
        gbc.gridy = 8;
        panel.add(deleteButton, gbc);

        return panel;
    }

    private JPanel buildSystemTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        infoArea.setText("Use this panel to reset the global high score table back to the built-in default scores.\n\nThis does not delete accounts. It only refreshes the leaderboard shown on the High Scores screen.");
        panel.add(infoArea, BorderLayout.CENTER);

        JButton resetTableButton = new JButton("RESET HIGH SCORE TABLE");
        resetTableButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Reset the high score table to the default records?",
                    "Confirm Reset",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                ProfileStore.resetHighScoreTable();
                JOptionPane.showMessageDialog(this, "The high score table was reset.");
            }
        });
        panel.add(resetTableButton, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshPlayers() {
        List<String> usernames = ProfileStore.getAllUsernames();
        playerListModel.clear();
        for (String username : usernames) {
            playerListModel.addElement(username);
        }
        if (!playerListModel.isEmpty()) {
            playerList.setSelectedIndex(0);
        } else {
            profileArea.setText("No player accounts found yet.");
        }
    }

    private void refreshSelectedProfile() {
        String selected = playerList.getSelectedValue();
        if (selected == null) {
            profileArea.setText("Select a player to inspect their statistics and progress.");
            return;
        }

        ProfileStore.ProfileData profile = ProfileStore.loadProfile(selected);
        StringBuilder builder = new StringBuilder();
        builder.append("Username: ").append(profile.username).append('\n');
        builder.append("High Score: ").append(profile.highScore).append('\n');
        builder.append("Current Stage: ").append(profile.currentLevel).append('\n');
        builder.append("Highest Unlocked Stage: ").append(profile.unlockedLevel).append('\n');
        builder.append("Average WPM: ").append(String.format("%.1f", profile.averageWpm)).append('\n');
        builder.append("Peak WPM: ").append(String.format("%.1f", profile.peakWpm)).append('\n');
        builder.append("Accuracy: ").append(String.format("%.1f%%", profile.accuracyPercent)).append('\n');
        builder.append("Error Count: ").append(profile.errorCount).append('\n');
        builder.append("Words Typed: ").append(profile.wordsTyped).append('\n');
        builder.append("Longest Streak: ").append(profile.longestStreak).append('\n');
        builder.append("Powerups Used: ").append(profile.powerupsUsed).append('\n');
        builder.append("Games Played: ").append(profile.gamesPlayed).append('\n');
        builder.append("Total Time Played: ").append(formatDuration(profile.totalTimePlayedSeconds)).append('\n');

        if (!profile.achievements.isEmpty()) {
            builder.append("\nAchievements:\n");
            for (String achievement : profile.achievements) {
                builder.append("- ").append(achievement).append('\n');
            }
        }

        if (!profile.recommendations.isEmpty()) {
            builder.append("\nRecommendations:\n");
            for (String tip : profile.recommendations) {
                builder.append("- ").append(tip).append('\n');
            }
        }

        profileArea.setText(builder.toString());
        profileArea.setCaretPosition(0);
    }

    private void createAccount() {
        String username = ProfileStore.sanitizeUsername(createUserField.getText());
        String password = new String(createPasswordField.getPassword());
        String validation = ProfileStore.validateNewAccount(username, password);
        if (validation != null) {
            JOptionPane.showMessageDialog(this, validation);
            return;
        }

        if (!ProfileStore.createAccount(username, password)) {
            JOptionPane.showMessageDialog(this, "The account could not be created.");
            return;
        }

        JOptionPane.showMessageDialog(this, "Account created for " + username + ".");
        createUserField.setText("");
        createPasswordField.setText("");
        refreshPlayers();
    }

    private void resetSelectedPassword() {
        String selected = playerList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a player first.");
            return;
        }

        String newPassword = new String(resetPasswordField.getPassword());
        if (!ProfileStore.resetPassword(selected, newPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords must be at least 4 characters.");
            return;
        }

        resetPasswordField.setText("");
        JOptionPane.showMessageDialog(this, "Password updated for " + selected + ".");
    }

    private void resetSelectedStatistics() {
        String selected = playerList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a player first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Reset all progress and statistics for " + selected + "?",
                "Confirm Reset",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION && ProfileStore.resetStatistics(selected)) {
            JOptionPane.showMessageDialog(this, "Statistics reset for " + selected + ".");
            refreshSelectedProfile();
        }
    }

    private void deleteSelectedAccount() {
        String selected = playerList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a player first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete the account for " + selected + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION && ProfileStore.deleteAccount(selected)) {
            JOptionPane.showMessageDialog(this, selected + " was deleted.");
            refreshPlayers();
        }
    }

    private String formatDuration(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
    }
}
