package com.app.addon.livestream.external.utils;



/**
 * Created by hitasoft on 12/3/18.
 */

public class Constants {

    public static final int PUBLISH_REQUEST_CODE = 502;

    // socket for live streaming
    public static final String USER_TYPE = "user_type";
    public static final String TAG_RECORDED = "recorded";
    public static final int MUTUAL_REQUEST_CODE = 500;
    public static final String TAG_PREMIUM_MEMBER = "premium_member";
    /*Used for Intent and other purpose*/
    public static final String TAG_LANGUAGE_CODE = "language_code";
    public static final String TAG_TRANSLATE_LANGUAGE_CODE = "translate_language_code";
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_FRENCH = "fr";
    public static final String TAG_DEFAULT_LANGUAGE_CODE = LANGUAGE_ENGLISH;
    public static final String TAG_TITLE = "title";
    public static final String TAG_ADMIN = "admin";
    public static final String TAG_DEVICE_TYPE = "1";
    public static final String TAG_FACEBOOK = "facebook";
    public static final String TAG_REPORT = "report";
    public static final String TAG_FROM = "from";
    public static final String TAG_VIDEO = "video";
    public static final String TAG_SHARE = "share";
    public static final String TAG_PHONENUMBER = "phonenumber";

    // Table column name:
    public static final String TAG_STATUS = "status";
    public static final String TAG_Stream_Token = "stream_token";

    public static String TAG_USER_NAME = "user_name";
    public static String TAG_USER_IMAGE = "user_image";
    public static final String TAG_USER_ID = "user_id";
    public static String TAG_RECEIVER_ID = "receiver_id";
    public static String TAG_MESSAGE = "message";
    public static String TAG_TYPE = "type";
    public static String TAG_SUCCESS = "success";
    public static final String TAG_IMAGE = "image";
    public static final String TAG_HISTORY = "history";
    public static final String TAG_TEXT = "text";
    public static final String TAG_VISIBILITY = "visibility";
    public static final String TAG_TRUE = "true";
    public static final String TAG_FALSE = "false";
    public static final String TAG_NAME = "name";
    public static final String TAG_OTHER_PROFILE = "other_profile";
    public static final String TAG_RECENT = "recent";
    public static final int MAX_LIMIT = 20;
    public static final String TAG_USER = "user";
    public static final String TAG_LIVE = "live";
    public static final String TAG_LIVE_STREAMING = "livestreaming";
    public static final String TAG_SUBSCRIBE = "subscribe";
    public static final String TAG_HOST_ID = "host_id";

    /* minute * 1000 * seconds * milliseconds */
    public static final int CAMERA_REQUEST_CODE = 111;
    public static final int STREAM_REQUEST_CODE = 114;

    public static boolean isExternalPlay = false;
    public static boolean isInStream = false;

    public static String SHARED_PREFERENCE = "SavedPref";
    public static final String TAG_GIFT_TITLE = "gift_title";
    public static final String TAG_PARTNER_ID = "partner_id";
    public static final String TAG_PARTNER_NAME = "partner_name";
    public static final String TAG_PARTNER_IMAGE = "partner_image";
    public static final String IS_VIDEO_DELETED = "isVideoDeleted";
    public static final String TAG_VIDEO_TRIM = "videorecord";
    public static final String TAG_MIX_VIDEO = "audiomixvideo";
    public static final String TAG_MOV_AUDIO = "movaudio";
    public static final String TAG_VIDEO_THUMBNAIL = "coverpic";
    public static final String TAG_SINGLE_VIDEO = "singleVideo";
    public static final String TAG_GIFT = "gift";
    public static final String TAG_COHOST_BLOCKED = "cohostblocked";
    public static final String TAG_GIFT_TO = "gift_to";
    public static final String TAG_GIFT_ICON = "gift_icon";
    public static final String TAG_ICON_TYPE = "icon_type";

    public static final String TAG_GIFT_SVG_ICON = "gft_svga_icon";

    public static final String TAG_GIFT_ID = "gift_id";
    public static final String TAG_TOTAL_GEMS = "total_gems";
    //public static final String APP_SHARE_URL = SITE_URL;
    public static final String TAG_HAND_RAISE = "handraise";

    public static final String TAG_HAND_RAISE_REJECT = "handraise_cancel";
    public static final String TAG_HAND_RAISE_ACCEPT = "handraise_accepted";
    public static final String TAG_DEACTIVATED="accountblocked";
    public static final String TAG_GEMS_COUNT = "gems_count";
    public static final String TAG_FOLLOW_USER = "Follow";
    public static final String TAG_UNFOLLOW_USER = "Unfollow";

    public static final String TAG_GIFT_POSITION = "gift_position";
    public static final String TAG_GIFT_FROM = "gift_from";
    public static final String TAG_URL = "url";
    public static final String TAG_FILE_NAME = "file_name";
    public static final String TAG_OUTPUT_PATH = "output_path";

    public static final String TAG_ANDROID = "android";
    public static final String  TAG_MULTIPLY_GEM_COUNT = "multiply_gem_count";


    public static final String TAG_BATTLE_ID = "battle_id";
    public static final String TAG_STREAM_NAME = "stream_name";
    public static final String  TAG_OPPONENT_STREAM_NAME = "opponent_stream_name";

    public static final String TAG_TOKEN = "token";

    public static final String TAG_STREAM_BASE_URL = "stream_base_url";
    public static boolean isSendInvite = false;

    //Added for sep9 for livestreaming
    public static final String TAG_AUTHORIZATION = "Authorization";
    public static final String TAG_TEMP = "temp";
    public static final String TAG_PROFILE_IMAGE = "profile_image";
    public static final String IMAGE_EXTENSION = ".jpg";
    public static final String TAG_ACCOUNT_BLOCKED = "accountblocked";
}
