import org.junit.jupiter.api.Test;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link WordRepository}.
 * Covers word selection, timer values, scoring, and level sanitization.
 *
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
class WordRepositoryTest {

    // ---------------------------------------------------- Scoring

    @Test
    void scoreIsPositive() {
        assertTrue(WordRepository.scoreWord(1, "cat", 1) > 0);
    }

    // ------------------------------------------------- Level sanitization

    @Test
    void validLevelsUnchanged() {
        assertEquals(1, WordRepository.sanitizeLevel(1));
        assertEquals(2, WordRepository.sanitizeLevel(2));
        assertEquals(3, WordRepository.sanitizeLevel(3));
    }

    // --- The following tests were generated with assistance from Claude (Anthropic) ---

    @Test
    void pickWordReturnsNonNull() {
        assertNotNull(WordRepository.pickWord(1, new Random()));
    }

    @Test
    void pickWordReturnsNonEmpty() {
        assertFalse(WordRepository.pickWord(1, new Random()).isEmpty());
    }

    @Test
    void pickWordLevel1IsShort() {
        String word = WordRepository.pickWord(1, new Random(0));
        assertTrue(word.length() <= 10);
    }

    @Test
    void pickWordLevel3CanBeLong() {
        String[] bank = WordRepository.getWordBank(3);
        boolean hasLongWord = false;
        for (String word : bank) {
            if (word.length() > 10) {
                hasLongWord = true;
                break;
            }
        }
        assertTrue(hasLongWord);
    }

    @Test
    void level1HasLongestTimer() {
        assertTrue(WordRepository.getTurnTimeSeconds(1) > WordRepository.getTurnTimeSeconds(2));
    }

    @Test
    void level2TimerFasterThanLevel3() {
        assertTrue(WordRepository.getTurnTimeSeconds(2) > WordRepository.getTurnTimeSeconds(3));
    }

    @Test
    void allTimersPositive() {
        assertTrue(WordRepository.getTurnTimeSeconds(1) > 0);
        assertTrue(WordRepository.getTurnTimeSeconds(2) > 0);
        assertTrue(WordRepository.getTurnTimeSeconds(3) > 0);
    }

    @Test
    void longerWordScoresMore() {
        int shortScore = WordRepository.scoreWord(1, "cat", 1);
        int longScore = WordRepository.scoreWord(1, "butterfly", 1);
        assertTrue(longScore > shortScore);
    }

    @Test
    void higherLevelScoresMore() {
        int level1Score = WordRepository.scoreWord(1, "test", 1);
        int level3Score = WordRepository.scoreWord(3, "test", 1);
        assertTrue(level3Score > level1Score);
    }

    @Test
    void comboIncreasesScore() {
        int noCombo = WordRepository.scoreWord(1, "test", 1);
        int withCombo = WordRepository.scoreWord(1, "test", 5);
        assertTrue(withCombo > noCombo);
    }

    @Test
    void levelBelowOneClampedToOne() {
        assertEquals(1, WordRepository.sanitizeLevel(0));
        assertEquals(1, WordRepository.sanitizeLevel(-5));
    }

    @Test
    void levelAboveThreeClampedToThree() {
        assertEquals(3, WordRepository.sanitizeLevel(4));
        assertEquals(3, WordRepository.sanitizeLevel(100));
    }

    @Test
    void completionBonusPositive() {
        assertTrue(WordRepository.levelCompletionBonus(1) > 0);
    }

    @Test
    void higherLevelHasHigherBonus() {
        assertTrue(WordRepository.levelCompletionBonus(3) > WordRepository.levelCompletionBonus(1));
    }
}
