import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses a CSV map file into a 2-D tile array, a list of {@link Trainer} NPCs,
 * and a list of {@link TerrainObjects}.
 *
 * <p>Tile values in the CSV:
 * <ul>
 *   <li>0  – walkable grass</li>
 *   <li>1  – tree (solid)</li>
 *   <li>2  – rock (solid)</li>
 *   <li>3  – pokeball (non-solid collectible)</li>
 *   <li>5  – exit tile</li>
 *   <li>8  – trainer spawn (replaced with 0 in the tile array)</li>
 *   <li>9  – tall grass (wild encounter trigger)</li>
 * </ul>
 * </p>
 *
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
public class MapLoader {
    private int[][] worldMap;
    private List<Trainer> trainers;
    private List<TerrainObjects> terrainObjects;

    public MapLoader(String filePath) {
        trainers = new ArrayList<>();
        terrainObjects = new ArrayList<>();
        load(filePath);
    }

    private void load(String filePath) {
        int trainerTier = resolveTrainerTier(filePath);

        // Read all lines first to determine actual dimensions
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error: Could not find map file: " + filePath);
            worldMap = new int[0][0];
            return;
        } catch (IOException e) {
            System.out.println("Error: Could not read map file: " + filePath);
            worldMap = new int[0][0];
            return;
        }

        int height = lines.size();
        int width = 0;
        for (String line : lines) {
            width = Math.max(width, line.split(",").length);
        }

        worldMap = new int[height][width];

        for (int row = 0; row < height; row++) {
            String[] values = lines.get(row).split(",");
            for (int col = 0; col < values.length && col < width; col++) {
                int value;
                try {
                    value = Integer.parseInt(values[col].trim());
                } catch (NumberFormatException e) {
                    value = 0;
                }

                if (value == 8) {
                    trainers.add(new Trainer(col, row, "images/trainerSprite.png", trainerTier, trainers.size()));
                    worldMap[row][col] = 0; // draw grass under trainer
                } else if (value == 5) {
                    worldMap[row][col] = 5; // exit tile
                } else if (value == 9) {
                    worldMap[row][col] = 9; // tall grass / encounter
                } else if (value >= 1 && value <= 7) {
                    boolean isPokeball = value == 3;
                    terrainObjects.add(new TerrainObjects(col, row, value, !isPokeball));
                    worldMap[row][col] = 0; // draw grass under terrain object
                } else {
                    worldMap[row][col] = value;
                }
            }
        }

        int tallGrassCount = 0;
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                if (worldMap[r][c] == 9) tallGrassCount++;
            }
        }
        System.out.println("MapLoader: loaded " + height + "x" + width + " map, "
                + tallGrassCount + " encounter tiles, " + trainers.size() + " trainer(s).");
    }

    private int resolveTrainerTier(String filePath) {
        if (filePath == null) {
            return 1;
        }

        String normalized = filePath.replace('\\', '/').toLowerCase();
        if (normalized.contains("tutorial")) {
            return 0;
        }
        if (normalized.contains("level3")) {
            return 3;
        }
        if (normalized.contains("level2")) {
            return 2;
        }
        return 1;
    }

    /**
     * Returns the parsed 2-D tile array for the loaded map.
     *
     * @return grid of tile values indexed by [row][col]
     */
    public int[][] getWorldMap() {
        return worldMap;
    }

    /**
     * Returns all {@link Trainer} NPCs parsed from tile value 8.
     *
     * @return list of trainers (may be empty)
     */
    public List<Trainer> getTrainers() {
        return trainers;
    }

    /**
     * Returns the first {@link Trainer} parsed from tile value 8, or {@code null} if none.
     *
     * @return first trainer, or null
     */
    public Trainer getTrainer() {
        return trainers.isEmpty() ? null : trainers.get(0);
    }

    /**
     * Returns the list of solid {@link TerrainObjects} parsed from tiles 1–7.
     *
     * @return list of terrain collision objects
     */
    public List<TerrainObjects> getTerrainObjects() {
        return terrainObjects;
    }
}
