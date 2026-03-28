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
                    String token = values[col].trim();
                    if (token.isEmpty()) {
                        worldMap[row][col] = 0;
                    } else {
                        try {
                            worldMap[row][col] = Integer.parseInt(token);
                        } catch (NumberFormatException e) {
                            worldMap[row][col] = 0;
                        }
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
}