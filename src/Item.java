/**
 * Consumable battle powerups stored in the player's {@link ItemBag}.
 *
 * <p>Each constant carries a human-readable {@code displayName}, a short
 * {@code description} shown in the bag UI, and a {@code color} used to
 * tint the item's button.</p>
 */
public enum Item {
    POTION      ("Potion",       "Heals your Keymon for 30 HP.",          new java.awt.Color(80, 200, 120)),
    FULL_HEAL   ("Full Heal",    "Fully restores your Keymon's HP.",       new java.awt.Color(60, 180, 220)),
    ATTACK_BOOST("Attack Boost", "Doubles your attack damage this turn.",  new java.awt.Color(220, 160, 40)),
    SLOW_TIME   ("Slow Time",    "Adds 5 seconds to the typing timer.",    new java.awt.Color(160, 80, 220));

    public final String displayName;
    public final String description;
    public final java.awt.Color color;

    Item(String displayName, String description, java.awt.Color color) {
        this.displayName = displayName;
        this.description = description;
        this.color       = color;
    }
}
