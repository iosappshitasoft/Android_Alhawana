package com.app.alhawanajd.home.eventbus;

public class FollowingUpdateGiftMessageCount {

    public String VideoId, giftOrMessage, Count;

    public FollowingUpdateGiftMessageCount(String videoId, String giftOrMessage, String count) {
        VideoId = videoId;
        this.giftOrMessage = giftOrMessage;
        Count = count;
    }
}
