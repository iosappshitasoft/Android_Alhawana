package com.app.utils;

public class Constants {

   //live
   /* public static final String SITE_URL = "https://service.alhawanajd.com";
    public static final String API_URL = SITE_URL + "/api/";
    public static final String CHAT_SOCKET_URL = "wss://service.alhawanajd.com/ws/";
//    public static final String CHAT_SOCKET_URL = "wss://services.hitasoft.in:3022/";
    public static final String PUSHER_KEY = "2ab45da73dc7ad42ccf9";
    public static final String PUSHER_BASE_URL = "https://service.alhawanajd.com/pusher/";
    public static final String PUSHER_AUTH_URL = "https://service.alhawanajd.com/pusher/user-auth";*/

    //dev
    public static final String SITE_URL = "https://dev.alhawanajd.com";
    public static final String API_URL = SITE_URL  + "/api/"  ;
    public static final String CHAT_SOCKET_URL = "wss://dev.alhawanajd.com/ws/";
    public static final String PUSHER_KEY = "2ab45da73dc7ad42ccf9";
    public static final String PUSHER_BASE_URL = "https://dev.alhawanajd.com/pusher/";
    public static final String PUSHER_AUTH_URL = "https://dev.alhawanajd.com/pusher/user-auth";


    //cmyshots
   /*  public static final String SITE_URL = "https://cmyshots.com:3000";
    public static final String API_URL = SITE_URL  + "/"  ;
    public static final String CHAT_SOCKET_URL = "wss://cmyshots.com:8087/";
    public static final String PUSHER_KEY = "2b21b24230bd9e3f7900";
    public static final String PUSHER_BASE_URL = "https://cmyshots.com:8083/pusher/";
    public static final String PUSHER_AUTH_URL = "https://cmyshots.com:8083/pusher/user-auth";*/


    /**
     For publish and subscribe
     */
    public static final String IMAGE_DIRECTORY = "";
    public static final String APPRTC_URL = API_URL + SITE_URL;
    public static String Login_method = "default_key";
    public static final String SOCKET_IO_URL = SITE_URL + "8082";//Port number

    public static final String GIFT_IMAGE_URL = SITE_URL + IMAGE_DIRECTORY + "/public/img/gifts/";
    public static final String PRIME_IMAGE_URL = "https://alhawanajd.s3.us-west-1.amazonaws.com"+ "/img/slider/";
   // public static final String USER_IMAGE_URL = SITE_URL + IMAGE_DIRECTORY + "/public/img/accounts/";
    public static final String USER_IMAGE_URL = "https://d3pqf70msuvtmp.cloudfront.net/img/accounts/";


    /**
     * Dynamic Link Share URL
     */
    public static final String APP_SHARE_URL = SITE_URL;

    /**
     * OneSignal App ID for push notification
     */

    public static final String ONESIGNAL_APP_ID = "";

    /**
     * Addon Live Stream
     */
    public static final boolean ADDON_LIVE_STREAM = false;
    /**
     * Addon Audio Call
     */
    public static final boolean ADDON_AUDIO_CALL = false;
    /**
     * Addon Chat Translation
     */
    public static final boolean ADDON_CHAT_TRANSLATION = false;
    /**
     * Addon Chat Translation
     */
    public static final boolean ADDON_VOICE_CHAT = false;
    /**
     * Addon Gif
     */
    public static final boolean ADDON_GIF = false;
    /**
     * Addon Smart Reply
     */
    public static final boolean ADDON_SMART_REPLY = false;
    /**
     * Addon Interstitial Ad
     */
    public static final boolean ADDON_INTERSTITIAL_AD = true;
    /**
     * Addon Login Video
     */
    public static final boolean ADDON_LOGIN_VIDEO = false;

    public static final boolean RANDOU_ENABLED = false;

    public static final long TYPING_DELAY = 1000L;

    public static final String TAG_SELECT_HASH_TAG = "Select hashtag";
    public static boolean overLaysDialogShown = false;

