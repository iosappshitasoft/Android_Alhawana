package com.app.alhawanajd.home.eventbus;

public class FollowingVideoLike{

    public String VideoId, LikeStatus, LikeCount;

    public FollowingVideoLike(String videoId, String likeStatus, String likeCount) {
        VideoId = videoId;
        LikeStatus = likeStatus;
        LikeCount = likeCount;
    }
}
