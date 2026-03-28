import java.awt.*;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Keymon");
        CardLayout cardLayout = new CardLayout();
        JPanel mainContainer = new JPanel(cardLayout);

        MapLoader loader = new MapLoader("map.csv");

        // Load saved player data [x, y, spriteRow] from JSON
        int[] savedData = SaveManager.loadGame();
        // Initialize Player using the saved data (New Constructor)
        // 32 is the TILE_SIZE used for positioning and rendering
        Player player = new Player(savedData[0], savedData[1], savedData[2], 32);

        // Just create instances of your other files
        mainContainer.add(new MenuPanel(cardLayout, mainContainer), "MENU");
        mainContainer.add(new GamePanel(cardLayout, mainContainer, player, loader.getWorldMap()), "PLAY GAME");
        PlayerScreen playerScreen = new PlayerScreen(cardLayout, mainContainer, player);
        mainContainer.add(playerScreen, "PLAYER_SCREEN");

        frame.add(mainContainer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }
}