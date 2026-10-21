package com.app.model.event;

public class FollowingScrollEvent {
    private boolean refresh;

    public FollowingScrollEvent(boolean refresh) {
        this.refresh = refresh;
    }

    public void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }

    public boolean isRefresh() {
        return refresh;
    }
}
