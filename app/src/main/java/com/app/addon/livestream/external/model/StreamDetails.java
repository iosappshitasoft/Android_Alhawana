
package com.app.addon.livestream.external.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class StreamDetails implements Serializable {

    @SerializedName("mode")
    public String mode;

    @SerializedName("streamblock")
    public String streamblock;

    @SerializedName("streamkickout")
    public String streamkickout;

    @SerializedName("verified_user")
    public String profile_verified;

    @SerializedName("main_stream_name")
    public String mainStreamName;

    @SerializedName("level")
    public String user_level;

    @SerializedName("last_gifted_date")
    public String last_gifted_date;

    @SerializedName("status")
    public String status;

    @SerializedName("message")
    public String message;

    @SerializedName("name")
    public String name;

    @SerializedName("stream_name")
    public String streamName;

    @SerializedName("stream_cohost_list")
    @Expose
    private List<StreamCohost> streamCohostList;

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

    public String getShareStreamLink() {
        return shareStreamLink;
    }

    public void setShareStreamLink(String shareStreamLink) {
        this.shareStreamLink = shareStreamLink;
    }

    @SerializedName("share_stream_link")
    public String shareStreamLink;

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

    @SerializedName("total_like_count")
    public String total_like_count;

    @SerializedName("total_gem_earning")
    public String totalGemEarning;

    @SerializedName("primestream")
    public String isprimestream;

    @SerializedName("battle_start_time")
    public String battle_start_time;

    @SerializedName("battle_choosed_duration")
    public String battle_choosed_duration;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getStreamblock() {
        return streamblock;
    }

    public void setStreamblock(String streamblock) {
        this.streamblock = streamblock;
    }

    public String getStreamkickout() {
        return streamkickout;
    }

    public void setStreamkickout(String streamkickout) {
        this.streamkickout = streamkickout;
    }

    public String getMainStreamName() {
        return mainStreamName;
    }

    public void setMainStreamName(String mainStreamName) {
        this.mainStreamName = mainStreamName;
    }

    public String getProfile_verified() {
        return profile_verified;
    }

    public void setProfile_verified(String profile_verified) {
        this.profile_verified = profile_verified;
    }

    public String getLast_gifted_date() {
        return last_gifted_date;
    }

    public void setLast_gifted_date(String last_gifted_date) {
        this.last_gifted_date = last_gifted_date;
    }

    public String getUser_level() {
        return user_level;
    }

    public void setUser_level(String user_level) {
        this.user_level = user_level;
    }


    public String getBattle_start_time() {
        return battle_start_time;
    }

    public void setBattle_start_time(String battle_start_time) {
        this.battle_start_time = battle_start_time;
    }

    public String getBattle_choosed_duration() {
        return battle_choosed_duration;
    }

    public void setBattle_choosed_duration(String battle_choosed_duration) {
        this.battle_choosed_duration = battle_choosed_duration;
    }

    public String getBattle_started() {
        return battle_started;
    }

    public void setBattle_started(String battle_started) {
        this.battle_started = battle_started;
    }

    public List<BattleStreamer> getBattleStreamerList() {
        return battleStreamerList;
    }

    public void setBattleStreamerList(List<BattleStreamer> battleStreamerList) {
        this.battleStreamerList = battleStreamerList;
    }

    @SerializedName("battle_started")
    public String battle_started;

    @SerializedName("battle_streamer_list")
    @Expose
    private List<BattleStreamer> battleStreamerList;
    public String getIsprimestream() {
        return isprimestream;
    }

    public void setIsprimestream(String isprimestream) {
        this.isprimestream = isprimestream;
    }


    public String getTotalGemEarning() {
        return totalGemEarning;
    }

    public void setTotalGemEarning(String totalGemEarning) {
        this.totalGemEarning = totalGemEarning;
    }




    public String getTotal_like_count() {
        return total_like_count;
    }

    public void setTotal_like_count(String total_like_count) {
        this.total_like_count = total_like_count;
    }



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

    public List<StreamCohost> getStreamCohostList() {
        return streamCohostList;
    }

    public void setStreamCohostList(List<StreamCohost> streamCohostList) {
        this.streamCohostList = streamCohostList;
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


    public class LiveViewers implements Serializable{

        @SerializedName("user_id")
        public String userId;

        @SerializedName("user_name")
        public String userName;

        @SerializedName("user_image")
        public String userImage;

        @SerializedName("subscriber_on")
        public String subscriberOn;

        public String getPremium_member() {
            return premium_member;
        }

        public void setPremium_member(String premium_member) {
            this.premium_member = premium_member;
        }

        @SerializedName("premium_member")
        public String premium_member;
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

    public class Comments implements Serializable{
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

    public class Gifts implements Serializable{
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

        public String getGift_to() {
            return gift_to;
        }

        public void setGift_to(String gift_to) {
            this.gift_to = gift_to;
        }

        @SerializedName("gift_to")
        public String gift_to;

        public int getGems_count() {
            return gems_count;
        }

        public void setGems_count(int gems_count) {
            this.gems_count = gems_count;
        }

        @SerializedName("gems_count")
        public int gems_count;

        public String getHost_id() {
            return host_id;
        }

        public void setHost_id(String host_id) {
            this.host_id = host_id;
        }

        @SerializedName("host_id")
        public String host_id;

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

    public static class StreamCohost {

        @SerializedName("user_id")
        @Expose
        private String userId;
        @SerializedName("user_type")
        @Expose
        private String userType;
        @SerializedName("user_name")
        @Expose
        private String userName;
        @SerializedName("gem_earned")
        @Expose
        private Integer gemEarned;
        @SerializedName("user_image")
        @Expose
        private String userImage;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserType() {
            return userType;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public Integer getGemEarned() {
            return gemEarned;
        }

        public void setGemEarned(Integer gemEarned) {
            this.gemEarned = gemEarned;
        }

        public String getUserImage() {
            return userImage;
        }

        public void setUserImage(String userImage) {
            this.userImage = userImage;
        }

    }

    public class BattleStreamer {

        @SerializedName("user_id")
        @Expose
        private String userId;
        @SerializedName("user_name")
        @Expose
        private String userName;
        @SerializedName("gem_earned")
        @Expose
        private Integer gemEarned;
        @SerializedName("blast_like_gems")
        @Expose
        private Integer blastLikeGems;
        @SerializedName("twox_gems")
        @Expose
        private Integer twoxGems;
        @SerializedName("threex_gems")
        @Expose
        private Integer threexGems;
        @SerializedName("user_image")
        @Expose
        private String userImage;

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

        public Integer getGemEarned() {
            return gemEarned;
        }

        public void setGemEarned(Integer gemEarned) {
            this.gemEarned = gemEarned;
        }

        public Integer getBlastLikeGems() {
            return blastLikeGems;
        }

        public void setBlastLikeGems(Integer blastLikeGems) {
            this.blastLikeGems = blastLikeGems;
        }

        public Integer getTwoxGems() {
            return twoxGems;
        }

        public void setTwoxGems(Integer twoxGems) {
            this.twoxGems = twoxGems;
        }

        public Integer getThreexGems() {
            return threexGems;
        }

        public void setThreexGems(Integer threexGems) {
            this.threexGems = threexGems;
        }

        public String getUserImage() {
            return userImage;
        }

        public void setUserImage(String userImage) {
            this.userImage = userImage;
        }

    }
}

