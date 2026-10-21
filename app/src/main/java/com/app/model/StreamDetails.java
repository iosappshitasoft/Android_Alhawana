
package com.app.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class StreamDetails implements Serializable {

    @SerializedName("status")
    public String status;

    @SerializedName("message")
    public String message;

    @SerializedName("name")
    public String name;

    @SerializedName("stream_name")
    public String streamName;

    @SerializedName("stream_image")
    public String streamImage;

    @SerializedName("title")
    public String title;

    @SerializedName("posted_by")
    public String postedBy;

    @SerializedName("publisher_id")
    public String publisherId;

    @SerializedName("publisher_name")
    public String publisherName;

    @SerializedName("likes")
    public String likes;

    @SerializedName("watch_count")
    public String watchCount;

    @SerializedName("reported")
    public String reported;

    @SerializedName("stream_thumbnail")
    public String streamThumbnail;

    @SerializedName("user_id")
    public String userId;

    @SerializedName("follow")
    public String follow;

    @SerializedName("hashtags")
    public ArrayList<String> hashTags;

    @SerializedName("publisher_image")
    public String publisherImage;

    @SerializedName("stream_blocked")
    public int streamBlocked;

    @SerializedName("type")
    public String type;

    @SerializedName("stream_id")
    public String streamId;

    @SerializedName("stream_type")
    public String streamType;

    @SerializedName("streamed_on")
    public String streamedOn;

    @SerializedName("invite_link")
    public String inviteLink;

    @SerializedName("playback_url")
    public String playBackUrl;

    @SerializedName("playback_ready")
    public String playBackReady;

    @SerializedName("playback_duration")
    public String playBackDuration;

    @SerializedName("live_viewers")
    public ArrayList<LiveViewers> liveViewers;

    @SerializedName("comments")
    public ArrayList<Comments> commentsList;

    @SerializedName("live_gifts")
    public ArrayList<Gifts> giftList;

    @SerializedName("mode")
    public String mode;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public String getStreamImage() {
        return streamImage;
    }

    public void setStreamImage(String streamImage) {
        this.streamImage = streamImage;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }

    public String getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(String publisherId) {
        this.publisherId = publisherId;
    }

    public ArrayList<String> getHashTags() {
        return hashTags;
    }

    public void setHashTags(ArrayList<String> hashTags) {
        this.hashTags = hashTags;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getWatchCount() {
        return watchCount;
    }

    public void setWatchCount(String watchCount) {
        this.watchCount = watchCount;
    }

    public String getReported() {
        return reported;
    }

    public void setReported(String reported) {
        this.reported = reported;
    }

    public String getStreamThumbnail() {
        return streamThumbnail;
    }

    public void setStreamThumbnail(String streamThumbnail) {
        this.streamThumbnail = streamThumbnail;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFollow() {
        return follow;
    }

    public void setFollow(String follow) {
        this.follow = follow;
    }

    public String getPublisherImage() {
        return publisherImage;
    }

    public void setPublisherImage(String publisherImage) {
        this.publisherImage = publisherImage;
    }

    public ArrayList<LiveViewers> getLiveViewers() {
        return liveViewers;
    }

    public void setLiveViewers(ArrayList<LiveViewers> liveViewers) {
        this.liveViewers = liveViewers;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }


    public int getStreamBlocked() {
        return streamBlocked;
    }

    public void setStreamBlocked(int streamBlocked) {
        this.streamBlocked = streamBlocked;
    }

    public ArrayList<Comments> getCommentsList() {
        return commentsList;
    }

    public void setCommentsList(ArrayList<Comments> commentsList) {
        this.commentsList = commentsList;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getStreamType() {
        return streamType;
    }

    public void setStreamType(String streamType) {
        this.streamType = streamType;
    }

    public String getStreamedOn() {
        return streamedOn;
    }

    public void setStreamedOn(String streamedOn) {
        this.streamedOn = streamedOn;
    }

    public String getInviteLink() {
        return inviteLink;
    }

    public void setInviteLink(String inviteLink) {
        this.inviteLink = inviteLink;
    }

    public String getPlayBackUrl() {
        return playBackUrl;
    }

    public void setPlayBackUrl(String playBackUrl) {
        this.playBackUrl = playBackUrl;
    }

    public String getPlayBackReady() {
        return playBackReady;
    }

    public void setPlayBackReady(String playBackReady) {
        this.playBackReady = playBackReady;
    }

    public String getPlayBackDuration() {
        return playBackDuration;
    }

    public void setPlayBackDuration(String playBackDuration) {
        this.playBackDuration = playBackDuration;
    }

    public ArrayList<Gifts> getGiftList() {
        return giftList;
    }

    public void setGiftList(ArrayList<Gifts> giftList) {
        this.giftList = giftList;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public class LiveViewers implements Serializable {

        @SerializedName("user_id")
        public String userId;

        @SerializedName("user_name")
        public String userName;

        @SerializedName("user_image")
        public String userImage;

        @SerializedName("subscriber_on")
        public String subscriberOn;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getUserImage() {
            return userImage;
        }

        public void setUserImage(String userImage) {
            this.userImage = userImage;
        }

        public String getSubscriberOn() {
            return subscriberOn;
        }

        public void setSubscriberOn(String subscriberOn) {
            this.subscriberOn = subscriberOn;
        }


        @Override
        public boolean equals(@Nullable Object object) {
            boolean result = false;
            if (object == null || object.getClass() != getClass()) {
                result = false;
            } else {
                LiveViewers method = (LiveViewers) object;
                if (this.userId.equals(method.getUserId())) {
                    result = true;
                }
            }
            return result;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 7 * hash + this.userId.hashCode();
            return hash;
        }
    }

    public class Comments implements Serializable {
        @SerializedName("type")
        public String type;

        @SerializedName("user_id")
        public String userId;

        @SerializedName("user_name")
        public String userName;

        @SerializedName("user_image")
        public String userImage;

        @SerializedName("stream_name")
        public String streamName;

        @SerializedName("commented_on")
        public String commentedOn;

        @SerializedName("message")
        public String message;

        @SerializedName("gift_title")
        public String giftTitle;

        @SerializedName("gift_icon")
        public String giftIcon;

        @SerializedName("like_color")
        public String likeColor;

        @SerializedName("time")
        public String time;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getUserImage() {
            return userImage;
        }

        public void setUserImage(String userImage) {
            this.userImage = userImage;
        }

        public String getStreamName() {
            return streamName;
        }

        public void setStreamName(String streamName) {
            this.streamName = streamName;
        }

        public String getCommentedOn() {
            return commentedOn;
        }

        public void setCommentedOn(String commentedOn) {
            this.commentedOn = commentedOn;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getGiftTitle() {
            return giftTitle;
        }

        public void setGiftTitle(String giftTitle) {
            this.giftTitle = giftTitle;
        }

        public String getGiftIcon() {
            return giftIcon;
        }

        public void setGiftIcon(String giftIcon) {
            this.giftIcon = giftIcon;
        }

        public String getLikeColor() {
            return likeColor;
        }

        public void setLikeColor(String likeColor) {
            this.likeColor = likeColor;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }

    public class Gifts implements Serializable {
        @SerializedName("type")
        public String type;

        @SerializedName("user_id")
        public String userId;

        @SerializedName("user_name")
        public String userName;

        @SerializedName("user_image")
        public String userImage;

        @SerializedName("stream_name")
        public String streamName;

        @SerializedName("time")
        public String time;

        @SerializedName("gift_title")
        public String giftTitle;

        @SerializedName("gift_icon")
        public String giftIcon;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getUserImage() {
            return userImage;
        }

        public void setUserImage(String userImage) {
            this.userImage = userImage;
        }

        public String getStreamName() {
            return streamName;
        }

        public void setStreamName(String streamName) {
            this.streamName = streamName;
        }

        public String getGiftTitle() {
            return giftTitle;
        }

        public void setGiftTitle(String giftTitle) {
            this.giftTitle = giftTitle;
        }

        public String getGiftIcon() {
            return giftIcon;
        }

        public void setGiftIcon(String giftIcon) {
            this.giftIcon = giftIcon;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }
}

