import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Stage selection screen that lets the player choose which level to play.
 *
 * <p>
 * Level 2 and Level 3 buttons are disabled until the corresponding stage
 * has been unlocked. Unlock state is read from
 * {@link ProfileStore#getHighestUnlockedLevel(String)}.
 * Tooltip text on each button shows the stage name and difficulty description.
 * </p>
 * 
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
public class LevelSelectPanel extends JPanel {
    private final Main mainApp;
    private final String username;
    private final CardLayout cardLayout;
    private final JPanel mainContainer;
    private BufferedImage backgroundImage;

    public LevelSelectPanel(JPanel mainContainer, Main mainApp, String username) {
        this.mainApp = mainApp;
        this.username = username;
        this.mainContainer = mainContainer;
        this.cardLayout = (CardLayout) mainContainer.getLayout();

        try {
            backgroundImage = ImageIO.read(new File("images/LoginBackground.png"));
        } catch (Exception e) {
            // fallback to solid colour below
        }

        setLayout(new GridBagLayout());
        setOpaque(backgroundImage == null);
        setBackground(new Color(34, 139, 34));

        JPanel dialogBox = new JPanel(new GridBagLayout());
        dialogBox.setBackground(new Color(245, 245, 245, 230));
        dialogBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 4),
                BorderFactory.createEmptyBorder(30, 50, 30, 50)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        JLabel title = new JLabel("SELECT A STAGE", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 26));
        gbc.gridy = 0;
        dialogBox.add(title, gbc);

        JLabel subtitle = new JLabel("Each stage gets faster and uses harder words.", SwingConstants.CENTER);
        subtitle.setFont(new Font("Monospaced", Font.PLAIN, 14));
        gbc.gridy = 1;
        dialogBox.add(subtitle, gbc);

        int unlockedLevel = ProfileStore.getHighestUnlockedLevel(username);

        JButton level1Btn = createStyledButton("LEVEL 1", Color.BLACK, Color.BLACK);
        level1Btn.addActionListener(e -> mainApp.startGame(username, "maps/level1_map.csv"));
        level1Btn.setToolTipText(WordRepository.getStageName(1) + " - " + WordRepository.getStageDescription(1));
        gbc.gridy = 2;
        dialogBox.add(level1Btn, gbc);

        JButton level2Btn = createStyledButton("LEVEL 2", Color.BLACK, Color.BLACK);
        level2Btn.addActionListener(e -> mainApp.startGame(username, "maps/level2_map.csv"));
        level2Btn.setToolTipText(WordRepository.getStageName(2) + " - " + WordRepository.getStageDescription(2));
        if (unlockedLevel < 2) {
            level2Btn.setEnabled(false);
            level2Btn.setText("LEVEL 2 (LOCKED)");
        }
        gbc.gridy = 3;
        dialogBox.add(level2Btn, gbc);

        JButton level3Btn = createStyledButton("LEVEL 3", Color.BLACK, Color.BLACK);
        level3Btn.addActionListener(e -> mainApp.startGame(username, "maps/level3_map.csv"));
        level3Btn.setToolTipText(WordRepository.getStageName(3) + " - " + WordRepository.getStageDescription(3));
        if (unlockedLevel < 3) {
            level3Btn.setEnabled(false);
            level3Btn.setText("LEVEL 3 (LOCKED)");
        }
        gbc.gridy = 4;
        dialogBox.add(level3Btn, gbc);

        JButton backBtn = createStyledButton("BACK", new Color(178, 34, 34), Color.BLACK);
        backBtn.addActionListener(e -> cardLayout.show(mainContainer, "PLAYER_SCREEN"));
        gbc.gridy = 5;
        dialogBox.add(backBtn, gbc);

        add(dialogBox);
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int pw = getWidth(), ph = getHeight();
            double scale = Math.max((double) pw / backgroundImage.getWidth(),
                    (double) ph / backgroundImage.getHeight());
            int dw = (int) (backgroundImage.getWidth() * scale);
            int dh = (int) (backgroundImage.getHeight() * scale);
            g2.drawImage(backgroundImage, (pw - dw) / 2, (ph - dh) / 2, dw, dh, this);
        }
    }
}
