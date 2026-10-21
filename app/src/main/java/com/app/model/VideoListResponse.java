package com.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VideoListResponse {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("result")
    @Expose
    private List<Result> result = null;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Result> getResult() {
        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }

    public static class Result {

        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("video_description")
        @Expose
        private String videoDescription;
        @SerializedName("mode")
        @Expose
        private String mode;
        @SerializedName("watch_count")
        @Expose
        private Integer watchCount;
        @SerializedName("streamed_on")
        @Expose
        private String streamedOn;
        @SerializedName("video_id")
        @Expose
        private String videoId;
        @SerializedName("publisher_id")
        @Expose
        private String publisherId;
        @SerializedName("publisher_image")
        @Expose
        private String publisherImage;
        @SerializedName("stream_thumbnail")
        @Expose
        private String streamThumbnail;
        @SerializedName("gif_url")
        @Expose
        private String gifUrl;


        @SerializedName("isFavorite")
        @Expose
        private String isFavorite;


        @SerializedName("posted_by")
        @Expose
        private String postedBy;
        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("likes")
        @Expose
        private String likes;
        @SerializedName("playback_ready")
        @Expose
        private String playbackReady;
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
        private boolean isLiked;
        @SerializedName("video_status")
        @Expose
        private String videoStatus;
        @SerializedName("position")
        @Expose
        private int position;

        @SerializedName("stream_name")
        @Expose
        private String streamName;

        public String getStreamId() {
            return streamId;
        }

        public void setStreamId(String streamId) {
            this.streamId = streamId;
        }

        @SerializedName("stream_id")
        @Expose
        private String streamId;

        @SerializedName("user_id")
        @Expose
        private String userId;
        @SerializedName("user_image")
        @Expose
        private String userImage;

        @SerializedName("user_name")
        @Expose
        private String userName;

        public List<RepostedData> getRepostedData() {
            return repostedData;
        }

        public void setRepostedData(List<RepostedData> repostedData) {
            this.repostedData = repostedData;
        }

        @SerializedName("repost_data")
        @Expose
        private List<RepostedData> repostedData;


        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVideoDescription() {
            return videoDescription;
        }

        public void setVideoDescription(String videoDescription) {
            this.videoDescription = videoDescription;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public Integer getWatchCount() {
            return watchCount;
        }

        public void setWatchCount(Integer watchCount) {
            this.watchCount = watchCount;
        }

        public String getStreamedOn() {
            return streamedOn;
        }

        public void setStreamedOn(String streamedOn) {
            this.streamedOn = streamedOn;
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

        public String getGifUrl() {
            return gifUrl;
        }

        public void setGifUrl(String gifUrl) {
            this.gifUrl = gifUrl;
        }

        public String getPostedBy() {
            return postedBy;
        }

        public void setPostedBy(String postedBy) {
            this.postedBy = postedBy;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getLikes() {
            return likes;
        }

        public void setLikes(String likes) {
            this.likes = likes;
        }

        public String getPlaybackReady() {
            return playbackReady;
        }

        public void setPlaybackReady(String playbackReady) {
            this.playbackReady = playbackReady;
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

        public String getIsFavorite() {
            return isFavorite;
        }

        public void setIsFavorite(String isFavorite) {
            this.isFavorite = isFavorite;
        }


        public Boolean getIsLiked() {
            return isLiked;
        }

        public void setIsLiked(Boolean isLiked) {
            this.isLiked = isLiked;
        }

        public String getVideoStatus() {
            return videoStatus;
        }

        public void setVideoStatus(String videoStatus) {
            this.videoStatus = videoStatus;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public String getStreamName() {
            return streamName;
        }

        public void setStreamName(String streamName) {
            this.streamName = streamName;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserImage() {
            return userImage;
        }

        public void setUserImage(String userImage) {
            this.userImage = userImage;
        }

        public static class RepostedData {
            @SerializedName("repost_id")
            @Expose
            private String repostId;

            @SerializedName("user_id")
            @Expose
            private String userId;

            @SerializedName("user_image")
            @Expose
            private String userImage;

            @SerializedName("display_name")
            @Expose
            private String displayName;

            @SerializedName("description")
            @Expose
            private String description;

            public String getRepostId() { return repostId; }
            public String getUserId() { return userId; }
            public String getUserImage() { return userImage; }
            public String getDisplayName() { return displayName; }
            public String getDescription() { return description; }
        }
    }
}
