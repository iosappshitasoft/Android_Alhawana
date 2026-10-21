package com.app.model;

import com.google.gson.annotations.SerializedName;

public class SignInRequest {
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
    @SerializedName("type")
    private String type;
    @SerializedName("location")
    private String location;
    @SerializedName("email")
    private String mailId;

    @SerializedName("bio")
    private String bio;

    @SerializedName("username")
    private String username;

    @SerializedName("image")
    private String image;

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
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @SerializedName("mail_id")
    private String email;

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMailId() {
        return mailId;
    }

    public void setMailId(String mailId) {
        this.mailId = mailId;
    }
}
