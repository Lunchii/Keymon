import java.awt.*;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Keymon");
        CardLayout cardLayout = new CardLayout();
        JPanel mainContainer = new JPanel(cardLayout);

        MapLoader loader = new MapLoader("map.csv");
        MapLoader tutorialLoader = new MapLoader("tutorial_map.csv");

        // Load saved player data [x, y, spriteRow] from JSON
        int[] savedData = SaveManager.loadGame();
        // Initialize Player using the saved data (New Constructor)
        // 32 is the TILE_SIZE used for positioning and rendering
        Player player = new Player(savedData[0], savedData[1], savedData[2], 32);
        Player tutorialPlayer = new Player(2, 2, 32);

        // Battle screen shown when the player steps onto a battle tile.
        BattlePanel battlePanel = new BattlePanel(cardLayout, mainContainer);

        // Add the main menu and game screens to the card layout.
        mainContainer.add(new MenuPanel(cardLayout, mainContainer), "MENU");
        // Normal gameplay screen with save-on-back enabled.
        mainContainer.add(new GamePanel(cardLayout, mainContainer, player, loader.getWorldMap(), true, "PLAY GAME", battlePanel), "PLAY GAME");
        // Tutorial gameplay screen also saves progress when returning.
        mainContainer.add(new GamePanel(cardLayout, mainContainer, tutorialPlayer, tutorialLoader.getWorldMap(), true, "TUTORIAL_GAME", battlePanel), "TUTORIAL_GAME");
        mainContainer.add(battlePanel, "BATTLE");
        mainContainer.add(new TutorialPanel(cardLayout, mainContainer), "TUTORIAL");

        frame.add(mainContainer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }
}