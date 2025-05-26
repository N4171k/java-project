package audio;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundManager {
    public void playSound(String soundName) {
        try {
            File soundFile = new File("resources/sounds/" + soundName + ".wav");
            if (!soundFile.exists()) {
                System.err.println("Sound error: " + soundFile.getPath() + " (The system cannot find the path specified)");
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            System.err.println("Sound error: " + e.getMessage());
        }
    }

    public void playMusic(String musicName) {
        // Placeholder for background music
    }

    public void stopMusic() {
        // Placeholder for stopping music
    }
} 