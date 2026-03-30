import java.io.*;
import java.util.Scanner;

public class MapLoader {
    private final int WIDTH = 64;
    private final int HEIGHT = 64;
    private int[][] worldMap;

    public MapLoader(String filePath) {
        worldMap = new int[HEIGHT][WIDTH];
        load(filePath);
    }

    private void load(String filePath) {
        try (Scanner sc = new Scanner(new File(filePath))) {
            int row = 0;
            while (sc.hasNextLine() && row < HEIGHT) {
                String line = sc.nextLine();
                String[] values = line.split(",");
                for (int col = 0; col < values.length && col < WIDTH; col++) {
                    worldMap[row][col] = Integer.parseInt(values[col].trim());
                }
                row++;
            }

            // Simple diagnostic: count how many encounter tiles (value 1) we loaded
            int tallGrassCount = 0;
            for (int r = 0; r < HEIGHT; r++) {
                for (int c = 0; c < WIDTH; c++) {
                    if (worldMap[r][c] == 1) {
                        tallGrassCount++;
                    }
                }
            }
            System.out.println("MapLoader: loaded " + tallGrassCount + " encounter tiles (value 1).");
        } catch (FileNotFoundException e) {
            System.out.println("Error: Could not find map.csv in project folder!");
        }
    }

    public int[][] getWorldMap() {
        return worldMap;
    }
}