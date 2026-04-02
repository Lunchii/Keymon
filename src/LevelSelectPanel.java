import java.awt.*;
import javax.swing.*;

public class LevelSelectPanel extends JPanel {
    private final Main mainApp;
    private final String username;
    private final CardLayout cardLayout;
    private final JPanel mainContainer;

    public LevelSelectPanel(JPanel mainContainer, Main mainApp, String username) {
        this.mainApp = mainApp;
        this.username = username;
        this.mainContainer = mainContainer;
        this.cardLayout = (CardLayout) mainContainer.getLayout();

        setLayout(new GridBagLayout());
        setBackground(new Color(34, 139, 34));

        JPanel dialogBox = new JPanel(new GridBagLayout());
        dialogBox.setBackground(new Color(245, 245, 245, 230));
        dialogBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 4),
                BorderFactory.createEmptyBorder(30, 50, 30, 50)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        JLabel title = new JLabel("SELECT A LEVEL", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 26));
        gbc.gridy = 0;
        dialogBox.add(title, gbc);

        int unlockedLevel = SaveManager.getHighestUnlockedLevel(username);

        JButton level1Btn = createStyledButton("LEVEL 1", Color.BLACK, Color.WHITE);
        level1Btn.addActionListener(e -> mainApp.startGame(username, "level1_map.csv"));
        gbc.gridy = 1;
        dialogBox.add(level1Btn, gbc);

        JButton level2Btn = createStyledButton("LEVEL 2", Color.BLACK, Color.WHITE);
        level2Btn.addActionListener(e -> mainApp.startGame(username, "level2_map.csv"));
        if (unlockedLevel < 2) {
            level2Btn.setEnabled(false);
            level2Btn.setText("LEVEL 2 (LOCKED)");
        }
        gbc.gridy = 2;
        dialogBox.add(level2Btn, gbc);

        JButton level3Btn = createStyledButton("LEVEL 3", Color.BLACK, Color.WHITE);
        level3Btn.addActionListener(e -> mainApp.startGame(username, "level3_map.csv"));
        if (unlockedLevel < 3) {
            level3Btn.setEnabled(false);
            level3Btn.setText("LEVEL 3 (LOCKED)");
        }
        gbc.gridy = 3;
        dialogBox.add(level3Btn, gbc);

        JButton backBtn = createStyledButton("BACK", new Color(178, 34, 34), Color.WHITE);
        backBtn.addActionListener(e -> cardLayout.show(mainContainer, "PLAYER_SCREEN"));
        gbc.gridy = 4;
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
}
