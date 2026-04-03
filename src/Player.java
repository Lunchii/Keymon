import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Represents the player character in the world map.
 *
 * <p>Handles sprite animation, tile-based grid movement, and terrain collision
 * detection. Movement is initiated externally (via {@link InputHandler}) by
 * setting {@code targetX}/{@code targetY}; the {@code update()} method
 * interpolates toward the target each game tick.</p>
 *
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
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

    // Terrain collision
    private List<TerrainObjects> terrainObjects;

    /**
     * Creates a player positioned at a tile coordinate.
     *
     * @param startTileX tile column for the starting position
     * @param startTileY tile row for the starting position
     * @param tileSize   pixel size of one tile
     */
    public Player(int startTileX, int startTileY, int tileSize) {
        this.x = startTileX * tileSize;
        this.y = startTileY * tileSize;
        this.targetX = x;
        this.targetY = y;

        try {
            spriteSheet = ImageIO.read(new File("images/playerSprite.png"));
            setDirection(10, 35, 50, 14, 29);
        } catch (Exception e) {
            System.err.println("Error: Could not load playerSprite.png");
        }
    }

    /**
     * Creates a player at an exact pixel position loaded from a save file.
     *
     * @param pixelX   saved pixel X position
     * @param pixelY   saved pixel Y position
     * @param row      sprite sheet row representing the facing direction
     * @param tileSize pixel size of one tile (used for movement calculations)
     */
    public Player(int pixelX, int pixelY, int row, int tileSize) {
        // Direct pixel assignment instead of tile-based calculation
        this.x = pixelX;
        this.y = pixelY;
        this.targetX = x;
        this.targetY = y;
        this.spriteRow = row;

        try {
            spriteSheet = ImageIO.read(new File("images/playerSprite.png"));
            // Re-initialize with Isaac's animation parameters
            setDirection(row, 35, 50, 14, 29);
        } catch (Exception e) {
            System.err.println("Error: Could not load playerSprite during save loading");
        }
    }

    /**
     * Sets the sprite sheet parameters for the current facing direction.
     *
     * @param row     sprite sheet Y offset for the direction row
     * @param width   frame width in pixels
     * @param height  frame height in pixels
     * @param xOffset X offset of the first frame within the sheet
     * @param gap     pixel gap between consecutive frames
     */
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

    /** Resets the animation to the first walking frame, ready for a new move. */
    public void startAnimation() {
        this.frameIndex = 1; // Start on the first walking step
        this.animationTick = 0;
        updateSprite(); // Crop the "stepping" frame immediately
    }

    /** Advances the player one game tick: moves toward the target tile and advances the animation frame. */
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

    /**
     * Draws the current animation frame at the player's world position.
     *
     * @param g graphics context supplied by the parent panel's paintComponent
     */
    public void draw(Graphics g) {
        if (currentFrame != null) {
            int drawY = y - (frameHeight - TILE_SIZE);
            g.drawImage(currentFrame, x, drawY, null);
        }
    }

    /**
     * Returns the current sprite sheet row, used to persist the facing direction in save files.
     *
     * @return sprite row index
     */
    public int getSpriteRow() {
        return this.spriteRow;
    }

    /**
     * Provides the terrain collision list used by {@link #isPositionWalkable(int, int)}.
     *
     * @param terrainObjects list of solid terrain tiles from the current map
     */
    public void setTerrainObjects(List<TerrainObjects> terrainObjects) {
        this.terrainObjects = terrainObjects;
    }

    /**
     * Returns {@code true} if the given pixel position does not collide with any terrain object.
     *
     * @param pixelX target X position in world pixels
     * @param pixelY target Y position in world pixels
     * @return true when the position is free of solid terrain
     */
    public boolean isPositionWalkable(int pixelX, int pixelY) {
        if (terrainObjects == null) {
            return true;
        }

        for (TerrainObjects terrain : terrainObjects) {
            if (!terrain.isSolid()) {
                continue;
            }
            if (terrain.collidesWith(pixelX, pixelY)) {
                return false;
            }
        }
        return true;
    }
}