package com.app.model;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class HomeResponse {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("loadmore")
    @Expose
    private boolean loadMore;
    @SerializedName("result")
    @Expose
    private List<Result> result = null;
    @SerializedName("position")
    @Expose
    private int position;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean getLoadMore() {
        return loadMore;
    }

    public void setLoadMore(boolean loadMore) {
        this.loadMore = loadMore;
    }

    public List<Result> getResult() {
        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @SuppressLint("ParcelCreator")
    public class Result implements Parcelable {
        public boolean isExpanded() {
            return isExpanded;
        }

        public void setExpanded(boolean expanded) {
            isExpanded = expanded;
        }

        boolean isExpanded = false;

        private boolean assetsVisible = true;

        public boolean isAssetsVisible() { return assetsVisible; }

        public void setAssetsVisible(boolean visible) {
            this.assetsVisible = visible;
        }

        @SerializedName("playtype")
        @Expose
        private String playtype;
        @SerializedName("video_description")
        @Expose
        private String videoDescription;
        @SerializedName("video_id")
        @Expose
        private String videoId;

        @SerializedName("name")
        @Expose
        private String videoname;

        public String getAudioCategory() {
            return audioCategory;
        }

        public void setAudioCategory(String audioCategory) {
            this.audioCategory = audioCategory;
        }

        @SerializedName("audiocategory")
        @Expose
        private String audioCategory;



        @SerializedName("link_type")
        @Expose
        private String link_type;

        @SerializedName("button_color")
        @Expose
        private String button_color;


        @SerializedName("text_color")
        @Expose
        private String text_color;

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
        @SerializedName("mode")
        @Expose
        private String mode;
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
        @SerializedName("ios_video")
        @Expose
        private String iosVideo;
        @SerializedName("android_video")
        @Expose
        private String androidVideo;
        @SerializedName("ios_url")
        @Expose
        private String iosUrl;

        @SerializedName("android_url")
        @Expose
        private String androidUrl;

        @SerializedName("images")
        @Expose
        private List<Image> images;

        @SerializedName("playback_duration")
        @Expose
        private String playbackDuration;
        @SerializedName("invite_link")
        @Expose
        private String inviteLink;
        @SerializedName("repost_count")
        @Expose
        private String repostCount;

        public String getRepostCount() {
            return repostCount;
        }

        public void setRepostCount(String repostCount) {
            this.repostCount = repostCount;
        }

        @SerializedName("hashobj")
        @Expose
        private List<Object> hashobj = null;

        @SerializedName("repost_data")
        @Expose
        private List<Result.RepostedData> repostedData;

        public List<RepostedData> getRepostedData() {
            return repostedData;
        }

        public void setRepostedData(List<RepostedData> repostedData) {
            this.repostedData = repostedData;
        }

        public List<Image> getImages() {
            return images;
        }

        public void setImages(List<Image> images) {
            this.images = images;
        }

        public String getPlaytype() {
            return playtype;
        }

        public void setPlaytype(String playtype) {
            this.playtype = playtype;
        }


        public String getText_color() {
            return text_color;
        }

        public void setText_color(String text_color) {
            this.text_color = text_color;
        }


        public String getVideoDescription() {
            return videoDescription;
        }

        public void setVideoDescription(String videoDescription) {
            this.videoDescription = videoDescription;
        }

        public String getVideoname() {
            return videoname;
        }

        public void setVideoname(String videoname) {
            this.videoname = videoname;
        }

        public String getButton_color() {
            return button_color;
        }

        public void setButton_color(String button_color) {
            this.button_color = button_color;
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

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
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

        public String getLink_type() {
            return link_type;
        }

        public void setLink_type(String link_type) {
            this.link_type = link_type;
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

        public String getIosVideo() {
            return iosVideo;
        }

        public void setIosVideo(String iosVideo) {
            this.iosVideo = iosVideo;
        }

        public String getAndroidVideo() {
            return androidVideo;
        }

        public void setAndroidVideo(String androidVideo) {
            this.androidVideo = androidVideo;
        }

        public String getIosUrl() {
            return iosUrl;
        }

        public void setIosUrl(String iosUrl) {
            this.iosUrl = iosUrl;
        }

        public String getAndroidUrl() {
            return androidUrl;
        }

        public void setAndroidUrl(String androidUrl) {
            this.androidUrl = androidUrl;
        }

        public String getPlaybackDuration() {
            return playbackDuration;
        }

        public void setPlaybackDuration(String playbackDuration) {
            this.playbackDuration = playbackDuration;
        }

        public String getInviteLink() {
            return inviteLink;
        }

        public void setInviteLink(String inviteLink) {
            this.inviteLink = inviteLink;
        }

        public List<Object> getHashobj() {
            return hashobj;
        }

        public void setHashobj(List<Object> hashobj) {
            this.hashobj = hashobj;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

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

            public void setRepostId(String repostId) {
                this.repostId = repostId;
            }

            public void setUserId(String userId) {
                this.userId = userId;
            }

            public void setUserImage(String userImage) {
                this.userImage = userImage;
            }

            public void setDisplayName(String displayName) {
                this.displayName = displayName;
            }

            public void setDescription(String description) {
                this.description = description;
            }

            public String getRepostId() { return repostId; }
            public String getUserId() { return userId; }
            public String getUserImage() { return userImage; }
            public String getDisplayName() { return displayName; }
            public String getDescription() { return description; }
        }
    }

    @SuppressLint("ParcelCreator")
    public class Image implements Parcelable {


        @SerializedName("image_url")
        @Expose
        private String image_url;

        public String getImage_url() {
            return image_url;
        }

        public void setImage_url(String image_url) {
            this.image_url = image_url;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel parcel, int i) {

        }
    }

    public static class Soundtracks implements Serializable {

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
        @SerializedName("isFavorite")
        @Expose
        private Boolean isFavorite;
        @SerializedName("duration")
        @Expose
        private String duration;
        @SerializedName("sound_by")
        @Expose
        private String soundBy;
        @SerializedName("use_audio")
        @Expose
        private boolean useAudio;

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

        public Boolean getIsFavorite() {
            return isFavorite;
        }

        public void setIsFavorite(Boolean isFavorite) {
            this.isFavorite = isFavorite;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public String getSoundBy() {
            return soundBy;
        }

        public void setSoundBy(String soundBy) {
            this.soundBy = soundBy;
        }

        public boolean isUseAudio() {
            return useAudio;
        }

        public void setUseAudio(boolean useAudio) {
            this.useAudio = useAudio;
        }
    }
}
