import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Parses a CSV map file into a 2-D tile array, a {@link Trainer}, and a
 * list of {@link TerrainObjects}.
 *
 * <p>Tile values in the CSV:
 * <ul>
 *   <li>0  – walkable grass</li>
 *   <li>1  – tree (solid)</li>
 *   <li>2  – rock (solid)</li>
 *   <li>3–7 – additional terrain objects</li>
 *   <li>8  – trainer spawn (replaced with 0 in the tile array)</li>
 *   <li>9  – tall grass (wild encounter trigger)</li>
 *   <li>5  – exit tile</li>
 * </ul>
 * </p>
 *
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
public class MapLoader {
    private final int WIDTH = 64;
    private final int HEIGHT = 64;
    private int[][] worldMap;
    private Trainer trainer;
    private List<TerrainObjects> terrainObjects;

    public MapLoader(String filePath) {
        worldMap = new int[HEIGHT][WIDTH];
        terrainObjects = new ArrayList<>();
        load(filePath);
    }

    private void load(String filePath) {
        try (Scanner sc = new Scanner(new File(filePath))) {
            int row = 0;
            while (sc.hasNextLine() && row < HEIGHT) {
                String line = sc.nextLine();
                String[] values = line.split(",");
                for (int col = 0; col < values.length && col < WIDTH; col++) {
                    int value = Integer.parseInt(values[col].trim());

                    if (value == 8) {
                        trainer = new Trainer(col, row, "trainerSprite.png");
                        worldMap[row][col] = 0; // still draw grass under trainer
                    } else if (value == 5) {
                        worldMap[row][col] = 5; // End tile, walkable finish
                    } else if (value == 9) {
                        // Encounter/tall grass tile
                        worldMap[row][col] = 9;
                    } else if (value >= 1 && value <= 7) {
                        // Terrain objects (trees, rocks, etc.)
                        boolean isPokeball = value == 3;
                        terrainObjects.add(new TerrainObjects(col, row, value, !isPokeball));
                        worldMap[row][col] = 0; // draw grass under terrain object
                    } else {
                        worldMap[row][col] = value;
                    }
                }
                row++;
            }

            // Simple diagnostic: count how many encounter tiles (value 9) we loaded
            int tallGrassCount = 0;
            for (int r = 0; r < HEIGHT; r++) {
                for (int c = 0; c < WIDTH; c++) {
                    if (worldMap[r][c] == 9) {
                        tallGrassCount++;
                    }
                }
            }
            System.out.println("MapLoader: loaded " + tallGrassCount + " encounter tiles (value 9).");
        } catch (FileNotFoundException e) {
            System.out.println("Error: Could not find map.csv in project folder!");
        }
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
     * Returns the {@link Trainer} NPC parsed from tile value 8, or {@code null} if none was found.
     *
     * @return trainer instance, or null
     */
    public Trainer getTrainer() {
        return trainer;
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