package com.app.model;

import com.app.alhawanajd.R;

public class GetSet {
    private static boolean isLogged = false;
    private static String userId = "";
    private static String name = null;
    private static String userName = null;
    private static String authToken = null;
    private static Float gems = null;
    private static String Password = null;
    private static String userImage = null;
    private static String dob = null;

    public static String getShare_profile_link() {
        return share_profile_link;
    }

    public static void setShare_profile_link(String share_profile_link) {
        GetSet.share_profile_link = share_profile_link;
    }

    private static String share_profile_link = null;
    private static String age = null;
    private static String loginId = null;
    private static String gender = null;
    private static String bio = null;
    private static String loginType = null;
    private static Long gifts = 0L;
    private static Long videos = 0L;
    private static Long videosHistory = 0L;
    private static String followersCount = null;
    private static String followingCount = null;
    private static int friendsCount = 0;
    private static int interestsCount = 0;
    private static int unlocksLeft = 0;
    private static String location = null;
    private static String premiumMember = "false";
    private static String premiumExpiry = null;
    private static boolean filterApplied;
    private static boolean locationApplied;
    private static String filterLocation = null;
    private static String filterMinAge = null;
    private static String filterMaxAge = null;
    private static String filterGender = null;
    private static String filterGems = null;
    private static String cameraGems = null;
    private static String Email = null;
    private static String privacyAge = null;
    private static String privacyContactMe = null;
    private static String showNotification = null;
    private static String chatNotification = null;
    private static String followNotification = null;
    private static boolean interestNotification;
    private static String giftEarnings = null;
    private static String fullName = null;
    private static String referalLink = null;
    private static String createdAt = null;
    private static String max_sound_duration = null;
    private static int max_video_duration = 30;
    private static String rating = "0";
    private static String ratingUserCount = "0";

    private static String getPrimeContent = null;
    private static String getPhonenumber = "";

    private static String postCommand = String.valueOf(R.string.settings_everyone);
    private static String sendMessage = String.valueOf(R.string.settings_everyone);


    private static String giftCoversionEarnings = null;
    private static String giftConversionValue = null;
    private static String paypal_id = null;

    private static String streamBaseUrl = null;

    private static String streamToken = null;

    private static String imageUrl = null;

    private static String token = null;
     public static String report_category = null;
    public static String report_subcategory = null;
    public static String report_childcategory = null;
    public static String youtube_link = null;
    public static String instagram_link = null;


    public static String getInstagram_link() {
        return instagram_link;
    }

    public static void setInstagram_link(String instagram_link) {
        GetSet.instagram_link = instagram_link;
    }

    public static String getYoutube_link() {
        return youtube_link;
    }

    public static void setYoutube_link(String youtube_link) {
        GetSet.youtube_link = youtube_link;
    }



    private static String stream_base_url= null;

    public static String getStream_base_url() {
        return stream_base_url;
    }

    public static void setStream_base_url(String stream_base_url) {
        GetSet.stream_base_url = stream_base_url;
    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        GetSet.token = token;
    }

    public static String getImageUrl() {
        return imageUrl;
    }

    public static void setImageUrl(String imageUrl) {
        GetSet.imageUrl = imageUrl;
    }
    public static String getEmail() {
        return Email;
    }

    public static void setEmail(String email) {
        Email = email;
    }
    public static String getRating() {
        return rating;
    }

    public static void setRating(String rating) {
        GetSet.rating = rating;
    }
    public static String getRatingUserCount() {
        return ratingUserCount;
    }

    public static void setRatingUserCount(String ratingUserCount) {
        GetSet.ratingUserCount = ratingUserCount;
    }
    public static String getStreamToken() {
        return streamToken;
    }

    public static void setStreamToken(String streamToken) {
        GetSet.streamToken = streamToken;
    }

    public static boolean isInterestNotification() {
        return interestNotification;
    }

    public static String getPaypal_id() {
        return paypal_id;
    }

    public static void setPaypal_id(String paypal_id) {
        GetSet.paypal_id = paypal_id;
    }

    public static String getGiftCoversionEarnings() {
        return giftCoversionEarnings;
    }

    public static void setGiftCoversionEarnings(String giftCoversionEarnings) {
        GetSet.giftCoversionEarnings = giftCoversionEarnings;
    }

    public static String getGiftConversionValue() {
        return giftConversionValue;
    }

    public static void setGiftConversionValue(String giftConversionValue) {
        GetSet.giftConversionValue = giftConversionValue;
    }

    public static String getStreamBaseUrl() {
        return streamBaseUrl;
    }

    public static void setStreamBaseUrl(String streamBaseUrl) {
        GetSet.streamBaseUrl = streamBaseUrl;
    }

    public static String getGetPrimeContent() {
        return getPrimeContent;
    }

    public static void setGetPrimeContent(String getPrimeContent) {
        GetSet.getPrimeContent = getPrimeContent;
    }

    public static String getPostCommand() {
        return postCommand;
    }

    public static void setPostCommand(String postCommand) {
        GetSet.postCommand = postCommand;
    }

    public static String getSendMessage() {
        return sendMessage;
    }

