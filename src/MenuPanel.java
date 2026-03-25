import java.awt.*;
import javax.swing.*;

public class MenuPanel extends JPanel {
    private Image backgroundGif;

    public MenuPanel(CardLayout layout, JPanel container) {
        // 1. Load the GIF
        backgroundGif = new ImageIcon("menuBackground.gif").getImage();

        // 2. Setup Layout
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));

        // 3. Add Title
        JLabel title = new JLabel("Keymon");
        title.setFont(new Font("Arial", Font.BOLD, 150));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        this.add(Box.createVerticalGlue());
        this.add(title);
        this.add(Box.createRigidArea(new Dimension(0, 50)));

        // 4. Button Loop
        String[] buttonLabels = { "PLAY GAME", "LOGIN", "HIGHSCORE", "TUTORIAL" };
        Dimension buttonSize = new Dimension(400, 500);

        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setMaximumSize(buttonSize);
            button.setFont(new Font("Arial", Font.PLAIN, 24));

            // Make buttons slightly transparent so they look "Modern"
            button.setBackground(new Color(255, 255, 255, 200));
            button.setOpaque(true);

            button.addActionListener(e -> layout.show(container, label));

            this.add(button);
            this.add(Box.createRigidArea(new Dimension(0, 20)));
        }

        this.add(Box.createVerticalGlue());
    }

    // This is the "Magic" that fills the screen
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw the GIF stretched to the current width and height of the panel
        if (backgroundGif != null) {
            g.drawImage(backgroundGif, 0, 0, getWidth(), getHeight(), this);
        }
    }
}