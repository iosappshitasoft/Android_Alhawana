
package com.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FollowersResponse {

    @SerializedName("status")
    private String status;
    @SerializedName("users_list")
    private List<FollowersList> followersList;
    @SerializedName("message")
    private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<FollowersList> getFollowersList() {
        return followersList;
    }

    public void setFollowersList(List<FollowersList> followersList) {
        this.followersList = followersList;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class FollowersList {

        @SerializedName("user_id")
        @Expose
        private String userId;
        @SerializedName("user_image")
        @Expose
        private String userImage;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("gender")
        @Expose
        private String gender;
        @SerializedName("premium_member")
        @Expose
        private String premiumMember;
        @SerializedName("isprofilemoderator")
        @Expose
        public String isprofilemoderator;

        public String getIsprofilemoderator() {
            return isprofilemoderator;
        }

        public void setIsprofilemoderator(String isprofilemoderator) {
            this.isprofilemoderator = isprofilemoderator;
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

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getPremiumMember() {
            return premiumMember;
        }

        public void setPremiumMember(String premiumMember) {
            this.premiumMember = premiumMember;
        }
    }
}
