package com.app.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.hendraanggrian.appcompat.socialview.autocomplete.Mentionable;
//import com.hendraanggrian.appcompat.widget.Mentionable;


import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by hitasoft on 25/2/18.
 */

public class UserList {
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

    public static class Result implements Mentionable {

        @SerializedName("user_id")
        @Expose
        private String userId;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("user_name")
        @Expose
        private String userName;
        @SerializedName("user_image")
        @Expose
        private String userImage;
        @SerializedName("followers")
        @Expose
        private Integer followers;
        @SerializedName("followings")
        @Expose
        private Integer followings;
        @SerializedName("follow")
        @Expose
        private boolean follow;
        @SerializedName("premium_member")
        @Expose
        private boolean primeMember;
        @SerializedName("blocked_by_me")
        @Expose
        private boolean blockedByMe;
        @SerializedName("blocked_me")
        @Expose
        private boolean blockedMe;

        @SerializedName("recent_post")
        @Expose
        private List<Post> recentPost;

        public List<Post> getRecentPost() {
            return recentPost;
        }

        public void setRecentPost(List<Post> recentPost) {
            this.recentPost = recentPost;
        }

        public boolean isBlockedByMe() {
            return blockedByMe;
        }

        public void setBlockedByMe(boolean blockedByMe) {
            this.blockedByMe = blockedByMe;
        }

        public boolean isBlockedMe() {
            return blockedMe;
        }

        public void setBlockedMe(boolean blockedMe) {
            this.blockedMe = blockedMe;
        }


        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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

        public Integer getFollowers() {
            return followers;
        }

        public void setFollowers(Integer followers) {
            this.followers = followers;
        }

        public Integer getFollowings() {
            return followings;
        }

        public void setFollowings(Integer followings) {
            this.followings = followings;
        }

        public boolean isFollow() {
            return follow;
        }

        public void setFollow(boolean follow) {
            this.follow = follow;
        }

        public boolean isPrimeMember() {
            return primeMember;
        }

        public void setPrimeMember(boolean primeMember) {
            this.primeMember = primeMember;
        }

        @NonNull
        @NotNull
        public CharSequence getUsername() {
            return userName;
        }

        @Nullable
        @org.jetbrains.annotations.Nullable
        public CharSequence getDisplayname() {
            return name;
        }

        @NonNull
        @Override
        public String toString() {
            return userName;
        }

        @Nullable
        @org.jetbrains.annotations.Nullable
        public Object getAvatar() {
            return userImage;
        }
    }

    public static class Post {

        @SerializedName("_id")
        private String id;

        @SerializedName("description")
        private String description;

        @SerializedName("publisher_id")
        private String publisher_id;

        @SerializedName("thumbnail")
        private String thumbnail;

        @SerializedName("playback_url")
        private String playbackUrl;

        @SerializedName("viewers")
        private int viewers;

        @SerializedName("likes")
        private int likes;

        public void setId(String id) {
            this.id = id;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getPublisher_id() {
            return publisher_id;
        }

        public void setPublisher_id(String publisher_id) {
            this.publisher_id = publisher_id;
        }

        public void setThumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
        }

        public void setPlaybackUrl(String playbackUrl) {
            this.playbackUrl = playbackUrl;
        }

        public void setViewers(int viewers) {
            this.viewers = viewers;
        }

        public void setLikes(int likes) {
            this.likes = likes;
        }

        public String getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public String getThumbnail() {
            return thumbnail;
        }

        public String getPlaybackUrl() {
            return playbackUrl;
        }

        public int getViewers() {
            return viewers;
        }

        public int getLikes() {
            return likes;
        }
    }
}
