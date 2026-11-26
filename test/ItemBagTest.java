import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ItemBag}.
 * Covers item counts, adding, using, and the starter bag defaults.
 *
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 * Generated with assistance from Claude (Anthropic).
 */
class ItemBagTest {

    private ItemBag bag;

    @BeforeEach
    void setup() {
        bag = new ItemBag();
    }

    // ------------------------------------------------- Empty bag

    @Test
    void newBagHasZeroForAllItems() {
        for (Item item : Item.values()) {
            assertEquals(0, bag.getCount(item));
        }
    }

    @Test
    void newBagHasNoItems() {
        for (Item item : Item.values()) {
            assertFalse(bag.has(item));
        }
    }

    // ------------------------------------------------- Adding items

    @Test
    void addIncreasesCount() {
        bag.add(Item.POTION, 3);
        assertEquals(3, bag.getCount(Item.POTION));
    }

    @Test
    void addMultipleTimesAccumulates() {
        bag.add(Item.POTION, 2);
        bag.add(Item.POTION, 3);
        assertEquals(5, bag.getCount(Item.POTION));
    }

    @Test
    void addOneMakesHasReturnTrue() {
        bag.add(Item.SLOW_TIME, 1);
        assertTrue(bag.has(Item.SLOW_TIME));
    }

    @Test
    void addDoesNotAffectOtherItems() {
        bag.add(Item.POTION, 5);
        assertEquals(0, bag.getCount(Item.FULL_HEAL));
        assertEquals(0, bag.getCount(Item.ATTACK_BOOST));
        assertEquals(0, bag.getCount(Item.SLOW_TIME));
    }

    // ------------------------------------------------- Using items

    @Test
    void useDecreasesCount() {
        bag.add(Item.POTION, 2);
        bag.use(Item.POTION);
        assertEquals(1, bag.getCount(Item.POTION));
    }

    @Test
    void useReturnsTrueWhenAvailable() {
        bag.add(Item.POTION, 1);
        assertTrue(bag.use(Item.POTION));
    }

    @Test
    void useReturnsFalseWhenEmpty() {
        assertFalse(bag.use(Item.POTION));
    }

    @Test
    void useLastItemMakesHasReturnFalse() {
        bag.add(Item.FULL_HEAL, 1);
        bag.use(Item.FULL_HEAL);
        assertFalse(bag.has(Item.FULL_HEAL));
    }

    @Test
    void useOnEmptyDoesNotGoNegative() {
        bag.use(Item.ATTACK_BOOST);
        assertEquals(0, bag.getCount(Item.ATTACK_BOOST));
    }

    // ------------------------------------------------- Starter bag

    @Test
    void starterBagHasPotions() {
        ItemBag starter = ItemBag.starterBag();
        assertTrue(starter.getCount(Item.POTION) > 0);
    }

    @Test
    void starterBagHasSlowTime() {
        ItemBag starter = ItemBag.starterBag();
        assertTrue(starter.getCount(Item.SLOW_TIME) > 0);
    }

    @Test
    void starterBagHasAttackBoost() {
        ItemBag starter = ItemBag.starterBag();
        assertTrue(starter.getCount(Item.ATTACK_BOOST) > 0);
    }

    @Test
    void starterBagHasFullHeal() {
        ItemBag starter = ItemBag.starterBag();
        assertTrue(starter.getCount(Item.FULL_HEAL) > 0);
    }

    @Test
    void starterBagTotalItemsCorrect() {
        ItemBag starter = ItemBag.starterBag();
        assertEquals(3, starter.getCount(Item.POTION));
        assertEquals(1, starter.getCount(Item.FULL_HEAL));
        assertEquals(2, starter.getCount(Item.ATTACK_BOOST));
        assertEquals(2, starter.getCount(Item.SLOW_TIME));
    }
}