    public static final boolean WATERMARK_ENABLED = false;
    public static boolean INAPP_UPDATE = false;

    public static boolean isInVideoCall = false ,isInStream = false, isInRandomCall = false;

    public static final String TAG_HTTP = "http";
    public static final String TAG_AUTHORIZATION = "Authorization";
    public static final String TAG_LANGUAGE_CODE = "language_code";
    public static final String TAG_LANGUAGE = "language";
    public static String DEFAULT_LANGUAGE = "English";
    public static final String DEFAULT_LANGUAGE_CODE = "en";
    public static final String DEFAULT_SUBSCRIPTION = "$ 99";
    public static final String DEFAULT_VALIDITY = "1M";
    public static final String TAG_ID = "id";
    public static final String TAG_EMAIL = "mail_id";
    public static final String TAG_TAGID = "tag_id";
    public static final String TAG_USER_ID = "user_id";
    public static final String TAG_HOST_ID = "host_id";
    public static final String TAG_USER_NAME = "user_name";
    public static final String TAG_USER_IMAGE = "user_image";
    public static final String TAG_PROFILE_IMAGE = "profile_image";
    public static final String TAG_CHAT_IMAGE = "chat_image";
    public static final String TAG_FIRST_NAME = "first_name";

    public static final String TAG_LAST_NAME = "last_name";

    public static final String TAG_IMAGE_URL = "image_url";

    public static final String TAG_IS_FROM_LINK = "is_from_link";
    public static final String TAG_FROM = "from";
    public static final String TAG_PROFILE_ID = "profile_id";
    public static final String TAG_TRUE = "true";
    public static final String TAG_FALSE = "false";
    public static final String TAG_REJECTED = "rejected";
    public static final String TAG_ACCOUNT_BLOCKED = "accountblocked";
    public static final String TAG_DEACTIVATED="accountblocked";

    public static final String TAG_MALE = "male";
    public static final String TAG_FEMALE = "female";
    public static final String TAG_BOTH = "both";
    public static final String TAG_STATUS = "status";
    public static final String TAG_MESSAGE = "message";
 public static final String TAG_OUT_OF_BALANCE_STATUS = "out_of_blance";
    public static final String TAG_PHONENUMBER = "phonenumber";
    public static final String TAG_EDIT = "edit";
    public static final String TAG_FACEBOOK = "facebook";
    public static final String TAG_DEVICE_TYPE = "1";
    public static final String TAG_PLATFORM = "platform";
    public static final String TAG_ANDROID = "android";
    public static final String TAG_REPORT = "report";
    public static final String TAG_ADDIDTOFAV = "add_id_to_favorite";
    public static final String TAG_FAV_TYPE = "favorite_type";
    public static final String TAG_UNN_FAV = "Unfavoured successfully";
    public static final String TAG_SOUND_FAV = "Favoured successfully";
    public static final String TAG_AUDIO_TRIM = "audiotrim";
    public static final String TAG_VIDEO_TRIM = "videorecord";
    public static final String TAG_VIDEO_AUDIO_TRIM = "videoaudiorecord";
    public static final String TAG_MIX_VIDEO = "audiomixvideo";
    public static final String TAG_VIDEO_THUMBNAIL = "coverpic";
    public static final String TAG_SINGLE_VIDEO = "singleVideo";

    public static final String TAG_SONG_TITLE = "songTitle";
    public static final String TAG_SONG_DURATION = "songDuration";
    public static final String TAG_SONG_ALBUM = "songAlbum";


    public static final String TAG_REPORT_CATEGORY = "report_category";
    public static final String TAG_REPORT_SUBCATEGORY = "report_subcategory";


