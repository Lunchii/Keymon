import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;

/**
 * Panel for creating new accounts with username and password validation.
 *
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
public class CreateAccountPanel extends JPanel {
    private Image backgroundGif;
    private Font customFont;
    private CardLayout cardLayout;
    private JPanel container;
    private Main mainApp;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    public CreateAccountPanel(CardLayout layout, JPanel container, Main mainApp) {
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
        OutlinedLabel titleLabel = new OutlinedLabel("CREATE NEW ACCOUNT");
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

        // Password field
        OutlinedLabel passwordLabel = new OutlinedLabel("Password:");
        passwordLabel.setFont(customFont.deriveFont(Font.PLAIN, 24f));
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        passwordField = new JPasswordField(20);
        passwordField.setFont(customFont.deriveFont(Font.PLAIN, 20f));
        passwordField.setMaximumSize(new Dimension(300, 40));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
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

        // Create button
        OutlinedButton createBtn = new OutlinedButton("CREATE ACCOUNT");
        createBtn.setFont(customFont.deriveFont(Font.PLAIN, 24f));
        createBtn.setBackground(new Color(100, 200, 100));
        createBtn.setForeground(Color.BLACK);
        createBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        createBtn.addActionListener(e -> createAccount());

        centerPanel.add(usernameLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerPanel.add(usernameField);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(passwordLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerPanel.add(passwordField);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(confirmPasswordLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        centerPanel.add(confirmPasswordField);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        centerPanel.add(createBtn);

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
                    createAccount();
                }
            }
        };

        usernameField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);
        confirmPasswordField.addKeyListener(enterKeyListener);
    }

    private void createAccount() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Validation
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            usernameField.requestFocus();
            return;
        }

        if (username.length() < 3) {
            JOptionPane.showMessageDialog(this, "Username must be at least 3 characters long.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            usernameField.requestFocus();
            return;
        }

        if (username.contains(" ")) {
            JOptionPane.showMessageDialog(this, "Username cannot contain spaces.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            usernameField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            passwordField.requestFocus();
            return;
        }

        if (password.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password must be at least 4 characters long.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            passwordField.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            confirmPasswordField.requestFocus();
            return;
        }

        // Check if username already exists
        if (ProfileStore.accountExists(username)) {
            JOptionPane.showMessageDialog(this, "An account with this username already exists.", "Username Taken", JOptionPane.ERROR_MESSAGE);
            usernameField.requestFocus();
            return;
        }

        // Create the account
        boolean success = ProfileStore.createAccount(username, password);
        if (success) {
            JOptionPane.showMessageDialog(this,
                "Account '" + username + "' has been created successfully!",
                "Account Created",
                JOptionPane.INFORMATION_MESSAGE);

            // Clear fields
            usernameField.setText("");
            passwordField.setText("");
            confirmPasswordField.setText("");

            // Go back to account management
            cardLayout.show(container, "ACCOUNT_MANAGEMENT");
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to create account. Please try again.",
                "Creation Failed",
                JOptionPane.ERROR_MESSAGE);
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