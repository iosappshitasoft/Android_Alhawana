package com.app.alhawanajd.home.eventbus;

public class ForYouUpdateGiftMessageCount {

    public String videoId, giftOrMessage, Count;

    public ForYouUpdateGiftMessageCount(String videoId, String giftOrMessage, String count) {
        this.videoId = videoId;
        this.giftOrMessage = giftOrMessage;
        this.Count = count;
    }
}
