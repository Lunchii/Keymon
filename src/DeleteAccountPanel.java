import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;

/**
 * Panel for deleting existing accounts permanently.
 *
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
public class DeleteAccountPanel extends JPanel {
    private Image backgroundGif;
    private Font customFont;
    private CardLayout cardLayout;
    private JPanel container;
    private Main mainApp;

    private JTextField usernameField;
    private JTextField confirmationField;

    public DeleteAccountPanel(CardLayout layout, JPanel container, Main mainApp) {
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
        OutlinedLabel titleLabel = new OutlinedLabel("DELETE ACCOUNT");
        titleLabel.setFont(customFont.deriveFont(Font.BOLD, 48f));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.add(titleLabel, BorderLayout.NORTH);

        // Center panel
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        // Warning messages
        OutlinedLabel warningLabel = new OutlinedLabel("DANGER: This action is permanent!");
        warningLabel.setFont(customFont.deriveFont(Font.BOLD, 22f));
        warningLabel.setForeground(Color.RED);
        warningLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        OutlinedLabel warningLabel2 = new OutlinedLabel("The account and ALL associated data will be deleted.");
        warningLabel2.setFont(customFont.deriveFont(Font.PLAIN, 18f));
        warningLabel2.setForeground(Color.RED);
        warningLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);

        OutlinedLabel warningLabel3 = new OutlinedLabel("This includes profiles, statistics, progress, and inventory.");
        warningLabel3.setFont(customFont.deriveFont(Font.PLAIN, 16f));
        warningLabel3.setForeground(Color.RED);
        warningLabel3.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username field
        OutlinedLabel usernameLabel = new OutlinedLabel("Username to Delete:");
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

        // Confirmation field
        OutlinedLabel confirmationLabel = new OutlinedLabel("Type 'DELETE' to confirm:");
        confirmationLabel.setFont(customFont.deriveFont(Font.PLAIN, 20f));
        confirmationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        confirmationField = new JTextField(20);
        confirmationField.setFont(customFont.deriveFont(Font.PLAIN, 20f));
        confirmationField.setMaximumSize(new Dimension(300, 40));
        confirmationField.setAlignmentX(Component.CENTER_ALIGNMENT);
        confirmationField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.RED, 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Delete button
        OutlinedButton deleteBtn = new OutlinedButton("DELETE ACCOUNT");
        deleteBtn.setFont(customFont.deriveFont(Font.PLAIN, 24f));
        deleteBtn.setBackground(new Color(200, 50, 50));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteBtn.addActionListener(e -> deleteAccount());

        centerPanel.add(warningLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerPanel.add(warningLabel2);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerPanel.add(warningLabel3);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        centerPanel.add(usernameLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerPanel.add(usernameField);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(confirmationLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerPanel.add(confirmationField);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        centerPanel.add(deleteBtn);

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
                    deleteAccount();
                }
            }
        };

        usernameField.addKeyListener(enterKeyListener);
        confirmationField.addKeyListener(enterKeyListener);
    }

    private void deleteAccount() {
        String username = usernameField.getText().trim();
        String confirmation = confirmationField.getText().trim();

        // Validation
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            usernameField.requestFocus();
            return;
        }

        if (!"DELETE".equals(confirmation)) {
            JOptionPane.showMessageDialog(this, "You must type 'DELETE' to confirm.", "Confirmation Required", JOptionPane.ERROR_MESSAGE);
            confirmationField.requestFocus();
            return;
        }

        // Check if account exists
        if (!ProfileStore.accountExists(username)) {
            JOptionPane.showMessageDialog(this, "Account '" + username + "' does not exist.", "Account Not Found", JOptionPane.ERROR_MESSAGE);
            usernameField.requestFocus();
            return;
        }

        // Triple confirmation
        int result1 = JOptionPane.showConfirmDialog(this,
            "Are you absolutely sure you want to PERMANENTLY DELETE the account '" + username + "'?\n\nThis will erase ALL data including:\n- Profile information\n- Typing statistics\n- Game progress\n- Inventory items\n\nThis action CANNOT be undone!",
            "Final Confirmation - Step 1",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE);

        if (result1 == JOptionPane.YES_OPTION) {
            int result2 = JOptionPane.showConfirmDialog(this,
                "This is your LAST CHANCE to cancel.\n\nAccount '" + username + "' will be completely removed from the system.\n\nAre you certain?",
                "Final Confirmation - Step 2",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE);

            if (result2 == JOptionPane.YES_OPTION) {
                boolean success = ProfileStore.deleteAccount(username);
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Account '" + username + "' has been permanently deleted from the system.",
                        "Account Deleted",
                        JOptionPane.INFORMATION_MESSAGE);

                    // Clear fields
                    usernameField.setText("");
                    confirmationField.setText("");

                    // Go back to account management
                    cardLayout.show(container, "ACCOUNT_MANAGEMENT");
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Failed to delete account. Please try again.",
                        "Deletion Failed",
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