    public static void setSendMessage(String sendMessage) {
        GetSet.sendMessage = sendMessage;
    }

    public static String getMax_sound_duration() {
        return max_sound_duration;
    }

    public static void setMax_sound_duration(String max_sound_duration) {
        GetSet.max_sound_duration = max_sound_duration;
    }

    public static int getMax_video_duration() {
        return max_video_duration;
    }

    public static void setMax_video_duration(int max_video_duration) {
        GetSet.max_video_duration = max_video_duration;
    }

    private static boolean oncePurchased;


    private static Integer likes = 0;
    private static String comment_privacy = null;
    private static String message_privacy = null;

    public static Integer getLikes() {
        return likes;
    }

    public static void setLikes(Integer likes) {
        GetSet.likes = likes;
    }

    public static String getComment_privacy() {
        return comment_privacy;
    }

    public static void setComment_privacy(String comment_privacy) {
        GetSet.comment_privacy = comment_privacy;
    }

    public static String getGetPhonenumber() {
        return getPhonenumber;
    }

    public static void setGetPhonenumber(String getPhonenumber) {
        GetSet.getPhonenumber = getPhonenumber;
    }

    public static String getMessage_privacy() {
        return message_privacy;
    }

    public static void setMessage_privacy(String message_privacy) {
        GetSet.message_privacy = message_privacy;
    }

    public static boolean isLogged() {
        return isLogged;
    }


    public static String getBio() {
        return bio;
    }

    public static void setBio(String bio) {
        GetSet.bio = bio;
    }

    public static void setLogged(boolean isLogged) {
        GetSet.isLogged = isLogged;
    }

    public static String getUserId() {
        return userId;
    }

    public static void setUserId(String userId) {
        GetSet.userId = userId;
    }

    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        GetSet.name = name;
    }

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String userName) {
        GetSet.userName = userName;
    }

