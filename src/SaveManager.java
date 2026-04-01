import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;

public class SaveManager {
    private static final String FILE_PATH = "savegame.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * save player position
     * @param x x-position
     * @param y y-position
     * @param row player spriteRow
     */
    public static void saveGame(int x, int y, int row) {
        PlayerData data = new PlayerData(x, y, row);
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(data, writer);
            System.out.println("Save successful: " + FILE_PATH);
        } catch (IOException e) {
            System.err.println("Save failed: " + e.getMessage());
        }
    }

    /**
     * read save
     * @return return x, y, row
     */
    public static int[] loadGame() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            // if no save json, set as default
            return new int[]{320, 320, 140};
        }

        try (FileReader reader = new FileReader(FILE_PATH)) {
            PlayerData data = gson.fromJson(reader, PlayerData.class);
            return new int[]{data.x, data.y, data.spriteRow};
        } catch (IOException e) {
            e.printStackTrace();
            return new int[]{320, 320, 140};
        }
    }

    private static class PlayerData {
        int x;
        int y;
        int spriteRow;

        PlayerData(int x, int y, int row) {
            this.x = x;
            this.y = y;
            this.spriteRow = row;
        }
    }
}