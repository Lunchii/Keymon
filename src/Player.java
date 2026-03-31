import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Player {
    // Sprite Data
    private BufferedImage spriteSheet;
    private BufferedImage currentFrame;

    // Dynamic dimensions
    private int frameWidth;
    private int frameHeight;
    private int spriteRow;
    private int xOffset;
    private int gapSize;

    private final int TILE_SIZE = 32;
    private int animationTick = 0;
    private int frameIndex = 0;
    private final int ANIMATION_SPEED = 3;

    // Grid Movement Data
    public int x, y;
    public int targetX, targetY;
    public boolean isMoving = false;
    private final int SPEED = 4;;

    public Player(int startTileX, int startTileY, int tileSize) {
        this.x = startTileX * tileSize;
        this.y = startTileY * tileSize;
        this.targetX = x;
        this.targetY = y;

        try {
            spriteSheet = ImageIO.read(new File("spritesheet.png"));
            setDirection(10, 35, 50, 14, 29);
        } catch (Exception e) {
            System.err.println("Error: Could not load spritesheet.png");
        }
    }

    // Constructor for loading from a save file (Added for Issue #14)
    public Player(int pixelX, int pixelY, int row, int tileSize) {
        // Direct pixel assignment instead of tile-based calculation
        this.x = pixelX;
        this.y = pixelY;
        this.targetX = x;
        this.targetY = y;
        this.spriteRow = row;

        try {
            spriteSheet = ImageIO.read(new File("spritesheet.png"));
            // Re-initialize with Isaac's animation parameters
            setDirection(row, 35, 50, 14, 29);
        } catch (Exception e) {
            System.err.println("Error: Could not load spritesheet during save loading");
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

    public void startAnimation() {
        this.frameIndex = 1; // Start on the first walking step
        this.animationTick = 0;
        updateSprite(); // Crop the "stepping" frame immediately
    }

    public void update() {
        if (isMoving) {
            // 1. Move (8 frames total to finish)
            if (x < targetX)
                x += SPEED;
            else if (x > targetX)
                x -= SPEED;
            else if (y < targetY)
                y += SPEED;
            else if (y > targetY)
                y -= SPEED;

            // 2. Animation (Needs to be fast!)
            animationTick++;
            if (animationTick >= ANIMATION_SPEED) {
                animationTick = 0;
                frameIndex++;
                if (frameIndex >= 4)
                    frameIndex = 0; // Cycle through all 8
                updateSprite();
            }

            // 3. Arrival
            if (x == targetX && y == targetY) {
                isMoving = false;
                // Don't reset frameIndex here if you want a smooth transition,
                // but usually, we reset to 0 for the "Idle" pose.
                frameIndex = 0;
                updateSprite();
            }
        }
    }

    public void moveUp() {
        this.targetY -= TILE_SIZE;
        this.isMoving = true;
        this.setDirection(200, 35, 52, 14, 29);
        this.startAnimation();
    }

    public void moveDown() {
        this.targetY += TILE_SIZE;
        this.isMoving = true;
        this.setDirection(10, 35, 50, 14, 29);
        this.startAnimation();
    }

    public void moveLeft() {
        this.targetX -= TILE_SIZE;
        this.isMoving = true;
        this.setDirection(76, 33, 48, 15, 32);
        this.startAnimation();
    }

    public void moveRight() {
        this.targetX += TILE_SIZE;
        this.isMoving = true;
        this.setDirection(140, 33, 48, 15, 32);
        this.startAnimation();
    }

    public void draw(Graphics g) {
        if (currentFrame != null) {
            int drawY = y - (frameHeight - TILE_SIZE);
            g.drawImage(currentFrame, x, drawY, null);
        }
    }

    // Getter for the current sprite row to save the player's facing direction
    public int getSpriteRow() {
        return this.spriteRow;
    }
}