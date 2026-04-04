import java.awt.*;
import java.io.File;
import javax.swing.*;

/**
 * Panel for viewing account details and performing account management actions.
 *
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
public class AccountDetailsPanel extends JPanel {
    private Image backgroundGif;
    private Font customFont;
    private CardLayout cardLayout;
    private JPanel container;
    private Main mainApp;
    private String username;

    public AccountDetailsPanel(CardLayout layout, JPanel container, Main mainApp, String username) {
        this.cardLayout = layout;
        this.container = container;
        this.mainApp = mainApp;
        this.username = username;

        backgroundGif = new ImageIcon("images/menuBackground.gif").getImage();
        loadCustomFont();

        initializeComponents();
    }

    private void initializeComponents() {
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        OutlinedLabel titleLabel = new OutlinedLabel("ACCOUNT DETAILS: " + username.toUpperCase());
        titleLabel.setFont(customFont.deriveFont(Font.BOLD, 36f));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(titleLabel, BorderLayout.NORTH);

        // Center panel
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        // Account info
        OutlinedLabel usernameLabel = new OutlinedLabel("Username: " + username);
        usernameLabel.setFont(customFont.deriveFont(Font.PLAIN, 24f));
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        OutlinedLabel profileExistsLabel = new OutlinedLabel("Profile Exists: " + (ProfileStore.accountExists(username) ? "Yes" : "No"));
        profileExistsLabel.setFont(customFont.deriveFont(Font.PLAIN, 24f));
        profileExistsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        OutlinedLabel statsExistsLabel = new OutlinedLabel("Stats Available: " + (ProfileStore.loadProfile(username) != null ? "Yes" : "No"));
        statsExistsLabel.setFont(customFont.deriveFont(Font.PLAIN, 24f));
        statsExistsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(usernameLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        centerPanel.add(profileExistsLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        centerPanel.add(statsExistsLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        // Action buttons
        OutlinedButton resetPasswordBtn = new OutlinedButton("RESET PASSWORD");
        resetPasswordBtn.setFont(customFont.deriveFont(Font.PLAIN, 20f));
        resetPasswordBtn.setBackground(new Color(255, 165, 0));
        resetPasswordBtn.setForeground(Color.BLACK);
        resetPasswordBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetPasswordBtn.addActionListener(e -> resetPassword());

        OutlinedButton resetStatsBtn = new OutlinedButton("RESET STATISTICS");
        resetStatsBtn.setFont(customFont.deriveFont(Font.PLAIN, 20f));
        resetStatsBtn.setBackground(new Color(255, 100, 100));
        resetStatsBtn.setForeground(Color.BLACK);
        resetStatsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetStatsBtn.addActionListener(e -> resetStats());

        OutlinedButton deleteAccountBtn = new OutlinedButton("DELETE ACCOUNT");
        deleteAccountBtn.setFont(customFont.deriveFont(Font.PLAIN, 20f));
        deleteAccountBtn.setBackground(new Color(200, 50, 50));
        deleteAccountBtn.setForeground(Color.WHITE);
        deleteAccountBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteAccountBtn.addActionListener(e -> deleteAccount());

        centerPanel.add(resetPasswordBtn);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        centerPanel.add(resetStatsBtn);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        centerPanel.add(deleteAccountBtn);

        this.add(centerPanel, BorderLayout.CENTER);

        // Back button
        OutlinedButton backBtn = new OutlinedButton("BACK TO LIST");
        backBtn.setFont(customFont.deriveFont(Font.PLAIN, 24f));
        backBtn.setBackground(new Color(150, 150, 150));
        backBtn.setForeground(Color.BLACK);
        backBtn.addActionListener(e -> cardLayout.show(container, "ACCOUNT_LIST"));

        JPanel backPanel = new JPanel();
        backPanel.setOpaque(false);
        backPanel.add(backBtn);
        this.add(backPanel, BorderLayout.SOUTH);
    }

    private void resetPassword() {
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to reset the password for '" + username + "'?\n\nThis will set the password to 'password'.",
            "Confirm Password Reset",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            boolean success = ProfileStore.resetPassword(username, "password");
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Password for '" + username + "' has been reset to 'password'.",
                    "Password Reset Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to reset password for '" + username + "'.",
                    "Password Reset Failed",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void resetStats() {
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to reset all typing statistics for '" + username + "'?\n\nThis action cannot be undone.",
            "Confirm Statistics Reset",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            boolean success = ProfileStore.resetStatistics(username);
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Typing statistics for '" + username + "' have been reset.",
                    "Statistics Reset Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to reset statistics for '" + username + "'.",
                    "Statistics Reset Failed",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteAccount() {
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to DELETE the account '" + username + "'?\n\nThis will permanently remove the profile and all associated data.\nThis action cannot be undone!",
            "Confirm Account Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            // Double confirmation
            String confirmation = JOptionPane.showInputDialog(this,
                "Type 'DELETE' to confirm account deletion:",
                "Final Confirmation",
                JOptionPane.WARNING_MESSAGE);

            if ("DELETE".equals(confirmation)) {
                boolean success = ProfileStore.deleteAccount(username);
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Account '" + username + "' has been permanently deleted.",
                        "Account Deleted",
                        JOptionPane.INFORMATION_MESSAGE);
                    // Go back to account list
                    cardLayout.show(container, "ACCOUNT_LIST");
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Failed to delete account '" + username + "'.",
                        "Deletion Failed",
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Account deletion cancelled.",
                    "Cancelled",
                    JOptionPane.INFORMATION_MESSAGE);
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