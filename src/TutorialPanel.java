import java.awt.*;
import javax.swing.*;

public class TutorialPanel extends JPanel {
    private Image backgroundGif;

    public TutorialPanel(CardLayout layout, JPanel container) {
        backgroundGif = new ImageIcon("menuBackground.gif").getImage();

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));

        JLabel title = new JLabel("Tutorial");
        title.setFont(new Font("Arial", Font.BOLD, 120));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        this.add(Box.createVerticalGlue());
        this.add(title);
        this.add(Box.createRigidArea(new Dimension(0, 40)));

        JLabel subtitle = new JLabel("Learn how to Play!");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 28));
        subtitle.setForeground(Color.WHITE);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.add(subtitle);
        this.add(Box.createRigidArea(new Dimension(0, 40)));

        Dimension buttonSize = new Dimension(400, 80);
        String[] buttonLabels = {"Start tutorial", "Info", "Back"};

        // Create the tutorial action buttons and wire them
        for (String label : buttonLabels) {
            JButton button = new JButton(label);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setMaximumSize(buttonSize);
            button.setFont(new Font("Arial", Font.PLAIN, 28));
            button.setBackground(new Color(255, 255, 255, 200));
            button.setOpaque(true);
            button.setFocusable(false);

            button.addActionListener(e -> {
                switch (label) {
                    case "Start tutorial":
                        layout.show(container, "TUTORIAL_GAME");
                        break;
                    case "Back":
                        layout.show(container, "MENU");
                        break;

                    case "Info":
                        JOptionPane.showMessageDialog(
                            this,
                            "Use W A S D to move your character across the grass map.\n" +
                            "Press Save & Back in the game to keep your position.\n" +
                            "Tiles that are a darker green indicate battle events.\n",
                            "Tutorial Info",
                            JOptionPane.INFORMATION_MESSAGE
                    
                        );
                        break;
                }
            });

            this.add(button);
            this.add(Box.createRigidArea(new Dimension(0, 20)));
        }

        this.add(Box.createVerticalGlue());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundGif != null) {
            g.drawImage(backgroundGif, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
