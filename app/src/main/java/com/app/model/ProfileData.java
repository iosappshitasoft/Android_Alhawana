package com.app.model;

import com.google.gson.annotations.SerializedName;
import com.app.utils.Constants;

/**
 * Created by hitasoft on 25/2/18.
 */

public class ProfileData {

    @SerializedName(Constants.TAG_STATUS)
    public String status;

    @SerializedName(Constants.TAG_MESSAGE)
    public String message;

    @SerializedName(Constants.TAG_RESULT)
    public Result result;

    public class Result {

        @SerializedName(Constants.TAG_USER_ID)
        public String userId;

        @SerializedName(Constants.TAG_USER_NAME)
        public String userName;

        @SerializedName(Constants.TAG_USER_IMAGE)
        public String userImage;

        @SerializedName(Constants.TAG_FULL_NAME)
        public String fullName;

        /*@SerializedName(Constants.TAG_VIDEOS_COUNT)
        public String videosCount;*/

        @SerializedName(Constants.TAG_FOLLOWERS_COUNT)
        public String followersCount;

        @SerializedName(Constants.TAG_FOLLOWING_COUNT)
        public String followingCount;

        @SerializedName(Constants.TAG_BLOCKED_COUNT)
        public String blockedCount;

        @SerializedName(Constants.TAG_NOTIFICATION_COUNT)
        public String notificationCount;

        @SerializedName(Constants.TAG_FOLLOWING)
        public String following;

        @SerializedName(Constants.TAG_BLOCKED)
        public String blocked;
    }
}
