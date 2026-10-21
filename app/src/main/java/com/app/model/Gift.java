
package com.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
public class Gift {
    /*
    @SerializedName("animation_duration")
    private double animation_duration;

    public double getAnimation_duration() {
        return animation_duration;
    }

    public void setAnimation_duration(double animation_duration) {
        this.animation_duration = animation_duration;
    }

*/
    @SerializedName("animation_duration")
    private Integer animationDuration;
    @SerializedName("gft_icon")
    private String giftIcon;
    @SerializedName("_id")
    private String giftId;
    @SerializedName("gft_gems")
    private int giftGems;
    @SerializedName("gft_gems_prime")
    private int giftGemsPrime;
    @SerializedName("gft_title")
    private String giftTitle;

    @SerializedName("gft_mp4_url")
    private String gft_mp4_url;

    @SerializedName("gft_direction")
    private String gft_direction;

    /*Non API keys used for Socket*/
    private String userId;
    private String partnerId;

    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("icon_type")
    @Expose
    private String iconType;
    @SerializedName("gft_svga_icon")
    @Expose
    private String gftSvgaIcon;
    @SerializedName("gft_html_icon")
    @Expose
    private String gftHtmlIcon;


    public String getGft_mp4_url() {
        return gft_mp4_url;
    }

    public void setGft_mp4_url(String gft_mp4_url) {
        this.gft_mp4_url = gft_mp4_url;
    }

    public String getGft_direction() {
        return gft_direction;
    }

    public void setGft_direction(String gft_direction) {
        this.gft_direction = gft_direction;
    }


    public String getGftHtmlIcon() {
        return gftHtmlIcon;
    }

    public void setGftHtmlIcon(String gftHtmlIcon) {
        this.gftHtmlIcon = gftHtmlIcon;
    }

    public String getGft_html_wav() {
        return gft_html_wav;
    }

    public void setGft_html_wav(String gft_html_wav) {
        this.gft_html_wav = gft_html_wav;
    }

    @SerializedName("gft_html_wav")
    @Expose
    private String gft_html_wav;


    public String getGiftIcon() {
        return giftIcon;
    }

    public void setGiftIcon(String giftIcon) {
        this.giftIcon = giftIcon;
    }

    public String getGiftId() {
        return giftId;
    }

    public void setGiftId(String giftId) {
        this.giftId = giftId;
    }

    public int getGiftGems() {
        return giftGems;
    }

    public void setGiftGems(int giftGems) {
        this.giftGems = giftGems;
    }

    public int getGiftGemsPrime() {
        return giftGemsPrime;
    }

    public void setGiftGemsPrime(int giftGemsPrime) {
        this.giftGemsPrime = giftGemsPrime;
    }

    public String getGiftTitle() {
        return giftTitle;
    }

    public void setGiftTitle(String giftTitle) {
        this.giftTitle = giftTitle;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getIconType() {
        return iconType;
    }

    public void setIconType(String iconType) {
        this.iconType = iconType;
    }

    public String getGftSvgaIcon() {
        return gftSvgaIcon;
    }

    public void setGftSvgaIcon(String gftSvgaIcon) {
        this.gftSvgaIcon = gftSvgaIcon;
    }

    public Integer getAnimationDuration() {
        return animationDuration;
    }

    public void setAnimationDuration(Integer animationDuration) {
        this.animationDuration = animationDuration;
    }
}
/*
public class Gift {

    @SerializedName("gft_icon")
    private String giftIcon;
    @SerializedName("_id")
    private String giftId;
    @SerializedName("gft_gems")
    private int giftGems;
    @SerializedName("gft_gems_prime")
    private int giftGemsPrime;
    @SerializedName("gft_title")
    private String giftTitle;

    */
/*Non API keys used for Socket*//*

    private String userId;
    private String partnerId;

    public String getGiftIcon() {
        return giftIcon;
    }

    public void setGiftIcon(String giftIcon) {
        this.giftIcon = giftIcon;
    }

    public String getGiftId() {
        return giftId;
    }

    public void setGiftId(String giftId) {
        this.giftId = giftId;
    }

    public int getGiftGems() {
        return giftGems;
    }

    public void setGiftGems(int giftGems) {
        this.giftGems = giftGems;
    }

    public int getGiftGemsPrime() {
        return giftGemsPrime;
    }

    public void setGiftGemsPrime(int giftGemsPrime) {
        this.giftGemsPrime = giftGemsPrime;
    }

    public String getGiftTitle() {
        return giftTitle;
    }

    public void setGiftTitle(String giftTitle) {
        this.giftTitle = giftTitle;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }
}
*/
