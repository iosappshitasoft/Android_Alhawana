
package com.app.model;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ProfileResponse implements Serializable {

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @SerializedName("message")
 @Expose
 private String message;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("blocked_by_me")
    @Expose
    private String blockedByMe;
    @SerializedName("blocked_me")
    @Expose
    private String blockedMe;
    @SerializedName("user_id")
    @Expose
    private String userId;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("user_name")
    @Expose
    private String userName;
    @SerializedName("age")
    @Expose
    private Integer age;
    @SerializedName("dob")
    @Expose
    private String dob;
    @SerializedName("gender")
    @Expose
    private String gender;

    @SerializedName("user_image")
    @Expose
    private String userImage;
    @SerializedName("paypal_id")
    @Expose
    private String paypalId;
    @SerializedName("followers")
    @Expose
    private Integer followers;
    @SerializedName("followings")
    @Expose
    private Integer followings;
    @SerializedName("follow")
    @Expose
    private String follow;
    @SerializedName("available_gems")
    @Expose
    private Float availableGems;
    @SerializedName("videos_count")
    @Expose
    private Integer videosCount;
    @SerializedName("available_gifts")
    @Expose
    private Integer availableGifts;
    @SerializedName("gift_earnings")
    @Expose
    private double giftEarnings;
    @SerializedName("premium_member")
    @Expose
    private String premiumMember;
    @SerializedName("premium_expiry_date")
    @Expose
    private String premiumExpiryDate;
    @SerializedName("show_notification")
    @Expose
    private Boolean showNotification;
    @SerializedName("chat_notification")
    @Expose
    private Boolean chatNotification;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("follow_notification")
    @Expose
    private Boolean followNotification;
    @SerializedName("referal_link")
    @Expose
    private String referalLink;
    @SerializedName("bio")
    @Expose
    private String bio;
    @SerializedName("likes")
    @Expose
    private Integer likes;

    @SerializedName("interest_name")
    @Expose
    private String interestList;

    @SerializedName("insta_link")
    @Expose
    private String instagramLink;

    @SerializedName("youtube_link")
    @Expose
    private String youtubeLink;

    public String getLoginViaNumber() {
        return loginViaNumber;
    }

    public void setLoginViaNumber(String loginViaNumber) {
        this.loginViaNumber = loginViaNumber;
    }

    @SerializedName("login_via_number")
    @Expose
    private String loginViaNumber;

    public String getInstagramLink() {
        return instagramLink;
    }

    public void setInstagramLink(String instagramLink) {
        this.instagramLink = instagramLink;
    }

    public String getYoutubeLink() {
        return youtubeLink;
    }

    public void setYoutubeLink(String youtubeLink) {
        this.youtubeLink = youtubeLink;
    }


    public String getInterestIdList() {
        return interestIdList;
    }

    public void setInterestIdList(String interestIdList) {
        this.interestIdList = interestIdList;
    }

    @SerializedName("interest_ids")
    @Expose
    private String interestIdList;

    @SerializedName("comment_privacy")
    @Expose
    private String commentPrivacy;

    @SerializedName("message_privacy")
    @Expose
    private String messagePrivacy;

    @SerializedName("phone_number")
    @Expose
    private String phone_number;

    public void setGiftEarnings(double giftEarnings) {
        this.giftEarnings = giftEarnings;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    @SerializedName("is_user_can_message")
    @Expose
    private Boolean isUserCanMessage;

    @SerializedName("money_price_value")
    @Expose
    private Float moneyPriceValue;

    @SerializedName("deleted_status")
    @Expose
    private String deleted_status;

    @SerializedName("activate_status")
    @Expose
    private String activate_status;

    @SerializedName("isreport")
    @Expose
    private Boolean isReport;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @SerializedName("email_id")
    @Expose
    private String email;
    @SerializedName("money_currency_symbol")
    @Expose
    private String moneyCurrencySymbol;

    @SerializedName("allow_livestream")
    @Expose
    private String allow_livestream;


    public String getShare_profile_link() {
        return share_profile_link;
    }

    public void setShare_profile_link(String share_profile_link) {
        this.share_profile_link = share_profile_link;
    }

    @SerializedName("share_profile_link")
    @Expose
    private String share_profile_link;
    public String getAllow_livestream() {
        return allow_livestream;
    }

    public void setAllow_livestream(String allow_livestream) {
        this.allow_livestream = allow_livestream;
    }

    public String getMoneyCurrencySymbol() {
        return moneyCurrencySymbol;
    }

    public void setMoneyCurrencySymbol(String moneyCurrencySymbol) {
        this.moneyCurrencySymbol = moneyCurrencySymbol;
    }

    public Float getMoneyPriceValue() {
        return moneyPriceValue;
    }

    public void setMoneyPriceValue(Float moneyPriceValue) {
        this.moneyPriceValue = moneyPriceValue;
    }

    public Boolean getReport() {
        return isReport;
    }

    public void setReport(Boolean report) {
        isReport = report;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getDeleted_status() {
        return deleted_status;
    }

    public void setDeleted_status(String deleted_status) {
        this.deleted_status = deleted_status;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getPaypalId() {
        return paypalId;
    }

    public void setPaypalId(String paypalId) {
        this.paypalId = paypalId;
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

    public String getFollow() {
        return follow;
    }

    public void setFollow(String follow) {
        this.follow = follow;
    }

    public String getActivate_status() {
        return activate_status;
    }

    public void setActivate_status(String activate_status) {
        this.activate_status = activate_status;
    }

/*    public Integer getAvailableGems() {
        return availableGems;
    }

    public void setAvailableGems(Integer availableGems) {
        this.availableGems = availableGems;
    }*/

    public Float getAvailableGems() {
        return availableGems;
    }

    public void setAvailableGems(Float availableGems) {
        this.availableGems = availableGems;
    }

    public Integer getVideosCount() {
        return videosCount;
    }

    public void setVideosCount(Integer videosCount) {
        this.videosCount = videosCount;
    }

    public Integer getAvailableGifts() {
        return availableGifts;
    }

    public void setAvailableGifts(Integer availableGifts) {
        this.availableGifts = availableGifts;
    }

    public double getGiftEarnings() {
        return giftEarnings;
    }

    public void setGiftEarnings(Integer giftEarnings) {
        this.giftEarnings = giftEarnings;
    }

    public String getPremiumMember() {
        return premiumMember;
    }

    public void setPremiumMember(String premiumMember) {
        this.premiumMember = premiumMember;
    }

    public String getPremiumExpiryDate() {
        return premiumExpiryDate;
    }

    public void setPremiumExpiryDate(String premiumExpiryDate) {
        this.premiumExpiryDate = premiumExpiryDate;
    }

    public Boolean getShowNotification() {
        return showNotification;
    }

    public void setShowNotification(Boolean showNotification) {
        this.showNotification = showNotification;
    }

    public Boolean getChatNotification() {
        return chatNotification;
    }

    public void setChatNotification(Boolean chatNotification) {
        this.chatNotification = chatNotification;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getFollowNotification() {
        return followNotification;
    }

    public void setFollowNotification(Boolean followNotification) {
        this.followNotification = followNotification;
    }

    public String getReferalLink() {
        return referalLink;
    }

    public void setReferalLink(String referalLink) {
        this.referalLink = referalLink;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public String getInterestList() {
        return interestList;
    }

    public void setInterestList(String interestList) {
        this.interestList = interestList;
    }

    public String getCommentPrivacy() {
        return commentPrivacy;
    }

    public void setCommentPrivacy(String commentPrivacy) {
        this.commentPrivacy = commentPrivacy;
    }

    public String getMessagePrivacy() {
        return messagePrivacy;
    }

    public void setMessagePrivacy(String messagePrivacy) {
        this.messagePrivacy = messagePrivacy;
    }

    public Boolean getUserCanMessage() {
        return isUserCanMessage;
    }

    public void setUserCanMessage(Boolean userCanMessage) {
        isUserCanMessage = userCanMessage;
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
