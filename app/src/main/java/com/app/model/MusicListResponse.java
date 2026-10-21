package com.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MusicListResponse {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("result")
    @Expose
    private List<Result> result = null;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Result> getResult() {
        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }

    public class Result {

        @SerializedName("cover_image")
        @Expose
        private String coverImage;
        @SerializedName("sound_id")
        @Expose
        private String soundId;
        @SerializedName("sound_by")
        @Expose
        private String soundBy;
        @SerializedName("isFavorite")
        @Expose
        private boolean isFavorite;
        @SerializedName("title")
        @Expose
        private String title;
        @SerializedName("sound_url")
        @Expose
        private String soundUrl;
        @SerializedName("duration")
        @Expose
        private String duration;

        public String getCoverImage() {
            return coverImage;
        }

        public void setCoverImage(String coverImage) {
            this.coverImage = coverImage;
        }

        public String getSoundId() {
            return soundId;
        }

        public void setSoundId(String soundId) {
            this.soundId = soundId;
        }

        public boolean getIsFavorite() {
            return isFavorite;
        }

        public void setIsFavorite(boolean isFavorite) {
            this.isFavorite = isFavorite;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSoundUrl() {
            return soundUrl;
        }

        public void setSoundUrl(String soundUrl) {
            this.soundUrl = soundUrl;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public String getSoundBy() {
            return soundBy;
        }

        public void setSoundBy(String soundBy) {
            this.soundBy = soundBy;
        }
    }
}