/*    public static Long getGems() {
        return gems;
    }

    public static void setGems(Long gems) {
        GetSet.gems = gems;
    }*/

    public static Float getGems() {
        return gems;
    }

    public static void setGems(Float gems) {
        GetSet.gems = gems;
    }

    public static String getPassword() {
        return Password;
    }

    public static void setPassword(String password) {
        Password = password;
    }
    public static String getFullName() {
        return fullName;
    }

    public static void setFullName(String fullName) {
        GetSet.fullName = fullName;
    }
    public static String getUserImage() {
        return userImage;
    }

    public static void setUserImage(String userImage) {
        GetSet.userImage = userImage;
    }

    public static String getAuthToken() {
        return authToken;
    }

    public static void setAuthToken(String authToken) {
        GetSet.authToken = authToken;
    }

    public static String getDob() {
        return dob;
    }

    public static void setDob(String dob) {
        GetSet.dob = dob;
    }

    public static String getAge() {
        return age;
    }

    public static void setAge(String age) {
        GetSet.age = age;
    }

    public static String getLoginId() {
        return loginId;
    }

    public static void setLoginId(String loginId) {
        GetSet.loginId = loginId;
    }

    public static String getGender() {
        return gender;
    }

    public static void setGender(String gender) {
        GetSet.gender = gender;
    }

    public static String getLoginType() {
        return loginType;
    }

    public static void setLoginType(String loginType) {
        GetSet.loginType = loginType;
    }

    public static boolean isIsLogged() {
        return isLogged;
    }

    public static void setIsLogged(boolean isLogged) {
        GetSet.isLogged = isLogged;
    }

    public static Long getGifts() {
        return gifts;
    }

    public static void setGifts(Long gifts) {
        GetSet.gifts = gifts;
    }


    public static Long getVideos() {
        return videos;
    }

    public static void setVideos(Long videos) {
        GetSet.videos = videos;
    }

    public static Long getVideosHistory() {
        return videosHistory;
    }

    public static void setVideosHistory(Long videosHistory) {
        GetSet.videosHistory = videosHistory;
    }

    public static String getFollowersCount() {
        return followersCount;
    }

    public static void setFollowersCount(String followersCount) {
        GetSet.followersCount = followersCount;
    }

    public static String getFollowingCount() {
        return followingCount;
    }

    public static void setFollowingCount(String followingCount) {
        GetSet.followingCount = followingCount;
    }

    public static String getLocation() {
        return location;
    }

    public static void setLocation(String location) {
        GetSet.location = location;
    }

    public static String getPremiumMember() {
        return premiumMember;
    }

    public static void setPremiumMember(String premiumMember) {
        GetSet.premiumMember = premiumMember;
    }

    public static String getPremiumExpiry() {
        return premiumExpiry;
    }

    public static void setPremiumExpiry(String premiumExpiry) {
        GetSet.premiumExpiry = premiumExpiry;
    }

    public static boolean isFilterApplied() {
        return filterApplied;
    }

    public static void setFilterApplied(boolean filterApplied) {
        GetSet.filterApplied = filterApplied;
    }

    public static String getFilterLocation() {
        return filterLocation;
    }

    public static void setFilterLocation(String filterLocation) {
        GetSet.filterLocation = filterLocation;
    }

    public static String getFilterMinAge() {
        return filterMinAge;
    }

    public static void setFilterMinAge(String filterMinAge) {
        GetSet.filterMinAge = filterMinAge;
    }

    public static String getFilterMaxAge() {
        return filterMaxAge;
    }

    public static void setFilterMaxAge(String filterMaxAge) {
        GetSet.filterMaxAge = filterMaxAge;
    }

    public static String getFilterGender() {
        return filterGender;
    }

    public static void setFilterGender(String filterGender) {
        GetSet.filterGender = filterGender;
    }

    public static String getPrivacyAge() {
        return privacyAge;
    }

    public static void setPrivacyAge(String privacyAge) {
        GetSet.privacyAge = privacyAge;
    }

    public static String getPrivacyContactMe() {
        return privacyContactMe;
    }

    public static void setPrivacyContactMe(String privacyContactMe) {
        GetSet.privacyContactMe = privacyContactMe;
    }

    public static String getReferalLink() {
        return referalLink;
    }

    public static void setReferalLink(String referalLink) {
        GetSet.referalLink = referalLink;
    }

    public static String getFilterGems() {
        return filterGems;
    }

    public static void setFilterGems(String filterGems) {
        GetSet.filterGems = filterGems;
    }

    public static String getCameraGems() {
        return cameraGems;
    }

    public static void setCameraGems(String cameraGems) {
        GetSet.cameraGems = cameraGems;
    }

    public static boolean isOncePurchased() {
        return oncePurchased;
    }

    public static void setOncePurchased(boolean oncePurchased) {
        GetSet.oncePurchased = oncePurchased;
    }

    public static boolean isLocationApplied() {
        return locationApplied;
    }

    public static void setLocationApplied(boolean locationApplied) {
        GetSet.locationApplied = locationApplied;
    }

    public static String getShowNotification() {
        return showNotification;
    }

    public static void setShowNotification(String showNotification) {
        GetSet.showNotification = showNotification;
    }

    public static String getChatNotification() {
        return chatNotification;
    }

    public static void setChatNotification(String chatNotification) {
        GetSet.chatNotification = chatNotification;
    }

    public static String getFollowNotification() {
        return followNotification;
    }

    public static void setFollowNotification(String followNotification) {
        GetSet.followNotification = followNotification;
    }

    public static String getGiftEarnings() {
        return giftEarnings;
    }

    public static void setGiftEarnings(String giftEarnings) {
        GetSet.giftEarnings = giftEarnings;
    }

    public static String getCreatedAt() {
        return createdAt;
    }

    public static void setCreatedAt(String createdAt) {
        GetSet.createdAt = createdAt;
    }

    public static int getFriendsCount() {
        return friendsCount;
    }

    public static void setFriendsCount(int friendsCount) {
        GetSet.friendsCount = friendsCount;
    }

    public static int getInterestsCount() {
        return interestsCount;
    }

    public static void setInterestsCount(int interestsCount) {
        GetSet.interestsCount = interestsCount;
    }

    public static boolean getInterestNotification() {
        return interestNotification;
    }

    public static void setInterestNotification(boolean interestNotification) {
        GetSet.interestNotification = interestNotification;
    }

    public static int getUnlocksLeft() {
        return unlocksLeft;
    }

    public static void setUnlocksLeft(int unlocksLeft) {
        GetSet.unlocksLeft = unlocksLeft;
    }

    public static void reset() {
        GetSet.setLogged(false);
        GetSet.setAuthToken(null);
        GetSet.setPassword(null);
        GetSet.setUserId(null);
        GetSet.setName(null);
        GetSet.setUserName(null);
        GetSet.setUserImage(null);
        GetSet.setAge(null);
        GetSet.setGems(0F);
        GetSet.setGifts(0L);
        GetSet.setVideos(0L);
        GetSet.setVideosHistory(0L);
        GetSet.setFollowingCount(null);
        GetSet.setFollowersCount(null);
        GetSet.setFriendsCount(0);
        GetSet.setInterestsCount(0);
        GetSet.setUnlocksLeft(0);
        GetSet.setLocation(null);
        GetSet.setLoginType(null);
        GetSet.setPrivacyAge(null);
        GetSet.setPrivacyContactMe(null);
        GetSet.setShowNotification(null);
        GetSet.setChatNotification(null);
        GetSet.setFollowNotification(null);
        GetSet.setFilterApplied(false);
        GetSet.setLocationApplied(false);
        GetSet.setFilterGender(null);
        GetSet.setFilterMinAge(null);
        GetSet.setFilterMaxAge(null);
        GetSet.setFilterLocation(null);
        GetSet.setPremiumMember("false");
        GetSet.setPremiumExpiry(null);
        GetSet.setCameraGems(null);
        GetSet.setFilterGems(null);
        GetSet.setOncePurchased(false);
        GetSet.setGiftEarnings(null);
        GetSet.setCreatedAt(null);
        GetSet.setInterestNotification(false);
        GetSet.setYoutube_link(null);
        GetSet.setInstagram_link(null);
    }
}
