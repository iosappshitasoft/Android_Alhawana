package com.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HashTagResponse {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("result")
    @Expose
    private Result result;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public static class Result {

        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("total")
        @Expose
        private String total;
        @SerializedName("videos")
        @Expose
        private List<VideoListResponse.Result> videos = null;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }

        public List<VideoListResponse.Result> getVideos() {
            return videos;
        }

        public void setVideos(List<VideoListResponse.Result> videos) {
            this.videos = videos;
        }
    }

}






