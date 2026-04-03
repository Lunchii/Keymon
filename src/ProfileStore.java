import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles local persistence for accounts, progress, statistics, inventory, and high scores.
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
public final class ProfileStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String SAVE_SUFFIX = "_profile.json";
    private static final String HIGHSCORE_FILE = "highscores.json";
    private static final String PARENT_PIN = "2212";
    private static final int DEFAULT_X = 320;
    private static final int DEFAULT_Y = 320;
    private static final int DEFAULT_ROW = 10;
    private static final int MAX_LEVELS = 3;

    private ProfileStore() {
    }

    /**
     * Validates the hardcoded teacher/parent pin.
     *
     * @param pin entered pin
     * @return true when the pin is correct
     */
    public static boolean verifyParentPin(String pin) {
        return PARENT_PIN.equals(pin);
    }

    /**
     * Validates account creation input and returns a user-facing message on failure.
     *
     * @param username chosen username
     * @param password chosen password
     * @return null when the input is valid, otherwise an explanation
     */
    public static String validateNewAccount(String username, String password) {
        String cleanUsername = sanitizeUsername(username);
        if (cleanUsername.isEmpty()) {
            return "Please enter a username.";
        }
        if (!cleanUsername.matches("[A-Za-z0-9_\\-]{3,16}")) {
            return "Use 3-16 letters, numbers, dashes, or underscores for the username.";
        }
        if (password == null || password.length() < 4) {
            return "Please choose a password with at least 4 characters.";
        }
        if (accountExists(cleanUsername)) {
            return "That username is already taken.";
        }
        return null;
    }

    /**
     * Creates a new local account file.
     *
     * @param username account username
     * @param password account password
     * @return true if the account was created
     */
    public static boolean createAccount(String username, String password) {
        String cleanUsername = sanitizeUsername(username);
        String validationMessage = validateNewAccount(cleanUsername, password);
        if (validationMessage != null) {
            return false;
        }

        SaveData data = createDefaultData(cleanUsername, password);
        return writeData(cleanUsername, data);
    }

    /**
     * Returns whether the supplied username already has a local account.
     *
     * @param username account username
     * @return true if the save file exists
     */
    public static boolean accountExists(String username) {
        return getSaveFile(sanitizeUsername(username)).exists();
    }

    /**
     * Checks a username/password pair.
     *
     * @param username account username
     * @param password account password
     * @return true if the credentials match
     */
    public static boolean authenticate(String username, String password) {
        String cleanUsername = sanitizeUsername(username);
        SaveData data = loadData(cleanUsername);
        if (data == null) {
            return false;
        }

        String attempt = password == null ? "" : password;
        String storedPassword = (data.password == null || data.password.isBlank()) ? cleanUsername : data.password;
        if (data.password == null || data.password.isBlank()) {
            data.password = storedPassword;
            writeData(cleanUsername, data);
        }
        return storedPassword.equals(attempt);
    }

    /**
     * Resets the password for an existing account.
     *
     * @param username target username
     * @param newPassword replacement password
     * @return true if the password was updated
     */
    public static boolean resetPassword(String username, String newPassword) {
        String cleanUsername = sanitizeUsername(username);
        if (newPassword == null || newPassword.length() < 4) {
            return false;
        }
        SaveData data = loadData(cleanUsername);
        if (data == null) {
            return false;
        }
        data.password = newPassword;
        return writeData(cleanUsername, data);
    }

    /**
     * Resets stored gameplay statistics and progress for an account while keeping the login.
     *
     * @param username target username
     * @return true if the reset completed
     */
    public static boolean resetStatistics(String username) {
        String cleanUsername = sanitizeUsername(username);
        SaveData data = loadData(cleanUsername);
        if (data == null) {
            return false;
        }

        String password = data.password == null || data.password.isBlank() ? cleanUsername : data.password;
        SaveData fresh = createDefaultData(cleanUsername, password);
        return writeData(cleanUsername, fresh);
    }

    /**
     * Deletes an account and removes it from the high score table.
     *
     * @param username target username
     * @return true if the file existed and was removed
     */
    public static boolean deleteAccount(String username) {
        String cleanUsername = sanitizeUsername(username);
        File file = getSaveFile(cleanUsername);
        boolean deleted = file.exists() && file.delete();
        if (deleted) {
            removeFromHighScoreTable(cleanUsername);
        }
        return deleted;
    }

    /**
     * Loads a lightweight profile snapshot for UI screens.
     *
     * @param username account username
     * @return profile snapshot with derived statistics and insights
     */
    public static ProfileData loadProfile(String username) {
        String cleanUsername = sanitizeUsername(username);
        SaveData data = loadData(cleanUsername);
        if (data == null) {
            data = createDefaultData(cleanUsername, cleanUsername);
        }

        ProfileData profile = new ProfileData(
            cleanUsername,
            data.highScore,
            data.currentLevel,
            data.unlockedLevel,
            data.getAverageWpm(),
            data.peakWpm,
            data.getAccuracyPercent(),
            data.errorCount,
            data.totalTimePlayedSeconds,
            data.wordsTyped,
            data.correctChars,
            data.totalTypedChars,
            data.longestStreak,
            data.powerupsUsed,
            data.gamesPlayed,
            data.levelWins
        );
        profile.achievements = ProfileInsights.buildAchievements(profile);
        profile.recommendations = ProfileInsights.buildRecommendations(profile);
        return profile;
    }

    /**
     * Returns all known usernames in alphabetical order.
     *
     * @return stored usernames
     */
    public static List<String> getAllUsernames() {
        List<String> usernames = new ArrayList<>();
        File[] files = new File(".").listFiles((dir, name) -> name.endsWith(SAVE_SUFFIX));
        if (files == null) {
            return usernames;
        }

        for (File file : files) {
            usernames.add(file.getName().replace(SAVE_SUFFIX, ""));
        }
        usernames.sort(String.CASE_INSENSITIVE_ORDER);
        return usernames;
    }

    /**
     * Saves an in-progress game using default inventory handling.
     *
     * @param username active username
     * @param x saved x position
     * @param y saved y position
     * @param row saved facing row
     * @param highScore best score so far
     * @param currentLevel current level identifier
     */
    public static void saveProgress(String username, int x, int y, int row, int highScore, int currentLevel) {
        saveProgress(username, 1, x, y, row, highScore, currentLevel, null, null);
    }

    /**
     * Saves an in-progress game using the default level slot.
     *
     * @param username active username
     * @param x saved x position
     * @param y saved y position
     * @param row saved facing row
     * @param highScore best score so far
     * @param currentLevel current level identifier
     * @param party active party snapshot
     */
    public static void saveProgress(String username, int x, int y, int row, int highScore,
                                    int currentLevel, List<Pokemon> party) {
        saveProgress(username, 1, x, y, row, highScore, currentLevel, party, null);
    }

    /**
     * Saves an in-progress game.
     *
     * @param username active username
     * @param levelId level identifier being played
     * @param x saved x position
     * @param y saved y position
     * @param row saved facing row
     * @param highScore best score so far
     * @param currentLevel current level identifier
     * @param party active party snapshot
     */
    public static void saveProgress(String username, int levelId, int x, int y, int row,
                                    int highScore, int currentLevel, List<Pokemon> party) {
        saveProgress(username, levelId, x, y, row, highScore, currentLevel, party, null);
    }

    /**
     * Saves an in-progress game together with the current inventory.
     *
     * @param username active username
     * @param levelId level identifier being played
     * @param x saved x position
     * @param y saved y position
     * @param row saved facing row
     * @param highScore best score so far
     * @param currentLevel current level identifier
     * @param party active party snapshot
     * @param itemBag current inventory snapshot
     */
    public static void saveProgress(String username, int levelId, int x, int y, int row,
                                    int highScore, int currentLevel, List<Pokemon> party, ItemBag itemBag) {
        String cleanUsername = sanitizeUsername(username);
        if (cleanUsername.isEmpty()) {
            return;
        }

        SaveData data = loadData(cleanUsername);
        if (data == null) {
            data = createDefaultData(cleanUsername, cleanUsername);
        }

        int levelIndex = clampLevelIndex(levelId);
        data.levelX[levelIndex] = x;
        data.levelY[levelIndex] = y;
        data.levelSpriteRow[levelIndex] = row;
        data.currentLevel = Math.max(1, Math.min(MAX_LEVELS, currentLevel));
        data.highScore = Math.max(data.highScore, highScore);

        if (party != null) {
            data.party = serializeParty(party);
        }
        if (itemBag != null) {
            data.itemCounts = serializeItemBag(itemBag);
        }

        writeData(cleanUsername, data);
    }

    /**
     * Loads a player position snapshot for the requested level.
     *
     * @param username active username
     * @return lightweight position snapshot
     */
    public static PlayerProgress loadProgress(String username) {
        return loadProgress(username, 1);
    }

    /**
     * Loads a player position snapshot for the requested level.
     *
     * @param username active username
     * @param levelId requested level identifier
     * @return lightweight position snapshot
     */
    public static PlayerProgress loadProgress(String username, int levelId) {
        SaveData data = loadData(sanitizeUsername(username));
        if (data == null) {
            return new PlayerProgress(DEFAULT_X, DEFAULT_Y, DEFAULT_ROW, 0, 1);
        }

        int levelIndex = clampLevelIndex(levelId);
        return new PlayerProgress(
            data.levelX[levelIndex],
            data.levelY[levelIndex],
            data.levelSpriteRow[levelIndex],
            data.highScore,
            data.currentLevel
        );
    }

    /**
     * Returns the player's current party, or an empty list if none has been saved.
     *
     * @param username active username
     * @return saved party
     */
    public static List<Pokemon> loadParty(String username) {
        SaveData data = loadData(sanitizeUsername(username));
        if (data == null || data.party == null) {
            return new ArrayList<>();
        }
        return deserializeParty(data.party);
    }

    /**
     * Loads the current item bag for the player.
     *
     * @param username active username
     * @return saved inventory, or the starter bag for first-time players
     */
    public static ItemBag loadItemBag(String username) {
        SaveData data = loadData(sanitizeUsername(username));
        if (data == null || data.itemCounts == null || data.itemCounts.isEmpty()) {
            return ItemBag.starterBag();
        }

        ItemBag bag = new ItemBag();
        for (Item item : Item.values()) {
            Integer count = data.itemCounts.get(item.name());
            if (count != null && count > 0) {
                bag.add(item, count);
            }
        }
        return bag;
    }

    /**
     * Returns the highest stage unlocked by the player.
     *
     * @param username active username
     * @return highest unlocked stage
     */
    public static int getHighestUnlockedLevel(String username) {
        SaveData data = loadData(sanitizeUsername(username));
        if (data == null) {
            return 1;
        }
        return data.unlockedLevel;
    }

    /**
     * Unlocks the next level after a successful completion.
     *
     * @param username active username
     * @param completedLevel completed stage identifier
     */
    public static void markLevelCompleted(String username, int completedLevel) {
        String cleanUsername = sanitizeUsername(username);
        SaveData data = loadData(cleanUsername);
        if (data == null) {
            data = createDefaultData(cleanUsername, cleanUsername);
        }

        int nextLevel = Math.min(MAX_LEVELS, completedLevel + 1);
        data.unlockedLevel = Math.max(data.unlockedLevel, nextLevel);
        data.currentLevel = Math.max(data.currentLevel, nextLevel);
        writeData(cleanUsername, data);
    }

    /**
     * Records score and typing statistics after a level attempt.
     *
     * @param username active username
     * @param session session summary to persist
     */
    public static void recordLevelSummary(String username, SessionStats session) {
        String cleanUsername = sanitizeUsername(username);
        if (cleanUsername.isEmpty() || session == null) {
            return;
        }

        SaveData data = loadData(cleanUsername);
        if (data == null) {
            data = createDefaultData(cleanUsername, cleanUsername);
        }

        data.highScore = Math.max(data.highScore, session.scoreEarned);
        data.currentLevel = Math.max(1, Math.min(MAX_LEVELS, session.levelId));
        data.gamesPlayed++;
        if (session.wonLevel) {
            data.levelWins++;
        }
        data.wordsTyped += Math.max(0, session.wordsTyped);
        data.correctChars += Math.max(0, session.correctChars);
        data.totalTypedChars += Math.max(0, session.totalTypedChars);
        data.errorCount += Math.max(0, session.errorCount);
        data.totalTimePlayedSeconds += Math.max(0L, session.timePlayedSeconds);
        data.peakWpm = Math.max(data.peakWpm, session.peakWpm);
        if (session.averageWpm > 0.0) {
            data.cumulativeAverageWpm += session.averageWpm;
            data.averageWpmSamples++;
        }
        data.longestStreak = Math.max(data.longestStreak, session.longestStreak);
        data.powerupsUsed += Math.max(0, session.powerupsUsed);

        writeData(cleanUsername, data);
        recordHighScore(cleanUsername, session.scoreEarned);
    }

    /**
     * Returns the global high score table for the UI.
     *
     * @return top saved high scores
     */
    public static List<PlayerRecord> getAllHighScores() {
        HighScoreTable table = loadHighScoreTable();
        table.ensureDefaults();
        table.records.sort(Comparator.comparingInt((PlayerRecord record) -> record.score).reversed());
        return new ArrayList<>(table.records);
    }

    /**
     * Resets the global high score table back to its default seeded entries.
     */
    public static void resetHighScoreTable() {
        HighScoreTable table = new HighScoreTable();
        table.records = defaultHighScores();
        writeHighScoreTable(table);
    }

    /**
     * Sanitizes a username for file access and lookups.
     *
     * @param username raw username
     * @return trimmed username, or an empty string when missing
     */
    public static String sanitizeUsername(String username) {
        return username == null ? "" : username.trim();
    }

    private static File getSaveFile(String username) {
        return new File(username + SAVE_SUFFIX);
    }

    private static SaveData loadData(String username) {
        String cleanUsername = sanitizeUsername(username);
        if (cleanUsername.isEmpty()) {
            return null;
        }

        File file = getSaveFile(cleanUsername);
        if (!file.exists()) {
            return null;
        }

        try (FileReader reader = new FileReader(file)) {
            SaveData data = GSON.fromJson(reader, SaveData.class);
            if (data == null) {
                data = createDefaultData(cleanUsername, cleanUsername);
            }
            data.ensureDefaults(cleanUsername);
            return data;
        } catch (IOException exception) {
            System.err.println("ProfileStore: failed to load " + cleanUsername + ": " + exception.getMessage());
            return null;
        }
    }

    private static boolean writeData(String username, SaveData data) {
        try (FileWriter writer = new FileWriter(getSaveFile(username))) {
            GSON.toJson(data, writer);
            return true;
        } catch (IOException exception) {
            System.err.println("ProfileStore: failed to save " + username + ": " + exception.getMessage());
            return false;
        }
    }

    private static SaveData createDefaultData(String username, String password) {
        SaveData data = new SaveData();
        data.username = username;
        data.password = password;
        data.highScore = 0;
        data.currentLevel = 1;
        data.unlockedLevel = 1;
        data.levelX = new int[] {DEFAULT_X, DEFAULT_X, DEFAULT_X};
        data.levelY = new int[] {DEFAULT_Y, DEFAULT_Y, DEFAULT_Y};
        data.levelSpriteRow = new int[] {DEFAULT_ROW, DEFAULT_ROW, DEFAULT_ROW};
        data.party = new ArrayList<>();
        data.itemCounts = serializeItemBag(ItemBag.starterBag());
        data.collectedPokeballs = new ArrayList<>();
        data.ensureDefaults(username);
        return data;
    }

    private static List<PokemonData> serializeParty(List<Pokemon> party) {
        List<PokemonData> result = new ArrayList<>();
        for (Pokemon pokemon : party) {
            result.add(new PokemonData(
                pokemon.name,
                pokemon.maxHp,
                pokemon.currentHp,
                pokemon.attack,
                pokemon.spritePath
            ));
        }
        return result;
    }

    private static List<Pokemon> deserializeParty(List<PokemonData> partyData) {
        List<Pokemon> result = new ArrayList<>();
        for (PokemonData pokemonData : partyData) {
            Pokemon pokemon = new Pokemon(
                pokemonData.name,
                pokemonData.maxHp,
                pokemonData.attack,
                pokemonData.spritePath
            );
            pokemon.currentHp = pokemonData.currentHp;
            pokemon.loadSprite();
            result.add(pokemon);
        }
        return result;
    }

    private static Map<String, Integer> serializeItemBag(ItemBag itemBag) {
        Map<String, Integer> counts = new HashMap<>();
        for (Map.Entry<Item, Integer> entry : itemBag.getAll().entrySet()) {
            counts.put(entry.getKey().name(), entry.getValue());
        }
        return counts;
    }

    private static int clampLevelIndex(int levelId) {
        return WordRepository.sanitizeLevel(levelId) - 1;
    }

    private static String pokeballKey(int levelId, int col, int row) {
        return levelId + ":" + col + ":" + row;
    }

    public static List<String> loadCollectedPokeballs(String username) {
        SaveData data = loadData(sanitizeUsername(username));
        if (data == null || data.collectedPokeballs == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(data.collectedPokeballs);
    }

    public static boolean hasCollectedPokeball(String username, int levelId, int col, int row) {
        SaveData data = loadData(sanitizeUsername(username));
        if (data == null || data.collectedPokeballs == null) {
            return false;
        }
        return data.collectedPokeballs.contains(pokeballKey(levelId, col, row));
    }

    public static void recordPokeballPickup(String username, int levelId, int col, int row) {
        String cleanUsername = sanitizeUsername(username);
        if (cleanUsername.isEmpty()) {
            return;
        }

        SaveData data = loadData(cleanUsername);
        if (data == null) {
            data = createDefaultData(cleanUsername, cleanUsername);
        }

        String key = pokeballKey(levelId, col, row);
        if (data.collectedPokeballs == null) {
            data.collectedPokeballs = new ArrayList<>();
        }
        if (!data.collectedPokeballs.contains(key)) {
            data.collectedPokeballs.add(key);
        }

        writeData(cleanUsername, data);
    }

    private static HighScoreTable loadHighScoreTable() {
        File file = new File(HIGHSCORE_FILE);
        if (!file.exists()) {
            HighScoreTable table = new HighScoreTable();
            table.records = defaultHighScores();
            writeHighScoreTable(table);
            return table;
        }

        try (FileReader reader = new FileReader(file)) {
            HighScoreTable table = GSON.fromJson(reader, HighScoreTable.class);
            if (table == null) {
                table = new HighScoreTable();
            }
            table.ensureDefaults();
            return table;
        } catch (IOException exception) {
            System.err.println("ProfileStore: failed to load high scores: " + exception.getMessage());
            HighScoreTable table = new HighScoreTable();
            table.records = defaultHighScores();
            return table;
        }
    }

    private static void writeHighScoreTable(HighScoreTable table) {
        try (FileWriter writer = new FileWriter(HIGHSCORE_FILE)) {
            GSON.toJson(table, writer);
        } catch (IOException exception) {
            System.err.println("ProfileStore: failed to write high scores: " + exception.getMessage());
        }
    }

    private static void recordHighScore(String username, int score) {
        if (score <= 0) {
            return;
        }

        HighScoreTable table = loadHighScoreTable();
        table.ensureDefaults();
        boolean updated = false;
        for (PlayerRecord record : table.records) {
            if (record.name.equalsIgnoreCase(username)) {
                record.score = Math.max(record.score, score);
                updated = true;
                break;
            }
        }
        if (!updated) {
            table.records.add(new PlayerRecord(username, score));
        }

        table.records.sort(Comparator.comparingInt((PlayerRecord record) -> record.score).reversed());
        while (table.records.size() > 10) {
            table.records.remove(table.records.size() - 1);
        }
        writeHighScoreTable(table);
    }

    private static void removeFromHighScoreTable(String username) {
        HighScoreTable table = loadHighScoreTable();
        table.records.removeIf(record -> record.name.equalsIgnoreCase(username));
        if (table.records.isEmpty()) {
            table.records = defaultHighScores();
        }
        writeHighScoreTable(table);
    }

    private static List<PlayerRecord> defaultHighScores() {
        List<PlayerRecord> defaults = new ArrayList<>();
        defaults.add(new PlayerRecord("Professor Lex", 1800));
        defaults.add(new PlayerRecord("Captain Keys", 1600));
        defaults.add(new PlayerRecord("Pixel Panda", 1400));
        defaults.add(new PlayerRecord("Rocket Robin", 1200));
        defaults.add(new PlayerRecord("Comet Fox", 1000));
        return defaults;
    }

    /**
     * Lightweight position and progress snapshot used by the map screens.
     */
    public static class PlayerProgress {
        public final int x;
        public final int y;
        public final int spriteRow;
        public final int highScore;
        public final int currentLevel;

        public PlayerProgress(int x, int y, int spriteRow, int highScore, int currentLevel) {
            this.x = x;
            this.y = y;
            this.spriteRow = spriteRow;
            this.highScore = highScore;
            this.currentLevel = currentLevel;
        }
    }

    /**
     * Rich profile snapshot used by player-facing and parent-facing screens.
     */
    public static class ProfileData {
        public final String username;
        public final int highScore;
        public final int currentLevel;
        public final int unlockedLevel;
        public final double averageWpm;
        public final double peakWpm;
        public final double accuracyPercent;
        public final int errorCount;
        public final long totalTimePlayedSeconds;
        public final int wordsTyped;
        public final int correctChars;
        public final int totalTypedChars;
        public final int longestStreak;
        public final int powerupsUsed;
        public final int gamesPlayed;
        public final int levelWins;
        public List<String> achievements = new ArrayList<>();
        public List<String> recommendations = new ArrayList<>();

        public ProfileData(String username, int highScore, int currentLevel, int unlockedLevel,
                           double averageWpm, double peakWpm, double accuracyPercent,
                           int errorCount, long totalTimePlayedSeconds, int wordsTyped,
                           int correctChars, int totalTypedChars, int longestStreak,
                           int powerupsUsed, int gamesPlayed, int levelWins) {
            this.username = username;
            this.highScore = highScore;
            this.currentLevel = currentLevel;
            this.unlockedLevel = unlockedLevel;
            this.averageWpm = averageWpm;
            this.peakWpm = peakWpm;
            this.accuracyPercent = accuracyPercent;
            this.errorCount = errorCount;
            this.totalTimePlayedSeconds = totalTimePlayedSeconds;
            this.wordsTyped = wordsTyped;
            this.correctChars = correctChars;
            this.totalTypedChars = totalTypedChars;
            this.longestStreak = longestStreak;
            this.powerupsUsed = powerupsUsed;
            this.gamesPlayed = gamesPlayed;
            this.levelWins = levelWins;
        }
    }

    /**
     * Session summary used when committing typing statistics after a level attempt.
     */
    public static class SessionStats {
        public final int levelId;
        public final int scoreEarned;
        public final double averageWpm;
        public final double peakWpm;
        public final int correctChars;
        public final int totalTypedChars;
        public final int errorCount;
        public final int wordsTyped;
        public final long timePlayedSeconds;
        public final int longestStreak;
        public final int powerupsUsed;
        public final boolean wonLevel;

        public SessionStats(int levelId, int scoreEarned, double averageWpm, double peakWpm,
                            int correctChars, int totalTypedChars, int errorCount, int wordsTyped,
                            long timePlayedSeconds, int longestStreak, int powerupsUsed,
                            boolean wonLevel) {
            this.levelId = levelId;
            this.scoreEarned = scoreEarned;
            this.averageWpm = averageWpm;
            this.peakWpm = peakWpm;
            this.correctChars = correctChars;
            this.totalTypedChars = totalTypedChars;
            this.errorCount = errorCount;
            this.wordsTyped = wordsTyped;
            this.timePlayedSeconds = timePlayedSeconds;
            this.longestStreak = longestStreak;
            this.powerupsUsed = powerupsUsed;
            this.wonLevel = wonLevel;
        }
    }

    /**
     * Simple record type used by the global high score table.
     */
    public static class PlayerRecord {
        public String name;
        public int score;

        public PlayerRecord(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }

    private static class HighScoreTable {
        List<PlayerRecord> records = new ArrayList<>();

        void ensureDefaults() {
            if (records == null || records.isEmpty()) {
                records = defaultHighScores();
            }
        }
    }

    private static class SaveData {
        String username;
        String password;
        int highScore;
        int currentLevel;
        int unlockedLevel;
        int[] levelX;
        int[] levelY;
        int[] levelSpriteRow;
        List<PokemonData> party;
        Map<String, Integer> itemCounts;
        List<String> collectedPokeballs;
        long totalTimePlayedSeconds;
        int wordsTyped;
        int correctChars;
        int totalTypedChars;
        int errorCount;
        double peakWpm;
        double cumulativeAverageWpm;
        int averageWpmSamples;
        int longestStreak;
        int powerupsUsed;
        int gamesPlayed;
        int levelWins;

        void ensureDefaults(String usernameValue) {
            if (username == null || username.isBlank()) {
                username = usernameValue;
            }
            if (password == null || password.isBlank()) {
                password = usernameValue;
            }
            if (currentLevel < 1) {
                currentLevel = 1;
            }
            if (unlockedLevel < 1) {
                unlockedLevel = 1;
            }
            if (levelX == null || levelX.length < MAX_LEVELS) {
                levelX = new int[] {DEFAULT_X, DEFAULT_X, DEFAULT_X};
            }
            if (levelY == null || levelY.length < MAX_LEVELS) {
                levelY = new int[] {DEFAULT_Y, DEFAULT_Y, DEFAULT_Y};
            }
            if (levelSpriteRow == null || levelSpriteRow.length < MAX_LEVELS) {
                levelSpriteRow = new int[] {DEFAULT_ROW, DEFAULT_ROW, DEFAULT_ROW};
            }
            if (party == null) {
                party = new ArrayList<>();
            }
            if (itemCounts == null || itemCounts.isEmpty()) {
                itemCounts = serializeItemBag(ItemBag.starterBag());
            }
            if (collectedPokeballs == null) {
                collectedPokeballs = new ArrayList<>();
            }
        }

        double getAverageWpm() {
            if (averageWpmSamples <= 0) {
                return 0.0;
            }
            return cumulativeAverageWpm / averageWpmSamples;
        }

        double getAccuracyPercent() {
            if (totalTypedChars <= 0) {
                return 100.0;
            }
            return (100.0 * correctChars) / totalTypedChars;
        }
    }

    private static class PokemonData {
        String name;
        int maxHp;
        int currentHp;
        int attack;
        String spritePath;

        PokemonData(String name, int maxHp, int currentHp, int attack, String spritePath) {
            this.name = name;
            this.maxHp = maxHp;
            this.currentHp = currentHp;
            this.attack = attack;
            this.spritePath = spritePath;
        }
    }
}
