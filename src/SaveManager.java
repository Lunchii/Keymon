import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;

/**
 * Utility class to manage player data persistence using JSON.
 * Supports multiple users by creating separate save files based on usernames.
 */
public class SaveManager {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Saves the player's current position and direction to a JSON file.
     * @param username The name of the current user session.
     * @param x The current x-coordinate.
     * @param y The current y-coordinate.
     * @param row The current sprite row (direction/animation state).
     */
    public static void saveGame(String username, int x, int y, int row) {
        if (username == null || username.isEmpty()) {
            System.err.println("Save failed: Invalid username.");
            return;
        }

        String fileName = username + "_save.json";
        PlayerData data = new PlayerData(x, y, row);

        try (FileWriter writer = new FileWriter(fileName)) {
            gson.toJson(data, writer);
            System.out.println("Game saved successfully for user: " + username);
        } catch (IOException e) {
            System.err.println("Save failed: " + e.getMessage());
        }
    }

    /**
     * Loads player data for the specified user.
     * @param username The name of the user to load.
     * @return An integer array [x, y, spriteRow]. Returns defaults if no save exists.
     */
    public static int[] loadGame(String username) {
        if (username == null || username.isEmpty()) {
            return new int[]{320, 320, 10}; // Default starting values
        }

        String fileName = username + "_save.json";
        File file = new File(fileName);

        // If no previous save is found, return the initial map coordinates
        if (!file.exists()) {
            return new int[]{320, 320, 10};
        }

        try (FileReader reader = new FileReader(fileName)) {
            PlayerData data = gson.fromJson(reader, PlayerData.class);
            if (data != null) {
                return new int[]{data.x, data.y, data.spriteRow};
            }
        } catch (IOException e) {
            System.err.println("Error reading save file: " + e.getMessage());
        }

        return new int[]{320, 320, 10};
    }

    /**
     * Internal data structure used for JSON serialization.
     */
    private static class PlayerData {
        int x, y, spriteRow;

        PlayerData(int x, int y, int row) {
            this.x = x;
            this.y = y;
            this.spriteRow = row;
        }
    }
}