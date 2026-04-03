import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ProfileInsights}.
 * Covers achievement unlocking and coaching recommendation generation.
 *
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
class ProfileInsightsTest {

    /** Creates a minimal ProfileData with all fields set to zero/defaults. */
    private ProfileStore.ProfileData blankProfile() {
        return new ProfileStore.ProfileData(
                "tester", 0, 1, 1,
                0.0, 0.0, 100.0,
                0, 0L, 0,
                0, 0, 0, 0, 0, 0);
    }

    /** Creates a ProfileData with specific stat values for targeted tests. */
    private ProfileStore.ProfileData profileWith(
            int highScore, int unlockedLevel, double avgWpm, double peakWpm,
            double accuracy, int wordsTyped, int longestStreak, int powerupsUsed,
            int gamesPlayed) {
        return new ProfileStore.ProfileData(
                "tester", highScore, 1, unlockedLevel,
                avgWpm, peakWpm, accuracy,
                0, 60L, wordsTyped,
                0, 0, longestStreak, powerupsUsed, gamesPlayed, 0);
    }

    // ------------------------------------------------- Null safety

    @Test
    void nullProfileReturnsEmptyAchievements() {
        assertTrue(ProfileInsights.buildAchievements(null).isEmpty());
    }

    @Test
    void nullProfileReturnsEmptyRecommendations() {
        assertTrue(ProfileInsights.buildRecommendations(null).isEmpty());
    }

    // ------------------------------------------------- Achievements

    @Test
    void noAchievementsForBlankProfile() {
        List<String> achievements = ProfileInsights.buildAchievements(blankProfile());
        assertTrue(achievements.isEmpty());
    }

    @Test
    void wordWranglerUnlockedAt10Words() {
        ProfileStore.ProfileData profile = profileWith(0, 1, 0, 0, 100, 10, 0, 0, 1);
        List<String> achievements = ProfileInsights.buildAchievements(profile);
        assertTrue(achievements.stream().anyMatch(a -> a.contains("Word Wrangler")));
    }

    @Test
    void speedSparkUnlockedAt30Wpm() {
        ProfileStore.ProfileData profile = profileWith(0, 1, 30, 30, 100, 0, 0, 0, 0);
        List<String> achievements = ProfileInsights.buildAchievements(profile);
        assertTrue(achievements.stream().anyMatch(a -> a.contains("Speed Spark")));
    }

    @Test
    void lightningFingersUnlockedAt45Wpm() {
        ProfileStore.ProfileData profile = profileWith(0, 1, 45, 45, 100, 0, 0, 0, 0);
        List<String> achievements = ProfileInsights.buildAchievements(profile);
        assertTrue(achievements.stream().anyMatch(a -> a.contains("Lightning Fingers")));
    }

    @Test
    void forestScoutUnlockedAtLevel2() {
        ProfileStore.ProfileData profile = profileWith(0, 2, 0, 0, 100, 0, 0, 0, 0);
        List<String> achievements = ProfileInsights.buildAchievements(profile);
        assertTrue(achievements.stream().anyMatch(a -> a.contains("Forest Scout")));
    }

    @Test
    void skyChallengerUnlockedAtLevel3() {
        ProfileStore.ProfileData profile = profileWith(0, 3, 0, 0, 100, 0, 0, 0, 0);
        List<String> achievements = ProfileInsights.buildAchievements(profile);
        assertTrue(achievements.stream().anyMatch(a -> a.contains("Sky Challenger")));
    }

    @Test
    void highScoreHeroUnlockedAt1500() {
        ProfileStore.ProfileData profile = profileWith(1500, 1, 0, 0, 100, 0, 0, 0, 0);
        List<String> achievements = ProfileInsights.buildAchievements(profile);
        assertTrue(achievements.stream().anyMatch(a -> a.contains("High Score Hero")));
    }

    // ----------------------------------------------- Recommendations

    @Test
    void recommendationsNotEmpty() {
        List<String> recs = ProfileInsights.buildRecommendations(blankProfile());
        assertFalse(recs.isEmpty());
    }

    @Test
    void recommendationsMaxThree() {
        List<String> recs = ProfileInsights.buildRecommendations(blankProfile());
        assertTrue(recs.size() <= 3);
    }

    @Test
    void lowAccuracyTriggersAccuracyTip() {
        ProfileStore.ProfileData profile = profileWith(0, 1, 25, 25, 80, 20, 3, 1, 2);
        List<String> recs = ProfileInsights.buildRecommendations(profile);
        assertTrue(recs.stream().anyMatch(r -> r.toLowerCase().contains("slow down") ||
                r.toLowerCase().contains("error") || r.toLowerCase().contains("accura")));
    }
}
