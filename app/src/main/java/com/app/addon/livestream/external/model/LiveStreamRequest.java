package com.app.addon.livestream.external.model;

import com.google.gson.annotations.SerializedName;

public class LiveStreamRequest {
    @SerializedName("user_id")
    private String userId;
    @SerializedName("search_key")
    private String searchKey;
    @SerializedName("limit")
    private String limit;
    @SerializedName("offset")
    private String offset;
    @SerializedName("match")
    private String match;
    @SerializedName("profile_id")
    private String profileId;
    @SerializedName("filter_applied")
    private String filterApplied;
    @SerializedName("gender")
    private String gender;
    @SerializedName("location")
    private String filterLocation;
    @SerializedName("blocked_users")
    private String blockedUsers;
    @SerializedName("min_age")
    private String minAge;
    @SerializedName("max_age")
    private String maxAge;
    @SerializedName("sort_by")
    private String sortBy;
    @SerializedName("type")
    private String type;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getFilterApplied() {
        return filterApplied;
    }

    public void setFilterApplied(String filterApplied) {
        this.filterApplied = filterApplied;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMinAge() {
        return minAge;
    }

    public void setMinAge(String minAge) {
        this.minAge = minAge;
    }

    public String getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(String maxAge) {
        this.maxAge = maxAge;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFilterLocation() {
        return filterLocation;
    }

    public void setFilterLocation(String filterLocation) {
        this.filterLocation = filterLocation;
    }

    public String getBlockedUsers() {
        return blockedUsers;
    }

    public void setBlockedUsers(String blockedUsers) {
        this.blockedUsers = blockedUsers;
    }
}
