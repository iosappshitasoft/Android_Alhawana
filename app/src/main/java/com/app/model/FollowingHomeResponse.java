package com.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FollowingHomeResponse {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("nextloadmore")
    @Expose
    private String nextloadmore;
    @SerializedName("result")
    @Expose
    private List<Result> result = null;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getNextloadmore() {
        return nextloadmore;
    }

    public void setNextloadmore(String nextloadmore) {
        this.nextloadmore = nextloadmore;
    }

    public List<Result> getResult() {
        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }

    public class Result {

        @SerializedName("video_description")
        @Expose
        private String videoDescription;
        @SerializedName("video_id")
        @Expose
        private String videoId;
        @SerializedName("publisher_id")
        @Expose
        private String publisherId;
        @SerializedName("soundtracks")
        @Expose
        private Soundtracks soundtracks;
        @SerializedName("publisher_image")
        @Expose
        private String publisherImage;
        @SerializedName("stream_thumbnail")
        @Expose
        private String streamThumbnail;
        @SerializedName("posted_by")
        @Expose
        private String postedBy;
        @SerializedName("likes")
        @Expose
        private String likes;
        @SerializedName("playback_url")
        @Expose
        private String playbackUrl;
        @SerializedName("video_gift_counts")
        @Expose
        private String videoGiftCounts;
        @SerializedName("video_comment_counts")
        @Expose
        private String videoCommentCounts;
        @SerializedName("isLiked")
        @Expose
        private Boolean isLiked;
        @SerializedName("followed_user")
        @Expose
        private Boolean followedUser;
        @SerializedName("video_isFavorite")
        @Expose
        private Boolean videoIsFavorite;
        @SerializedName("is_video_reported")
        @Expose
        private Boolean isVideoReported;
        @SerializedName("share_link")
        @Expose
        private String shareLink;
        @SerializedName("video_type")
        @Expose
        private String videoType;

        public String getVideoDescription() {
            return videoDescription;
        }

        public void setVideoDescription(String videoDescription) {
            this.videoDescription = videoDescription;
        }

        public String getVideoId() {
            return videoId;
        }

        public void setVideoId(String videoId) {
            this.videoId = videoId;
        }

        public String getPublisherId() {
            return publisherId;
        }

        public void setPublisherId(String publisherId) {
            this.publisherId = publisherId;
        }

        public Soundtracks getSoundtracks() {
            return soundtracks;
        }

        public void setSoundtracks(Soundtracks soundtracks) {
            this.soundtracks = soundtracks;
        }

        public String getPublisherImage() {
            return publisherImage;
        }

        public void setPublisherImage(String publisherImage) {
            this.publisherImage = publisherImage;
        }

        public String getStreamThumbnail() {
            return streamThumbnail;
        }

        public void setStreamThumbnail(String streamThumbnail) {
            this.streamThumbnail = streamThumbnail;
        }

        public String getPostedBy() {
            return postedBy;
        }

        public void setPostedBy(String postedBy) {
            this.postedBy = postedBy;
        }

        public String getLikes() {
            return likes;
        }

        public void setLikes(String likes) {
            this.likes = likes;
        }

        public String getPlaybackUrl() {
            return playbackUrl;
        }

        public void setPlaybackUrl(String playbackUrl) {
            this.playbackUrl = playbackUrl;
        }

        public String getVideoGiftCounts() {
            return videoGiftCounts;
        }

        public void setVideoGiftCounts(String videoGiftCounts) {
            this.videoGiftCounts = videoGiftCounts;
        }

        public String getVideoCommentCounts() {
            return videoCommentCounts;
        }

        public void setVideoCommentCounts(String videoCommentCounts) {
            this.videoCommentCounts = videoCommentCounts;
        }

        public Boolean getIsLiked() {
            return isLiked;
        }

        public void setIsLiked(Boolean isLiked) {
            this.isLiked = isLiked;
        }

        public Boolean getFollowedUser() {
            return followedUser;
        }

        public void setFollowedUser(Boolean followedUser) {
            this.followedUser = followedUser;
        }

        public Boolean getVideoIsFavorite() {
            return videoIsFavorite;
        }

        public void setVideoIsFavorite(Boolean videoIsFavorite) {
            this.videoIsFavorite = videoIsFavorite;
        }

        public Boolean getIsVideoReported() {
            return isVideoReported;
        }

        public void setIsVideoReported(Boolean isVideoReported) {
            this.isVideoReported = isVideoReported;
        }

        public String getShareLink() {
            return shareLink;
        }

        public void setShareLink(String shareLink) {
            this.shareLink = shareLink;
        }

        public String getVideoType() {
            return videoType;
        }

        public void setVideoType(String videoType) {
            this.videoType = videoType;
        }

    }

    public static class Soundtracks {

        @SerializedName("sound_id")
        @Expose
        private String soundId;
        @SerializedName("title")
        @Expose
        private String title;
        @SerializedName("sound_url")
        @Expose
        private String soundUrl;
        @SerializedName("cover_image")
        @Expose
        private String coverImage;
        @SerializedName("duration")
        @Expose
        private String duration;
        @SerializedName("isFavorite")
        @Expose
        private Boolean isFavorite;

        public Boolean getFavorite() {
            return isFavorite;
        }

        public void setFavorite(Boolean favorite) {
            isFavorite = favorite;
        }

        public String getSoundId() {
            return soundId;
        }

        public void setSoundId(String soundId) {
            this.soundId = soundId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSoundUrl() {
            return soundUrl;
        }

        public void setSoundUrl(String soundUrl) {
            this.soundUrl = soundUrl;
        }

        public String getCoverImage() {
            return coverImage;
        }

        public void setCoverImage(String coverImage) {
            this.coverImage = coverImage;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

    }


}
