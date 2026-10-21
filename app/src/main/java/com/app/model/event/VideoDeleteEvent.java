package com.app.model.event;

public class VideoDeleteEvent {
    private boolean isVideoDeleted;
    private String videoId;

    public VideoDeleteEvent(boolean isVideoDeleted, String videoId) {
        this.isVideoDeleted = isVideoDeleted;
        this.videoId = videoId;
    }

    public boolean isVideoDeleted() {
        return isVideoDeleted;
    }

    public void setVideoDeleted(boolean videoDeleted) {
        isVideoDeleted = videoDeleted;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }
}