    // JSON constant keys
    public static final String TAG_RESULT = "result";
    public static final String TAG_TOKEN = "token";
    public static final String TAG_FULL_NAME = "full_name";
    public static final String TAG_FIRST_LOGIN = "first_time_logged";
    public static final String TAG_FOLLOWERS_COUNT = "followers_count";
    public static final String TAG_FOLLOWING_COUNT = "following_count";
    public static final String TAG_BLOCKED_COUNT = "blocked_count";
    public static final String TAG_NOTIFICATION_COUNT = "notification_count";
    public static final String TAG_FOLLOWING = "following";
    public static final String TAG_FOR_YOU = "foryou";
    public static final String TAG_RELATED = "related";

    /*Socket Constants*/
    public static final String TAG_BIO = "bio";
    public static final String TAG_NAME = "name";
    public static final String TAG_GENDER = "gender";
    public static final String TAG_LOCATION = "location";
    public static final String TAG_FOLLOW = "follow";
    public static final String TAG_FOLLOW_USER = "Follow";
    public static final String TAG_UNFOLLOW_USER = "Unfollow";
    public static final String TAG_FOLLOWERS = "followers";
    public static final String TAG_FOLLOWINGS = "followings";
    public static final String TAG_INTEREST = "interest";
    public static final String TAG_MATCH = "match";
    public static final String TAG_FOLLOWFROM_OTHERS = "follow_from_others";
    public static final String TAG_FOLLOWER_ON_LIVE = "followeronlive";
    public static final String TAG_FOLLOWER_ON_POST = "followerpostvideo";
    public static final String TAG_STREAM_INVITATION = "streaminvitation";
    public static final String TAG_STREAM_NAME = "stream_name";
    public static final String TAG_LIKE = "like";
    public static final String TAG_REPOST = "repost";
    public static final String TAG_TYPE = "type";
    public static final String TAG_VISIBILITY = "visibility";
    public static String LOGINMETHODE = "";
    public static String LOGIN_NAME = "";
    public static final String TAG_SEARCH_MAIN_PAGE = "search_mainpage";
    public static final String TAG_SEARCH_USER = "username";
    public static final String TAG_SEARCH_VIDEO = "video";
    public static final String TAG_SEARCH_SOUND = "sound";
    public static final String TAG_SEARCH_HASH_TAG = "hashtag";
    public static final String TAG_LIVE = "live";
    public static final String TAG_DISCOVER = "discover";
    public static final String TAG_FAV = "favourite";
    public static final String TAG_RECORDED = "recorded";
    public static final String TAG_PARTNER_ID = "partner_id";
    public static final String TAG_RECEIVE_CHAT = "_receiveChat";
    public static final String TAG_SEND_CHAT = "_sendChat";
    public static final String TAG_CREATE_CALL = "_createCall";
    public static final String TAG_CALL_RECEIVED = "_callReceived";
    public static final String TAG_OFFLINE_CHATS = "_offlineChat";
    public static final String TAG_USER_TYPING = "_userTyping";
    public static final String TAG_lISTEN_TYPING = "_listenTyping";
    public static final String TAG_BLOCK_USER = "_blockUser";
    public static final String TAG_BLOCK_USER_ID = "block_user_id";
    public static final String TAG_BLOCK_STATUS = "block_status";
    public static final String _ONLINE_LIST = "_onlineList";
    public static final String TAG_REPORTBY = "report_by";
    public static final String TAG_ONLINE_LIST_STATUS = "_onlineListStatus";
    public static final String TAG_PROFILE_LIVE = "_profileLive";
    public static final String TAG_PROFILE_STATUS = "_profileStatus";
    public static final String TAG_NOTIFY_USER = "_userNotify";
    public static final String TAG_LISTEN_USER = "_listenNotify";
    public static final String TAG_CALL_REJECTED = "_callRejected";
    public static final String TAG_ONLINE_STATUS = "online_status";
    public static final String TAG_MIN_AGE = "min_age";
    public static final String TAG_MAX_AGE = "max_age";
    public static final String TAG_BLOCKED = "blocked";
    public static final String TAG_BLOCKED_BY_ME = "blocked_by_me";
    public static final String TAG_TYPING_STATUS = "typing_status";
    public static final String TAG_RECORD_STATUS = "recording_status";
    public static final String TAG_TYPING = "typing";
    public static final String TAG_RECORDING = "recording";
    public static final String TAG_UNTYPING = "untyping";
    public static final String TAG_MYVIDEOS = "myvideos";
    public static final String TAG_LIKEDVIDEOS = "likedvideos";
    public static final String TAG_REPOSTED_VIDEO = "repostedvideos";
    public static final String TAG_BOOKMARKS_VIDEO = "bookmarkvideos";
    public static final String TAG_PRIVATE_VIDEO = "privatevideos";
    public static final String TAG_REPOSTEDVIDEOS = "repostedvideos";


