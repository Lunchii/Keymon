import java.awt.Image;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Data model for a Keymon creature.
 *
 * <p>Stores base stats (HP and attack), the current HP, and a lazy-loaded
 * sprite image. The static pool ({@link #getPool()}) defines the six
 * creatures available in the game. Gson skips the transient {@code sprite}
 * field so it is re-loaded via {@link #loadSprite()} after deserialisation.</p>
 *
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
public class Pokemon {
    public String name;
    public int maxHp;
    public int currentHp;
    public int attack;
    public String spritePath;

    // transient = Gson skips this field; loaded lazily via loadSprite()
    public transient Image sprite;

    /** No-arg constructor required for Gson deserialisation. */
    public Pokemon() {}

    /**
     * Creates a Pokemon with full HP.
     *
     * @param name       display name shown in battle
     * @param maxHp      maximum (and starting) hit points
     * @param attack     base attack stat used for damage calculation
     * @param spritePath relative path to the sprite image file
     */
    public Pokemon(String name, int maxHp, int attack, String spritePath) {
        this.name      = name;
        this.maxHp     = maxHp;
        this.currentHp = maxHp;
        this.attack    = attack;
        this.spritePath = spritePath;
    }

    /**
     * Returns {@code true} if this Pokemon has no remaining HP.
     *
     * @return true when currentHp is zero or less
     */
    public boolean isFainted() { return currentHp <= 0; }

    /** Loads the sprite image from {@code spritePath}. Silently ignored if the file is missing. */
    public void loadSprite() {
        if (spritePath == null) return;
        try { sprite = ImageIO.read(new File(spritePath)); } catch (Exception ignored) {}
    }

    /**
     * Returns the sprite image, loading it lazily on first access.
     *
     * @return the sprite Image, or {@code null} if it could not be loaded
     */
    public Image getSprite() {
        if (sprite == null) loadSprite();
        return sprite;
    }

    // ── Pool ────────────────────────────────────────────────────────────────
    private static final Pokemon[] POOL = {
        new Pokemon("Flamezard",  95,  26, "keymonsters/flamezard.png"),
        new Pokemon("Puffypuff", 130,  11, "keymonsters/puffypuff.png"),
        new Pokemon("Wukong",     60,  35, "keymonsters/wukong.png"),
        new Pokemon("Mimi",      110,  17, "keymonsters/mimi.png"),
        new Pokemon("Starcap",    75,  29, "keymonsters/starcap.png"),
        new Pokemon("Daxblade",   85,  31, "keymonsters/daxblade.png")
    };

    /** All available monsters (for team selection UI). */
    public static List<Pokemon> getPool() {
        List<Pokemon> list = new java.util.ArrayList<>();
        for (Pokemon t : POOL) list.add(t);
        return list;
    }

    /** Return a fresh copy of a random Pokemon at full HP, with sprite loaded. */
    public static Pokemon random() {
        Pokemon template = POOL[(int) (Math.random() * POOL.length)];
        Pokemon p = new Pokemon(template.name, template.maxHp, template.attack, template.spritePath);
        p.loadSprite();
        return p;
    }

    /** Shallow copy used when adding a caught Pokemon to the party. */
    public Pokemon copy() {
        Pokemon p = new Pokemon(name, maxHp, attack, spritePath);
        p.currentHp = currentHp;
        p.sprite = sprite;
        return p;
    }
}
