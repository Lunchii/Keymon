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

    // ── Save ────────────────────────────────────────────────────────────────

    public static void saveGame(String username, int x, int y, int row,
                                int highScore, int currentLevel) {
        saveGame(username, x, y, row, highScore, currentLevel, null);
    }

    public static void saveGame(String username, int x, int y, int row,
                                int highScore, int currentLevel, List<Pokemon> party) {
        if (username == null || username.isEmpty()) return;

        List<PokemonData> partyData = new ArrayList<>();
        if (party != null) {
            for (Pokemon p : party)
                partyData.add(new PokemonData(p.name, p.maxHp, p.currentHp, p.attack, p.spritePath));
        }

        SaveData data = new SaveData(x, y, row, highScore, currentLevel, partyData);
        try (FileWriter writer = new FileWriter(username + "_save.json")) {
            gson.toJson(data, writer);
            System.out.println("Game saved for user: " + username);
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    // ── Load position ───────────────────────────────────────────────────────

    public static PlayerData loadGame(String username) {
        SaveData data = loadData(username);
        if (data != null)
            return new PlayerData(data.x, data.y, data.spriteRow, data.highScore, data.currentLevel);
        return new PlayerData(320, 320, 10, 0, 1);
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
            return gson.fromJson(reader, SaveData.class);
        } catch (IOException e) {
            System.err.println("Load failed: " + e.getMessage());
            return null;
        }
    }

    // ── Data classes ────────────────────────────────────────────────────────

    private static class SaveData {
        int x, y, spriteRow, highScore, currentLevel;
        List<PokemonData> party;
        SaveData(int x, int y, int row, int hs, int lvl, List<PokemonData> party) {
            this.x = x; this.y = y; this.spriteRow = row;
            this.highScore = hs; this.currentLevel = lvl; this.party = party;
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