    public static final String TAG_SOUND_TITLE = "title";
    public static final String TAG_SOUND_URL = "sound_url";
    public static final String TAG_SOUND_IS_FAV = "isFavorite";
    public static final String TAG_SOUND_COVER = "cover_image";
    public static final String TAG_SOUND_DURATION = "duration";

    public static final String TAG_EVERYONE = "Everyone";
    public static final String TAG_FRIENDS = "Friends";
    public static final String TAG_FANS = "Fans";
    public static final String TAG_OFF = "Off";

    public static final String TAG_STREAM_BASE_URL = "stream_base_url";


    public static final Integer MAX_AGE = 99;
    public static final String TAG_LIMIT = "limit";
    public static final String TAG_OFFSET = "offset";
    public static final Integer MIN_AGE = 13;
    public static final String OFFLINE = "0";
    public static final String ONLINE = "1";
    public static final String TAG_GIFT_ID = "gift_id";
    public static final String TAG_GEMS_EARNINGS = "gems_earnings";
    public static final String TAG_GEMS_COUNT = "gems_count";
    public static final String TAG_TOTAL_GEMS = "total_gems";
    public static final String TAG_GIFT_FROM = "gift_from";
    public static final String TAG_GIFT_TO = "gift_to";
    public static final String TAG_FILTER_LOCATION = "filter_location";
    public static final String TAG_FILTER_APPLIED = "filter_applied";
    public static final String TAG_LOCATION_SELECTED = "location_selected";
    public static final String TAG_FROM_FOREGROUND = "from_foreground";

    public static final String TAG_PROFILE_DATA = "profile_data";
    public static final String TAG_PROFILE = "profile";
    public static final int CAMERA_REQUEST_CODE = 111;
    public static final int STORAGE_REQUEST_CODE = 112;
    public static final int DOWNLOAD_REQUEST_CODE = 113;
    public static final int VOICE_REQUEST_CODE = 678;
    public static final int OVERLAY_REQUEST_CODE = 115;
    public static final int PRIME_REQUEST_CODE = 105;
    public static final int INTENT_REQUEST_CODE = 100;
    public static final int PROFILE_REQUEST_CODE = 200;
    public static final int PROFILE_OWN_SEARCH_CODE = 201;
    public static final int DEVICE_LOCK_REQUEST_CODE = 302;
    public static final int REQUEST_APP_UPDATE_IMMEDIATE = 400;
    public static final int REQUEST_APP_UPDATE_FLEXIBLE = 401;
    public static final int HASHTAG_REQUEST_CODE = 501;
    public static final int MUSIC_REQUEST_CODE = 503;
    public static final int OPEN_AUDIO_FILE_REQUEST_CODE = 506;
    public static final int MAIN_ACTIVITY_TO_CAMERA__REQUEST_CODE = 507;
    public static final int CAMERA_SOUND = 508;
    public static final int VIDEO_TRIMMER_CODE = 509;
    public static final int VIDEO_PRIVACY = 510;
    public static final int SETTING_POST_COMMAND = 511;
    public static final int SETTING_POST_MESSAGE = 512;
    public static final int COMMENT_MUSIC_REQUEST_CODE = 513;
    public static final int SHARE_REQUEST_CODE = 555;
    public static final int PROFILE_VIDEO_RESET = 603;
    public static final int NOTIFICATION_ACTIVITY = 606;
    public static final int CAMERA_POST_PAGE = 701;


