import java.awt.*;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Keymon");
        CardLayout cardLayout = new CardLayout();
        JPanel mainContainer = new JPanel(cardLayout);

        MapLoader loader = new MapLoader("map.csv");

        // Just create instances of your other files
        mainContainer.add(new MenuPanel(cardLayout, mainContainer), "MENU");
        mainContainer.add(new GamePanel(loader.getWorldMap()), "PLAY GAME");

        frame.add(mainContainer);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }
}