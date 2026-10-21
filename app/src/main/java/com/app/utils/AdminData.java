package com.app.utils;


import com.app.alhawanajd.AudioCategory;
import com.app.model.AppDefaultResponse;
import com.app.model.FilterGems;
import com.app.model.FilterOptions;
import com.app.model.GetSet;
import com.app.model.Gift;
import com.app.model.GiftsDetails;
import com.app.model.MembershipPackages;
import com.app.model.PrimeDetails;
import com.app.model.Report;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class AdminData {
    public static final String TAG = AdminData.class.getSimpleName();
    public static Long freeGems = 0L;
    public static List<Report> reportList = new ArrayList<>();
    public static List<Report> userReportList = new ArrayList<>();
    public static PrimeDetails primeDetails = new PrimeDetails();
    public static GiftsDetails giftsDetails = new GiftsDetails();
    public static List<Gift> giftList = new ArrayList<>();
    public static int LIVE_ALLOW_COUNT = 500;
    public static String is_livestream="1";
    public static List<Gift> livestream_gifts = new ArrayList<>();
    public static List<String> locationList = new ArrayList<>();
    public static List<MembershipPackages> membershipList = new ArrayList<>();
    public static String contactEmail = "";
    public static FilterGems filterGems = new FilterGems();
    public static FilterOptions filterOptions = new FilterOptions();
    public static String showAds = "1";
    public static String max_sound_duration = "60";
    public static int max_video_duration = 60;
    public static String arabic_ad_key = "";
    public static String english_ad_key = "";
    public static String ad_status = "";

    public static Long inviteCredits;
    public static String welcomeMessage = "";
    public static String showVideoAd = "1";
    public static String showMoneyConversion = "0";
    public static long videoAdsDuration = 5;
  //  public static List<String> audioCategory = new ArrayList<>();
    public static List<AudioCategory> audioCategory = new ArrayList<>();

    public static String postAuthorizeMessage = "";
    public static boolean galleryUpload = false;
    public static boolean downloadVideo = false;
    public static boolean uploadSound = false;
    public static long videoCallsGems = 0;
    public static AppDefaultResponse.StreamConnectionInfo streamDetails = null;
    public static int two_x_duration = 0;
    public static int three_x_duration = 0;

    public static List<Gift> getLivestream_gifts() {
        return livestream_gifts;
    }

    public static void setLivestream_gifts(List<Gift> livestream_gifts) {
        AdminData.livestream_gifts = livestream_gifts;
    }

    public static List<String> battle_durations=new ArrayList<>();

    public static List<Report> livestream_reports = new ArrayList<>();

    public static List<Report> getLivestream_reports() {
        return livestream_reports;
    }

    public static void setLivestream_reports(List<Report> livestream_reports) {
        AdminData.livestream_reports = livestream_reports;
    }

    public static void resetData() {
        freeGems = 0L;
        giftList = new ArrayList<>();
        giftsDetails = new GiftsDetails();
        primeDetails = new PrimeDetails();
        reportList = new ArrayList<>();
        /*Add first item as Select all location filter*/
        locationList = new ArrayList<>();
        membershipList = new ArrayList<>();
        filterGems = new FilterGems();
        filterOptions = new FilterOptions();
        showAds = "1";
        showVideoAd = "1";
        inviteCredits = 0L;
        showMoneyConversion = "0";
        videoAdsDuration = 5;
        audioCategory = new ArrayList<>();
        postAuthorizeMessage = "";
        galleryUpload = false;
        downloadVideo = false;
        uploadSound = false;
        streamDetails = null;
        battle_durations = new ArrayList<>();
    }

    public static boolean isAdEnabled() {
        boolean isAdEnabled = true;
        if (showAds.equals("1")) {
            if (GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                isAdEnabled = false;
            }
        } else {
            isAdEnabled = false;
        }
        return isAdEnabled;
    }
    //newly added for live streaming
    public static String mediaURL = "https://d309eu4wskznyx.cloudfront.net/";
}
