package com.app.Zego.Model

import com.google.gson.annotations.SerializedName

data class LiveStreamRequest1(
    @SerializedName("user_id")
    var userId: String? = null,

    @SerializedName("search_key")
    var searchKey: String? = null,

    @SerializedName("limit")
    var limit: String? = null,

    @SerializedName("offset")
    var offset: String? = null,

    @SerializedName("match")
    var match: String? = null,

    @SerializedName("profile_id")
    var profileId: String? = null,

    @SerializedName("filter_applied")
    var filterApplied: String? = null,

    @SerializedName("gender")
    var gender: String? = null,

    @SerializedName("location")
    var filterLocation: String? = null,

    @SerializedName("blocked_users")
    var blockedUsers: String? = null,

    @SerializedName("min_age")
    var minAge: String? = null,

    @SerializedName("max_age")
    var maxAge: String? = null,

    @SerializedName("sort_by")
    var sortBy: String? = null,

    @SerializedName("type")
    var type: String? = null
)
