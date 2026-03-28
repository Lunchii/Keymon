import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * LoginPanel.java
 * Purpose: Provides a user interface for player authentication.
 * Integrated with CardLayout for screen switching.
 * Part of Issue #9.
 */
public class LoginPanel extends JPanel {
    private Image backgroundGif;
    private CardLayout layout;
    private JPanel container;

    public LoginPanel(CardLayout layout, JPanel container) {
        this.layout = layout;
        this.container = container;

        // Load the background GIF (Consistent with MenuPanel)
        this.backgroundGif = new ImageIcon("menuBackground.gif").getImage();

        // Main layout of the panel itself to center the login box
        this.setLayout(new GridBagLayout());

        // --- VISUAL OPTIMIZATION: Semi-transparent Login Box ---
        // Creates a "Glass Pane" effect for better readability against a moving background
        JPanel loginBox = new JPanel(new GridBagLayout());
        loginBox.setBackground(new Color(0, 0, 0, 150)); // Black with 150/255 transparency
        loginBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.CYAN, 2), // Thematic border color
                new EmptyBorder(30, 40, 30, 40) // Internal padding for components
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Spacing between components
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- COMPONENT INITIALIZATION ---
        JLabel loginTitle = new JLabel("USER LOGIN", SwingConstants.CENTER);
        loginTitle.setFont(new Font("Monospaced", Font.BOLD, 30));
        loginTitle.setForeground(Color.CYAN);

        JTextField userField = new JTextField(15);
        JPasswordField passField = new JPasswordField(15);

        JButton loginBtn = new JButton("LOGIN");
        JButton backBtn = new JButton("BACK");

        // --- LAYOUT ARRANGEMENT ---
        // Header
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        loginBox.add(loginTitle, gbc);

        // Username row
        gbc.gridwidth = 1; gbc.gridy = 1;
        loginBox.add(createStyledLabel("Username:"), gbc);
        gbc.gridx = 1;
        loginBox.add(userField, gbc);

        // Password row
        gbc.gridx = 0; gbc.gridy = 2;
        loginBox.add(createStyledLabel("Password:"), gbc);
        gbc.gridx = 1;
        loginBox.add(passField, gbc);

        // Action Buttons
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        loginBox.add(loginBtn, gbc);

        gbc.gridy = 4;
        loginBox.add(backBtn, gbc);

        // Add the styled login box to the center of the main panel
        this.add(loginBox);

        // --- INTERACTION LOGIC ---
        // Handles login validation and screen transition
        loginBtn.addActionListener(e -> {
            String username = userField.getText();
            char[] password = passField.getPassword();

            if (username.length() > 0 && password.length > 0) {
                // Success: Clear fields and return to main menu
                userField.setText("");
                passField.setText("");
                layout.show(container, "MENU");
            } else {
                // Failure: Show error dialog
                JOptionPane.showMessageDialog(this,
                        "Fields cannot be empty!",
                        "Login Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Simply returns to the Menu screen
        backBtn.addActionListener(e -> layout.show(container, "MENU"));
    }

    /**
     * Helper method to maintain consistent label styling
     */
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        return label;
    }

    /**
     * Overridden to render the animated background
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundGif != null) {
            g.drawImage(backgroundGif, 0, 0, getWidth(), getHeight(), this);
        }
    }
}