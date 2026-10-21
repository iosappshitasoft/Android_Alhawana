package com.app.model;

import com.google.gson.annotations.SerializedName;

public class FollowRequest {
    @SerializedName("user_id")
    private String userId;
    @SerializedName("follower_id")
    private String followerId;
    @SerializedName("type")
    private String type;
    @SerializedName("token")
    private String token;

    @SerializedName("user_name")
    private String user_name;


    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFollowerId() {
        return followerId;
    }

    public void setFollowerId(String followerId) {
        this.followerId = followerId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
