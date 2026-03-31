import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Trainer {

    private final int TILE_SIZE = 32;

    private BufferedImage spriteSheet;
    private BufferedImage currentFrame;

    private int x, y;

    private int frameIndex = 0;

    // Dynamic dimensions
    private int frameWidth;
    private int frameHeight;
    private int spriteRow;
    private int xOffset;
    private int gapSize;

    Trainer(int x, int y, String spritePath) {
        this.x = x;
        this.y = y;

        try {
            spriteSheet = ImageIO.read(new File("playerSprite.png"));
            setDirection(10, 35, 50, 14, 29);
        } catch (Exception e) {
            System.err.println("Error: Could not load playerSprite.png");
        }
    }

    // New Helper Method to set the direction from GamePanel
    public void setDirection(int row, int width, int height, int xOffset, int gap) {
        this.spriteRow = row;
        this.frameWidth = width;
        this.frameHeight = height;
        this.gapSize = gap;
        this.xOffset = xOffset;
        updateSprite(); // Refresh the frame immediately
    }

    // Centralized sprite cropping so idle and moving frames both use it
    private void updateSprite() {
        int startX = xOffset + (frameIndex * (frameWidth + gapSize));
        currentFrame = spriteSheet.getSubimage(startX, spriteRow, frameWidth, frameHeight);
    }

    public void draw(Graphics g) {
        if (currentFrame != null) {
            int drawY = y - (frameHeight - TILE_SIZE);
            g.drawImage(currentFrame, x, drawY, null);
        }
    }
}
