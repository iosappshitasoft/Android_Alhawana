package com.app.alhawanajd.home.eventbus;

public class FollowingVideoFav {

    public String videoId, isFav;
    public Boolean status;

    public FollowingVideoFav(String videoId, String isFavReport, Boolean status) {
        this.videoId = videoId;
        this.isFav = isFavReport;
        this.status = status;
    }
}
