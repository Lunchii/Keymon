import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * NPC trainer that patrols a fixed tile position in the world map.
 *
 * <p>Uses a simple vertical line-of-sight check ({@link #canSeePlayer}) to
 * detect when the player steps into its sight range and trigger a battle.
 * Once defeated, the trainer is flagged and will no longer initiate battles.</p>
 *
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
public class Trainer {
    private final int TILE_SIZE = 32;

    private BufferedImage spriteSheet;
    private BufferedImage currentFrame;

    public final int tileX, tileY; // grid position
    private final int pixelX, pixelY;

    // Trainer faces downward (spriteRow 10 = down-facing idle)
    private static final int SPRITE_ROW   = 10;
    private static final int FRAME_WIDTH  = 35;
    private static final int FRAME_HEIGHT = 50;
    private static final int X_OFFSET     = 14;
    private static final int GAP          = 29;

    // Line-of-sight: trainer faces DOWN, sight range in tiles
    public static final int SIGHT_RANGE = 4;

    private boolean defeated = false;

    // Trainer's own team of 3
    private List<Pokemon> team;

    public Trainer(int col, int row, String spritePath) {
        this.tileX  = col;
        this.tileY  = row;
        this.pixelX = col * TILE_SIZE;
        this.pixelY = row * TILE_SIZE;

        try {
            spriteSheet = ImageIO.read(new File("images/playerSprite.png"));
            currentFrame = spriteSheet.getSubimage(X_OFFSET, SPRITE_ROW, FRAME_WIDTH, FRAME_HEIGHT);
        } catch (Exception e) {
            System.err.println("Trainer: could not load playerSprite.png");
        }

        buildTeam();
    }

    private void buildTeam() {
        team = new ArrayList<>();
        team.add(freshPokemon("Flamezard",  95, 26, "keymonsters/flamezard.png"));
        team.add(freshPokemon("Starcap",    75, 29, "keymonsters/starcap.png"));
        team.add(freshPokemon("Daxblade",   85, 31, "keymonsters/daxblade.png"));
    }

    private Pokemon freshPokemon(String name, int hp, int atk, String path) {
        Pokemon p = new Pokemon(name, hp, atk, path);
        p.loadSprite();
        return p;
    }

    /** Reset trainer team to full HP (called when player re-enters the area after reload). */
    public void resetTeam() { buildTeam(); }

    public List<Pokemon> getTeam() { return team; }

    public boolean isDefeated() { return defeated; }
    public void setDefeated(boolean d) { this.defeated = d; }

    /**
     * Returns true if the player (at playerTileX, playerTileY) is in the
     * trainer's downward line of sight and the trainer hasn't been defeated.
     */
    public boolean canSeePlayer(int playerTileX, int playerTileY) {
        if (defeated) return false;
        // Trainer faces down: same column, player is below trainer, within range
        if (playerTileX != tileX) return false;
        int dy = playerTileY - tileY;
        return dy > 0 && dy <= SIGHT_RANGE;
    }

    public void draw(Graphics g) {
        if (currentFrame == null) return;
        int drawY = pixelY - (FRAME_HEIGHT - TILE_SIZE);
        if (defeated) {
            // Draw slightly transparent to indicate defeated
            Graphics2D g2 = (Graphics2D) g;
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f);
            g2.setComposite(ac);
            g2.drawImage(currentFrame, pixelX, drawY, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        } else {
            g.drawImage(currentFrame, pixelX, drawY, null);
        }
    }

    public int getX() {
        return pixelX;
    }

    public int getY() {
        return pixelY;
    }
}
