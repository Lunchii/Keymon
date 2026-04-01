import java.awt.*;
import javax.swing.*;

public class Main {

    private static final int TILE_SIZE = 32;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Keymon");
        CardLayout cardLayout = new CardLayout();
        JPanel mainContainer = new JPanel(cardLayout);

        MapLoader loader = new MapLoader("map.csv");
        Trainer trainer = loader.getTrainer();

        // Load saved player data [x, y, spriteRow] from JSON
        int[] savedData = SaveManager.loadGame();
        // Initialize Player using the saved data (New Constructor)
        // 32 is the TILE_SIZE used for positioning and rendering
        Player player = new Player(savedData[0], savedData[1], savedData[2], TILE_SIZE);

        mainContainer.add(new MenuPanel(cardLayout, mainContainer), "MENU");
        mainContainer.add(new GamePanel(cardLayout, mainContainer, player, loader.getWorldMap(), trainer,
                loader.getTerrainObjects()), "PLAY GAME");

        frame.add(mainContainer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }
}