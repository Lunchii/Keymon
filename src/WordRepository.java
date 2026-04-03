import java.util.Random;

/**
 * Central repository for stage-specific word lists and scoring rules.
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
public final class WordRepository {
    private static final String[] LEVEL_ONE_WORDS = {
        "cat", "dog", "sun", "leaf", "tree", "rock", "pond", "jump", "run", "glow",
        "mint", "drum", "cloud", "grass", "frog", "bug", "play", "smile", "happy", "spark",
        "train", "blue", "green", "track", "slide", "light", "brave", "quick", "piano", "story",
        "water", "castle", "garden", "feather", "ribbon", "kitten", "rainbow", "planet", "rocket", "school"
    };

    private static final String[] LEVEL_TWO_WORDS = {
        "thunder", "monster", "journey", "lantern", "correct", "battle", "puzzle", "potion", "typing", "teacher",
        "velvet", "whisper", "harvest", "compass", "glacier", "crystal", "magnet", "victory", "sparkle", "traffic",
        "library", "orchard", "kingdom", "blanket", "marble", "ginger", "pepper", "program", "painter", "rhythm",
        "blanket", "sunrise", "backpack", "triangle", "adventure", "keyboard", "practice", "festival", "velocity", "starlight"
    };

    private static final String[] LEVEL_THREE_WORDS = {
        "lightning", "adventure", "knowledge", "champion", "keyboarder", "wonderland", "firecracker", "transcript",
        "guardians", "mountainous", "friendship", "telescope", "microscope", "brilliant", "adrenaline", "flashlight",
        "butterflies", "commander", "celebration", "dragonfruit", "typewriter", "labyrinth", "symphonic", "trailblazer",
        "vocabulary", "remarkable", "helicopter", "harmonica", "constellation", "jubilant", "perfection", "enchanted",
        "grandmaster", "treasuremap", "surprising", "dinosaur", "watermelon", "chocolate", "metronome", "adaptation"
    };

    private WordRepository() {
    }

    /**
     * Picks a random word appropriate for the supplied level.
     *
     * @param levelId current stage identifier
     * @param random source of randomness
     * @return a word from the corresponding level pool
     */
    public static String pickWord(int levelId, Random random) {
        String[] bank = getWordBank(levelId);
        return bank[random.nextInt(bank.length)];
    }

    /**
     * Returns the full word bank for a level.
     *
     * @param levelId current stage identifier
     * @return level-specific word array
     */
    public static String[] getWordBank(int levelId) {
        switch (sanitizeLevel(levelId)) {
            case 1:
                return LEVEL_ONE_WORDS;
            case 2:
                return LEVEL_TWO_WORDS;
            default:
                return LEVEL_THREE_WORDS;
        }
    }

    /**
     * Returns the default typing timer for a stage.
     *
     * @param levelId current stage identifier
     * @return seconds given for a typing turn
     */
    public static int getTurnTimeSeconds(int levelId) {
        switch (sanitizeLevel(levelId)) {
            case 1:
                return 12;
            case 2:
                return 10;
            default:
                return 8;
        }
    }

    /**
     * Returns the stage name shown in the UI.
     *
     * @param levelId current stage identifier
     * @return player-facing stage name
     */
    public static String getStageName(int levelId) {
        switch (sanitizeLevel(levelId)) {
            case 1:
                return "Meadow Path";
            case 2:
                return "Shadow Grove";
            default:
                return "Sky Summit";
        }
    }

    /**
     * Returns a short description of the stage difficulty.
     *
     * @param levelId current stage identifier
     * @return player-facing description
     */
    public static String getStageDescription(int levelId) {
        switch (sanitizeLevel(levelId)) {
            case 1:
                return "Short, friendly words for building confidence.";
            case 2:
                return "Longer words and faster turns test your control.";
            default:
                return "Tricky words, fast clocks, and fierce battles.";
        }
    }

    /**
     * Calculates score awarded for a correct word.
     *
     * @param levelId current stage identifier
     * @param word correctly typed word
     * @param combo current combo streak after the successful word
     * @return points awarded
     */
    public static int scoreWord(int levelId, String word, int combo) {
        int sanitizedLevel = sanitizeLevel(levelId);
        int comboBonus = Math.min(6, Math.max(0, combo - 1)) * 8;
        return 20 + (word.length() * (sanitizedLevel * 4 + 2)) + comboBonus;
    }

    /**
     * Returns an enemy damage bonus for a stage.
     *
     * @param levelId current stage identifier
     * @return extra enemy attack damage
     */
    public static int enemyAttackBonus(int levelId) {
        switch (sanitizeLevel(levelId)) {
            case 1:
                return 0;
            case 2:
                return 4;
            default:
                return 8;
        }
    }

    /**
     * Returns how much typing damage a keying mistake causes in a stage.
     *
     * @param levelId current stage identifier
     * @return focus damage per mistake
     */
    public static int mistakeDamage(int levelId) {
        switch (sanitizeLevel(levelId)) {
            case 1:
                return 5;
            case 2:
                return 6;
            default:
                return 7;
        }
    }

    /**
     * Returns how much bonus score is granted for finishing a level.
     *
     * @param levelId current stage identifier
     * @return completion bonus points
     */
    public static int levelCompletionBonus(int levelId) {
        return 250 * sanitizeLevel(levelId);
    }

    /**
     * Sanitizes out-of-range level identifiers.
     *
     * @param levelId current stage identifier
     * @return supported stage identifier in the range 1-3
     */
    public static int sanitizeLevel(int levelId) {
        return Math.max(1, Math.min(3, levelId));
    }
}