    public static final int MAX_LENGTH = 30;
    public static final int MAX_LENGTH_NAME = 20;
    public static final String NOTIFICATION = "notification";
    public static final String TAG_GLOBAL = "global";
    public static final String TAG_COUNTRY = "country";
    public static final int MAX_LIMIT = 20;
    public static final String TAG_BROADCAST = "broadcast";
    public static final String TAG_SEARCH = "search";
    public static final String TAG_RECENT = "recent";
    public static String DEFAULT_SUBS_SKU = "become_prime";
    public static final String TYPE_STICKERS = "stickers";
    public static final String TYPE_GIFTS = "gifts";
    public static final String TAG_RECEIVER_ID = "receiver_id";
    public static final String TAG_CHAT_ID = "chat_id";
    public static final String TAG_MSG_ID = "msg_id";
    public static final String TAG_USER_CHAT = "user_chat";
    public static final String TAG_DATE = "date";
    public static final String TAG_SEND = "send";
    public static final String TAG_SENDING = "sending";
    public static final String TAG_COMPLETED = "completed";
    public static final String TAG_RECEIVE = "receive";
    public static final String TAG_RECEIVED = "received";
    public static final String TAG_SENT = "sent";
    public static final String TAG_CALL = "call";
    public static final String TAG_CALL_TYPE = "call_type";
    public static final String TAG_ROOM_ID = "room_id";
    public static final String TAG_CREATED_AT = "created_at";
    public static final String MESSAGE_CRYPT_KEY = "crypt@123";
    public static final String TAG_GEMS = "gems";
    public static final String TAG_GIFT = "gift";
    public static final String TAG_COMMENT = "comment";
    public static final String TAG_MENTION = "mention";
    public static final String TAG_CASH = "cash";
    public static final String TAG_OWN_PROFILE = "own_profile";
    public static final String TAG_OTHER_PROFILE = "other_profile";
    public static final String IMAGE_EXTENSION = ".jpg";
    public static final String TAG_SCOPE = "scope";
    public static final String TAG_UPDATE_LIVE = "_updateLive";
    public static final String TAG_RECEIVE_READ_STATUS = "_receiveReadStatus";
    public static final String TAG_OFFLINE_READ_STATUS = "_offlineReadStatus";
    public static final String TAG_UPDATE_READ = "_updateRead";


