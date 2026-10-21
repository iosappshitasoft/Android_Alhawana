package com.app.utils.api;

import com.app.Zego.Model.CohostListModel;
import com.app.Zego.Model.CreateStreamModel;
import com.app.Zego.Model.LeaderBoardUser;
import com.app.Zego.Model.StreamersList;
import com.app.Zego.Model.TokenResponse;
import com.app.model.AddDeviceRequest;
import com.app.model.AdminMessageResponse;
import com.app.model.AppDefaultResponse;
import com.app.model.AppDefaultResponseNew;
import com.app.model.CommonResponse;
import com.app.model.ConvertGiftRequest;
import com.app.model.ConvertGiftResponse;
import com.app.model.CountryResponse;
import com.app.model.DiscoverSoundResponse;
import com.app.model.ExploreStreamResponse;
import com.app.model.FavSoundResponse;
import com.app.model.FavVideoResponse;
import com.app.model.FollowRequest;
import com.app.model.FollowResponse;
import com.app.model.FollowersResponse;
import com.app.model.GemsPurchaseRequest;
import com.app.model.GemsPurchaseResponse;
import com.app.model.GemsStoreResponse;
import com.app.model.GetVideosResponse;
import com.app.model.HashTagRequest;
import com.app.model.HashTagResponse;
import com.app.model.HelpResponse;
import com.app.model.HomeResponse;
import com.app.model.InterestResponse;
import com.app.model.LiveStreamRequest;
import com.app.model.MusicListResponse;
import com.app.model.MutualListResponse;
import com.app.model.NotificationResponse;
import com.app.model.PostHashTagRes;
import com.app.model.ProfileRequest;
import com.app.model.ProfileResponse;
import com.app.model.RecentHistoryResponse;
import com.app.model.RecentUser;
import com.app.model.ReferFriendResponse;
import com.app.model.RenewalRequest;
import com.app.model.ReportRequest;
import com.app.model.ReportResponceNew;
import com.app.model.ReportResponse;
import com.app.model.RepostResponse;
import com.app.model.Request.DiscoverSoundRequest;
import com.app.model.Request.GetVideosRequest;
import com.app.model.Request.HomeRequest;
import com.app.model.Request.InterestRequest;
import com.app.model.SearchHashTagRes;
import com.app.model.SearchPageResponse;
import com.app.model.SearchSoundRes;
import com.app.model.SearchUserResponse;
import com.app.model.SignInRequest;
import com.app.model.SignInResponse;
import com.app.model.SingleVideoResponse;
import com.app.model.StartStreamResponse;
import com.app.model.StreamDetails;
import com.app.model.SubscriptionRequest;
import com.app.model.SubscriptionResponse;
import com.app.model.TermsResponse;
import com.app.model.TrendingStreamResponse;
import com.app.model.UserList;
import com.app.model.VideoCommentResponse;
import com.app.model.VideoListResponse;
import com.app.utils.Constants;
import com.app.addon.livestream.external.model.LiveStreamResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("activities/appdefaults/{platform}")
    Call<AppDefaultResponseNew> getAppDefaultData1(@Path("platform") String platform, @Query("lang") String language);

    @GET("activities/appdefaults/{platform}")
    Call<AppDefaultResponse> getAppDefaultData(@Path("platform") String platform, @Query("lang") String language);

    @POST("giftconversions/gifttomoneyconversion")
    Call<ConvertGiftResponse> convertToMoney(@Body ConvertGiftRequest request);

    @POST("accounts/signin")
    Call<SignInResponse> callSignIn(@Body SignInRequest request);

    @POST("accounts/signup")
    Call<SignInResponse> callSignUp(@Body SignInRequest request);

    @Multipart
    @POST("accounts/uploadprofile")
    Call<Map<String, String>> uploadProfileImage(@Part MultipartBody.Part image, @Part("user_id") RequestBody user_id);

    @Multipart
    @POST("accounts/upmychat")
    Call<Map<String, String>> uploadChatImage(@Part MultipartBody.Part image, @Part("user_id") RequestBody user_id);

    @Multipart
    @POST("accounts/upmychat")
    Call<Map<String, String>> uploadAudio(@Part MultipartBody.Part image, @Part("user_id") RequestBody user_id);

    @POST("devices/register")
    Call<Map<String, String>> pushSignIn(@Body AddDeviceRequest request);

    @DELETE("devices/unregister/{device_id}")
    Call<HashMap<String, String>> pushSignOut(@Path("device_id") String deviceId);

    @GET("accounts/deleteaccount/{user_id}")
    Call<HashMap<String, String>> delete_account(@Path("user_id") String userId);

    @POST("accounts/profile")
    Call<ProfileResponse> getProfile(@Body ProfileRequest request);

    @POST("accounts/profile")
    Call<ProfileResponse> getlivestreamProfile(@Body ProfileRequest request);

    @GET("activities/followerslist/{user_id}/{offset}/{limit}")
    Call<FollowersResponse> getFollowers(@Path("user_id") String userId, @Path("offset") int offset, @Path("limit") int limit);

    @GET("activities/followingslist/{user_id}/{offset}/{limit}")
    Call<FollowersResponse> getFollowings(@Path("user_id") String userId, @Path("offset") int offset, @Path("limit") int limit);

    @POST("activities/follow")
    Call<Map<String, String>> followUser(@Body FollowRequest followRequest);

    @POST("activities/mutualfollowers")
    Call<MutualListResponse> getMutualList(@Body LiveStreamRequest request);

    @GET("activities/blockeduserslist/{user_id}/{offset}/{limit}")
    Call<SearchUserResponse> getBlockedList(@Path("user_id") String userId, @Path("offset") int offset, @Path("limit") int limit);

    @FormUrlEncoded
    @POST("activities/blockuser")
    Call<HashMap<String, String>> blockUser(@FieldMap HashMap<String, String> requestMap);

    @FormUrlEncoded
    @POST("activities/updatewatchvideo")
    Call<HashMap<String, String>> updateWatchVideo(@FieldMap HashMap<String, String> requestMap);

    @FormUrlEncoded
    @POST("activities/unlockinterest")
    Call<HashMap<String, String>> unlockUser(@FieldMap HashMap<String, String> requestMap);

    @FormUrlEncoded
    @POST("accounts/searchuser")
    Call<SearchUserResponse> searchByUserName(@FieldMap HashMap<String, String> requestMap);

    @GET("helps/loginterms")
    Call<TermsResponse> getTerms();

    @GET("helps/allterms")
    Call<HelpResponse> getHelps(@Query("lang") String language);

    @GET("accounts/isActive/{user_id}")
    Call<HashMap<String, String>> isActive(@Path("user_id") String userId);

    @GET("chats/recentvideochats/{user_id}/{offset}/{limit}")
    Call<RecentHistoryResponse> getRecentHistory(@Path("user_id") String userId, @Path("offset") int offset, @Path("limit") int limit);

    @POST("activities/reportuser")
    Call<ReportResponse> reportUser(@Body ReportRequest request);

    @Multipart
    @POST("activities/uploadreport")
    Call<CommonResponse> reportScreenUpload(@Part MultipartBody.Part image, @Part("report_id") RequestBody reportId);

    @Multipart
    @POST("accounts/upmyvideo")
    Call<CommonResponse> chatScreenUpload(@Part MultipartBody.Part image, @Part("user_id") RequestBody userId, @Part("partner_id") RequestBody partnerId);

    @GET("activities/gemstore/{user_id}/{platform}/{offset}/{limit}")
    Call<GemsStoreResponse> getGemsList(@Header("Authorization") String user_token,@Path("user_id") String userId, @Path("platform") String platform, @Path("offset") int offset, @Path("limit") int limit);

    @POST("payments/subscription")
    Call<SubscriptionResponse> paySubscription(@Body SubscriptionRequest request);

    @POST("payments/purchasegems")
    Call<GemsPurchaseResponse> buyGems(@Body GemsPurchaseRequest request);

    @POST("activities/gifttogems")
    Call<ConvertGiftResponse> convertGifts(@Body ConvertGiftRequest request);

    @GET("accounts/invitecredits/{referal_code}")
    Call<ReferFriendResponse> updateReferal(@Path("referal_code") String referalCode);

    @GET("accounts/rewardvideo/{user_id}")
    Call<Map<String, String>> updateVideoGems(@Path("user_id") String userId);

    @GET("activities/adminmessages/{user_id}/{platform}/{update_from}/{timestamp}")
    Call<AdminMessageResponse> getAdminMessages(@Path("user_id") String userId, @Path("platform") String platform, @Path("update_from") String updateFrom, @Path("timestamp") long currentTimeMillis);

    @GET("chats/clearvideochats/{user_id}")
    Call<HashMap<String, String>> clearRecentHistory(@Path("user_id") String userId);

    @GET("accounts/chargecalls/{user_id}")
    Call<HashMap<String, String>> chargeCalls(@Path("user_id") String userId);

    @POST("payments/renewalsubscription")
    Call<HashMap<String, String>> verifyPayment(@Body RenewalRequest renewalRequest);

    @POST("livestreams")
    Call<LiveStreamResponse> getCurrentStreams(@Body LiveStreamRequest request);

    @GET("activities/watchhistory/{user_id}/{offset}/{limit}")
    Call<LiveStreamResponse> getRecentWatched(@Path("user_id") String userId, @Path("limit") String limit, @Path("offset") String offset);

    @FormUrlEncoded
    @POST("accounts/showall")
    Call<UserList> getUsers(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("livestreams/streamdetails")
    Call<StreamDetails> getStreamDetails(@Field(Constants.TAG_USER_ID) String userId, @Field(Constants.TAG_NAME) String streamName);

    @GET("livestreams/createstream/{publisher_id}")
    Call<HashMap<String, String>> getStreamName(@Path("publisher_id") String publisherId);

    @FormUrlEncoded
    @POST("livestreams/startstream")
    Call<Map<String, String>> startStream(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("livestreams/stopstream")
    Call<Map<String, String>> stopStream(@FieldMap Map<String, String> params);

    @Multipart
    @POST("livestreams/uploadstream")
    Call<Map<String, String>> uploadStreamImage(@Part MultipartBody.Part image,
                                                @Part("publisher_id") RequestBody publisherId,
                                                @Part("name") RequestBody streamName);


    @Multipart
    @POST("activities/albumimage")
    Call<Map<String, String>> uploadAlbumImage(@Part MultipartBody.Part image,
                                               @Part("user_id") RequestBody userId,
                                               @Part("sound_id") RequestBody soundId);


    @Multipart
    @POST("activities/uploadsound")
    Call<Map<String, String>> uploadAudio(@Part MultipartBody.Part file,
                                          @Part("user_id") RequestBody userId,
                                          @Part("token") RequestBody token,
                                          @Part("title") RequestBody title,
                                          @Part("duration") RequestBody duration);

    @Multipart
    @POST("livestreams/uploadstream")
    Call<Map<String, String>> uploadCoverPhoto(@Part MultipartBody.Part file,
                                               @Part("video_id") RequestBody videoId);

    @Multipart
    @POST("livestreams/postvideo")
    Call<Map<String, String>> postVideo(@Part MultipartBody.Part file, @PartMap() Map<String, RequestBody> partMap);

    @FormUrlEncoded
    @PATCH("livestreams/updatevideo")
    Call<Map<String, String>> updateVideo(@FieldMap() Map<String, String> requestMap);

    @DELETE("livestreams/deletevideo/{user_id}/{video_id}")
    Call<Map<String, String>> deleteVideoId(@Path(Constants.TAG_USER_ID) String userId, @Path(Constants.TAG_VIDEO_ID) String videoId);

    @FormUrlEncoded
    @POST("livestreams/reportstream")
    Call<Map<String, String>> reportStream(@FieldMap Map<String, String> params);


    @FormUrlEncoded
    @POST("livestreams/report")
    Call<Map<String, String>> reportVideo(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("livestreams/unreport")
    Call<Map<String, String>> unReportVideo(@FieldMap Map<String, String> params);

    @DELETE("livestreams/deletestream/{user_id}/{name}")
    Call<Map<String, String>> deleteStream(@Path(Constants.TAG_USER_ID) String userId, @Path(Constants.TAG_NAME) String streamName);

    @FormUrlEncoded
    @POST("activities/updatewatchcount")
    Call<HashMap<String, String>> updateWatchCount(@FieldMap Map<String, String> params);

    @DELETE("activities/clearwatchhistory/{user_id}")
    Call<Map<String, String>> clearWatched(@Path(Constants.TAG_USER_ID) String userId);

    @POST("activities/hashtags")
    Call<HashTagResponse> getHashTags(@Body HashTagRequest request);

    @POST("activities/explorestreams")
    Call<ExploreStreamResponse> exploreStreams(@Body HashTagRequest request);

    @GET("activities/popularcountries/{popularType}")
    Call<CountryResponse> getPopularCountries(@Path("popularType") String popularType);

    @GET("activities/trendinghashtags")
    Call<TrendingStreamResponse> getTrendingHashTags();

    @FormUrlEncoded
    @POST("livestreams/sharestream")
    Call<Map<String, String>> shareStreams(@FieldMap Map<String, String> request);

    @GET("livestreams/streamtoken/{user_id}")
    Call<TokenResponse> getStreamToken(@Path("user_id") String userId);

    /* Fundoo*/
    @FormUrlEncoded
    @POST("activities/isMutualfollow")
    Call<Map<String, String>> mutualcontact(@FieldMap Map<String, String> request);


    @FormUrlEncoded
    @POST("accounts/messagecheck")
    Call<Map<String, String>> messagecheck(@FieldMap Map<String, String> request);


    @GET("accounts/getInterests")
    Call<InterestResponse> getInterestList();

    @POST("accounts/saveinterest")
    Call<Map<String, String>> saveInterest(@Body InterestRequest interestRequest);

    @POST("livestreams/home")
    Call<HomeResponse> getHomeData(@Body HomeRequest homeRequest);

    @POST("livestreams/home")
    Call<HomeResponse> getFollowingHomeData(@Body HomeRequest homeRequest);

    @FormUrlEncoded
    @POST("livestreams/singlevideo")
    Call<SingleVideoResponse> getSingleVideo(@FieldMap Map<String, String> request);

    @POST("livestreams/getallvideos")
    Call<HomeResponse> getAllVideos(@Body HomeRequest homeRequest);

    @FormUrlEncoded
    @POST("activities/homesearch")
    Call<SearchPageResponse> getSearchList(@FieldMap Map<String, String> request);

    /*@FormUrlEncoded
    @POST("activities/homesearch")
    Observable<SearchPageResponse> getSearchList(@FieldMap Map<String, String> request);*/

    @FormUrlEncoded
    @POST("activities/search")
    Call<UserList> getSearchUser(@FieldMap Map<String, String> request);

    @POST("activities/findfriends")
    Call<UserList> findFriends(@Body RequestBody body);

    @FormUrlEncoded
    @POST("activities/search")
    Call<VideoListResponse> getSearchVideo(@FieldMap Map<String, String> request);

    @FormUrlEncoded
    @POST("activities/search")
    Call<SearchSoundRes> getSearchSound(@FieldMap Map<String, String> request);

    @FormUrlEncoded
    @POST("activities/search")
    Call<SearchHashTagRes> getSearchHash(@FieldMap Map<String, String> request);

    @FormUrlEncoded
    @POST("livestreams/heart")
    Call<Map<String, String>> getHeartStatus(@FieldMap Map<String, String> request);

    @FormUrlEncoded
    @POST("activities/addfavorite")
    Call<Map<String, String>> setFav(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("livestreams/sendgift")
    Call<Map<String, String>> sendGift(@FieldMap Map<String, String> params);

    @POST("activities/findsounds")
    Call<DiscoverSoundResponse> getSound(@Body DiscoverSoundRequest soundRequest);

    @POST("livestreams/getvideos")
    Call<VideoListResponse> getvideos(@Body GetVideosRequest getVideoRequest);

    @FormUrlEncoded
    @POST("livestreams/getvideos")
    Call<GetVideosResponse> getRelatedSoundVideos(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("livestreams/getvideos")
    Call<VideoListResponse> getSoundVideos(@FieldMap Map<String, String> params);

    @POST("activities/getfavorites")
    Call<FavVideoResponse> getfavvideos(@Body GetVideosRequest getVideoRequest);

    @POST("activities/getfavorites")
    Call<FavSoundResponse> getfavsounds(@Body GetVideosRequest getVideoRequest);

    @FormUrlEncoded
    @POST("chats/getcomment")
    Call<VideoCommentResponse> getVideoComments(@FieldMap Map<String, String> request);


    @FormUrlEncoded
    @POST("activities/notifications")
    Call<NotificationResponse> getNotifications(@FieldMap Map<String, String> request);

    /*@FormUrlEncoded
    @POST("chats/postcomment")
    Call<VideoCommentResponse> postComments(@FieldMap Map<String, String> request);*/

    @FormUrlEncoded
    @POST("chats/postcomment")
    Call<Map<String, String>> postComments(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("chats/deletecomment")
    Call<Map<String, String>> deleteComment(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("activities/hashtags")
    Call<PostHashTagRes> getPostHashTags(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("activities/categorysearch")
    Call<UserList> getUsersList(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("activities/categorysearch")
    Call<VideoListResponse> getVideoList(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("activities/categorysearch")
    Call<MusicListResponse> getMusicList(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("livestreams/contributor_leaderboard")
    Call<LeaderBoardUser> contributorleaderboard(@Field("user_id") String user_id,
                                                 @Field("type") String type,
                                                 @Field("battle_id") String battle_id,
                                                 @Field("stream_id") String stream_id,
                                                 @Field("page_type") String page_type);


    @FormUrlEncoded
    @POST("livestreams/startstream")
    Call<StartStreamResponse> startStreamNew( @Header("Authorization") String user_token,@Field("publisher_id") String publisherId, @Field("title") String title, @Field("name") String name, @Field("type") String type, @Field("stream_token") String stream_token,
                                             @Field("invite_link") String invite_link, @Field("recording") String recording);

    @FormUrlEncoded
    @POST("livestreams/streamdetails")
    Call<com.app.addon.livestream.external.model.StreamDetails> getStreamDetailsNew( @Header("Authorization") String user_token,@Field(com.app.addon.livestream.external.utils.Constants.TAG_USER_ID) String userId, @Field(com.app.addon.livestream.external.utils.Constants.TAG_NAME) String streamName);


    @FormUrlEncoded
    @POST("livestreams/stopstream")
    Call<Map<String, String>> stopStreamNew(@FieldMap Map<String, String> params);


    @FormUrlEncoded
    @POST("livestreams/stoppkstream")
    Call<Map<String,String>> stopPk(@FieldMap Map<String,String> params);


    @FormUrlEncoded
    @POST("livestreams/updatestream")
    Call<CommonResponse> updateStream(@Field("userid") String publisherId,
                                      @Field("type") String type,
                                      @Field("stream_id") String stream_id);

    @POST("activities/follow")
    Call<FollowResponse> followUser2(@Body FollowRequest followRequest);



    @FormUrlEncoded
    @POST("livestreams/initiatebattle")
    Call<Map<String,String>> initiateBattle(@FieldMap Map<String,String> params);

    @POST("livestreams/publishersinglelivelist")
    Call<StreamersList> getStreamersList(/*@Header("Authorization") String user_token,*/
            @Field("userid") String publisherId,
            @Field("stream_id") String stream_id);

    @FormUrlEncoded
    @POST("livestreams/createstream")
    Call<CreateStreamModel> getLiveStreamName(@Header("auth") String user_token,
                                              @Field("publisher_id") String publisherId,
                                              @Field("type") String type);



    @POST("accounts/profile")
    Call<ProfileResponse> getProfilee(@Body ProfileRequest request);



    @FormUrlEncoded
    @POST("activities/cohostlist")
    Call<CohostListModel> cohostlist(@Field("user_id") String user_id, @Field("stream_name") String stream_name);

    @FormUrlEncoded
    @POST("livestreams/stopstream")
    Call<Map<String, String>> stopStream(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("livestreams/stopstream")
    Call<CommonResponse> stopStream(@Header("Authorization") String user_token, @Field("name") String streamName, @Field("TaskId") String taskId,  @Field("publisher_id") String publisherName);


    @GET("livestreams/streamtoken/{userId}")
    Call<HashMap<String, String>> streamtoken(@Header("Authorization") String user_token, @Path("userId") String publisherId);



    @Multipart
    @POST("livestreams/uploadstream")
    Call<Map<String, String>> uploadStreamImage(@Header("Authorization") String user_token, @Part MultipartBody.Part image, @Part("publisher_id") RequestBody publisherId, @Part("video_id") RequestBody videoId, @Part("name") RequestBody streamName);



    @POST("livestreams")
    Call<com.app.addon.livestream.external.model.LiveStreamResponse> getCurrentStreams(@Header("Authorization") String user_token, @Body com.app.addon.livestream.external.model.LiveStreamRequest request);



    @FormUrlEncoded
    @POST("livestreams/streamdetails")
    Call<com.app.addon.livestream.external.model.StreamDetails> getStreamDetails(@Header("Authorization") String user_token, @Field(com.app.addon.livestream.external.utils.Constants.TAG_USER_ID) String userId, @Field(com.app.addon.livestream.external.utils.Constants.TAG_NAME) String streamName);

    @DELETE("livestreams/deletestream/{user_id}/{name}")
    Call<Map<String, String>> deleteVideo(@Header("Authorization") String user_token, @Path(com.app.addon.livestream.external.utils.Constants.TAG_USER_ID) String userId, @Path(com.app.addon.livestream.external.utils.Constants.TAG_NAME) String streamName);

    @FormUrlEncoded
    @POST("livestreams/reportstream")
    Call<Map<String, String>> reportStream(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @GET("livestreams/createstream/{userId}")
    Call<HashMap<String, String>> getStreamName(@Header("Authorization") String user_token, @Path("userId") String publisherId);

    //new api for getting reports list like tik tok
    @GET("livestreams/reportList")
    Call<ReportResponceNew> getReportList( @Query("lang") String language);

    @POST("livestreams/report")
    Call<Map<String, String>> reportVideoNew( @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("livestreams/startstream")
    Call<StartStreamResponse> startStream(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @GET("activities/watchhistory/{user_id}/{offset}/{limit}")
    Call<LiveStreamResponse> getRecentWatched2(@Path("user_id") String userId, @Path("limit") String limit, @Path("offset") String offset);

    @POST("accounts/getotp")
    Call<SignInResponse> callSignOtp(@Body SignInRequest request);

    @POST("accounts/verifyotp")
    Call<SignInResponse> verifyOtp(@Body SignInRequest request);

    @FormUrlEncoded
    @POST("devices/devicelanguage")
    Call<CommonResponse> changeLangugae(@Header("Authorization") String user_token,
                                        @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("livestreams/repost")
    Call<RepostResponse> repostVideo(@Header("Authorization") String user_token,
                                     @Field("video_id") String videoId,
                                     @Field("repost_userid") String userId,
                                     @Field("repost_action") String action,
                                     @Field("repost_desc") String desc,
                                     @Field("repost_id") String repostId
    );

    @FormUrlEncoded
    @POST("livestreams/recentusers")
    Call<RecentUser> getRecentUsers(
            @FieldMap Map<String, String> request
    );

    @FormUrlEncoded
    @POST("livestreams/sentvideo")
    Call<CommonResponse> sentVideo(@Header("Authorization") String user_token,
                                     @Field("video_id") String videoId,
                                     @Field("user_id") String userId,
                                     @Field("receiver_id") String receiverId
    );
}
