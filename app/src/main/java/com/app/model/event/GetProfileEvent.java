package com.app.model.event;

import com.app.model.ProfileResponse;

public class GetProfileEvent {
    private ProfileResponse profileResponse;

    public ProfileResponse getProfileResponse() {
        return profileResponse;
    }

    public void setProfileResponse(ProfileResponse profileResponse) {
        this.profileResponse = profileResponse;
    }
}
