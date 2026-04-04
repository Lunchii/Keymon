import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;

/**
 * Panel for resetting typing statistics for existing accounts.
 *
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
public class ResetStatsPanel extends JPanel {
    private Image backgroundGif;
    private Font customFont;
    private CardLayout cardLayout;
    private JPanel container;
    private Main mainApp;

    private JTextField usernameField;

    public ResetStatsPanel(CardLayout layout, JPanel container, Main mainApp) {
        this.cardLayout = layout;
        this.container = container;
        this.mainApp = mainApp;

        backgroundGif = new ImageIcon("images/menuBackground.gif").getImage();
        loadCustomFont();

        initializeComponents();
    }

    private void initializeComponents() {
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        OutlinedLabel titleLabel = new OutlinedLabel("RESET STATISTICS");
        titleLabel.setFont(customFont.deriveFont(Font.BOLD, 48f));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(titleLabel, BorderLayout.NORTH);

        // Center panel
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        // Warning message
        OutlinedLabel warningLabel = new OutlinedLabel("WARNING: This will permanently reset all typing statistics!");
        warningLabel.setFont(customFont.deriveFont(Font.BOLD, 20f));
        warningLabel.setForeground(Color.RED);
        warningLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        OutlinedLabel warningLabel2 = new OutlinedLabel("This includes WPM, accuracy, words typed, and all progress.");
        warningLabel2.setFont(customFont.deriveFont(Font.PLAIN, 18f));
        warningLabel2.setForeground(Color.RED);
        warningLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username field
        OutlinedLabel usernameLabel = new OutlinedLabel("Username:");
        usernameLabel.setFont(customFont.deriveFont(Font.PLAIN, 24f));
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        usernameField = new JTextField(20);
        usernameField.setFont(customFont.deriveFont(Font.PLAIN, 20f));
        usernameField.setMaximumSize(new Dimension(300, 40));
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Reset button
        OutlinedButton resetBtn = new OutlinedButton("RESET STATISTICS");
        resetBtn.setFont(customFont.deriveFont(Font.PLAIN, 24f));
        resetBtn.setBackground(new Color(255, 100, 100));
        resetBtn.setForeground(Color.BLACK);
        resetBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetBtn.addActionListener(e -> resetStats());

        centerPanel.add(warningLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerPanel.add(warningLabel2);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        centerPanel.add(usernameLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerPanel.add(usernameField);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        centerPanel.add(resetBtn);

        this.add(centerPanel, BorderLayout.CENTER);

        // Back button
        OutlinedButton backBtn = new OutlinedButton("BACK");
        backBtn.setFont(customFont.deriveFont(Font.PLAIN, 24f));
        backBtn.setBackground(new Color(150, 150, 150));
        backBtn.setForeground(Color.BLACK);
        backBtn.addActionListener(e -> cardLayout.show(container, "ACCOUNT_MANAGEMENT"));

        JPanel backPanel = new JPanel();
        backPanel.setOpaque(false);
        backPanel.add(backBtn);
        this.add(backPanel, BorderLayout.SOUTH);

        // Add key listener for Enter key
        usernameField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    resetStats();
                }
            }
        });
    }

    private void resetStats() {
        String username = usernameField.getText().trim();

        // Validation
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            usernameField.requestFocus();
            return;
        }

        // Check if account exists
        if (!ProfileStore.accountExists(username)) {
            JOptionPane.showMessageDialog(this, "Account '" + username + "' does not exist.", "Account Not Found", JOptionPane.ERROR_MESSAGE);
            usernameField.requestFocus();
            return;
        }

        // Double confirmation
        int result1 = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to reset ALL typing statistics for '" + username + "'?\n\nThis will permanently delete their progress and cannot be undone!",
            "Confirm Statistics Reset",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (result1 == JOptionPane.YES_OPTION) {
            // Second confirmation
            int result2 = JOptionPane.showConfirmDialog(this,
                "FINAL WARNING: This action cannot be undone.\n\nType 'RESET' to confirm:",
                "Final Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE);

            if (result2 == JOptionPane.YES_OPTION) {
                boolean success = ProfileStore.resetStatistics(username);
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "All typing statistics for '" + username + "' have been reset.",
                        "Statistics Reset Successful",
                        JOptionPane.INFORMATION_MESSAGE);

                    // Clear field
                    usernameField.setText("");

                    // Go back to account management
                    cardLayout.show(container, "ACCOUNT_MANAGEMENT");
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Failed to reset statistics. Please try again.",
                        "Statistics Reset Failed",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void loadCustomFont() {
        try {
            File fontFile = new File("fonts/ka1.ttf");
            customFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(customFont);
        } catch (Exception e) {
            customFont = new Font("Arial", Font.BOLD, 24);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundGif != null) {
            g.drawImage(backgroundGif, 0, 0, getWidth(), getHeight(), this);
        }
    }
}