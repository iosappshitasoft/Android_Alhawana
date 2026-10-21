
package com.app.addon.livestream.external.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ProfileResponse implements Serializable {

    private String age;
    @SerializedName("available_gems")
    private String availableGems;
    @SerializedName("available_gifts")
    private String availableGifts;
    @SerializedName("dob")
    private String dob;
    @SerializedName("followers")
    private String followers;
    @SerializedName("followings")
    private String followings;
    @SerializedName("gender")
    private String gender;
    @SerializedName("location")
    private String location;
    @SerializedName("login_id")
    private String loginId;
    @SerializedName("name")
    private String name;
    @SerializedName("user_name")
    private String userName;
    @SerializedName("paypal_id")
    private String paypalId;
    @SerializedName("premium_expiry_date")
    private String premiumExpiryDate;
    @SerializedName("premium_member")
    private String premiumMember;
    @SerializedName("referal_link")
    private String referalLink;
    @SerializedName("status")
    private String status;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("user_image")
    private String userImage;
    @SerializedName("message")
    private String message;
    @SerializedName("follow")
    private String follow;
    @SerializedName("privacy_age")
    private String privacyAge;
    @SerializedName("privacy_contactme")
    private String privacyContactMe;
    @SerializedName("show_notification")
    private String showNotification;
    @SerializedName("chat_notification")
    private String chatNotification;
    @SerializedName("follow_notification")
    private String followNotification;
    @SerializedName("gift_earnings")
    private String giftEarnings;
    @SerializedName("blocked_by_me")
    private String blockedByMe;
    @SerializedName("blocked_me")
    private String blockedMe;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("videos_count")
    private String videosCount;
    @SerializedName("watched_count")
    private Long watchedCount;
    @SerializedName("unread_broadcasts")
    private boolean unReadBroadcasts;
    @SerializedName("bio")
    @Expose
    private String bio;
    @SerializedName("likes")
    @Expose
    private Integer likes;
    @SerializedName("allow_livestream")
    @Expose
    private String allow_livestream;

    public String getAllow_livestream() {
        return allow_livestream;
    }

    public void setAllow_livestream(String allow_livestream) {
        this.allow_livestream = allow_livestream;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }
    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getAvailableGems() {
        return availableGems;
    }

    public void setAvailableGems(String availableGems) {
        this.availableGems = availableGems;
    }

    public String getAvailableGifts() {
        return availableGifts;
    }

    public void setAvailableGifts(String availableGifts) {
        this.availableGifts = availableGifts;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getFollowers() {
        return followers;
    }

    public void setFollowers(String followers) {
        this.followers = followers;
    }

    public String getFollowings() {
        return followings;
    }

    public void setFollowings(String followings) {
        this.followings = followings;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPaypalId() {
        return paypalId;
    }

    public void setPaypalId(String paypalId) {
        this.paypalId = paypalId;
    }

    public String getPremiumExpiryDate() {
        return premiumExpiryDate;
    }

    public void setPremiumExpiryDate(String premiumExpiryDate) {
        this.premiumExpiryDate = premiumExpiryDate;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPremiumMember() {
        return premiumMember;
    }

    public void setPremiumMember(String premiumMember) {
        this.premiumMember = premiumMember;
    }

    public String getReferalLink() {
        return referalLink;
    }

    public void setReferalLink(String referalLink) {
        this.referalLink = referalLink;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFollow() {
        return follow;
    }

    public void setFollow(String follow) {
        this.follow = follow;
    }

    public String getPrivacyAge() {
        return privacyAge;
    }

    public void setPrivacyAge(String privacyAge) {
        this.privacyAge = privacyAge;
    }

    public String getPrivacyContactMe() {
        return privacyContactMe;
    }

    public void setPrivacyContactMe(String privacyContactMe) {
        this.privacyContactMe = privacyContactMe;
    }

    public String getShowNotification() {
        return showNotification;
    }

    public void setShowNotification(String showNotification) {
        this.showNotification = showNotification;
    }

    public String getChatNotification() {
        return chatNotification;
    }

    public void setChatNotification(String chatNotification) {
        this.chatNotification = chatNotification;
    }

    public String getFollowNotification() {
        return followNotification;
    }

    public void setFollowNotification(String followNotification) {
        this.followNotification = followNotification;
    }

    public String getGiftEarnings() {
        return giftEarnings;
    }

    public void setGiftEarnings(String giftEarnings) {
        this.giftEarnings = giftEarnings;
    }

    public String getBlockedByMe() {
        return blockedByMe;
    }

    public void setBlockedByMe(String blockedByMe) {
        this.blockedByMe = blockedByMe;
    }


    public String getBlockedMe() {
        return blockedMe;
    }

    public void setBlockedMe(String blockedMe) {
        this.blockedMe = blockedMe;
    }

    public boolean isUnReadBroadcasts() {
        return unReadBroadcasts;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }


    public String getVideosCount() {
        return videosCount;
    }

    public void setVideosCount(String videosCount) {
        this.videosCount = videosCount;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getWatchedCount() {
        return watchedCount;
    }

    public void setWatchedCount(Long watchedCount) {
        this.watchedCount = watchedCount;
    }

    public boolean hasUnReadBroadcasts() {
        return unReadBroadcasts;
    }

    public void setUnReadBroadcasts(boolean unReadBroadcasts) {
        this.unReadBroadcasts = unReadBroadcasts;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        boolean result = false;
        if (object != null && object.getClass() == getClass()) {
            ProfileResponse method = (ProfileResponse) object;
            if (this.getUserId() != null &&
                    this.userId.equals(method.getUserId())) {
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
