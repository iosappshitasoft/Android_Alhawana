package com.app.utils.api;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.app.alhawanajd.ChatActivity;
import com.app.alhawanajd.ChatFragment;
import com.app.helper.LocaleManager;
import com.app.alhawanajd.App;
import com.app.model.AppDefaultResponseNew;
import com.app.model.ProfileRequest;
import com.app.model.ProfileResponse;
import com.google.gson.Gson;
import com.app.helper.NetworkReceiver;
import com.app.model.AppDefaultResponse;
import com.app.model.GetSet;
import com.app.model.ReportRequest;
import com.app.model.ReportResponse;
import com.app.utils.AdminData;
import com.app.utils.Constants;
import com.app.utils.SharedPref;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class CommonApi {

//    Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void getAppDefaultData1(Context context) {
        ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
        if (NetworkReceiver.isConnected()) {
            mExecutorService.execute(new Runnable() {
                public void run() {
                    ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
                    String lang = LocaleManager.getLanguageCode(context);
                    Call<AppDefaultResponse> call = apiInterface.getAppDefaultData(Constants.TAG_ANDROID, lang);
                    call.enqueue(new Callback<AppDefaultResponse>() {
                        @Override
                        public void onResponse(Call<AppDefaultResponse> call, Response<AppDefaultResponse> response) {
                            if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                                AppDefaultResponse defaultData = response.body();
                                Log.e("Commonapi getAppDefaultData: %s", App.getGsonPrettyInstance().toJson(defaultData));
                                AdminData.resetData();

                                AdminData.livestream_gifts = defaultData.getLivestream_gifts();
                                Log.d("Commonapi", "onResponse_livestream_reports: "+defaultData.getLivestream_reports());

                                AdminData.freeGems = defaultData.getFreeGems();
                                AdminData.giftList = defaultData.getGifts();
                                AdminData.giftsDetails = defaultData.getGiftsDetails();
                                AdminData.primeDetails = defaultData.getPrimeDetails();
                                AdminData.reportList = defaultData.getReports();
                               /* AdminData.userReportList = defaultData.getUserReports();*/
                                AdminData.userReportList = defaultData.getUserReportsNew();
                                Log.d("CommonApi", "onResponse_Report: "+ AdminData.reportList);
                                AdminData.livestream_reports = defaultData.getLivestream_reports();
                                /*Add first item as Select all location filter*/
                                /*AdminData.locationList = new ArrayList<>();
                                AdminData.locationList.add(getString(R.string.select_all));
                                AdminData.locationList.addAll(defaultData.getLocations());*/
                                AdminData.membershipList = defaultData.getMembershipPackages();
                                AdminData.filterGems = defaultData.getFilterGems();
                                AdminData.filterOptions = defaultData.getFilterOptions();
                                AdminData.inviteCredits = defaultData.getInviteCredits();
                                AdminData.showAds = defaultData.getShowAds();
                                AdminData.showVideoAd = defaultData.getVideoAds();
                                AdminData.welcomeMessage = defaultData.getWelcomeMessage();
                                AdminData.videoAdsDuration = defaultData.getVideoAdsDuration();
                                AdminData.postAuthorizeMessage = defaultData.getPostAuthorizeMessage();
                                AdminData.galleryUpload = defaultData.isGalleryUpload();
                                AdminData.downloadVideo = defaultData.isDownloadVideo();
                                AdminData.uploadSound = defaultData.isUploadSound();
                                AdminData.contactEmail = defaultData.getContactEmail();
                                AdminData.showMoneyConversion = defaultData.getShowMoneyConversion();
                               // AdminData.audioCategory = defaultData.getAudioCategory();
                                AdminData.audioCategory = defaultData.getAudioCategory();
                                AdminData.max_sound_duration = defaultData.getMaxSoundDuration();
                                AdminData.max_video_duration = defaultData.getMaxVideoDuration();

                                GetSet.setMax_sound_duration(defaultData.getMaxSoundDuration());
                                GetSet.setMax_video_duration(defaultData.getMaxVideoDuration());

                                if (AdminData.membershipList != null && AdminData.membershipList.size() > 0) {
                                    SharedPref.putString(SharedPref.DEFAULT_SUBS_SKU, AdminData.membershipList.get(0).getSubsTitle());
                                } else {
                                    SharedPref.putString(SharedPref.DEFAULT_SUBS_SKU, Constants.DEFAULT_SUBS_SKU);
                                }

                                //Addon Live Stream
                                if (Constants.ADDON_LIVE_STREAM) {
                                    AdminData.streamDetails = defaultData.getStreamConnectionInfo();
                                    GetSet.setStreamBaseUrl(defaultData.getStreamConnectionInfo().getStreamBaseUrl());
                                    SharedPref.putString(SharedPref.STREAM_BASE_URL, defaultData.getStreamConnectionInfo().getStreamBaseUrl());
                                    SharedPref.putString(SharedPref.STREAM_WEBSOCKET_URL, defaultData.getStreamConnectionInfo().getWebSocketUrl());
                                    SharedPref.putString(SharedPref.STREAM_VOD_URL, defaultData.getStreamConnectionInfo().getStreamVodUrl());
                                    SharedPref.putString(SharedPref.STREAM_API_URL, defaultData.getStreamConnectionInfo().getStreamApiUrl());
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<AppDefaultResponse> call, Throwable t) {
                            call.cancel();
                        }
                    });
                }
            });
        }
    }

    public static void getAppDefaultData(Context context) {
        ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
        if (NetworkReceiver.isConnected()) {
            mExecutorService.execute(new Runnable() {
                public void run() {
                    ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
                    String lang = LocaleManager.getLanguageCode(context);
                    Call<AppDefaultResponse> call = apiInterface.getAppDefaultData(Constants.TAG_ANDROID,lang);
                    call.enqueue(new Callback<AppDefaultResponse>() {
                        @Override
                        public void onResponse(Call<AppDefaultResponse> call, Response<AppDefaultResponse> response) {
                            if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                                AppDefaultResponse defaultData = response.body();

                           Log.d("Commonapi getAppDefaultData: %s", App.getGsonPrettyInstance().toJson(defaultData));
                                AdminData.resetData();
                                AdminData.livestream_gifts = defaultData.getLivestream_gifts();
                                Log.d("Commonapi", "onResponse_livestream_reports: "+defaultData.getLivestream_reports());

                                AdminData.freeGems = defaultData.getFreeGems();
                                AdminData.giftList = defaultData.getGifts();
                                AdminData.giftsDetails = defaultData.getGiftsDetails();
                                AdminData.primeDetails = defaultData.getPrimeDetails();
                                AdminData.reportList = defaultData.getReports();
                               /* AdminData.userReportList = defaultData.getUserReports();*/
                                AdminData.userReportList = defaultData.getUserReportsNew();
                                Log.d("CommonApi", "onResponse_Report: "+ AdminData.reportList);
                                AdminData.livestream_reports = defaultData.getLivestream_reports();
                                /*Add first item as Select all location filter*/
                                /*AdminData.locationList = new ArrayList<>();
                                AdminData.locationList.add(getString(R.string.select_all));
                                AdminData.locationList.addAll(defaultData.getLocations());*/
                                AdminData.membershipList = defaultData.getMembershipPackages();
                                AdminData.filterGems = defaultData.getFilterGems();
                                AdminData.filterOptions = defaultData.getFilterOptions();
                                AdminData.inviteCredits = defaultData.getInviteCredits();
                                AdminData.showAds = defaultData.getShowAds();
                                AdminData.showVideoAd = defaultData.getVideoAds();
                                AdminData.welcomeMessage = defaultData.getWelcomeMessage();
                                AdminData.videoAdsDuration = defaultData.getVideoAdsDuration();
                                AdminData.postAuthorizeMessage = defaultData.getPostAuthorizeMessage();
                                AdminData.galleryUpload = defaultData.isGalleryUpload();
                                AdminData.downloadVideo = defaultData.isDownloadVideo();
                                AdminData.uploadSound = defaultData.isUploadSound();
                                AdminData.contactEmail = defaultData.getContactEmail();
                                AdminData.showMoneyConversion = defaultData.getShowMoneyConversion();
                                AdminData.audioCategory = defaultData.getAudioCategory();
                                 //AdminData.audioCategory = defaultData.getAudioCategory();
                                AdminData.max_sound_duration = defaultData.getMaxSoundDuration();
                                AdminData.max_video_duration = defaultData.getMaxVideoDuration();

                                GetSet.setMax_sound_duration(defaultData.getMaxSoundDuration());
                                GetSet.setMax_video_duration(defaultData.getMaxVideoDuration());

                                if (AdminData.membershipList != null && AdminData.membershipList.size() > 0) {
                                    SharedPref.putString(SharedPref.DEFAULT_SUBS_SKU, AdminData.membershipList.get(0).getSubsTitle());
                                } else {
                                    SharedPref.putString(SharedPref.DEFAULT_SUBS_SKU, Constants.DEFAULT_SUBS_SKU);
                                }

                                //Addon Live Stream
                                if (Constants.ADDON_LIVE_STREAM) {
                                    AdminData.streamDetails = defaultData.getStreamConnectionInfo();
                                    GetSet.setStreamBaseUrl(defaultData.getStreamConnectionInfo().getStreamBaseUrl());
                                    SharedPref.putString(SharedPref.STREAM_BASE_URL, defaultData.getStreamConnectionInfo().getStreamBaseUrl());
                                    SharedPref.putString(SharedPref.STREAM_WEBSOCKET_URL, defaultData.getStreamConnectionInfo().getWebSocketUrl());
                                    SharedPref.putString(SharedPref.STREAM_VOD_URL, defaultData.getStreamConnectionInfo().getStreamVodUrl());
                                    SharedPref.putString(SharedPref.STREAM_API_URL, defaultData.getStreamConnectionInfo().getStreamApiUrl());
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<AppDefaultResponse> call, Throwable t) {
                            call.cancel();
                            Log.d("Commonapi failure: %s", t.getMessage());

                        }
                    });
                }
            });
        }
    }

    public static void getGiftsList(Context context) {
        ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
        if (NetworkReceiver.isConnected()) {
            mExecutorService.execute(new Runnable() {
                public void run() {
                    ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
                    String lang = LocaleManager.getLanguageCode(context);
                    Call<AppDefaultResponse> call = apiInterface.getAppDefaultData(Constants.TAG_ANDROID,lang);
                    call.enqueue(new Callback<AppDefaultResponse>() {
                        @Override
                        public void onResponse(Call<AppDefaultResponse> call, Response<AppDefaultResponse> response) {
                            if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                                AppDefaultResponse defaultData = response.body();
                                AdminData.giftList = defaultData.getGifts();

                            }
                        }

                        @Override
                        public void onFailure(Call<AppDefaultResponse> call, Throwable t) {
                            call.cancel();
                            Log.d("Commonapi failure: %s", t.getMessage());

                        }
                    });
                }
            });
        }
    }


    public static void getAppDefaultDataForGift(Context context) {
        ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
        if (NetworkReceiver.isConnected()) {
            mExecutorService.execute(new Runnable() {
                public void run() {
                    ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
                    String lang = LocaleManager.getLanguageCode(context);
                    Call<AppDefaultResponseNew> call = apiInterface.getAppDefaultData1(Constants.TAG_ANDROID,lang);
                    call.enqueue(new Callback<AppDefaultResponseNew>() {
                        @Override
                        public void onResponse(Call<AppDefaultResponseNew> call, Response<AppDefaultResponseNew> response) {
                            if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                                AppDefaultResponseNew defaultData = response.body();
                                AdminData.livestream_gifts = defaultData.getLivestream_gifts();
                                Log.d("Commonapi", "onResponse_livestream_reports: "+defaultData.getLivestream_reports());

                            }
                        }

                        @Override
                        public void onFailure(Call<AppDefaultResponseNew> call, Throwable t) {
                            call.cancel();
                            Log.d("Commonapi failure: %s", t.getMessage());

                        }
                    });
                }
            });
        }
    }


    public static void reportUser(String partnerId, String title, String id, ApiCallback apiCallback) {
        if (NetworkReceiver.isConnected()) {
            ReportRequest request = new ReportRequest();
            request.setUserId(partnerId);
            request.setReportBy(GetSet.getUserId());
            request.setReport(title);
            //newly added on sep29
            request.setReport_id(id);
            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<ReportResponse> call = apiInterface.reportUser(request);
            call.enqueue(new Callback<ReportResponse>() {
                @Override
                public void onResponse(Call<ReportResponse> call, Response<ReportResponse> response) {
                    apiCallback.onResponse(response.body());
                }

                @Override
                public void onFailure(Call<ReportResponse> call, Throwable t) {
                    call.cancel();
                    apiCallback.onFailure(t);
                }
            });
        }
    }

    public static void reportVideo(String videoId, String title, boolean report, ApiCallback apiCallback){
        reportVideo(videoId,title,report,"","",apiCallback);
    }
    public static void reportVideo(String videoId, String title, boolean report , String reportEnteredTxt , String personName, ApiCallback apiCallback ) {
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.TAG_USER_ID, GetSet.getUserId());
        map.put(Constants.TAG_NAME, videoId);
        map.put("report_id" ,"");
        if (!reportEnteredTxt.isEmpty())
            map.put("textbox", reportEnteredTxt);
        if (!personName.isEmpty())
            map.put("personsearch", personName);
        if (report) {
//            map.put(Constants.TAG_REPORT, title);

        if (GetSet.report_category != null){
            map.put("report_category", GetSet.report_category);
        }

        if (GetSet.report_subcategory != null){
            map.put("report_subcategory", GetSet.report_subcategory);
        }
        if (GetSet.report_childcategory != null){
            map.put("report_childcategory", GetSet.report_childcategory);
        }
        }

        Timber.d("sendReport: %s", new Gson().toJson(map));

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<Map<String, String>> call = apiInterface.unReportVideo(map);
        if (report) {
            call = apiInterface.reportVideo(map);
        }
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                apiCallback.onResponse(response);
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
                apiCallback.onFailure(t);
            }
        });
    }

    public static void reportVideoNew(String videoId, String title, boolean report, ApiCallback apiCallback) {

        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.TAG_USER_ID, GetSet.getUserId());
        map.put(Constants.TAG_REPORT_CATEGORY, GetSet.getUserId());
        map.put(Constants.TAG_REPORT_SUBCATEGORY, GetSet.getUserId());
        map.put(Constants.TAG_NAME, videoId);
        if (report) {
            map.put(Constants.TAG_REPORT, title);
        }
        Timber.d("sendReport: %s", new Gson().toJson(map));

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<Map<String, String>> call = apiInterface.reportVideoNew(map);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                apiCallback.onResponse(response);
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
                apiCallback.onFailure(t);
            }
        });
    }

    public static void getProfile(String profileId, ApiCallback apiCallback) {
        ProfileRequest request = new ProfileRequest();
        request.setUserId(GetSet.getUserId());
        request.setProfileId(profileId);

        Timber.d("getProfile: %s", new Gson().toJson(request));
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ProfileResponse> call = apiInterface.getProfile(request);
        call.enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(@NotNull Call<ProfileResponse> call, @NotNull Response<ProfileResponse> response) {

                try {
                    if (response.isSuccessful() && response.body() != null && response.body().getStatus() != null)
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            Timber.d("OthersProfileActivity: %s", new Gson().toJson(response.body()));
                            ProfileResponse profile = response.body();
                            apiCallback.onResponse(profile);
                        }

                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Log.e(TAG, "getProfile: " + t.getMessage());
                call.cancel();
            }
        });
    }


    public static void getProfile(Context context,String profileId) {
        ProfileRequest request = new ProfileRequest();
        request.setUserId(GetSet.getUserId());
        request.setProfileId(profileId);

        Timber.d("getProfile: %s", new Gson().toJson(request));
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ProfileResponse> call = apiInterface.getProfile(request);
        call.enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(@NotNull Call<ProfileResponse> call, @NotNull Response<ProfileResponse> response) {

                try {
                    if (response.isSuccessful() && response.body() != null && response.body().getStatus() != null)
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {

                            Timber.d("getProfile: %s", new Gson().toJson(response.body()));

                            ProfileResponse profile = response.body();
                            GetSet.setUserId(profile.getUserId());
                            /*GetSet.setLoginId(profile.getLoginId());*/
                            GetSet.setName(profile.getName());
                            GetSet.setUserName(profile.getUserName());
                            GetSet.setDob(profile.getDob());
                            GetSet.setAge(String.valueOf(profile.getAge()));
                            GetSet.setGender(profile.getGender());
                            GetSet.setUserImage(profile.getUserImage());
                            /*GetSet.setLocation(profile.getLocation());*/
                            GetSet.setFollowingCount(String.valueOf(profile.getFollowings()));
                            GetSet.setFollowersCount(String.valueOf(profile.getFollowers()));
                            GetSet.setGifts(profile.getAvailableGifts() != null ? Long.parseLong(String.valueOf(profile.getAvailableGifts())) : 0);
                            GetSet.setGems(profile.getAvailableGems() != null ? profile.getAvailableGems() : 0F);
                            GetSet.setVideos(profile.getVideosCount() != null ? Long.parseLong(String.valueOf(profile.getVideosCount())) : 0);
                            /*GetSet.setVideosHistory(profile.getWatchedCount() != null ? profile.getWatchedCount() : 0);*/
                            GetSet.setPremiumMember(profile.getPremiumMember());
                            GetSet.setPremiumExpiry(profile.getPremiumExpiryDate());
                            /*GetSet.setPrivacyAge(profile.getPrivacyAge());*/
                            GetSet.setPrivacyContactMe(profile.getBlockedByMe());
                            GetSet.setShowNotification(String.valueOf(profile.getShowNotification()));
                            GetSet.setFollowNotification(String.valueOf(profile.getFollowNotification()));
                            GetSet.setChatNotification(String.valueOf(profile.getChatNotification()));
                            GetSet.setGiftEarnings(String.valueOf(profile.getGiftEarnings()));
                            GetSet.setBio(profile.getBio());
                            GetSet.setPostCommand(profile.getCommentPrivacy());
                            GetSet.setSendMessage(profile.getMessagePrivacy());
                            GetSet.setPaypal_id(profile.getPaypalId());

                            SharedPref.putString(SharedPref.USER_ID, GetSet.getUserId());
                            SharedPref.putString(SharedPref.LOGIN_ID, GetSet.getLoginId());
                            SharedPref.putString(SharedPref.NAME, GetSet.getName());
                            SharedPref.putString(SharedPref.USER_NAME, GetSet.getUserName());
                            SharedPref.putString(SharedPref.DOB, GetSet.getDob());
                            SharedPref.putString(SharedPref.AGE, GetSet.getAge());
                            SharedPref.putString(SharedPref.GENDER, GetSet.getGender());
                            SharedPref.putString(SharedPref.USER_IMAGE, GetSet.getUserImage());
                            /*SharedPref.putString(SharedPref.LOCATION, GetSet.getLocation());*/
                            SharedPref.putString(SharedPref.FOLLOWINGS_COUNT, GetSet.getFollowingCount());
                            SharedPref.putString(SharedPref.FOLLOWERS_COUNT, GetSet.getFollowersCount());
                            SharedPref.putInt(SharedPref.INTEREST_COUNT, GetSet.getInterestsCount());
                            SharedPref.putInt(SharedPref.FRIENDS_COUNT, GetSet.getFriendsCount());
                            SharedPref.putInt(SharedPref.UNLOCKS_LEFT, GetSet.getUnlocksLeft());
                            SharedPref.putLong(SharedPref.GIFTS, GetSet.getGifts());
                            SharedPref.putFloat(SharedPref.GEMS, GetSet.getGems());
                            SharedPref.putLong(SharedPref.VIDEOS, GetSet.getVideos());
                            SharedPref.putLong(SharedPref.VIDEOS_HISTORY, GetSet.getVideosHistory());
                            SharedPref.putString(SharedPref.IS_PREMIUM_MEMBER, GetSet.getPremiumMember());
                            SharedPref.putString(SharedPref.PREMIUM_EXPIRY, GetSet.getPremiumExpiry());
                            SharedPref.putString(SharedPref.PRIVACY_AGE, GetSet.getPrivacyAge());
                            SharedPref.putString(SharedPref.PRIVACY_CONTACT_ME, GetSet.getPrivacyContactMe());
                            SharedPref.putString(SharedPref.SHOW_NOTIFICATION, GetSet.getShowNotification());
                            SharedPref.putString(SharedPref.FOLLOW_NOTIFICATION, GetSet.getFollowNotification());
                            SharedPref.putString(SharedPref.CHAT_NOTIFICATION, GetSet.getChatNotification());
                            SharedPref.putBoolean(SharedPref.INTEREST_NOTIFICATION, GetSet.getInterestNotification());
                            SharedPref.putString(SharedPref.GIFT_EARNINGS, GetSet.getGiftEarnings());
                            SharedPref.putInt(SharedPref.INTEREST_COUNT, GetSet.getInterestsCount());
                            SharedPref.putInt(SharedPref.FRIENDS_COUNT, GetSet.getFriendsCount());
                            SharedPref.putString(SharedPref.BIO, GetSet.getBio());
                            SharedPref.putString(SharedPref.PAYPAL_ID, GetSet.getPaypal_id());
                            SharedPref.putString(SharedPref.POST_COMMAND, GetSet.getPostCommand());
                            SharedPref.putString(SharedPref.SEND_MESSAGE, GetSet.getSendMessage());

                        }

                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Log.e(TAG, "getProfile: " + t.getMessage());
                call.cancel();
            }
        });
    }

    public static void openChatWithSharedContent(Context context, String contentType, String text, Uri uri) {
        Intent chatIntent = new Intent(context, ChatFragment.class);
        chatIntent.putExtra("content_type", contentType);

        if (text != null)
            chatIntent.putExtra("shared_text", text);

        if (uri != null)
            chatIntent.putExtra("shared_uri", uri.toString());

        chatIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(chatIntent);
    }

    public static void openChatWithMultipleContent(Context context, String type, ArrayList<Uri> uris) {
        Intent chatIntent = new Intent(context, ChatFragment.class);
        chatIntent.putExtra("content_type", type);
        chatIntent.putParcelableArrayListExtra("shared_uris", uris);
        chatIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(chatIntent);
    }
}
