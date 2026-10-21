package com.app.alhawanajd.home.eventbus;

public class SpeedChangeStartEvent {
    private int position; // Optional: to identify which video item

    public SpeedChangeStartEvent() {
        this.position = -1;
    }

    public SpeedChangeStartEvent(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
