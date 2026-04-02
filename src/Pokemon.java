import java.awt.Image;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;

public class Pokemon {
    public String name;
    public int maxHp;
    public int currentHp;
    public int attack;
    public String spritePath;

    // transient = Gson skips this field; loaded lazily via loadSprite()
    public transient Image sprite;

    public Pokemon() {} // required for Gson deserialization

    public Pokemon(String name, int maxHp, int attack, String spritePath) {
        this.name      = name;
        this.maxHp     = maxHp;
        this.currentHp = maxHp;
        this.attack    = attack;
        this.spritePath = spritePath;
    }

    public boolean isFainted() { return currentHp <= 0; }

    public void loadSprite() {
        if (spritePath == null) return;
        try { sprite = ImageIO.read(new File(spritePath)); } catch (Exception ignored) {}
    }

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
