import java.io.Serializable;

public class TimeManager implements Serializable {
    private long totalTimeMillis; // Cumulative time from all previous sessions
    private long sessionStartTime; // When the current session started
    private boolean isRunning;

    public TimeManager(long savedTime) {
        this.totalTimeMillis = savedTime;
        this.isRunning = false;
    }

    // Start or Resume the clock (Call this when entering the GamePanel)
    public void resume() {
        if (!isRunning) {
            sessionStartTime = System.currentTimeMillis();
            isRunning = true;
        }
    }

    // Stop the clock (Call this when Pausing or opening Stats)
    public void pause() {
        if (isRunning) {
            totalTimeMillis += (System.currentTimeMillis() - sessionStartTime);
            isRunning = false;
        }
    }

    // Returns the total time in milliseconds (for saving to file)
    public long getTotalMillis() {
        if (isRunning) {
            return totalTimeMillis + (System.currentTimeMillis() - sessionStartTime);
        }
        return totalTimeMillis;
    }

    // Returns "XXh YYm" for your Stats UI
    public String getFormattedTime() {
        long currentTotal = getTotalMillis();

        long seconds = (currentTotal / 1000) % 60;
        long minutes = (currentTotal / (1000 * 60)) % 60;
        long hours = (currentTotal / (1000 * 60 * 60));

        // Formats as "12h 35m"
        return String.format("%dh %02dm %ds", hours, minutes, seconds);
    }
}