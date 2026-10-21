package com.app.alhawanajd;

public class Utility {
    //audio format in which file after trim will be saved.
    public static final String AUDIO_FORMAT = ".mp3";
    public static final String VIDEO_FORMAT = ".mp4";
    public static final String IMAGE_FORMAT = ".jpg";

    //audio mime type in which file after trim will be saved.
    public static final String AUDIO_MIME_TYPE = "audio/mp3";

    public static long getCurrentTime() {
        return System.nanoTime() / 1000000;
    }

}
