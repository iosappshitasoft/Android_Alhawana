package com.app.model.event;

public class ForYouScrollEvent {
    boolean refresh;

    public ForYouScrollEvent(boolean refresh) {
        this.refresh = refresh;
    }

    public void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }

    public boolean isRefresh() {
        return refresh;
    }
}
