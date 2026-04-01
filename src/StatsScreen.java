import java.awt.*;

public class StatsScreen {
    private GamePanel gp;

    public StatsScreen(GamePanel gp) {
        this.gp = gp;
    }

    public void drawPlayerStat(Graphics2D g2d) {

        g2d.setColor(new Color(0, 0, 0, 220));
        g2d.fillRect(0, 0, gp.getWidth(), gp.getHeight());

        int x = 200;
        int y = 200;
        int w = gp.getWidth() / 3;
        int h = gp.getHeight() / 3;

        // 1. Draw player box
        g2d.setColor(new Color(45, 52, 71)); // Dark blue-grey
        g2d.fillRoundRect(x, y, w, h, 20, 20);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(x, y, w, h, 20, 20);

        // 2. Draw Header
        g2d.setFont(new Font("Monospaced", Font.BOLD, 35));
        g2d.drawString("PLAYER STATS:", x + 30, y + 50);
        g2d.drawLine(x + 30, y + 60, x + 350, y + 60);

        // 3. Stats Data
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 25));
        g2d.drawString("NAME: ASH", x + 30, y + 110);

        // HP Bar
        g2d.drawString("HP: ", x + 30, y + 160);
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(x + 100, y + 140, 200, 25);
        g2d.setColor(new Color(114, 186, 158)); // Greenish
        g2d.fillRect(x + 100, y + 140, 200, 25); // Set width to (currentHP/maxHP * 200)

        // MP Bar
        g2d.setColor(Color.WHITE);
        g2d.drawString("MP: ", x + 30, y + 210);
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(x + 100, y + 190, 200, 25);
        g2d.setColor(new Color(103, 146, 219)); // Blue
        g2d.fillRect(x + 100, y + 190, 100, 25); // Example 50% fill

        // -- Top right -- //
        int rx = x + 700; // Horizontal offset for right column
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
        g2d.drawString("🕒 WPM:         75 WPM", rx + 30, y + 100);
        g2d.drawString("🕒 TIME PLAYED: " + gp.getTimeManager().getFormattedTime(), rx + 30, y + 150);

        // -- Bottom Right -- //
        int ry_bottom = y + 250;
        int rh_bottom = 250;

        g2d.setColor(new Color(45, 52, 71, 230));
        g2d.fillRoundRect(rx, ry_bottom, rw, rh_bottom, 20, 20);
        g2d.setColor(Color.WHITE);
        g2d.drawRoundRect(rx, ry_bottom, rw, rh_bottom, 20, 20);

        // Trainers Content
        g2d.setFont(new Font("Monospaced", Font.BOLD, 28));
        g2d.drawString("TRAINERS BEAT:", rx + 30, ry_bottom + 45);

        // Big Centered Counter
        g2d.setFont(new Font("Monospaced", Font.BOLD, 50));
        g2d.drawString("32 / 50", rx + 80, ry_bottom + 130);
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 20));
        g2d.drawString("TOTAL", rx + 160, ry_bottom + 160);

        // -- Press Enter to go back label -- //
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.ITALIC, 20));
        g2d.drawString("PRESS ENTER TO GO BACK", gp.getWidth() / 2 - 150, gp.getHeight() - 50);
    }
}
