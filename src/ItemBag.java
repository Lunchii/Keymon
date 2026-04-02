import java.util.LinkedHashMap;
import java.util.Map;

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

    public void add(Item item, int amount) {
        counts.put(item, counts.getOrDefault(item, 0) + amount);
    }

    public int getCount(Item item) {
        return counts.getOrDefault(item, 0);
    }

    public boolean has(Item item) {
        return getCount(item) > 0;
    }

    public boolean use(Item item) {
        if (!has(item)) return false;
        counts.put(item, counts.get(item) - 1);
        return true;
    }

    public Map<Item, Integer> getAll() { return counts; }
}
