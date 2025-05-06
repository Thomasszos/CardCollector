package org.example.cardcollectorproject.utils;

import javafx.scene.media.MediaPlayer;
import javafx.scene.media.Media;
import javafx.util.Duration;

import java.net.URISyntaxException;
import java.net.URL;

/**
 * Manages audio for the application (background music, sound effects)
 */
public class AudioManager {
    private static AudioManager instance;

    // Background music settings
    private double musicVolume = 0.1;
    private boolean isMusicMuted = false;

    // Sound effects settings
    private double sfxVolume = 0.3;
    private boolean isSfxMuted = false;

    // Current music player and track name
    private MediaPlayer backgroundMusicPlayer;
    private String currentTrack;

    /**
     * Get the AudioManager singleton instance
     */
    public static synchronized AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    /**
     * Start playing background music from a resource file
     * @param musicFileName Name of the music file in the resources/audio folder
     */
    public void playBackgroundMusic(String musicFileName) {
        // Always stop current playback for a guaranteed clean state
        stopBackgroundMusic();

        try {
            System.out.println("Loading background music: " + musicFileName);
            URL resource = getClass().getResource("/org/example/cardcollectorproject/audio/" + musicFileName);
            if (resource == null) {
                System.err.println("Could not find audio file: " + musicFileName);
                return;
            }

            Media media = new Media(resource.toURI().toString());
            backgroundMusicPlayer = new MediaPlayer(media);

            // Configure player
            backgroundMusicPlayer.setVolume(isMusicMuted ? 0 : musicVolume);
            backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop indefinitely

            // Handle end of media
            backgroundMusicPlayer.setOnEndOfMedia(() -> {
                backgroundMusicPlayer.seek(Duration.ZERO); // Restart from beginning
            });

            // Set error handler to detect any issues
            backgroundMusicPlayer.setOnError(() -> {
                System.err.println("Media player error: " + backgroundMusicPlayer.getError().getMessage());
            });

            // Start playback
            backgroundMusicPlayer.play();
            currentTrack = musicFileName;

            System.out.println("Background music started: " + musicFileName);

        } catch (URISyntaxException e) {
            System.err.println("Error loading audio file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error playing audio: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Stop any currently playing background music
     */
    public void stopBackgroundMusic() {
        if (backgroundMusicPlayer != null) {
            try {
                System.out.println("Stopping background music");
                backgroundMusicPlayer.stop();
                backgroundMusicPlayer.dispose();
            } catch (Exception e) {
                System.err.println("Error stopping background music: " + e.getMessage());
            } finally {
                backgroundMusicPlayer = null;
            }
        }
    }

    /**
     * Play a sound effect once
     * @param sfxFileName Name of the sound effect file in the resources/audio folder
     */
    public void playSoundEffect(String sfxFileName) {
        if (isSfxMuted) {
            return; // Don't play if sound effects are muted
        }

        try {
            URL resource = getClass().getResource("/org/example/cardcollectorproject/audio/" + sfxFileName);
            if (resource == null) {
                System.err.println("Could not find sound effect file: " + sfxFileName);
                return;
            }

            Media media = new Media(resource.toURI().toString());
            MediaPlayer sfxPlayer = new MediaPlayer(media);

            // Configure player with sound effects volume
            sfxPlayer.setVolume(sfxVolume);

            // Auto-dispose after playing
            sfxPlayer.setOnEndOfMedia(sfxPlayer::dispose);

            // Set error handler
            sfxPlayer.setOnError(() -> {
                System.err.println("Sound effect error: " + sfxPlayer.getError().getMessage());
                sfxPlayer.dispose();
            });

            // Start playback
            sfxPlayer.play();

        } catch (URISyntaxException e) {
            System.err.println("Error loading sound effect file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error playing sound effect: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Set the background music volume
     * @param volume Volume level from 0.0 (silent) to 1.0 (max)
     */
    public void setVolume(double volume) {
        this.musicVolume = Math.max(0, Math.min(1, volume)); // Clamp between 0 and 1

        // Update player volume if it exists
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.setVolume(isMusicMuted ? 0 : this.musicVolume);
        }
    }

    /**
     * Get the current background music volume level
     * @return Volume level from 0.0 to 1.0
     */
    public double getVolume() {
        return musicVolume;
    }

    /**
     * Set the sound effects volume
     * @param volume Volume level from 0.0 (silent) to 1.0 (max)
     */
    public void setSfxVolume(double volume) {
        this.sfxVolume = Math.max(0, Math.min(1, volume)); // Clamp between 0 and 1
    }

    /**
     * Get the current sound effects volume level
     * @return Volume level from 0.0 to 1.0
     */
    public double getSfxVolume() {
        return sfxVolume;
    }

    /**
     * Set whether background music is muted
     * @param muted True to mute, false to unmute
     */
    public void setMuted(boolean muted) {
        this.isMusicMuted = muted;
        System.out.println("Background music muted state set to: " + muted);

        // Update player state
        if (backgroundMusicPlayer != null) {
            if (muted) {
                backgroundMusicPlayer.setVolume(0);
            } else {
                backgroundMusicPlayer.setVolume(musicVolume);
            }
        }
    }

    /**
     * Check if background music is currently muted
     * @return True if background music is muted
     */
    public boolean isMuted() {
        return isMusicMuted;
    }

    /**
     * Set whether sound effects are muted
     * @param muted True to mute, false to unmute
     */
    public void setSfxMuted(boolean muted) {
        this.isSfxMuted = muted;
        System.out.println("Sound effects muted state set to: " + muted);
    }

    /**
     * Check if sound effects are currently muted
     * @return True if sound effects are muted
     */
    public boolean isSfxMuted() {
        return isSfxMuted;
    }

    /**
     * Toggle mute state for background music and return the new state
     * @return True if now muted, false if now unmuted
     */
    public boolean toggleMute() {
        setMuted(!this.isMusicMuted);
        return this.isMusicMuted;
    }

    /**
     * Toggle mute state for sound effects and return the new state
     * @return True if now muted, false if now unmuted
     */
    public boolean toggleSfxMute() {
        setSfxMuted(!this.isSfxMuted);
        return this.isSfxMuted;
    }

    /**
     * Get the name of the currently playing track
     * @return The filename of the current track, or null if none is playing
     */
    public String getCurrentTrack() {
        return currentTrack;
    }
}

// ** Some Methods have not been implemented yet but may be added in the future ** //