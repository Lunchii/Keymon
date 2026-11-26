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
    private final int encounterTier;
    private final int trainerIndex;

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

    // Trainer's own team
    private List<Pokemon> team;

    public Trainer(int col, int row, String spritePath) {
        this(col, row, spritePath, 1, 0);
    }

    public Trainer(int col, int row, String spritePath, int encounterTier, int trainerIndex) {
        this.tileX  = col;
        this.tileY  = row;
        this.pixelX = col * TILE_SIZE;
        this.pixelY = row * TILE_SIZE;
        this.encounterTier = Math.max(0, Math.min(3, encounterTier));
        this.trainerIndex = Math.max(0, trainerIndex);

        try {
            String resolvedSpritePath = spritePath == null || spritePath.isBlank()
                ? "images/trainerSprite.png"
                : spritePath;
            spriteSheet = ImageIO.read(new File(resolvedSpritePath));
            currentFrame = spriteSheet.getSubimage(X_OFFSET, SPRITE_ROW, FRAME_WIDTH, FRAME_HEIGHT);
        } catch (Exception e) {
            System.err.println("Trainer: could not load trainer sprite");
        }

        buildTeam();
    }

    private void buildTeam() {
        team = new ArrayList<>();
        switch (encounterTier) {
            case 0:
                buildTutorialTeam();
                break;
            case 1:
                buildLevelOneTeam();
                break;
            case 2:
                buildLevelTwoTeam();
                break;
            default:
                buildLevelThreeTeam();
                break;
        }
    }

    private void buildTutorialTeam() {
        team.add(createPokemon("Puffypuff", -15, -1));
    }

    private void buildLevelOneTeam() {
        switch (trainerIndex % 3) {
            case 0:
                team.add(createPokemon("Puffypuff", -10, 0));
                break;
            case 1:
                team.add(createPokemon("Flamezard", 0, 0));
                team.add(createPokemon("Mimi", -5, 0));
                break;
            default:
                team.add(createPokemon("Starcap", 0, 0));
                team.add(createPokemon("Daxblade", -5, 0));
                break;
        }
    }

    private void buildLevelTwoTeam() {
        switch (trainerIndex % 3) {
            case 0:
                team.add(createPokemon("Flamezard", 8, 2));
                team.add(createPokemon("Daxblade", 6, 2));
                break;
            case 1:
                team.add(createPokemon("Starcap", 10, 2));
                team.add(createPokemon("Mimi", 8, 1));
                team.add(createPokemon("Puffypuff", 0, 2));
                break;
            default:
                team.add(createPokemon("Wukong", 6, 3));
                team.add(createPokemon("Flamezard", 4, 2));
                break;
        }
    }

    private void buildLevelThreeTeam() {
        switch (trainerIndex % 3) {
            case 0:
                team.add(createPokemon("Daxblade", 14, 4));
                team.add(createPokemon("Wukong", 10, 4));
                team.add(createPokemon("Starcap", 12, 4));
                break;
            case 1:
                team.add(createPokemon("Mimi", 18, 3));
                team.add(createPokemon("Flamezard", 12, 4));
                team.add(createPokemon("Daxblade", 10, 5));
                break;
            default:
                team.add(createPokemon("Starcap", 14, 5));
                team.add(createPokemon("Puffypuff", 12, 3));
                team.add(createPokemon("Wukong", 12, 5));
                break;
        }
    }

    private Pokemon createPokemon(String name, int hpDelta, int attackDelta) {
        switch (name) {
            case "Flamezard":
                return freshPokemon("Flamezard", 95 + hpDelta, 26 + attackDelta, "keymonsters/flamezard.png");
            case "Puffypuff":
                return freshPokemon("Puffypuff", 130 + hpDelta, 11 + attackDelta, "keymonsters/puffypuff.png");
            case "Wukong":
                return freshPokemon("Wukong", 60 + hpDelta, 35 + attackDelta, "keymonsters/wukong.png");
            case "Mimi":
                return freshPokemon("Mimi", 110 + hpDelta, 17 + attackDelta, "keymonsters/mimi.png");
            case "Starcap":
                return freshPokemon("Starcap", 75 + hpDelta, 29 + attackDelta, "keymonsters/starcap.png");
            default:
                return freshPokemon("Daxblade", 85 + hpDelta, 31 + attackDelta, "keymonsters/daxblade.png");
        }
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

    public boolean occupies(int targetPixelX, int targetPixelY) {
        return targetPixelX >= pixelX && targetPixelX < pixelX + TILE_SIZE
            && targetPixelY >= pixelY && targetPixelY < pixelY + TILE_SIZE;
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
