import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
                        trainer = new Trainer(col * 32, row * 32, "trainerSprite.png");
                        worldMap[row][col] = 0; // still draw grass under trainer
                    } else if (value == 5) {
                        worldMap[row][col] = 5; // End tile, walkable finish
                    } else if (value >= 1 && value <= 7) {
                        // Terrain objects (trees, rocks, etc.)
                        terrainObjects.add(new TerrainObjects(col, row, value));
                        worldMap[row][col] = 0; // draw grass under terrain object
                    } else {
                        worldMap[row][col] = value;
                    }
                }
                row++;
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error: Could not find map.csv in project folder!");
        }
    }

    public int[][] getWorldMap() {
        return worldMap;
    }

    public Trainer getTrainer() {
        return trainer;
    }

    public List<TerrainObjects> getTerrainObjects() {
        return terrainObjects;
    }
}