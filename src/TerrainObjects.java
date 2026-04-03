import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Collidable terrain tile placed in the world map.
 *
 * <p>Each instance maps a CSV tile value to a sprite file and a pixel
 * bounding box used for collision detection. The static {@link #SPRITE_MAP}
 * associates tile values (1 = tree, 2 = rock, ...) to their image files.</p>
 *
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
public class TerrainObjects {
    private static final int TILE_SIZE = 32;

    // Sprite mapping: tile value -> sprite file path
    private static final Map<Integer, String> SPRITE_MAP = new HashMap<>();

    static {
        SPRITE_MAP.put(1, "tree.png");
        SPRITE_MAP.put(2, "rock.png");
        SPRITE_MAP.put(3, "pokeball.png");
        // You can add more objects as needed
    }

    private BufferedImage sprite;
    private int x, y;
    private int tileValue;
    private boolean solid;

    public TerrainObjects(int col, int row, int tileValue) {
        this(col, row, tileValue, true);
    }

    public TerrainObjects(int col, int row, int tileValue, boolean solid) {
        this.tileValue = tileValue;
        this.x = col * TILE_SIZE;
        this.y = row * TILE_SIZE;
        this.solid = solid;

        // Load sprite based on tile value
        String spritePath = SPRITE_MAP.getOrDefault(tileValue, "tree.png");
        try {
            sprite = ImageIO.read(new File(spritePath));
        } catch (Exception e) {
            System.err.println("Error: Could not load sprite: " + spritePath);
        }
    }

    public void draw(Graphics g) {
        if (sprite != null) {
            g.drawImage(sprite, x, y, TILE_SIZE, TILE_SIZE, null);
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getTileSize() {
        return TILE_SIZE;
    }

    public boolean isSolid() {
        return solid;
    }

    // Check if a pixel position collides with this terrain object
    public boolean collidesWith(int pixelX, int pixelY) {
        return pixelX >= x && pixelX < x + TILE_SIZE &&
                pixelY >= y && pixelY < y + TILE_SIZE;
    }

    public int getTileValue() {
        return tileValue;
    }
}
