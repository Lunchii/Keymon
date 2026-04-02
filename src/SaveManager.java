import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SaveManager {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // ── Save ────────────────────────────────────────────────────────────────

    public static void saveGame(String username, int x, int y, int row, List<Pokemon> party) {
        if (username == null || username.isEmpty()) return;
        String fileName = username + "_save.json";

        List<PokemonData> partyData = new ArrayList<>();
        if (party != null) {
            for (Pokemon p : party)
                partyData.add(new PokemonData(p.name, p.maxHp, p.currentHp, p.attack, p.spritePath));
        }

        try (FileWriter writer = new FileWriter(fileName)) {
            gson.toJson(new SaveData(x, y, row, partyData), writer);
            System.out.println("Game saved for user: " + username);
        } catch (IOException e) {
            System.err.println("Save failed: " + e.getMessage());
        }
    }

    // ── Load position ───────────────────────────────────────────────────────

    public static int[] loadGame(String username) {
        SaveData data = loadData(username);
        if (data != null) return new int[]{ data.x, data.y, data.spriteRow };
        return new int[]{ 320, 320, 10 };
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
        int x, y, spriteRow;
        List<PokemonData> party;
        SaveData(int x, int y, int row, List<PokemonData> party) {
            this.x = x; this.y = y; this.spriteRow = row; this.party = party;
        }
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
