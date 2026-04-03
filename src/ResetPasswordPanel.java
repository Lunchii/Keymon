import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;

/**
 * Panel for resetting passwords for existing accounts.
 *
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
public class ResetPasswordPanel extends JPanel {
    private Image backgroundGif;
    private Font customFont;
    private CardLayout cardLayout;
    private JPanel container;
    private Main mainApp;

    private JTextField usernameField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    public ResetPasswordPanel(CardLayout layout, JPanel container, Main mainApp) {
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
        OutlinedLabel titleLabel = new OutlinedLabel("RESET PASSWORD");
        titleLabel.setFont(customFont.deriveFont(Font.BOLD, 48f));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(titleLabel, BorderLayout.NORTH);

        // Center panel
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

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

        // New password field
        OutlinedLabel newPasswordLabel = new OutlinedLabel("New Password:");
        newPasswordLabel.setFont(customFont.deriveFont(Font.PLAIN, 24f));
        newPasswordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        newPasswordField = new JPasswordField(20);
        newPasswordField.setFont(customFont.deriveFont(Font.PLAIN, 20f));
        newPasswordField.setMaximumSize(new Dimension(300, 40));
        newPasswordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        newPasswordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Confirm password field
        OutlinedLabel confirmPasswordLabel = new OutlinedLabel("Confirm Password:");
        confirmPasswordLabel.setFont(customFont.deriveFont(Font.PLAIN, 24f));
        confirmPasswordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(customFont.deriveFont(Font.PLAIN, 20f));
        confirmPasswordField.setMaximumSize(new Dimension(300, 40));
        confirmPasswordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Reset button
        OutlinedButton resetBtn = new OutlinedButton("RESET PASSWORD");
        resetBtn.setFont(customFont.deriveFont(Font.PLAIN, 24f));
        resetBtn.setBackground(new Color(255, 165, 0));
        resetBtn.setForeground(Color.BLACK);
        resetBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetBtn.addActionListener(e -> resetPassword());

        centerPanel.add(usernameLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerPanel.add(usernameField);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(newPasswordLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerPanel.add(newPasswordField);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(confirmPasswordLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerPanel.add(confirmPasswordField);
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

        // Add key listeners for Enter key
        KeyAdapter enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    resetPassword();
                }
            }
        };

        usernameField.addKeyListener(enterKeyListener);
        newPasswordField.addKeyListener(enterKeyListener);
        confirmPasswordField.addKeyListener(enterKeyListener);
    }

    private void resetPassword() {
        String username = usernameField.getText().trim();
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Validation
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            usernameField.requestFocus();
            return;
        }

        if (newPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "New password cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            newPasswordField.requestFocus();
            return;
        }

        if (newPassword.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password must be at least 4 characters long.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            newPasswordField.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            confirmPasswordField.requestFocus();
            return;
        }

        // Check if account exists
        if (!ProfileStore.accountExists(username)) {
            JOptionPane.showMessageDialog(this, "Account '" + username + "' does not exist.", "Account Not Found", JOptionPane.ERROR_MESSAGE);
            usernameField.requestFocus();
            return;
        }

        // Confirm reset
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to reset the password for '" + username + "'?",
            "Confirm Password Reset",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            boolean success = ProfileStore.resetPassword(username, newPassword);
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Password for '" + username + "' has been reset successfully!",
                    "Password Reset Successful",
                    JOptionPane.INFORMATION_MESSAGE);

                // Clear fields
                usernameField.setText("");
                newPasswordField.setText("");
                confirmPasswordField.setText("");

                // Go back to account management
                cardLayout.show(container, "ACCOUNT_MANAGEMENT");
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to reset password. Please try again.",
                    "Password Reset Failed",
                    JOptionPane.ERROR_MESSAGE);
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