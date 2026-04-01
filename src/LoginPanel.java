import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * Step 3: LoginPanel with Tiled Retro Background and Styled Dialog.
 * Fixes the incomplete background filling issue by using tiling logic.
 */
public class LoginPanel extends JPanel {
    private JTextField usernameField;
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

        // --- RPG Dialog Box Styling ---
        JPanel dialogBox = new JPanel(new GridBagLayout());
        dialogBox.setBackground(new Color(245, 245, 245, 230)); // Semi-transparent off-white
        dialogBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.BLACK, 4), // Thick retro border
                BorderFactory.createEmptyBorder(25, 50, 25, 50) // Internal padding
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 5, 12, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 2. Prompt Label
        JLabel promptLabel = new JLabel("IDENTIFY YOURSELF, TRAINER", SwingConstants.CENTER);
        promptLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
        gbc.gridx = 0; gbc.gridy = 0;
        dialogBox.add(promptLabel, gbc);

        // 3. Username Input Field
        usernameField = new JTextField(12);
        usernameField.setFont(new Font("Monospaced", Font.PLAIN, 18));
        usernameField.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        gbc.gridy = 1;
        dialogBox.add(usernameField, gbc);

        // 4. Confirm Button
        JButton loginBtn = new JButton("CONFIRM");
        loginBtn.setFont(new Font("Monospaced", Font.BOLD, 16));
        loginBtn.setBackground(Color.BLACK);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        loginBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            if (!username.isEmpty()) {
                mainApp.startGame(username);
            } else {
                JOptionPane.showMessageDialog(this, "A Trainer needs a name!");
            }
        });

        gbc.gridy = 2;
        dialogBox.add(loginBtn, gbc);

        // Add the styled dialog box to the center of the panel
        add(dialogBox);
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