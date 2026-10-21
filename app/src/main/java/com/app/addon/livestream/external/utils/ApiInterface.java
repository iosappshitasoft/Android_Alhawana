package com.app.addon.livestream.external.utils;


import com.app.Zego.ApiBase.TokenAuthendicatior;
import com.app.Zego.Model.TokenResponse;
import com.google.gson.JsonObject;
import com.app.Zego.Model.CohostListModel;
import com.app.Zego.Model.CreateStreamModel;
import com.app.Zego.Model.LeaderBoardUser;
import com.app.Zego.Model.StreamersList;
import com.app.addon.livestream.external.model.LiveStreamRequest;
import com.app.addon.livestream.external.model.LiveStreamResponse;
import com.app.addon.livestream.external.model.StreamDetails;
import com.app.model.CommonResponse;
import com.app.model.FollowRequest;
import com.app.model.FollowResponse;
import com.app.model.GemsPurchaseRequest;
import com.app.model.GemsPurchaseResponse;
import com.app.model.GemsStoreResponse;
import com.app.model.StartStreamResponse;
import com.app.model.ProfileRequest;
import org.json.JSONArray;
import com.app.model.ProfileResponse;
import java.util.HashMap;
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
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * Created by hitasoft on 24/1/18.
 */

public interface ApiInterface {

    @GET("admindatas")
    Call<HashMap<String, String>> adminData();

    @FormUrlEncoded
    @POST("signin")
    Call<HashMap<String, String>> signin(@FieldMap Map<String, String> params);

/*    @FormUrlEncoded
    @POST("updatemycontacts")
    Call<ContactsData> updatemycontacts(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);*/


