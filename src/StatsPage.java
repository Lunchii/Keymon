import java.awt.*;
import javax.swing.*;

/**
 * Full player-statistics screen showing typing metrics, achievement badges, and
 * coaching tips.
 *
 * <p>
 * Data is loaded fresh from {@link ProfileStore} each time
 * {@link #setUserData(String)} is called so the view is always up to date.
 * The screen is divided into two scrollable text panels: a typing snapshot
 * on the left and badges/tips on the right.
 * </p>
 * 
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
public class StatsPage extends JPanel {
    private Main mainApp;
    private String username;
    private JLabel titleLabel;
    private JTextArea statsArea;
    private JTextArea achievementArea;

    public StatsPage(Main mainApp) {
        this.mainApp = mainApp;
        setBackground(new Color(18, 30, 22));
        setLayout(new BorderLayout(18, 18));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        titleLabel = new JLabel("PLAYER STATISTICS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel, BorderLayout.NORTH);

        statsArea = createReadOnlyArea();
        achievementArea = createReadOnlyArea();

        JPanel center = new JPanel(new GridLayout(1, 2, 18, 18));
        center.setOpaque(false);
        center.add(wrapArea("Typing Snapshot", statsArea));
        center.add(wrapArea("Badges And Tips", achievementArea));
        add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);

        JButton backBtn = new JButton("BACK TO TRAINER SCREEN");
        backBtn.setFont(new Font("Monospaced", Font.BOLD, 16));
        backBtn.addActionListener(e -> mainApp.showPlayerScreen(username));
        footer.add(backBtn);
        add(footer, BorderLayout.SOUTH);
    }

    public void setUserData(String username) {
        this.username = username;
        ProfileStore.ProfileData profile = ProfileStore.loadProfile(username);
        titleLabel.setText(username.toUpperCase() + " - PLAYER STATISTICS");

        statsArea.setText(buildStatsText(profile));
        achievementArea.setText(buildAchievementText(profile));
        statsArea.setCaretPosition(0);
        achievementArea.setCaretPosition(0);
    }

    private JTextArea createReadOnlyArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 15));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(new Color(240, 245, 240));
        area.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        return area;
    }

    private JPanel wrapArea(String heading, JTextArea area) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(245, 245, 245, 230));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 3),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel label = new JLabel(heading, SwingConstants.CENTER);
        label.setFont(new Font("Monospaced", Font.BOLD, 18));
        panel.add(label, BorderLayout.NORTH);
        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        return panel;
    }

    private String buildStatsText(ProfileStore.ProfileData profile) {
        return "High Score: " + profile.highScore + '\n'
                + "Highest Stage Unlocked: " + profile.unlockedLevel + '\n'
                + "Average WPM: " + String.format("%.1f", profile.averageWpm) + '\n'
                + "Peak WPM: " + String.format("%.1f", profile.peakWpm) + '\n'
                + "Accuracy: " + String.format("%.1f%%", profile.accuracyPercent) + '\n'
                + "Error Count: " + profile.errorCount + '\n'
                + "Words Typed: " + profile.wordsTyped + '\n'
                + "Correct Characters: " + profile.correctChars + '\n'
                + "Total Typed Characters: " + profile.totalTypedChars + '\n'
                + "Powerups Used: " + profile.powerupsUsed + '\n'
                + "Games Played: " + profile.gamesPlayed + '\n'
                + "Stage Wins: " + profile.levelWins + '\n'
                + "Longest Streak: " + profile.longestStreak + '\n'
                + "Total Time Played: " + formatDuration(profile.totalTimePlayedSeconds);
    }

    private String buildAchievementText(ProfileStore.ProfileData profile) {
        StringBuilder builder = new StringBuilder();
        if (profile.achievements.isEmpty()) {
            builder.append("No badges unlocked yet.\n");
        } else {
            builder.append("Unlocked Badges:\n");
            for (String achievement : profile.achievements) {
                builder.append("- ").append(achievement).append('\n');
            }
        }

        builder.append("\nCoach Recommendations:\n");
        for (String tip : profile.recommendations) {
            builder.append("- ").append(tip).append('\n');
        }
        return builder.toString();
    }

    private String formatDuration(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
    }
}
