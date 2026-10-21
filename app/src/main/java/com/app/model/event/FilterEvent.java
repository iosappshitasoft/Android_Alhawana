package com.app.model.event;

import android.os.Bundle;

public class FilterEvent {
    private Bundle bundle;

    public FilterEvent(Bundle bundle) {
        this.bundle = bundle;
    }

    public Bundle getBundle() {
        return bundle;
    }
}
