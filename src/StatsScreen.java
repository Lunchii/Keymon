import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Lightweight in-game stats overlay shown from the pause menu.
 */
public class StatsScreen {
    private GamePanel gamePanel;

    public void updateData(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void drawPlayerStat(Graphics2D graphics, int screenWidth, int screenHeight) {
        if (gamePanel == null) {
            return;
        }

        graphics.setColor(new Color(0, 0, 0, 225));
        graphics.fillRect(0, 0, screenWidth, screenHeight);

        int panelX = 120;
        int panelY = 100;
        int panelW = screenWidth - 240;
        int panelH = screenHeight - 180;

        graphics.setColor(new Color(18, 28, 42, 240));
        graphics.fillRoundRect(panelX, panelY, panelW, panelH, 24, 24);
        graphics.setColor(Color.WHITE);
        graphics.setStroke(new BasicStroke(3f));
        graphics.drawRoundRect(panelX, panelY, panelW, panelH, 24, 24);

        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("SansSerif", Font.BOLD, 30));
        graphics.drawString("LIVE STAGE STATS", panelX + 28, panelY + 44);

        graphics.setFont(new Font("SansSerif", Font.PLAIN, 16));
        graphics.drawString("Press ESC or Enter to return to the pause menu.", panelX + 28, panelY + 72);

        int leftX = panelX + 28;
        int leftY = panelY + 118;
        int rightX = panelX + panelW / 2 + 10;

        graphics.setFont(new Font("SansSerif", Font.BOLD, 20));
        graphics.drawString("Session Snapshot", leftX, leftY);
        graphics.drawString("Current Goals", rightX, leftY);

        graphics.setFont(new Font("Monospaced", Font.PLAIN, 18));
        int lineY = leftY + 40;
        drawLine(graphics, leftX, lineY, "Stage", gamePanel.getStageName() + " (" + gamePanel.getCurrentLevelId() + "/3)");
        lineY += 34;
        drawLine(graphics, leftX, lineY, "Score", String.valueOf(gamePanel.getLevelScore()));
        lineY += 34;
        drawLine(graphics, leftX, lineY, "Best", String.valueOf(gamePanel.getBestScore()));
        lineY += 34;
        drawLine(graphics, leftX, lineY, "Focus", gamePanel.getFocusHealth() + "/" + gamePanel.getMaxFocusHealth());
        lineY += 34;
        drawLine(graphics, leftX, lineY, "WPM", String.format("%.1f", gamePanel.getCurrentWpm()));
        lineY += 34;
        drawLine(graphics, leftX, lineY, "Peak", String.format("%.1f", gamePanel.getPeakWpm()));
        lineY += 34;
        drawLine(graphics, leftX, lineY, "Accuracy", String.format("%.1f%%", gamePanel.getAccuracyPercent()));
        lineY += 34;
        drawLine(graphics, leftX, lineY, "Words", String.valueOf(gamePanel.getWordsTyped()));
        lineY += 34;
        drawLine(graphics, leftX, lineY, "Errors", String.valueOf(gamePanel.getErrorCount()));
        lineY += 34;
        drawLine(graphics, leftX, lineY, "Combo", "x" + Math.max(1, gamePanel.getComboStreak()));
        lineY += 34;
        drawLine(graphics, leftX, lineY, "Powerups", String.valueOf(gamePanel.getPowerupsUsed()));

        graphics.setFont(new Font("SansSerif", Font.PLAIN, 18));
        int goalY = leftY + 42;
        graphics.drawString("Reach the exit tile to clear the stage.", rightX, goalY);
        goalY += 36;
        graphics.drawString("Mistakes reduce Focus, so accuracy matters.", rightX, goalY);
        goalY += 36;
        graphics.drawString("Win battles to earn extra score and item drops.", rightX, goalY);
        goalY += 36;
        graphics.drawString("Use BAG items for healing, extra time, and boosts.", rightX, goalY);
        goalY += 36;
        graphics.drawString("Longer streaks increase your scoring pace.", rightX, goalY);

        String banner = gamePanel.getStatusBanner();
        if (banner != null && !banner.isBlank()) {
            graphics.setFont(new Font("SansSerif", Font.BOLD, 18));
            graphics.setColor(new Color(255, 230, 150));
            graphics.drawString("Latest Update: " + banner, rightX, panelY + panelH - 48);
        }
    }

    private void drawLine(Graphics2D graphics, int x, int y, String label, String value) {
        graphics.drawString(String.format("%-10s %s", label + ":", value), x, y);
    }
}
