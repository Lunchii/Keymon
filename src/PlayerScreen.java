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

        // 2. Quick Stats Summary
        statsSummaryLabel = new JLabel("Best Score: 0 | Level: 1", SwingConstants.CENTER);
        statsSummaryLabel.setFont(new Font("Monospaced", Font.PLAIN, 18));
        gbc.gridy = 1;
        dialogBox.add(statsSummaryLabel, gbc);

        // 3. START GAME Button
        JButton startBtn = createStyledButton("START GAME", Color.BLACK, Color.WHITE);
        startBtn.addActionListener(e -> mainApp.startGame(username));
        gbc.gridy = 2;
        dialogBox.add(startBtn, gbc);

        // 4. VIEW STATS Button (Isaac's Request)
        JButton statsBtn = createStyledButton("VIEW STATS", new Color(70, 130, 180), Color.WHITE);
        statsBtn.addActionListener(e -> {
            // Logic: Refresh and show the standalone Stats Panel
            mainApp.showStatsPage(username);
        });
        gbc.gridy = 3;
        dialogBox.add(statsBtn, gbc);

        // 5. LOGOUT Button
        JButton logoutBtn = createStyledButton("LOGOUT", new Color(178, 34, 34), Color.WHITE);
        logoutBtn.addActionListener(e -> cardLayout.show(container, "MENU"));
        gbc.gridy = 4;
        dialogBox.add(logoutBtn, gbc);

        // 6. DELETE ACCOUNT
        JButton deleteBtn = new JButton("Delete Save");
        deleteBtn.setFont(new Font("Monospaced", Font.ITALIC, 12));
        deleteBtn.setContentAreaFilled(false);
        deleteBtn.setBorderPainted(false);
        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Delete all data for " + username + "?", "Warning", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                SaveManager.deleteAccount(username);
                cardLayout.show(container, "MENU");
            }
        });
        gbc.gridy = 5;
        dialogBox.add(deleteBtn, gbc);

        add(dialogBox);
    }

    public void setUserData(String username) {
        this.username = username;
        this.welcomeLabel.setText("WELCOME, " + username.toUpperCase());

        SaveManager.PlayerData data = SaveManager.loadGame(username);
        this.statsSummaryLabel.setText("Best Score: " + data.highScore + " | Level: " + data.currentLevel);
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