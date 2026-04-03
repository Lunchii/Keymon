import java.util.ArrayList;
import java.util.List;

/**
 * Builds achievement badges and coaching suggestions from saved player data.
 */
public final class ProfileInsights {
    private ProfileInsights() {
    }

    /**
     * Returns achievement-style badges earned by the player.
     *
     * @param profile saved profile snapshot
     * @return unlocked badges
     */
    public static List<String> buildAchievements(ProfileStore.ProfileData profile) {
        List<String> achievements = new ArrayList<>();
        if (profile == null) {
            return achievements;
        }

        if (profile.wordsTyped >= 10) {
            achievements.add("Word Wrangler: typed 10 words correctly.");
        }
        if (profile.peakWpm >= 30.0) {
            achievements.add("Speed Spark: reached 30 WPM.");
        }
        if (profile.peakWpm >= 45.0) {
            achievements.add("Lightning Fingers: reached 45 WPM.");
        }
        if (profile.accuracyPercent >= 95.0 && profile.wordsTyped >= 25) {
            achievements.add("Accuracy Ace: held 95% accuracy.");
        }
        if (profile.longestStreak >= 5) {
            achievements.add("Combo Captain: chained a 5-word streak.");
        }
        if (profile.unlockedLevel >= 2) {
            achievements.add("Forest Scout: unlocked Stage 2.");
        }
        if (profile.unlockedLevel >= 3) {
            achievements.add("Sky Challenger: unlocked Stage 3.");
        }
        if (profile.highScore >= 1500) {
            achievements.add("High Score Hero: scored 1500 points.");
        }
        return achievements;
    }

    /**
     * Returns practical coaching tips tailored to the player's statistics.
     *
     * @param profile saved profile snapshot
     * @return short, actionable recommendations
     */
    public static List<String> buildRecommendations(ProfileStore.ProfileData profile) {
        List<String> recommendations = new ArrayList<>();
        if (profile == null) {
            return recommendations;
        }

        if (profile.wordsTyped < 15) {
            recommendations.add("Play a few more short battles to build rhythm before chasing speed.");
        }
        if (profile.accuracyPercent < 85.0) {
            recommendations.add("Slow down slightly and watch the highlighted prefix to reduce errors.");
        }
        if (profile.averageWpm < 22.0) {
            recommendations.add("Replay Stage 1 and focus on smooth, even typing to raise your baseline WPM.");
        }
        if (profile.peakWpm - profile.averageWpm >= 12.0) {
            recommendations.add("You already have bursts of speed. Work on consistency between words.");
        }
        if (profile.longestStreak < 3) {
            recommendations.add("Aim for longer streaks. Consecutive correct words increase your score quickly.");
        }
        if (profile.powerupsUsed == 0 && profile.wordsTyped >= 10) {
            recommendations.add("Use powerups during tough battles. They can protect your focus health.");
        }
        if (profile.unlockedLevel == 1 && profile.wordsTyped >= 20) {
            recommendations.add("You are ready for harder words. Push to finish Stage 1 and unlock Stage 2.");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Great balance so far. Keep pushing for higher streaks and cleaner accuracy.");
        }

        while (recommendations.size() > 3) {
            recommendations.remove(recommendations.size() - 1);
        }
        return recommendations;
    }
}
