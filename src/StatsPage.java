import javax.swing.*;
import java.awt.*;

public class StatsPage extends JPanel {
    private Main mainApp;
    private String username;
    private StatsScreen drawer;

    public StatsPage(Main mainApp) {
        this.mainApp = mainApp;
        this.setBackground(Color.BLACK);
        // Initializing drawer with updated constructor
        this.drawer = new StatsScreen();

        JButton backBtn = new JButton("BACK TO TRAINER SCREEN");
        backBtn.setFont(new Font("Monospaced", Font.BOLD, 16));
        backBtn.addActionListener(e -> mainApp.showPlayerScreen(username));
        this.setLayout(new BorderLayout());
        this.add(backBtn, BorderLayout.SOUTH);
    }

    public void setUserData(String username) {
        this.username = username;
        // Load real data from SaveManager
        SaveManager.PlayerData data = SaveManager.loadGame(username);
        // Inject data into the drawer
        drawer.updateData(username, data);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Pass panel dimensions instead of relying on GamePanel
        drawer.drawPlayerStat((Graphics2D) g, getWidth(), getHeight());
    }
}