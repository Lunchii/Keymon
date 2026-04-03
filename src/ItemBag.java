import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Inventory container that tracks how many of each {@link Item} the player owns.
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 *
 * <p>Items are indexed by their enum constant. {@link #starterBag()} returns a
 * pre-populated bag with the default starting quantities. The bag is persisted
 * through {@link ProfileStore} as part of the player's save data.</p>
 */
public class ItemBag {
    private Map<Item, Integer> counts = new LinkedHashMap<>();

    public ItemBag() {
        // Start with zero for everything
        for (Item item : Item.values()) counts.put(item, 0);
    }

    /** Give the player a starting set of items. */
    public static ItemBag starterBag() {
        ItemBag bag = new ItemBag();
        bag.add(Item.POTION,       3);
        bag.add(Item.FULL_HEAL,    1);
        bag.add(Item.ATTACK_BOOST, 2);
        bag.add(Item.SLOW_TIME,    2);
        return bag;
    }

    /**
     * Adds the specified quantity of an item to the bag.
     *
     * @param item   the item to add
     * @param amount number of units to add (must be positive)
     */
    public void add(Item item, int amount) {
        counts.put(item, counts.getOrDefault(item, 0) + amount);
    }

    /**
     * Returns how many of the given item the player currently holds.
     *
     * @param item the item to query
     * @return current count, or 0 if the item has never been added
     */
    public int getCount(Item item) {
        return counts.getOrDefault(item, 0);
    }

    /**
     * Returns {@code true} if the player has at least one of the given item.
     *
     * @param item the item to check
     * @return true when count is greater than zero
     */
    public boolean has(Item item) {
        return getCount(item) > 0;
    }

    /**
     * Consumes one unit of the given item, decrementing its count.
     *
     * @param item the item to consume
     * @return true if the item was available and has been consumed; false if the count was zero
     */
    public boolean use(Item item) {
        if (!has(item)) return false;
        counts.put(item, counts.get(item) - 1);
        return true;
    }

    /**
     * Returns the full item-count map (read-write reference).
     *
     * @return map of all items to their current counts
     */
    public Map<Item, Integer> getAll() { return counts; }
}
