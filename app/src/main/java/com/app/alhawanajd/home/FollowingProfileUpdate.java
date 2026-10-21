package com.app.alhawanajd.home;

public class FollowingProfileUpdate {

    public final String id;
    public final String profileImage;
    public final String userType;

    /*public FollowingProfileUpdate(String id, String profileImage) {
        this.id = id;
        this.profileImage = profileImage;
    }*/

    public FollowingProfileUpdate(String id, String profileImage, String userType) {
        this.id = id;
        this.profileImage = profileImage;
        this.userType = userType;
    }
}