    @FormUrlEncoded
    @POST("updatemyprofile")
    Call<HashMap<String, String>> updatemyprofile(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @Multipart
    @POST("upmyprofile")
    Call<HashMap<String, String>> upmyprofile(@Header("Authorization") String user_token, @Part MultipartBody.Part image, @Part("user_id") RequestBody user_id);

    @Multipart
    @POST("upmychat")
    Call<HashMap<String, String>> upmychat(@Header("Authorization") String user_token, @Part MultipartBody.Part attachment, @Part("user_id") RequestBody user_id);

    @FormUrlEncoded
    @POST("pushsignin")
    Call<Map<String, String>> pushsignin(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("pushsignout")
    Call<Map<String, String>> pushsignout(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @GET("getuserprofile/{phone_no}/{contact_id}")
    Call<Map<String, String>> getuserprofile(@Header("Authorization") String user_token, @Path("phone_no") String phone_no, @Path("contact_id") String contact_id);

/*    @GET("recentchats/{user_id}")
    Call<RecentsData> recentchats(@Header("Authorization") String user_token, @Path("user_id") String user_id);

    @GET("getblockstatus/{user_id}")
    Call<BlocksData> getblockstatus(@Header("Authorization") String user_token, @Path("user_id") String user_id);*/

    @Multipart
    @POST("modifyGroupimage")
    Call<HashMap<String, String>> uploadGroupImage(@Header("Authorization") String user_token, @Part MultipartBody.Part image, @Part("group_id") RequestBody groupId);

/*    @GET("/groupinvites/{user_id}")
    Call<GroupInvite> getGroupInvites(@Header("Authorization") String token, @Path("user_id") String userId);

    @FormUrlEncoded
    @POST("groupinfo")
    Call<GroupResult> getGroupInfo(@Header("Authorization") String token, @Field("group_list") String group_list);*/
@POST("accounts/profile")
Call<ProfileResponse> getProfile(@Body ProfileRequest request);

    @FormUrlEncoded
    @POST("deviceinfo")
    Call<Map<String, String>> deviceinfo(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @Multipart
    @POST("upmychat")
    Call<Map<String, String>> upchat(@Header("Authorization") String user_token, @Part MultipartBody.Part attachment, @Part("user_id") RequestBody user_id);

    @Multipart
    @POST("upmygroupchat")
    Call<HashMap<String, String>> uploadGroupChat(@Header("Authorization") String user_token, @Part MultipartBody.Part attachment, @Part("user_id") RequestBody user_id);

    @Multipart
    @POST("upmygroupchat")
    Call<Map<String, String>> upGroupchat(@Header("Authorization") String user_token, @Part MultipartBody.Part attachment, @Part("user_id") RequestBody user_id);

    @FormUrlEncoded
    @POST("chatreceived")
    Call<Map<String, String>> chatreceived(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

/*    @FormUrlEncoded
    @POST("modifyGroupinfo")
    Call<GroupUpdateResult> updateGroup(@Header("Authorization") String user_token, @Field("group_id") String groupId,
                                        @Field("group_name") String groupName);

    @FormUrlEncoded
    @POST("modifyGroupinfo")
    Call<GroupUpdateResult> updateGroup(@Header("Authorization") String user_token, @Field("group_id") String groupId,
                                        @Field("group_members") JSONArray members);

    @GET("recentgroupchats/{user_id}")
    Call<GroupChatResult> getRecentGroupChats(@Header("Authorization") String token, @Path("user_id") String userId);

    @FormUrlEncoded
    @POST("savemycontacts")
    Call<SaveMyContacts> saveMyContacts(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);*/

    @FormUrlEncoded
    @POST("updatemyprivacy")
    Call<HashMap<String, String>> updateMyPrivacy(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("phonenumberprivacy")
    Call<HashMap<String, String>> setphonenumberrprivacy(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("modifyGroupmembers")
    Call<HashMap<String, String>> modifyGroupmembers(@Header("Authorization") String user_token, @Field("user_id") String userId, @Field("group_id") String groupId,
                                                     @Field("group_members") JSONArray members);

/*    @GET("MySubscribedChannels/{user_id}")
    Call<ChannelResult> getMySubscribedChannels(@Header("Authorization") String user_token, @Path("user_id") String userId);

    @GET("MyChannels/{user_id}")
    Call<ChannelResult> getMyChannels(@Header("Authorization") String user_token, @Path("user_id") String userId);

    @GET("helps")
    Call<HelpData> getHelpList();*/

    @Multipart
    @POST("modifyChannelImage")
    Call<HashMap<String, String>> uploadChannelImage(@Header("Authorization") String user_token, @Part MultipartBody.Part body, @Part("channel_id") RequestBody channelID);

/*    @GET("recentcalls/{user_id}")
    Call<CallData> recentcalls(@Header("Authorization") String user_token, @Path("user_id") String user_id);

    @FormUrlEncoded
    @POST("channelinfo")
    Call<ChannelResult> getChannelInfo(@Header("Authorization") String user_token, @Field("channel_list") JSONArray channelList);*/

    @FormUrlEncoded
    @POST("updatemychannel")
    Call<HashMap<String, String>> updateChannel(@Header("Authorization") String user_token, @FieldMap HashMap<String, String> map);

    @Multipart
    @POST("upmychannelchat")
    Call<HashMap<String, String>> uploadChannelChat(@Header("Authorization") String user_token, @Part MultipartBody.Part attachment, @Part("channel_id") RequestBody channel_Id, @Part("user_id") RequestBody userId);

    @Multipart
    @POST("upmychannelchat")
    Call<Map<String, String>> upChannelChat(@Header("Authorization") String user_token, @Part MultipartBody.Part attachment, @Part("channel_id") RequestBody channel_Id, @Part("user_id") RequestBody userId);

/*    @GET("recentChannelChats/{user_id}")
    Call<ChannelChatResult> recentChannelChats(@Header("Authorization") String user_token, @Path("user_id") String user_id);

    @GET("adminchannels/{user_id}")
    Call<AdminChannel> getAdminChannels(@Header("Authorization") String user_token, @Path("user_id") String user_id);

    @GET("msgfromadminchannels/{timestamp}")
    Call<AdminChannelMsg> getMsgFromAdminChannels(@Header("Authorization") String user_token, @Path("timestamp") String timestamp);

    @GET("recentChannelInvites/{user_id}")
    Call<ChannelResult> getRecentChannelInvites(@Header("Authorization") String user_token, @Path("user_id") String user_id);

    @GET("AllPublicChannels/{user_id}/{search_string}/{offset}/{limit}")
    Call<ChannelResult> getAllPublicChannels(@Header("Authorization") String user_token, @Path("user_id") String user_id, @Path("search_string") String search, @Path("offset") String offSet, @Path("limit") String limit);

    @GET("channelSubscribers/{channel_id}/{phone_no}/{offset}/{limit}")
    Call<ContactsData> getChannelSubscribers(@Header("Authorization") String user_token, @Path("channel_id") String channel_id, @Path("phone_no") String phoneNo, @Path("offset") String offSet, @Path("limit") String limit);

    @GET("MyGroups/{user_id}")
    Call<GroupResult> getMyGroups(@Header("Authorization") String user_token, @Path("user_id") String userId);*/

    @GET("deleteMyAccount/{user_id}")
    Call<HashMap<String, String>> deleteMyAccount(@Header("Authorization") String user_token, @Path("user_id") String userId);

    @GET("getphonenumberprivacy/{user_id}")
    Call<HashMap<String, String>> getphonenumberprivacy(@Header("Authorization") String user_token, @Path("user_id") String userId);


    @GET("verifyMyNumber/{user_id}/{phone_no}")
    Call<Map<String, String>> verifyNewNumber(@Header("Authorization") String user_token, @Path("user_id") String userId, @Path("phone_no") String phoneNumber);


   /* @GET("changeMyNumber/{user_id}/{phone_no}/{country_code}")
    Call<ChangeNumberResult> changeMyNumber(@Header("Authorization") String user_token, @Path("user_id") String userId, @Path("phone_no") String phoneNumber, @Path("country_code") String countryCode);

    @GET("checkforupdates")
    Call<CheckForUpdateResponse> checkForUpdates();*/

    @FormUrlEncoded
    @POST("reportchannel")
    Call<HashMap<String, String>> reportChannel(@Header("Authorization") String user_token, @FieldMap HashMap<String, String> hashMap);

    @POST("livestreams")
    Call<LiveStreamResponse> getCurrentStreams(@Header("Authorization") String user_token, @Body LiveStreamRequest request);

    @POST("livestreams")
    Call<LiveStreamResponse> getCurrentStreams(/*@Header("Authorization") String user_token,*/ @Body LiveStreamRequest request);


    @FormUrlEncoded
    @POST("livestreams/streamdetails")
    Call<StreamDetails> getStreamDetails(@Header("Authorization") String user_token, @Field(Constants.TAG_USER_ID) String userId, @Field(Constants.TAG_NAME) String streamName);

    @DELETE("livestreams/deletestream/{user_id}/{name}")
    Call<Map<String, String>> deleteVideo(@Header("Authorization") String user_token, @Path(Constants.TAG_USER_ID) String userId, @Path(Constants.TAG_NAME) String streamName);

    @FormUrlEncoded
    @POST("livestreams/reportstream")
    Call<Map<String, String>> reportStream(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @GET("livestreams/createstream/{userId}")
    Call<HashMap<String, String>> getStreamName(@Header("Authorization") String user_token, @Path("userId") String publisherId);

    @FormUrlEncoded
    @POST("livestreams/startstream")
    Call<StartStreamResponse> startStream(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("livestreams/stopstream")
    Call<Map<String, String>> stopStream(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);


    @GET("livestreams/streamtoken/{userId}")
    Call<HashMap<String, String>> streamtoken(@Header("Authorization") String user_token, @Path("userId") String publisherId);



    @Multipart
    @POST("livestreams/uploadstream")
    Call<Map<String, String>> uploadStreamImage(@Header("Authorization") String user_token, @Part MultipartBody.Part image, @Part("publisher_id") RequestBody publisherId, @Part("video_id") RequestBody videoId, @Part("name") RequestBody streamName);

/*    @GET("termsandpolicy")
    Call<TermsAndPrivacyResponse> termsAndPrivacy();

    @GET("wallet/{userId}/")
    Call<WalletResponse> myWallet(@Header("Authorization") String userToken,
                                  @Path("userId") String userId);

    @GET("walletHistory/{userId}/{offset}/{limit}/")
    Call<WalletHistoryResponse> walletHistory(@Header("Authorization") String userToken,
                                              @Path("userId") String userId,
                                              @Path("offset") String offset,
                                              @Path("limit") String limit);

    @FormUrlEncoded
    @POST("withdrawRequest")
    Call<WithdrawRequestResponse> withdrawRequest(@Header("Authorization") String userToken,
                                                  @FieldMap Map<String, String> map);*/

    @GET("admincallmonetization/{userId}")
    Call<JsonObject> adminCallMonetization(@Header("Authorization") String token,
                                           @Path("userId") String userId);

    @GET("activities/watchhistory/{user_id}/{offset}/{limit}")
    Call<LiveStreamResponse> getRecentWatched(@Path("user_id") String userId, @Path("limit") String limit, @Path("offset") String offset);

    @DELETE("activities/clearwatchhistory/{user_id}")
    Call<Map<String, String>> clearWatched(@Path(Constants.TAG_USER_ID) String userId);





    @POST("activities/follow")
    Call<FollowResponse> followUser(@Body FollowRequest followRequest);

    @FormUrlEncoded
    @POST("activities/cohostlist")
    Call<CohostListModel> cohostlist(@Field("user_id") String user_id, @Field("stream_name") String stream_name);


    @FormUrlEncoded
    @POST("livestreams/contributor_leaderboard")
    Call<LeaderBoardUser> contributorleaderboard(@Field("user_id") String user_id,
                                                 @Field("type") String type,
                                                 @Field("battle_id") String battle_id,
                                                 @Field("stream_id") String stream_id,
                                                 @Field("page_type") String page_type);


    @POST("payments/purchasegems")
    Call<GemsPurchaseResponse> buyGems(@Body GemsPurchaseRequest request);

    @FormUrlEncoded
    @POST("livestreams/streamdetails")
    Call<StreamDetails> getStreamDetailsNew( @Field(Constants.TAG_USER_ID) String userId, @Field(Constants.TAG_NAME) String streamName);


    @FormUrlEncoded
    @POST("livestreams/startstream")
    Call<StartStreamResponse> startStreamNew(@Field("publisher_id") String publisherId,@Field("title") String title, @Field("name") String name, @Field("type") String type, @Field("stream_token") String stream_token,
                                             @Field("invite_link") String invite_link, @Field("recording") String recording);




    @FormUrlEncoded
    @POST("livestreams/stopstream")
    Call<Map<String, String>> stopStreamNew(@FieldMap Map<String, String> params);




    @FormUrlEncoded
    @POST("livestreams/updatestream")
    Call<CommonResponse> updateStream(@Field("userid") String publisherId,
                                      @Field("type") String type,
                                      @Field("stream_id") String stream_id);

    @FormUrlEncoded
    @POST("livestreams/stoppkstream")
    Call<Map<String,String>> stopPk(@FieldMap Map<String,String> params);
    @FormUrlEncoded

    @POST("livestreams/publishersinglelivelist")
    Call<StreamersList> getStreamersList(@Header("Authorization") String user_token,
            @Field("userid") String publisherId,
            @Field("stream_id") String stream_id);


    @GET("activities/gemstore/{user_id}/{platform}/{offset}/{limit}")
    Call<GemsStoreResponse> getGemsList(@Header("Authorization") String user_token, @Path("user_id") String userId, @Path("platform") String platform, @Path("offset") int offset, @Path("limit") int limit);



    @FormUrlEncoded
    @POST("livestreams/initiatebattle")
    Call<Map<String,String>> initiateBattle(@FieldMap Map<String,String> params);


    @FormUrlEncoded
    @POST("livestreams/createstream")
    Call<CreateStreamModel> getLiveStreamName(@Header("auth") String user_token,
                                              @Field("publisher_id") String publisherId,
                                              @Field("type") String type);

    @POST("accounts/refresh-token")
    Call<TokenAuthendicatior.TokenResponse> refreshToken(@Body TokenAuthendicatior.TokenRequest request);


    @POST("livestreams")
    Call<LiveStreamResponse> getCurrentStreams1(@Header("Authorization") String user_token, @Body LiveStreamRequest request);


    @GET("livestreams/streamtoken/{user_id}")
    Call<TokenResponse> getStreamToken(@Path("user_id") String userId);

    @FormUrlEncoded
    @POST("livestreams/createlivestream")
    Call<CreateStreamModel> getLiveStreamName(@Field("publisher_id") String publisherId,
                                              @Field("type") String type);


    @FormUrlEncoded
    @POST("livestreams/startstream")
    Call<StartStreamResponse> startStream( @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("livestreams/updatestreamdynamic")
    Call<CommonResponse> updateStreamDynamic(@Field("userid") String publisherId,
                                             @Field("type") String type,
                                             @Field("stream_id") String stream_id,
                                             @Field("opp_host_streamId") String opp_host_streamId,
                                             @Field("opp_host_id") String opp_host_id,
                                             @Field("battle_id") String battle_id,
                                             @Field("pk_id") String pk_id,
                                             @Field("list_opp_host") String list_opp_host,
                                             @Field("main_stream_name") String mainStreamName,
                                             @Field("user_type") String userType,
                                             @Field("user_name") String userName);

    @FormUrlEncoded
    @POST("livestreams/getallviewers")
    Call<StreamDetails> getStreamViewersInfo(@Field("stream_name") String stream_name,
                                             @Field("user_id") String user_id);

    @POST("payments/androidverifyReceipt")
    Call<GemsPurchaseResponse> androidverifyReceipt(@Body GemsPurchaseRequest request);


    @FormUrlEncoded
    @POST("livestreams/getstreamInfo")
    Call<StreamDetails> getStreamInfo(@Field("user_id") String user_id,
                                      @Field("name") String stream_name,
                                      @Field("user_type") String user_type,
                                      @Field("live_type") String live_type,
                                      @Field("main_stream_name") String main_stream_name);
}


