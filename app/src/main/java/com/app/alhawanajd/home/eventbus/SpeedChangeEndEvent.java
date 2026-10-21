package com.app.alhawanajd.home.eventbus;

public class SpeedChangeEndEvent {

    private int position; // Optional: to identify which video item

    public SpeedChangeEndEvent() {
        this.position = -1;
    }

    public SpeedChangeEndEvent(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
