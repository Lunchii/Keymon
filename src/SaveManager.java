import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced SaveManager to support High Scores, Levels, and Account Management.
 */
public class SaveManager {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Updated: Now saves highScore and currentLevel
    public static void saveGame(String username, int x, int y, int row, int highScore, int currentLevel) {
        if (username == null || username.isEmpty()) return;

        String fileName = username + "_save.json";
        PlayerData data = new PlayerData(x, y, row, highScore, currentLevel);

        try (FileWriter writer = new FileWriter(fileName)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    // Updated: Returns full PlayerData object for the Player Screen
    public static PlayerData loadGame(String username) {
        if (username == null || username.isEmpty()) return new PlayerData(320, 320, 10, 0, 1);

        String fileName = username + "_save.json";
        File file = new File(fileName);
        if (!file.exists()) return new PlayerData(320, 320, 10, 0, 1);

        try (FileReader reader = new FileReader(fileName)) {
            return gson.fromJson(reader, PlayerData.class);
        } catch (IOException e) {
            return new PlayerData(320, 320, 10, 0, 1);
        }
    }

    // New: Delete account logic (Requirement 3.1.6)
    public static boolean deleteAccount(String username) {
        File file = new File(username + "_save.json");
        return file.exists() && file.delete();
    }

    // New: For HighScore Table (Requirement 3.1.9)
    public static List<PlayerRecord> getAllHighScores() {
        List<PlayerRecord> records = new ArrayList<>();
        File folder = new File(".");
        File[] files = folder.listFiles((dir, name) -> name.endsWith("_save.json"));

        if (files != null) {
            for (File file : files) {
                try (FileReader reader = new FileReader(file)) {
                    PlayerData data = gson.fromJson(reader, PlayerData.class);
                    String name = file.getName().replace("_save.json", "");
                    records.add(new PlayerRecord(name, data.highScore));
                } catch (Exception e) {}
            }
        }
        records.sort((a, b) -> Integer.compare(b.score, a.score));
        return records;
    }

    // Data Structure
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
}