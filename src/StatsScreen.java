import java.awt.*;

public class StatsScreen {
    // Modified: Using PlayerData and username instead of GamePanel
    private SaveManager.PlayerData data;
    private String username;

    public StatsScreen() {
        // Constructor no longer requires GamePanel
    }

    // New method to update data before drawing
    public void updateData(String username, SaveManager.PlayerData data) {
        this.username = username;
        this.data = data;
    }

    public void drawPlayerStat(Graphics2D g2d, int screenWidth, int screenHeight) {
        // If data is null, provide default/empty data to prevent crash
        if (data == null) return;

        // Dark semi-transparent overlay
        g2d.setColor(new Color(0, 0, 0, 220));
        g2d.fillRect(0, 0, screenWidth, screenHeight);

        int x = 200;
        int y = 200;
        int w = screenWidth / 3;
        int h = screenHeight / 3;

        // 1. Draw player box (Background)
        g2d.setColor(new Color(45, 52, 71));
        g2d.fillRoundRect(x, y, w, h, 20, 20);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(x, y, w, h, 20, 20);

        // 2. Draw Header
        g2d.setFont(new Font("Monospaced", Font.BOLD, 35));
        g2d.drawString("PLAYER STATS:", x + 30, y + 50);
        g2d.drawLine(x + 30, y + 60, x + 350, y + 60);

        // 3. Stats Data (Modified to use real username)
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 25));
        g2d.drawString("NAME: " + (username != null ? username.toUpperCase() : "N/A"), x + 30, y + 110);

        // HP Bar
        g2d.drawString("HP: ", x + 30, y + 160);
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(x + 100, y + 140, 200, 25);
        g2d.setColor(new Color(114, 186, 158));
        g2d.fillRect(x + 100, y + 140, 200, 25);

        // MP Bar
        g2d.setColor(Color.WHITE);
        g2d.drawString("MP: ", x + 30, y + 210);
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(x + 100, y + 190, 200, 25);
        g2d.setColor(new Color(103, 146, 219));
        g2d.fillRect(x + 100, y + 190, 100, 25);

        // -- Top right (Progress Box) -- //
        int rx = x + 700;
        int rw = 400;
        int rh = 200;

        g2d.setColor(new Color(45, 52, 71, 230));
        g2d.fillRoundRect(rx, y, rw, rh, 20, 20);
        g2d.setColor(Color.WHITE);
        g2d.drawRoundRect(rx, y, rw, rh, 20, 20);

        g2d.setFont(new Font("Monospaced", Font.BOLD, 28));
        g2d.drawString("GAME PROGRESS", rx + 30, y + 45);
        g2d.drawLine(rx + 30, y + 55, rx + 250, y + 55);

        g2d.setFont(new Font("Monospaced", Font.PLAIN, 20));
        // Using real highScore from SaveManager
        g2d.drawString("🏆 BEST SCORE:  " + data.highScore, rx + 30, y + 100);
        // Note: Real Time played needs a TimeManager save logic; showing Level for now
        g2d.drawString("📍 LEVEL:        " + data.currentLevel, rx + 30, y + 150);

        // -- Bottom Right (Trainer Box) -- //
        int ry_bottom = y + 250;
        int rh_bottom = 250;

        g2d.setColor(new Color(45, 52, 71, 230));
        g2d.fillRoundRect(rx, ry_bottom, rw, rh_bottom, 20, 20);
        g2d.setColor(Color.WHITE);
        g2d.drawRoundRect(rx, ry_bottom, rw, rh_bottom, 20, 20);

        g2d.setFont(new Font("Monospaced", Font.BOLD, 28));
        g2d.drawString("TOTAL PROGRESS:", rx + 30, ry_bottom + 45);

        g2d.setFont(new Font("Monospaced", Font.BOLD, 50));
        g2d.drawString(data.currentLevel + " / 3", rx + 80, ry_bottom + 130);
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 20));
        g2d.drawString("STAGES", rx + 160, ry_bottom + 160);
    }
}