    public static String TAG_MSG_TYPE = "msg_type";
    public static String TAG_COVER_IMAGE = "cover_image";
    public static String TAG_MESSAGE_END = "message_end";
    public static String TAG_CHAT_TYPE = "chat_type";
    public static String TAG_RECENT_TYPE = "recent_type";
    public static String TAG_DELIVERY_STATUS = "delivery_status";
    public static String TAG_UNREAD_COUNT = "unread_count";
    public static String TAG_READ = "read";
    public static String TAG_UNREAD = "unread";
    public static String TAG_READ_STATUS = "read_status";
    public static String TAG_CHAT_TIME = "chat_time";
    public static String TAG_RECEIVED_TIME = "received_time";
    public static String TAG_TIMESTAMP = "timestamp";
    public static final String TAG_ADMIN = "admin";
    public static final String TAG_THUMBNAIL = "thumbnail";
    public static final String TAG_TEMP = "temp";
    public static final String TAG_PROGRESS = "progress";
    public static final String TAG_IMAGE = "image";
    public static final String TAG_TEXT = "text";
    public static final String TAG_AUDIO = "audio";
    public static final String TAG_VIDEO = "video";
    public static final String TAG_SOUND = "sound";
    public static final String TAG_URL = "url";
    public static final String TAG_FILE_PATH = "file_path";
    public static final String TAG_VIDEO_LIST = "video_list";
    public static final String TAG_VIDEO_ID = "video_id";
    public static final String TAG_VIDEO_PATH = "video_path";
    public static final String TAG_VIDEO_STATUS = "video_status";
    public static final String TAG_AUDIOCATEGORY = "audio_category";
    public static final String TAG_USE_AUDIO = "useaudio";
    public static final String TAG_FOLLOWER_ID = "follower_id";
    public static final String TAG_VIDEO_THUMBANIL = "video_thumbnail";
    public static final String TAG_COMMENT_OPEN = "videocommentOpen";
    public static final String TAG_PARTNER_NAME = "partner_name";
    public static final String TAG_PARTNER_IMAGE = "partner_image";
    public static final String TAG_TITLE = "title";
    public static final String TAG_DESCRIPTION = "description";
    public static final String TAG_CREATED = "created";
    public static final String TAG_ENDED = "ended";
    public static final String TAG_WAITING = "waiting";
    public static final String TAG_MISSED = "missed";
    public static final String CALL_TAG = "RandouCall";
    public static final String TAG_RECORDS = "records";
    public static final String TAG_TEXT_CHAT = "txtchat";
    public static final String TAG_POSTVIDEO_NOTIFICATION = "videoposted";
    public static final String TAG_VIDEO_CALL = "videocall";
    public static final String TAG_USERS_LIST = "users_list";
    public static final String TAG_PUBLISH = "publish";
    public static final String TAG_PUBLISHER_ID = "publisher_id";
    public static final String TAG_INTENT = "intent";
    public static final String TAG_INTENT_DATA = "intent_data";
    public static final String TAG_SOUND_DATA = "intent_data";
    public static final String TAG_SOUND_ID = "sound_id";
    public static final String TAG_COUNT = "count";
    public static final String TAG_SHARE = "share";
    public static final String TAG_HOME = "home";
    public static final String TAG_BANNER = "banners";
    public static final String TAG_USERS = "users";
    public static final String TAG_USER = "User";
    public static final String TAG_HASHTAG = "hashtag";
    public static final String TAG_HASHTAGS = "hashtags";
    public static final String TAG_VIDEOS = "videos";
    public static final String TAG_MUSIC = "music";
    public static final String TAG_VIEW = "view";
    public static final String TAG_RELATED_HASHTAG = "related_hashtag";

    public static final String TAG_FOR_YOU_PROFILE_UPDATE = "forYouProfileUpdate";
    public static final String TAG_FOLLOWING_PROFILE_UPDATE = "followingProfileUpdate";
    public static final String TAG_LIKED_VIDEOS = "likedVideos";
    public static final String TAG_OTHER_PROFILE_UPDATE = "otherProfileUpdate";

    public static final String TAG_OTHER_PROFILE_FANS_FOLLOWERS = "otherProfileToFansFollowing";
    public static final String TAG_NOTIFICATION_PAGE = "notification";

    public static final String FOR_YOU = "For You";
    public static final String TAG_THUMB = "thumb";
    public static final String TAG_VIDEO_SENT = "video_sent";

    public static final String TAG_SINGLE_TAP = "single_tap";
    public static final String TAG_DOWNLOAD = "download";
    public static final String TAG_PLAYBACK = "playback";
    public static final String TAG_PERMISSION = "permission";
    public static final String TAG_FILE_NAME = "file_name";
    public static final String TAG_OUTPUT_PATH = "output_path";
    public static final String TAG_DATA = "data";
    public static final String TAG_POSITION = "position";
    public static final String TAG_VIDEO_START = "video_start";
    public static final String TAG_VIDEO_END = "video_end";
    public static final String TAG_SIGNUP = "signup";
    public static final String TAG_MUTED = "Muted";
    public static final String TAG_DELETE = "delete";
    public static final String TAG_PUBLISH_MODE = "publish_mode";
    public static final String TAG_PRIVACY = "privacy";
    public static final String TAG_SUBSCRIBE = "subscribe";
    /**
     * ScreenLock Constants
     */
    public static final String SECRET_MESSAGE = "Very secret message";
    public static final String KEY_NAME_NOT_INVALIDATED = "key_not_invalidated";
    public static final String DEFAULT_KEY_NAME = "default_key";

