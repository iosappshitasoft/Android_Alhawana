package com.app.alhawanajd.home.eventbus;

public class ForYouProfileUpdate {

    public final String id;
    public final String profileImage;
    public final String userType;

    public ForYouProfileUpdate(String id, String profileImage, String userType) {
        this.id = id;
        this.profileImage = profileImage;
        this.userType = userType;
    }
}
