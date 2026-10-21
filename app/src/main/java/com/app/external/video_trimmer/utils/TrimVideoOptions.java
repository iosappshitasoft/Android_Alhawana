package com.app.external.video_trimmer.utils;

public class TrimVideoOptions {

    public String fileName;

    public TrimType trimType = TrimType.DEFAULT;

    public long minDuration, fixedDuration;

    public boolean hideSeekBar;

    public boolean accurateCut;

    public boolean showFileLocationAlert;

    public long[] minToMax;

    public String title;

    public String local;

    public String mediaFrom;

    public String soundId;

    public String soundUrl;

    public CompressOption compressOption;

    public TrimVideoOptions() {
    }

}
