import java.io.Serializable;

/**
 * Tracks cumulative play time across sessions.
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 *
 * <p>Combines a saved baseline ({@code totalTimeMillis}) with the elapsed time
 * of the current in-progress session. Call {@link #resume()} when the player
 * enters the game world and {@link #pause()} on every pause, stat screen visit,
 * and level exit so no dead time is counted.</p>
 */
public class TimeManager implements Serializable {
    private long totalTimeMillis; // Cumulative time from all previous sessions
    private long sessionStartTime; // When the current session started
    private boolean isRunning;

    /**
     * Creates a TimeManager seeded with previously saved time.
     *
     * @param savedTime cumulative milliseconds from prior sessions (0 for a new session)
     */
    public TimeManager(long savedTime) {
        this.totalTimeMillis = savedTime;
        this.isRunning = false;
    }

    /** Starts or resumes the clock. Call this when the player enters or unpauses the game. */
    public void resume() {
        if (!isRunning) {
            sessionStartTime = System.currentTimeMillis();
            isRunning = true;
        }
    }

    /** Stops the clock and accumulates the elapsed session time. Call this on pause or level exit. */
    public void pause() {
        if (isRunning) {
            totalTimeMillis += (System.currentTimeMillis() - sessionStartTime);
            isRunning = false;
        }
    }

    /**
     * Returns the total elapsed time in milliseconds, including the current running session if active.
     *
     * @return cumulative play time in milliseconds
     */
    public long getTotalMillis() {
        if (isRunning) {
            return totalTimeMillis + (System.currentTimeMillis() - sessionStartTime);
        }
        return totalTimeMillis;
    }

    /**
     * Returns the total play time formatted as {@code Xh YYm Zs} for display in the stats UI.
     *
     * @return human-readable duration string
     */
    public String getFormattedTime() {
        long currentTotal = getTotalMillis();

        long seconds = (currentTotal / 1000) % 60;
        long minutes = (currentTotal / (1000 * 60)) % 60;
        long hours = (currentTotal / (1000 * 60 * 60));

        // Formats as "12h 35m"
        return String.format("%dh %02dm %ds", hours, minutes, seconds);
    }
}