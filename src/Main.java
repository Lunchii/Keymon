import java.awt.*;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Keymon");
        CardLayout cardLayout = new CardLayout();
        JPanel mainContainer = new JPanel(cardLayout);

        // Just create instances of your other files
        mainContainer.add(new MenuPanel(cardLayout, mainContainer), "MENU");
        mainContainer.add(new GamePanel(cardLayout, mainContainer), "PLAY GAME");

        frame.add(mainContainer);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }
}