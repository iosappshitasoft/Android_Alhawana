package com.app.model.event;

public class NewStreamEvent {
    private boolean hasNewStreams;

    public boolean hasNewStreams() {
        return hasNewStreams;
    }

    public void setHasNewStreams(boolean hasNewStreams) {
        this.hasNewStreams = hasNewStreams;
    }
}