    /**
     * Addon Voice message Constants
     */
    public static final String API_UPLOAD_AUDIO = API_URL + "uploadaudio";
    public static final String TAG_CHAT_AUDIO = "Fundoo";
    public static final String TAG_AUDIO_DURATION = "audio_duration";
    public static final String TAG_AUDIO_SENT = "audio_sent";
    public static final String TAG_AUDIO_RECEIVE = "audio_receive";
    public static final String AUDIO_EXTENSION = ".mp3";
    public static final String CHAT_IMAGE_URL = SITE_URL + IMAGE_DIRECTORY + "/public/img/chats/";

    /**
     * Addon Interstitial Ad
     */
    public static final String INTERSTITIAL_AD = "ca-app-pub-3940256099942544/1033173712";

    /**
     * Addon Emojis and gifs in chat
     */
    public static final String KEY_GIPHY_API = "or4yESJhLlDX0DHe65nAZxtYps4Ve5EM";
    public static final String TAG_GIF = "gif";
    public static String Mailid = "";

    /**
     * Addon Chat Translate
     */
    public static final String TAG_CHAT = "chat";
    public static final int LANGUAGE_REQUEST_CODE = 110;
   // public static final String CHAT_TRANSLATE_API_KEY = "AIzaSyCnwt_jj_Pc9UPtQ0mLRAZT6rxXQ3WGFIA";
    public static final String CHAT_TRANSLATE_API_KEY = "AIzaSyAcLN3iArCxK--jhLojAFNRtK6G56sO6Bo";
    public static final String TAG_DEFAULT_LANGUAGE_CODE = "en";
    public static final String TAG_MOV_AUDIO = "movaudio";

    public static final String[] STREAMS = new String[]{
            "https://appservices.hitasoft.in/fundoo_new/public/video/videodata-1653456042430/videodata-1653456042430.m3u8",
            "https://appservices.hitasoft.in/fundoo_new/public/video/videodata-1652794633629/videodata-1652794633629.m3u8",
            "https://appservices.hitasoft.in/fundoo_new/public/video/videodata-1650442510669/videodata-1650442510669.m3u8",
            "https://appservices.hitasoft.in/fundoo_new/public/video/videodata-1650440338530/videodata-1650440338530.m3u8",
            "https://appservices.hitasoft.in/fundoo_new/public/video/videodata-1647598202710/videodata-1647598202710.m3u8",
            "https://appservices.hitasoft.in/fundoo_new/public/video/videodata-1646848117588/videodata-1646848117588.m3u8"
    };

    //Added for sep9 for livestreaming
    public static boolean isSendInvite = false;
 public static final String KEY_LAST_BG_TIME = "last_bg_time";
 public static final long TIMEOUT_MS = 150 * 1000;
 public static boolean TAG_STREAM_HOST = false;
 public static final String TAG_HAND_RAISE = "handraise";
 public static boolean SHOULD_RECREATE_ACTIVITIES_ON_RESUME = false;
 public static final String TAG_HAND_RAISE_REJECT = "handraise_cancel";
 public static final String TAG_HAND_RAISE_ACCEPT = "handraise_accepted";
 public static final String TAG_MOBILE_NUMBER = "mobile_number";
  public static final String TAG_DOB = "date_of_birth";
 public static final String TAG_PAYPAL_ID = "paypal_id";
    public static final String TAG_LINK = "link";
    public static final String TAG_EMAIL_TEXT = "email";

    public static final int USERNAME_MAX_LENGTH = 24;
    public static final int BIO_MAX_LENGTH = 160;
    public static final String USER_TYPE = "user_type";
    public static final String TAG_GIFT_TITLE = "gift_title";
    public static final String TAG_GIFT_ICON = "gift_icon";
    public static final String TAG_ICON_TYPE = "icon_type";
    public static Boolean LOCALE_CHANGED_FLAG = false;
}
