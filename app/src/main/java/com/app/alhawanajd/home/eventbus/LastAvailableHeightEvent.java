package com.app.alhawanajd.home.eventbus;

public class LastAvailableHeightEvent {
    public final int availableHeightPx;

    public LastAvailableHeightEvent(int availableHeightPx) {
        this.availableHeightPx = availableHeightPx;
    }
}