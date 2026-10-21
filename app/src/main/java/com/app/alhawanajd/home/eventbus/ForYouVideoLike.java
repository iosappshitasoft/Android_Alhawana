package com.app.alhawanajd.home.eventbus;

public class ForYouVideoLike {

    public String VideoId, LikeStatus, LikeCount;

    public ForYouVideoLike(String videoId, String likeStatus, String likeCount) {
        VideoId = videoId;
        LikeStatus = likeStatus;
        LikeCount = likeCount;
    }
}
