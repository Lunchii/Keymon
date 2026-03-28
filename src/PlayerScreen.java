import java.awt.*;
import javax.swing.*;

public class PlayerScreen extends JPanel {
    private Player player;
    private CardLayout layout;
    private JPanel container;

    public PlayerScreen(CardLayout layout, JPanel container, Player player) {
        this.layout = layout;
        this.container = container;
        this.player = player;

        this.setLayout(new BorderLayout());
        this.setBackground(new Color(20, 20, 20)); // 深色背景

        // 1. Title at top
        JLabel title = new JLabel("PLAYER PROFILE", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 60));
        title.setForeground(Color.CYAN);
        title.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));
        this.add(title, BorderLayout.NORTH);

        // 2. Centered Sprite Display (Fixed Position)
        JPanel centerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (player != null && player.getCurrentFrame() != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    // Disable smoothing for sharp pixel art
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                    int scale = 6; // 6x magnification
                    int w = player.getCurrentFrame().getWidth() * scale;
                    int h = player.getCurrentFrame().getHeight() * scale;
                    int x = (getWidth() - w) / 2;
                    int y = (getHeight() - h) / 2;

                    g2d.drawImage(player.getCurrentFrame(), x, y, w, h, null);
                }
            }
        };
        centerPanel.setOpaque(false);
        this.add(centerPanel, BorderLayout.CENTER);

        // 3. Back Button at bottom
        JButton backButton = new JButton("BACK TO GAME");
        backButton.setFont(new Font("Arial", Font.BOLD, 25));
        backButton.addActionListener(e -> {
            layout.show(container, "PLAY GAME");
            // FIX for Test 3: Re-gain focus so WASD works immediately
            container.getComponent(1).requestFocusInWindow();
        });

        JPanel southPanel = new JPanel();
        southPanel.setOpaque(false);
        southPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 50, 0));
        southPanel.add(backButton);
        this.add(southPanel, BorderLayout.SOUTH);
    }
}