
package com.app.model;

import com.app.alhawanajd.AudioCategory;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AppDefaultResponse {

    @SerializedName("freegems")
    private Long freeGems;
    @SerializedName("gifts")
    private List<Gift> gifts;

    @SerializedName("livestream_gifts")
    private List<Gift> livestream_gifts;
    @SerializedName("gifts_details")
    private GiftsDetails giftsDetails;
    @SerializedName("prime_details")
    private PrimeDetails primeDetails;
    @SerializedName("reports")
    private List<Report> reports;
    @SerializedName("user_reports")
    private List<Report> userReports;

    public List<Report> getUserReportsNew() {
        return userReportsNew;
    }

    public void setUserReportsNew(List<Report> userReportsNew) {
        this.userReportsNew = userReportsNew;
    }

    @SerializedName("userreports")
    private List<Report> userReportsNew;


    @SerializedName("livestream_reports")
    private List<Report> livestream_reports;


    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;
    /*@SerializedName("locations")
    private List<String> locations;*/
    @SerializedName("membership_packages")
    private List<MembershipPackages> membershipPackages;
    @SerializedName("filter_gems")
    private FilterGems filterGems;
    @SerializedName("filter_options")
    private FilterOptions filterOptions;
    @SerializedName("invite_credits")
    private Long inviteCredits;
    @SerializedName("google_ads_client")
    private String googleAdsClient;
    @SerializedName("show_ads")
    private String showAds;
    @SerializedName("video_ads")
    private String videoAds;
    @SerializedName("contact_email")
    private String contactEmail;
    @SerializedName("welcome_message")
    private String welcomeMessage;
    @SerializedName("post_popup_text")
    private String postAuthorizeMessage;
    @SerializedName("show_money_conversion")
    private String showMoneyConversion;
    @SerializedName("schedule_video_ads")
    private Long videoAdsDuration;
    @SerializedName("video_ads_client")
    private String videoAdsClient;
    @SerializedName("max_sound_duration")
    private String maxSoundDuration;
    @SerializedName("max_video_duration")
    private int maxVideoDuration;
    @SerializedName("interest_categories")
    private List<AudioCategory> audioCategory;
 /*  @SerializedName("interest_categories")
    private List<String> audioCategory;*/
    @SerializedName("gallery_upload")
    private boolean galleryUpload;
    @SerializedName("download_video")
    private boolean downloadVideo;
    @SerializedName("upload_sound")
    private boolean uploadSound;

    @SerializedName("english_ad_key")
    private String english_ad_key;

    @SerializedName("arabic_ad_key")
    private String arabic_ad_key;

    @SerializedName("ad_status")
    private String ad_status;

    public String isEnglish_ad_key() {
        return english_ad_key;
    }

    public void setEnglish_ad_key(String english_ad_key) {
        this.english_ad_key = english_ad_key;
    }

    public String isad_status() {
        return ad_status;
    }

    public void ad_status(String ad_status) {
        this.ad_status = ad_status;
    }

    public String isArabic_ad_key() {
        return arabic_ad_key;
    }

    public void setArabic_ad_key(String arabic_ad_key) {
        this.arabic_ad_key = arabic_ad_key;
    }

    @SerializedName("video_calls")
    private Long videoCalls;

    @SerializedName("stream_connection_info")
    private StreamConnectionInfo streamConnectionInfo;

    public Long getFreeGems() {
        return freeGems;
    }

    public void setFreeGems(Long freeGems) {
        this.freeGems = freeGems;
    }

    public List<Gift> getLivestream_gifts() {
        return livestream_gifts;
    }

    public void setLivestream_gifts(List<Gift> livestream_gifts) {
        this.livestream_gifts = livestream_gifts;
    }

    public List<Gift> getGifts() {
        return gifts;
    }

    public void setGifts(List<Gift> gifts) {
        this.gifts = gifts;
    }

    public GiftsDetails getGiftsDetails() {
        return giftsDetails;
    }

    public void setGiftsDetails(GiftsDetails giftsDetails) {
        this.giftsDetails = giftsDetails;
    }

    public PrimeDetails getPrimeDetails() {
        return primeDetails;
    }

    public void setPrimeDetails(PrimeDetails primeDetails) {
        this.primeDetails = primeDetails;
    }

    public List<Report> getReports() {
        return reports;
    }

    public void setReports(List<Report> reports) {
        this.reports = reports;
    }

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

    /*public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }*/

    public List<MembershipPackages> getMembershipPackages() {
        return membershipPackages;
    }

    public void setMembershipPackages(List<MembershipPackages> membershipPackages) {
        this.membershipPackages = membershipPackages;
    }

    public FilterGems getFilterGems() {
        return filterGems;
    }

    public void setFilterGems(FilterGems filterGems) {
        this.filterGems = filterGems;
    }

    public FilterOptions getFilterOptions() {
        return filterOptions;
    }

    public void setFilterOptions(FilterOptions filterOptions) {
        this.filterOptions = filterOptions;
    }

    public Long getInviteCredits() {
        return inviteCredits;
    }

    public void setInviteCredits(Long inviteCredits) {
        this.inviteCredits = inviteCredits;
    }

    public String getGoogleAdsClient() {
        return googleAdsClient;
    }

    public void setGoogleAdsClient(String googleAdsClient) {
        this.googleAdsClient = googleAdsClient;
    }

    public String getShowAds() {
        return showAds;
    }

    public void setShowAds(String showAds) {
        this.showAds = showAds;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    public String getPostAuthorizeMessage() {
        return postAuthorizeMessage;
    }

    public void setPostAuthorizeMessage(String postAuthorizeMessage) {
        this.postAuthorizeMessage = postAuthorizeMessage;
    }

    public String getVideoAds() {
        return videoAds;
    }

    public void setVideoAds(String videoAds) {
        this.videoAds = videoAds;
    }

    public String getShowMoneyConversion() {
        return showMoneyConversion;
    }

    public void setShowMoneyConversion(String showMoneyConversion) {
        this.showMoneyConversion = showMoneyConversion;
    }

    public String getVideoAdsClient() {
        return videoAdsClient;
    }

    public void setVideoAdsClient(String videoAdsClient) {
        this.videoAdsClient = videoAdsClient;
    }

    public Long getVideoAdsDuration() {
        return videoAdsDuration;
    }

    public void setVideoAdsDuration(Long videoAdsDuration) {
        this.videoAdsDuration = videoAdsDuration;
    }

    public Long getVideoCalls() {
        return videoCalls;
    }

    public void setVideoCalls(Long videoCalls) {
        this.videoCalls = videoCalls;
    }

    public StreamConnectionInfo getStreamConnectionInfo() {
        return streamConnectionInfo;
    }

    public void setStreamConnectionInfo(StreamConnectionInfo streamConnectionInfo) {
        this.streamConnectionInfo = streamConnectionInfo;
    }

    public String getMaxSoundDuration() {
        return maxSoundDuration;
    }

    public void setMaxSoundDuration(String maxSoundDuration) {
        this.maxSoundDuration = maxSoundDuration;
    }

    public int getMaxVideoDuration() {
        return maxVideoDuration;
    }

    public void setMaxVideoDuration(int maxVideoDuration) {
        this.maxVideoDuration = maxVideoDuration;
    }

   /* public List<String> getAudioCategory() {
        return audioCategory;
    }

    public void setAudioCategory(List<String> audioCategory) {
        this.audioCategory = audioCategory;
    }*/

    public List<AudioCategory> getAudioCategory() {
        return audioCategory;
    }

    public void setAudioCategory(List<AudioCategory> audioCategory) {
        this.audioCategory = audioCategory;
    }
    public List<Report> getUserReports() {
        return userReports;
    }

    public void setUserReports(List<Report> userReports) {
        this.userReports = userReports;
    }
    public List<Report> getLivestream_reports() {
        return livestream_reports;
    }

    public void setLivestream_reports(List<Report> livestream_reports) {
        this.livestream_reports = livestream_reports;
    }

    public boolean isGalleryUpload() {
        return galleryUpload;
    }

    public void setGalleryUpload(boolean galleryUpload) {
        this.galleryUpload = galleryUpload;
    }

    public boolean isDownloadVideo() {
        return downloadVideo;
    }

    public void setDownloadVideo(boolean downloadVideo) {
        this.downloadVideo = downloadVideo;
    }

    public boolean isUploadSound() {
        return uploadSound;
    }

    public void setUploadSound(boolean uploadSound) {
        this.uploadSound = uploadSound;
    }

    public class StreamConnectionInfo {
        @SerializedName("base_url")
        private String streamBaseUrl;
        @SerializedName("api_url")
        private String streamApiUrl;
        @SerializedName("websocket_url")
        private String webSocketUrl;
        @SerializedName("vod_url")
        private String streamVodUrl;

        public String getStreamBaseUrl() {
            return streamBaseUrl;
        }

        public void setStreamBaseUrl(String streamBaseUrl) {
            this.streamBaseUrl = streamBaseUrl;
        }

        public String getStreamApiUrl() {
            return streamApiUrl;
        }

        public void setStreamApiUrl(String streamApiUrl) {
            this.streamApiUrl = streamApiUrl;
        }

        public String getWebSocketUrl() {
            return webSocketUrl;
        }

        public void setWebSocketUrl(String webSocketUrl) {
            this.webSocketUrl = webSocketUrl;
        }

        public String getStreamVodUrl() {
            return streamVodUrl;
        }

        public void setStreamVodUrl(String streamVodUrl) {
            this.streamVodUrl = streamVodUrl;
        }

    }
}
