import java.awt.*;
import javax.swing.*;

public class GamePanel extends JPanel {
    private boolean isPaused = false;

    public GamePanel(CardLayout layout, JPanel container) {
        this.setBackground(Color.BLACK);
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> layout.show(container, "MENU"));
        this.add(backButton);
        // All your game loop and pause logic lives here!
    }
}