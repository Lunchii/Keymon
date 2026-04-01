import java.awt.*;

public class PauseMenu {
    private GamePanel gp;

    public PauseMenu(GamePanel gp) {
        this.gp = gp;
    }

    public void draw(Graphics2D g2d) {
        // Darken background
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, gp.getWidth(), gp.getHeight());

        // Draw Header
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 50));
        g2d.drawString("PAUSED", gp.getWidth() / 2 - 100, 150);

        // Draw Menu Options
        g2d.setFont(new Font("Arial", Font.PLAIN, 30));
        String[] options = gp.getPauseOptions();
        int currentIndex = gp.getPauseOptionIndex();

        for (int i = 0; i < options.length; i++) {
            if (i == currentIndex) {
                // This is where your "Label only" color logic lives
                g2d.setColor(Color.YELLOW);
                g2d.drawString("> " + options[i], gp.getWidth() / 2 - 100, 250 + (i * 50));
            } else {
                g2d.setColor(Color.WHITE);
                g2d.drawString("  " + options[i], gp.getWidth() / 2 - 100, 250 + (i * 50));
            }
        }
    }
}