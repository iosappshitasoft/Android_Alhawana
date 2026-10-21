package com.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SearchMainPageResponse {

    @SerializedName("isbanner_enabled")
    @Expose
    private String isbannerEnabled;
    @SerializedName("banner_img")
    @Expose
    private String bannerImg;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("result")


    @Expose
    private List<Result> result = null;

    public String getIsbannerEnabled() {
        return isbannerEnabled;
    }

    public void setIsbannerEnabled(String isbannerEnabled) {
        this.isbannerEnabled = isbannerEnabled;
    }

    public String getBannerImg() {
        return bannerImg;
    }

    public void setBannerImg(String bannerImg) {
        this.bannerImg = bannerImg;
    }

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

    public static class Result {

        @SerializedName("total")
        @Expose
        private String total;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("streams")
        @Expose
        private List<Stream> streams = null;
        @SerializedName("isFavorite")
        @Expose
        private Boolean isFavorite;

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Stream> getStreams() {
            return streams;
        }

        public void setStreams(List<Stream> streams) {
            this.streams = streams;
        }

        public Boolean getIsFavorite() {
            return isFavorite;
        }

        public void setIsFavorite(Boolean isFavorite) {
            this.isFavorite = isFavorite;
        }

        public static class Stream {

            @SerializedName("stream_name")
            @Expose
            private String streamName;
            @SerializedName("video_id")
            @Expose
            private String videoId;
            @SerializedName("stream_thumbnail")
            @Expose
            private String streamThumbnail;

            public String getStreamName() {
                return streamName;
            }

            public void setStreamName(String streamName) {
                this.streamName = streamName;
            }

            public String getVideoId() {
                return videoId;
            }

            public void setVideoId(String videoId) {
                this.videoId = videoId;
            }

            public String getStreamThumbnail() {
                return streamThumbnail;
            }

            public void setStreamThumbnail(String streamThumbnail) {
                this.streamThumbnail = streamThumbnail;
            }

        }

    }

}
