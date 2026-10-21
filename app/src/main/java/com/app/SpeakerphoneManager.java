package com.app;
import android.media.AudioManager;
import android.util.Log;
import android.content.Context;

public class SpeakerphoneManager {
    private static final String TAG = "SpeakerphoneManager";
    private AudioManager audioManager;

    public SpeakerphoneManager(Context context) {
        // Get AudioManager instance
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public boolean setSpeakerphoneOn(boolean on) {
        try {
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioManager.setSpeakerphoneOn(on);
            Log.d(TAG, "Speakerphone turned " + (on ? "on" : "off"));
            return true; // Successfully set speakerphone state
        } catch (Exception e) {
            Log.e(TAG, "Error setting speakerphone state: " + e.getMessage());
            return false; // Failed to set speakerphone state
        }
    }

    // Example usage:
    public static void main(String[] args) {
        // Initialize SpeakerphoneManager with context
        Context context = null; // Replace with your actual context
        SpeakerphoneManager speakerphoneManager = new SpeakerphoneManager(context);

        // Turn on speakerphone
        speakerphoneManager.setSpeakerphoneOn(true);

        // Turn off speakerphone
        speakerphoneManager.setSpeakerphoneOn(false);
    }
}