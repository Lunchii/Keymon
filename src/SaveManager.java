import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced SaveManager supporting position, party, high scores, levels and account management.
 */
public class SaveManager {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final int DEFAULT_X = 320;
    private static final int DEFAULT_Y = 320;
    private static final int DEFAULT_ROW = 10;
    private static final int MAX_LEVELS = 3;

    // ── Save ────────────────────────────────────────────────────────────────

    public static void saveGame(String username, int x, int y, int row,
                                int highScore, int currentLevel) {
        saveGame(username, 1, x, y, row, highScore, currentLevel, null);
    }

    public static void saveGame(String username, int x, int y, int row,
                                int highScore, int currentLevel, List<Pokemon> party) {
        saveGame(username, 1, x, y, row, highScore, currentLevel, party);
    }

    public static void saveGame(String username, int levelId, int x, int y, int row,
                                int highScore, int currentLevel, List<Pokemon> party) {
        if (username == null || username.isEmpty()) return;

        SaveData data = loadData(username);
        if (data == null) data = createDefaultData();
        data.ensureLevelData();

        int levelIndex = clampLevelIndex(levelId);
        data.levelX[levelIndex] = x;
        data.levelY[levelIndex] = y;
        data.levelSpriteRow[levelIndex] = row;
        data.highScore = Math.max(data.highScore, highScore);
        data.currentLevel = currentLevel;

        List<PokemonData> partyData = new ArrayList<>();
        if (party != null) {
            for (Pokemon p : party)
                partyData.add(new PokemonData(p.name, p.maxHp, p.currentHp, p.attack, p.spritePath));
            data.party = partyData;
        }
        try (FileWriter writer = new FileWriter(username + "_save.json")) {
            gson.toJson(data, writer);
            System.out.println("Game saved for user: " + username);
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    // ── Load position ───────────────────────────────────────────────────────

    public static PlayerData loadGame(String username) {
        return loadGame(username, 1);
    }

    public static PlayerData loadGame(String username, int levelId) {
        SaveData data = loadData(username);
        if (data != null) {
            data.ensureLevelData();
            int levelIndex = clampLevelIndex(levelId);
            return new PlayerData(data.levelX[levelIndex], data.levelY[levelIndex],
                    data.levelSpriteRow[levelIndex], data.highScore, data.currentLevel);
        }
        return new PlayerData(320, 320, 10, 0, 1);
    }

    public static int getHighestUnlockedLevel(String username) {
        SaveData data = loadData(username);
        if (data == null) return 1;
        return Math.max(1, Math.min(MAX_LEVELS, data.unlockedLevel));
    }

    public static void markLevelCompleted(String username, int completedLevel) {
        if (username == null || username.isEmpty()) return;
        SaveData data = loadData(username);
        if (data == null) data = createDefaultData();
        int unlockTo = Math.min(MAX_LEVELS, completedLevel + 1);
        data.unlockedLevel = Math.max(data.unlockedLevel, unlockTo);
        try (FileWriter writer = new FileWriter(username + "_save.json")) {
            gson.toJson(data, writer);
            System.out.println("Unlocked level " + data.unlockedLevel + " for user: " + username);
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    // ── Load party ──────────────────────────────────────────────────────────

    public static List<Pokemon> loadParty(String username) {
        SaveData data = loadData(username);
        List<Pokemon> party = new ArrayList<>();
        if (data == null || data.party == null) return party;
        for (PokemonData pd : data.party) {
            Pokemon p = new Pokemon(pd.name, pd.maxHp, pd.attack, pd.spritePath);
            p.currentHp = pd.currentHp;
            p.loadSprite();
            party.add(p);
        }
        return party;
    }

    // ── Account management ──────────────────────────────────────────────────

    public static boolean deleteAccount(String username) {
        File file = new File(username + "_save.json");
        return file.exists() && file.delete();
    }

    // ── High scores ─────────────────────────────────────────────────────────

    public static List<PlayerRecord> getAllHighScores() {
        List<PlayerRecord> records = new ArrayList<>();
        File folder = new File(".");
        File[] files = folder.listFiles((dir, name) -> name.endsWith("_save.json"));
        if (files != null) {
            for (File file : files) {
                try (FileReader reader = new FileReader(file)) {
                    SaveData data = gson.fromJson(reader, SaveData.class);
                    String name = file.getName().replace("_save.json", "");
                    records.add(new PlayerRecord(name, data != null ? data.highScore : 0));
                } catch (Exception ignored) {}
            }
        }
        records.sort((a, b) -> Integer.compare(b.score, a.score));
        return records;
    }

    // ── Internal helpers ────────────────────────────────────────────────────

    private static SaveData loadData(String username) {
        if (username == null || username.isEmpty()) return null;
        File file = new File(username + "_save.json");
        if (!file.exists()) return null;
        try (FileReader reader = new FileReader(file)) {
            SaveData data = gson.fromJson(reader, SaveData.class);
            if (data != null) data.ensureLevelData();
            return data;
        } catch (IOException e) {
            System.err.println("Load failed: " + e.getMessage());
            return null;
        }
    }

    private static SaveData createDefaultData() {
        return new SaveData(DEFAULT_X, DEFAULT_Y, DEFAULT_ROW, 0, 1, new ArrayList<>());
    }

    private static int clampLevelIndex(int levelId) {
        int clamped = Math.max(1, Math.min(MAX_LEVELS, levelId));
        return clamped - 1;
    }

    // ── Data classes ────────────────────────────────────────────────────────

    private static class SaveData {
        int x, y, spriteRow, highScore, currentLevel;
        int unlockedLevel;
        int[] levelX;
        int[] levelY;
        int[] levelSpriteRow;
        List<PokemonData> party;
        SaveData(int x, int y, int row, int hs, int lvl, List<PokemonData> party) {
            this.x = x; this.y = y; this.spriteRow = row;
            this.highScore = hs; this.currentLevel = lvl; this.party = party;
            this.unlockedLevel = 1;
            this.levelX = new int[] { x, DEFAULT_X, DEFAULT_X };
            this.levelY = new int[] { y, DEFAULT_Y, DEFAULT_Y };
            this.levelSpriteRow = new int[] { row, DEFAULT_ROW, DEFAULT_ROW };
        }

        void ensureLevelData() {
            if (levelX == null || levelX.length < MAX_LEVELS) {
                levelX = new int[] { x, DEFAULT_X, DEFAULT_X };
            }
            if (levelY == null || levelY.length < MAX_LEVELS) {
                levelY = new int[] { y, DEFAULT_Y, DEFAULT_Y };
            }
            if (levelSpriteRow == null || levelSpriteRow.length < MAX_LEVELS) {
                levelSpriteRow = new int[] { spriteRow, DEFAULT_ROW, DEFAULT_ROW };
            }
            if (unlockedLevel < 1) unlockedLevel = 1;
        }
    }

    public static class PlayerData {
        public int x, y, spriteRow, highScore, currentLevel;
        public PlayerData(int x, int y, int row, int hs, int lvl) {
            this.x = x; this.y = y; this.spriteRow = row;
            this.highScore = hs; this.currentLevel = lvl;
        }
    }

    public static class PlayerRecord {
        public String name; public int score;
        public PlayerRecord(String n, int s) { this.name = n; this.score = s; }
    }

    private static class PokemonData {
        String name, spritePath;
        int maxHp, currentHp, attack;
        PokemonData(String name, int maxHp, int currentHp, int attack, String spritePath) {
            this.name = name; this.maxHp = maxHp; this.currentHp = currentHp;
            this.attack = attack; this.spritePath = spritePath;
        }
    }
}
