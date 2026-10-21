
package com.app.model;

import com.google.gson.annotations.SerializedName;

public class SignInResponse {
    @SerializedName("otp")
    private String otp;
    @SerializedName("device_id")
    private String device_id;
    @SerializedName("verify_id")
    private String verify_id;
    @SerializedName("verification_sid")
    private String verification_sid;
    @SerializedName("phone")
    private String phone;
    @SerializedName("cc")
    private String cc;
    @SerializedName("age")
    private String age;
    @SerializedName("dob")
    private String dob;
    @SerializedName("gender")
    private String gender;
    @SerializedName("login_id")
    private String loginId;
    @SerializedName("name")
    private String name;
    @SerializedName("user_name")
    private String userName;
    @SerializedName("status")
    private String status;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("accountexists")
    private boolean accountExists;
    @SerializedName("accountdeleted")
    private boolean accountdeleted;
    @SerializedName("auth_token")
    private String authToken;
    @SerializedName("user_image")
    private String userImage;
    @SerializedName("message")
    private String message;
    @SerializedName("location")
    private String location;
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
    @SerializedName("interest_notification")
    private boolean interestNotification;

    @SerializedName("premium_member")
    private String premiumMember;

    public String getVerify_id() {
        return verify_id;
    }

    public void setVerify_id(String verify_id) {
        this.verify_id = verify_id;
    }


    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getVerification_sid() {
        return verification_sid;
    }

    public void setVerification_sid(String verification_sid) {
        this.verification_sid = verification_sid;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getRating_user_count() {
        return rating_user_count;
    }

    public void setRating_user_count(String rating_user_count) {
        this.rating_user_count = rating_user_count;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    @SerializedName("email_id")
    private String email;

    @SerializedName("photo")
    private String photo;

    @SerializedName("full_name")
    private String full_name;


    @SerializedName("rating_user_count")
    private String rating_user_count;


    @SerializedName("rating")
    private String rating;
    public boolean isInterestNotification() {
        return interestNotification;
    }

    public String getPremiumMember() {
        return premiumMember;
    }

    public void setPremiumMember(String premiumMember) {
        this.premiumMember = premiumMember;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
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

    public boolean isAccountdeleted() {
        return accountdeleted;
    }

    public void setAccountdeleted(boolean accountdeleted) {
        this.accountdeleted = accountdeleted;
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

    public boolean isAccountExists() {
        return accountExists;
    }

    public void setAccountExists(boolean accountExists) {
        this.accountExists = accountExists;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public boolean getInterestNotification() {
        return interestNotification;
    }

    public void setInterestNotification(boolean interestNotification) {
        this.interestNotification = interestNotification;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
