package com.app.model;

import com.google.gson.annotations.SerializedName;
import com.app.utils.Constants;

public class UserData {

    @SerializedName(Constants.TAG_STATUS)
    public String status;


    @SerializedName(Constants.TAG_MESSAGE)
    public String message;

    @SerializedName(Constants.TAG_USER_ID)
    public String userId;

    @SerializedName(Constants.TAG_USER_NAME)
    public String userName;

    @SerializedName("full_name")
    public String fullName;

    @SerializedName(Constants.TAG_USER_IMAGE)
    public String imageUrl;

    @SerializedName(Constants.TAG_TOKEN)
    public String token;

    @SerializedName(Constants.TAG_FIRST_LOGIN)
    public String firstTime;


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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getFirstTime() {
        return firstTime;
    }

    public void setFirstTime(String firstTime) {
        this.firstTime = firstTime;
    }
}
