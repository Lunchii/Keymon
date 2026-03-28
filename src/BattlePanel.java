import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class BattlePanel extends JPanel {
    private CardLayout layout;
    private JPanel container;
    private String returnPanelName = "PLAY GAME";
    private GamePanel sourceGamePanel;

    public BattlePanel(CardLayout layout, JPanel container) {
        this.layout = layout;
        this.container = container;

        this.setLayout(new GridBagLayout());
        this.setBackground(Color.BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 20, 20, 20);

        JLabel title = new JLabel("Battle");
        title.setFont(new Font("Arial", Font.BOLD, 80));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.add(title, gbc);

        JButton backButton = createBattleButton("Back", e -> finishBattle());
        gbc.gridy = 1;
        this.add(backButton, gbc);
    }

    private JButton createBattleButton(String label, ActionListener action) {
        // Helper to create the simple battle screen buttons.
        JButton button = new JButton(label);
        button.setFont(new Font("Arial", Font.PLAIN, 26));
        button.setBackground(new Color(220, 220, 220));
        button.setOpaque(true);
        button.setFocusable(false);
        button.addActionListener(action);
        return button;
    }

    public void startBattle(String returnPanelName, GamePanel sourceGamePanel) {
        this.returnPanelName = returnPanelName;
        this.sourceGamePanel = sourceGamePanel;
        // Show the battle screen and block normal movement.
        layout.show(container, "BATTLE");
    }

    private void finishBattle() {
        // Exit the battle screen and allow movement again in the previous game panel.
        if (sourceGamePanel != null) {
            sourceGamePanel.setBattleActive(false);
        }
        layout.show(container, returnPanelName);
    }
}
