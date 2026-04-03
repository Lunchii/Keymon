import org.junit.jupiter.api.*;
import java.io.File;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ProfileStore}.
 * Covers PIN verification, account creation, authentication, password reset,
 * statistics reset, and the high score table.
 *
 * @author Group 72 (Isaac, Miguel, Ryan, Nathaniel, Dehou)
 * @version 1.0
 */
class ProfileStoreTest {

    private static final String TEST_USER = "testuser_junit";
    private static final String TEST_PASS = "pass1234";

    @BeforeEach
    void setup() {
        // Clean up any leftover test account before each test
        ProfileStore.deleteAccount(TEST_USER);
    }

    @AfterEach
    void teardown() {
        ProfileStore.deleteAccount(TEST_USER);
    }

    // ------------------------------------------------------------------ PIN

    @Test
    void correctPinAccepted() {
        assertTrue(ProfileStore.verifyParentPin("2212"));
    }

    @Test
    void wrongPinRejected() {
        assertFalse(ProfileStore.verifyParentPin("1234"));
    }

    @Test
    void emptyPinRejected() {
        assertFalse(ProfileStore.verifyParentPin(""));
    }

    @Test
    void nullPinRejected() {
        assertFalse(ProfileStore.verifyParentPin(null));
    }

    // -------------------------------------------------------- Account creation

    @Test
    void createAccountSucceeds() {
        assertTrue(ProfileStore.createAccount(TEST_USER, TEST_PASS));
    }

    @Test
    void accountExistsAfterCreation() {
        ProfileStore.createAccount(TEST_USER, TEST_PASS);
        assertTrue(ProfileStore.accountExists(TEST_USER));
    }

    @Test
    void duplicateAccountRejected() {
        ProfileStore.createAccount(TEST_USER, TEST_PASS);
        assertFalse(ProfileStore.createAccount(TEST_USER, "other1234"));
    }

    @Test
    void shortPasswordRejected() {
        assertNotNull(ProfileStore.validateNewAccount(TEST_USER, "ab"));
    }

    @Test
    void shortUsernameRejected() {
        assertNotNull(ProfileStore.validateNewAccount("ab", TEST_PASS));
    }

    @Test
    void blankUsernameRejected() {
        assertNotNull(ProfileStore.validateNewAccount("", TEST_PASS));
    }

    // ----------------------------------------------------- Authentication

    @Test
    void correctCredentialsAuthenticate() {
        ProfileStore.createAccount(TEST_USER, TEST_PASS);
        assertTrue(ProfileStore.authenticate(TEST_USER, TEST_PASS));
    }

    @Test
    void wrongPasswordRejected() {
        ProfileStore.createAccount(TEST_USER, TEST_PASS);
        assertFalse(ProfileStore.authenticate(TEST_USER, "wrongpass"));
    }

    @Test
    void unknownUserRejected() {
        assertFalse(ProfileStore.authenticate("nonexistent_user_xyz", "anypass"));
    }

    // ---------------------------------------------------- Password reset

    @Test
    void passwordResetWorks() {
        ProfileStore.createAccount(TEST_USER, TEST_PASS);
        assertTrue(ProfileStore.resetPassword(TEST_USER, "newpass99"));
        assertTrue(ProfileStore.authenticate(TEST_USER, "newpass99"));
        assertFalse(ProfileStore.authenticate(TEST_USER, TEST_PASS));
    }

    @Test
    void shortNewPasswordRejectedOnReset() {
        ProfileStore.createAccount(TEST_USER, TEST_PASS);
        assertFalse(ProfileStore.resetPassword(TEST_USER, "ab"));
    }

    // ------------------------------------------------- Statistics reset

    @Test
    void statisticsResetKeepsAccount() {
        ProfileStore.createAccount(TEST_USER, TEST_PASS);
        assertTrue(ProfileStore.resetStatistics(TEST_USER));
        assertTrue(ProfileStore.accountExists(TEST_USER));
    }

    @Test
    void statisticsResetClearsHighScore() {
        ProfileStore.createAccount(TEST_USER, TEST_PASS);
        ProfileStore.recordLevelSummary(TEST_USER, new ProfileStore.SessionStats(
                1, 500, 30.0, 40.0, 100, 110, 10, 20, 60L, 5, 1, true));
        ProfileStore.resetStatistics(TEST_USER);
        ProfileStore.ProfileData profile = ProfileStore.loadProfile(TEST_USER);
        assertEquals(0, profile.highScore);
    }

    // ---------------------------------------------------- High score table

    @Test
    void highScoreTableResetRestoresDefaults() {
        ProfileStore.resetHighScoreTable();
        List<ProfileStore.PlayerRecord> records = ProfileStore.getAllHighScores();
        assertFalse(records.isEmpty());
    }

    @Test
    void highScoreTableSortedDescending() {
        ProfileStore.resetHighScoreTable();
        List<ProfileStore.PlayerRecord> records = ProfileStore.getAllHighScores();
        for (int i = 0; i < records.size() - 1; i++) {
            assertTrue(records.get(i).score >= records.get(i + 1).score);
        }
    }

    @Test
    void recordedScoreAppearsInTable() {
        ProfileStore.createAccount(TEST_USER, TEST_PASS);
        ProfileStore.recordLevelSummary(TEST_USER, new ProfileStore.SessionStats(
                1, 9999, 30.0, 40.0, 100, 110, 5, 15, 60L, 3, 0, true));
        List<ProfileStore.PlayerRecord> records = ProfileStore.getAllHighScores();
        boolean found = records.stream().anyMatch(r -> r.name.equalsIgnoreCase(TEST_USER));
        assertTrue(found);
    }

    // ---------------------------------------------------- Profile loading

    @Test
    void loadProfileReturnsDefaultForNewUser() {
        ProfileStore.createAccount(TEST_USER, TEST_PASS);
        ProfileStore.ProfileData profile = ProfileStore.loadProfile(TEST_USER);
        assertEquals(0, profile.highScore);
        assertEquals(1, profile.unlockedLevel);
    }

    @Test
    void getAllUsernamesContainsCreatedUser() {
        ProfileStore.createAccount(TEST_USER, TEST_PASS);
        List<String> users = ProfileStore.getAllUsernames();
        assertTrue(users.contains(TEST_USER));
    }

    @Test
    void deleteAccountRemovesUser() {
        ProfileStore.createAccount(TEST_USER, TEST_PASS);
        ProfileStore.deleteAccount(TEST_USER);
        assertFalse(ProfileStore.accountExists(TEST_USER));
    }
}
