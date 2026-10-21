package com.app.alhawanajd.home.eventbus;

public class ForYouVideoFav {

    public String videoId, isFav;
    public Boolean status;

    //Change state for Sound, Fav, Report
    public ForYouVideoFav(String videoId, String isFavReport, Boolean status) {
        this.videoId = videoId;
        this.isFav = isFavReport;
        this.status = status;
    }
}
