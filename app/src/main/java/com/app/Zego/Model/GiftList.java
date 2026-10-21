package com.app.Zego.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GiftList {
        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("video_path")
        @Expose
        private String videoPath;
        @SerializedName("audio_path")
        @Expose
        private String audioPath;
        @SerializedName("svga_file")
        @Expose
        private String svgaFile;
    @SerializedName("animation_duration")
    @Expose
    private double animation_time;

    public double getAnimation_time() {
        return animation_time;
    }

    public void setAnimation_time(double animation_time) {
        this.animation_time = animation_time;
    }
    @SerializedName("loop_count")
    @Expose
    private String loopCount;

    public GiftList(String type, String videoPath, String audioPath, String svgaFile, String loop_count,double animation_time) {
        this.type = type;
        this.videoPath = videoPath;
        this.audioPath = audioPath;
        this.svgaFile = svgaFile;
        this.loopCount = loop_count;
        this.animation_time = animation_time;
    }

    public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getVideoPath() {
            return videoPath;
        }

        public void setVideoPath(String videoPath) {
            this.videoPath = videoPath;
        }

        public String getAudioPath() {
            return audioPath;
        }

        public void setAudioPath(String audioPath) {
            this.audioPath = audioPath;
        }

        public String getSvgaFile() {
            return svgaFile;
        }

        public void setSvgaFile(String svgaFile) {
            this.svgaFile = svgaFile;
        }

    public String getLoopCount() {
        return loopCount;
    }

    public void setLoopCount(String loopCount) {
        this.loopCount = loopCount;
    }

}
