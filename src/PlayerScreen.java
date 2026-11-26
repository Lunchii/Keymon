import java.awt.*;
import javax.swing.*;

public class PlayerScreen extends JPanel {
    private Player player;
    private CardLayout layout;
    private JPanel container;

    public PlayerScreen(CardLayout layout, JPanel container, Player player) {
        this.layout = layout;
        this.container = container;
        this.player = player;

        this.setLayout(new BorderLayout());
        this.setBackground(new Color(20, 20, 20));

        // 1. Title at top
        JLabel title = new JLabel("PLAYER PROFILE", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 60));
        title.setForeground(Color.CYAN);
        title.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));
        this.add(title, BorderLayout.NORTH);

        // 2. Centered Sprite Display (Fixed Position)
        JPanel centerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (player != null && player.getCurrentFrame() != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    // Disable smoothing for sharp pixel art
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                    int scale = 6; // 6x magnification
                    int w = player.getCurrentFrame().getWidth() * scale;
                    int h = player.getCurrentFrame().getHeight() * scale;
                    int x = (getWidth() - w) / 2;
                    int y = (getHeight() - h) / 2;

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

        JButton continueBtn = createStyledButton("CONTINUE JOURNEY", new Color(70, 190, 255), Color.BLACK);
        continueBtn.addActionListener(e -> mainApp.continueGame(username));
        gbc.gridy = 3;
        dialogBox.add(continueBtn, gbc);

        JButton newGameBtn = createStyledButton("NEW GAME", new Color(40, 160, 80), Color.BLACK);
        newGameBtn.addActionListener(e -> mainApp.startNewGame(username));
        gbc.gridy = 4;
        dialogBox.add(newGameBtn, gbc);

        JButton selectLevelBtn = createStyledButton("SELECT STAGE", new Color(70, 130, 180), Color.BLACK);
        selectLevelBtn.addActionListener(e -> mainApp.startGame(username));
        gbc.gridy = 5;
        dialogBox.add(selectLevelBtn, gbc);

        JButton statsBtn = createStyledButton("VIEW FULL STATS", new Color(90, 110, 170), Color.BLACK);
        statsBtn.addActionListener(e -> mainApp.showStatsPage(username));
        gbc.gridy = 6;
        dialogBox.add(statsBtn, gbc);

        JButton logoutBtn = createStyledButton("LOGOUT", new Color(178, 34, 34), Color.BLACK);
        logoutBtn.addActionListener(e -> cardLayout.show(container, "MENU"));
        gbc.gridy = 7;
        dialogBox.add(logoutBtn, gbc);

        JButton deleteBtn = new JButton("Delete Save");
        deleteBtn.setFont(new Font("Monospaced", Font.ITALIC, 12));
        deleteBtn.setContentAreaFilled(false);
        deleteBtn.setBorderPainted(false);
        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Delete all data for " + username + "?", "Warning",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                ProfileStore.deleteAccount(username);
                cardLayout.show(container, "MENU");
            }
        };
        centerPanel.setOpaque(false);
        this.add(centerPanel, BorderLayout.CENTER);

        // 3. Back Button at bottom
        JButton backButton = new JButton("BACK TO GAME");
        backButton.setFont(new Font("Arial", Font.BOLD, 25));
        backButton.addActionListener(e -> {
            layout.show(container, "PLAY GAME");
            // FIX for Test 3: Re-gain focus so WASD works immediately
            container.getComponent(1).requestFocusInWindow();
        });
        gbc.gridy = 8;
        dialogBox.add(deleteBtn, gbc);

        JPanel southPanel = new JPanel();
        southPanel.setOpaque(false);
        southPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 50, 0));
        southPanel.add(backButton);
        this.add(southPanel, BorderLayout.SOUTH);
    }
}