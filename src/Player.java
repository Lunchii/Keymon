import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Player {
    // Sprite Data
    private BufferedImage spriteSheet;
    private BufferedImage currentFrame;

    private int frameWidth;
    private int frameHeight;
    private int animationTick = 0;
    private int frameIndex = 0;
    private final int ANIMATION_SPEED = 10;
    private int spriteRow = 140; // Default row (Side-view based on your previous code)

    // Grid Movement Data
    public int x, y;
    public int targetX, targetY;
    public boolean isMoving = false;
    private final int SPEED = 4;
    private final int TILE_SIZE = 32;

    public Player(int startTileX, int startTileY, int tileSize) {
        this.x = startTileX * tileSize;
        this.y = startTileY * tileSize;
        this.targetX = x;
        this.targetY = y;

        try {
            spriteSheet = ImageIO.read(new File("spritesheet.png"));
            setDirection(10, 34, 50);
        } catch (Exception e) {
            System.err.println("Error: Could not load spritesheet.png");
        }
    }

    // New Helper Method to set the direction from GamePanel
    public void setDirection(int row, int width, int height) {
        this.spriteRow = row;
        this.frameWidth = width;
        this.frameHeight = height;
        updateSprite(); // Refresh the frame immediately
    }

    // Centralized sprite cropping so idle and moving frames both use it
    private void updateSprite() {
        int startX = 15 + (frameIndex * (frameWidth + 32));
        currentFrame = spriteSheet.getSubimage(startX, spriteRow, frameWidth, frameHeight);
    }

    public void update() {
        if (isMoving) {
            // 1. Move towards target
            if (x < targetX)
                x += SPEED;
            else if (x > targetX)
                x -= SPEED;
            else if (y < targetY)
                y += SPEED;
            else if (y > targetY)
                y -= SPEED;

            // 2. Animation logic
            animationTick++;
            if (animationTick >= ANIMATION_SPEED) {
                animationTick = 0;
                frameIndex++;
                if (frameIndex >= 8)
                    frameIndex = 0;
            }

            // 3. Update the image
            updateSprite();

            if (x == targetX && y == targetY) {
                isMoving = false;
                frameIndex = 0; // Reset to idle frame of the CURRENT direction
                updateSprite();
            }
        }
    }

    public void draw(Graphics g) {
        if (currentFrame != null) {
            int drawY = y - (frameHeight - TILE_SIZE);
            g.drawImage(currentFrame, x, drawY, null);
        }
    }
}