import java.awt.*;
import java.util.List;
import javax.swing.*;

public class Main {

    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private static final int TILE_SIZE = 32;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().initGUI());
    }

    private void initGUI() {
        frame = new JFrame("Keymon");
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        mainContainer.add(new MenuPanel(cardLayout, mainContainer), "MENU");
        mainContainer.add(new LoginPanel(mainContainer, this), "LOGIN");

        frame.add(mainContainer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        cardLayout.show(mainContainer, "MENU");
    }

    /** Called by LoginPanel after username confirmed — show team select screen. */
    public void showTeamSelect(String username) {
        // Remove old team select if present
        try { mainContainer.remove(mainContainer.getComponentCount() - 1); } catch (Exception ignored) {}
        mainContainer.add(new TeamSelectPanel(mainContainer, this, username), "TEAM_SELECT");
        cardLayout.show(mainContainer, "TEAM_SELECT");
    }

    /** Called by TeamSelectPanel after team confirmed. */
    public void startGame(String username, List<Pokemon> team) {
        int[] savedData = SaveManager.loadGame(username);

        MapLoader loader = new MapLoader("map.csv");
        Trainer trainer = loader.getTrainer();

        Player player = new Player(savedData[0], savedData[1], savedData[2], TILE_SIZE);

        GamePanel gamePanel = new GamePanel(cardLayout, mainContainer, player, loader.getWorldMap(),
                trainer, loader.getTerrainObjects(), username);
        gamePanel.setParty(team);
        mainContainer.add(gamePanel, "PLAY GAME");

        System.out.println("Session started for user: " + username);

        cardLayout.show(mainContainer, "PLAY GAME");
        mainContainer.getComponent(mainContainer.getComponentCount() - 1).requestFocusInWindow();
    }
}
