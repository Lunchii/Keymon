import javax.sound.sampled.*;
import java.io.File;

/**
 * Singleton music player that loops WAV tracks.
 * Call {@link #play(String)} with a track name to start music,
 * and {@link #stop()} to silence it.
 *
 * @author Group 72
 * @version 1.0
 */
public class MusicPlayer {

    private static Clip currentClip;
    private static String currentTrack = "";

    private MusicPlayer() {}

    /**
     * Plays a looping WAV track from the {@code music/} folder.
     * If the requested track is already playing, this is a no-op.
     *
     * @param trackName filename without extension (e.g. "menu", "battle", "level1")
     */
    public static void play(String trackName) {
        if (trackName.equals(currentTrack) && currentClip != null && currentClip.isRunning()) {
            return;
        }
        stop();
        try {
            File file = new File("music/" + trackName + ".wav");
            if (!file.exists()) {
                System.err.println("MusicPlayer: file not found — " + file.getPath());
                return;
            }
            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            currentClip = AudioSystem.getClip();
            currentClip.open(stream);
            if (currentClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);
                gain.setValue(trackName.equals("battle") ? -15.0f : -12.0f);
            }
            currentClip.loop(Clip.LOOP_CONTINUOUSLY);
            currentClip.start();
            currentTrack = trackName;
        } catch (Exception e) {
            System.err.println("MusicPlayer: could not play " + trackName + " — " + e.getMessage());
        }
    }

    /**
     * Plays a one-shot sound effect from the {@code music/} folder without
     * interrupting the background music track.
     *
     * @param effectName filename without extension (e.g. "attack")
     */
    public static void playEffect(String effectName) {
        try {
            File file = new File("music/" + effectName + ".wav");
            if (!file.exists()) {
                return;
            }
            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gain.setValue(-9.0f);
            }
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
            clip.start();
        } catch (Exception e) {
            System.err.println("MusicPlayer: could not play effect " + effectName + " — " + e.getMessage());
        }
    }

    /**
     * Stops and closes the currently playing track.
     */
    public static void stop() {
        if (currentClip != null) {
            currentClip.stop();
            currentClip.close();
            currentClip = null;
        }
        currentTrack = "";
    }
}
