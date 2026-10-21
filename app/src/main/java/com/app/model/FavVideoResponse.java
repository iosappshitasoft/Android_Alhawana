package com.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FavVideoResponse {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("videos")
    @Expose
    private List<VideoListResponse.Result> videos = null;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<VideoListResponse.Result> getVideos() {
        return videos;
    }

    public void setVideos(List<VideoListResponse.Result> videos) {
        this.videos = videos;
    }

}
