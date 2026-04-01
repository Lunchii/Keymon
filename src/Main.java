import java.awt.*;
import javax.swing.*;

public class Main {

    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private static final int TILE_SIZE = 32;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().initGUI());
    }

    /**
     * Initializes the GUI and sets the Menu as the starting screen.
     */
    private void initGUI() {
        frame = new JFrame("Keymon");
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // 1. Register the Main Menu - Set as the first screen
        mainContainer.add(new MenuPanel(cardLayout, mainContainer), "MENU");
        // 2. Register the Login Panel - Pass 'this' to allow calling startGame()
        mainContainer.add(new LoginPanel(mainContainer, this), "LOGIN");

        frame.add(mainContainer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        // --- Modification: Show Menu on startup instead of Login ---
        cardLayout.show(mainContainer, "MENU");
    }

    /**
     * Initializes game data after successful login.
     * Called by LoginPanel after user submits their username.
     * @param username The name used for multi-user save file identification.
     */
    public void startGame(String username) {
        // 1. Load data for the specific user using the updated SaveManager
        int[] savedData = SaveManager.loadGame(username);

        // 2. Load map and trainer resources
        MapLoader loader = new MapLoader("map.csv");
        Trainer trainer = loader.getTrainer();

        // 3. Initialize player with loaded coordinates
        // 32 is the TILE_SIZE used for positioning and rendering
        Player player = new Player(savedData[0], savedData[1], savedData[2], TILE_SIZE);

        // 4. Create and add GamePanel dynamically to pass the current username
        mainContainer.add(new GamePanel(cardLayout, mainContainer, player, loader.getWorldMap(), trainer,
                loader.getTerrainObjects(), username), "PLAY GAME");

        System.out.println("Session started for user: " + username);

        // 5. Switch to the game screen and request focus for controls
        cardLayout.show(mainContainer, "PLAY GAME");
        mainContainer.getComponent(mainContainer.getComponentCount() - 1).requestFocusInWindow();
    }
}