import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;

/**
 * Login and account-creation screen.
 *
 * <p>
 * Displays a centred dialog box over a scaled {@code LoginBackground.png}
 * image. Delegates authentication and account creation to {@link ProfileStore}.
 * On success the user is forwarded to {@link PlayerScreen} via
 * {@link Main#showPlayerScreen(String)}.
 * </p>
 * 
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
public class LoginPanel extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private Main mainApp;
    private BufferedImage backgroundImage;

    public LoginPanel(JPanel container, Main mainApp) {
        this.mainApp = mainApp;

        // 1. Load the tileable grass background image (Ensure it's in project root)
        try {
            backgroundImage = ImageIO.read(new File("LoginBackground.png"));
        } catch (Exception e) {
            System.err.println("LoginPanel: Could not load grass.png. Using fallback color.");
        }

        // Set layout to GridBagLayout to center the login dialog box
        setLayout(new GridBagLayout());
        setOpaque(false); // Required to see the custom painted background

        JPanel dialogBox = new JPanel(new GridBagLayout());
        dialogBox.setBackground(new Color(245, 245, 245, 235));
        dialogBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.BLACK, 4),
                BorderFactory.createEmptyBorder(25, 40, 25, 40)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel promptLabel = new JLabel("TRAINER LOGIN", SwingConstants.CENTER);
        promptLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        gbc.gridy = 0;
        dialogBox.add(promptLabel, gbc);

        JLabel helpLabel = new JLabel(
                "<html><div style='text-align:center;'>Use your username and password.<br/>New trainers can create an account here.</div></html>",
                SwingConstants.CENTER);
        helpLabel.setFont(new Font("Monospaced", Font.PLAIN, 13));
        gbc.gridy = 1;
        dialogBox.add(helpLabel, gbc);

        usernameField = new JTextField(14);
        usernameField.setFont(new Font("Monospaced", Font.PLAIN, 18));
        usernameField.setBorder(BorderFactory.createTitledBorder("Username"));
        gbc.gridy = 2;
        dialogBox.add(usernameField, gbc);

        passwordField = new JPasswordField(14);
        passwordField.setFont(new Font("Monospaced", Font.PLAIN, 18));
        passwordField.setBorder(BorderFactory.createTitledBorder("Password"));
        gbc.gridy = 3;
        dialogBox.add(passwordField, gbc);

        JPanel buttonRow = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonRow.setOpaque(false);

        JButton loginBtn = createActionButton("LOGIN", Color.BLACK);
        loginBtn.addActionListener(e -> handleLogin());
        buttonRow.add(loginBtn);

        JButton createBtn = createActionButton("CREATE ACCOUNT", new Color(40, 110, 70));
        createBtn.addActionListener(e -> handleCreateAccount());
        buttonRow.add(createBtn);

        gbc.gridy = 4;
        dialogBox.add(buttonRow, gbc);

        JButton backBtn = createActionButton("BACK TO MENU", new Color(140, 45, 45));
        backBtn.addActionListener(e -> mainApp.returnToMenu());
        gbc.gridy = 5;
        dialogBox.add(backBtn, gbc);

        JLabel legacyHint = new JLabel("Legacy accounts can try their username as the password.",
                SwingConstants.CENTER);
        legacyHint.setFont(new Font("Monospaced", Font.ITALIC, 11));
        gbc.gridy = 6;
        dialogBox.add(legacyHint, gbc);

        add(dialogBox);
    }

    private JButton createActionButton(String text, Color bg) {
        JButton button = new JButton(text);
        button.setFont(new Font("Monospaced", Font.BOLD, 15));
        button.setBackground(bg);
        double luminance = 0.299 * bg.getRed() + 0.587 * bg.getGreen() + 0.114 * bg.getBlue();
        button.setForeground(luminance < 128 ? Color.WHITE : Color.BLACK);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        return button;
    }

    private void handleLogin() {
        String username = ProfileStore.sanitizeUsername(usernameField.getText());
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both a username and a password.");
            return;
        }

        if (!ProfileStore.authenticate(username, password)) {
            JOptionPane.showMessageDialog(this, "That username or password was not recognized.");
            return;
        }

        mainApp.showPlayerScreen(username);
    }

    private void handleCreateAccount() {
        String username = ProfileStore.sanitizeUsername(usernameField.getText());
        String password = new String(passwordField.getPassword());
        String validationMessage = ProfileStore.validateNewAccount(username, password);
        if (validationMessage != null) {
            JOptionPane.showMessageDialog(this, validationMessage);
            return;
        }

        if (!ProfileStore.createAccount(username, password)) {
            JOptionPane.showMessageDialog(this, "The account could not be created right now.");
            return;
        }

        JOptionPane.showMessageDialog(this, "Account created. Welcome to Keymon, " + username + "!");
        mainApp.showPlayerScreen(username);
    }

    /**
     * Paints the background by tiling the image across the entire panel area.
     * This fixes the incomplete filling issue seen in previous scale-based code.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int imgWidth = backgroundImage.getWidth();
            int imgHeight = backgroundImage.getHeight();

            double widthRatio = (double) panelWidth / imgWidth;
            double heightRatio = (double) panelHeight / imgHeight;
            double scale = Math.max(widthRatio, heightRatio);

            int drawWidth = (int) (imgWidth * scale);
            int drawHeight = (int) (imgHeight * scale);

            int x = (panelWidth - drawWidth) / 2;
            int y = (panelHeight - drawHeight) / 2;

            g2d.drawImage(backgroundImage, x, y, drawWidth, drawHeight, this);
        } else {
            g.setColor(new Color(34, 139, 34));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
