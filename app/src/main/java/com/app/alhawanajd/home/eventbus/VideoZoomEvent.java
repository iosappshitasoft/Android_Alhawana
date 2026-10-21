package com.app.alhawanajd.home.eventbus;


/*public class VideoZoomEvent {

    *//** 0f = fullscreen, 1f = shrunk for comments *//*
    public final float currentZoomScale;

    *//** Optional available height for video area (0 means use full screen) *//*
    public final int availableHeightPx;

    *//** Basic constructor — only zoom scale *//*
    public VideoZoomEvent(float currentZoomScale) {
        this.currentZoomScale = currentZoomScale;
        this.availableHeightPx = 0; // default to full height
    }

    *//** Extended constructor — zoom scale + available height *//*
    public VideoZoomEvent(float currentZoomScale, int availableHeightPx) {
        this.currentZoomScale = currentZoomScale;
        this.availableHeightPx = availableHeightPx;
    }
}*/

public class VideoZoomEvent {
    public final float currentZoomScale; // 0f = full, 1f = shrunk
    public final int availableHeightPx;  // 0 = reset

    public VideoZoomEvent(float currentZoomScale) {
        this(currentZoomScale, 0);
    }

    public VideoZoomEvent(float currentZoomScale, int availableHeightPx) {
        this.currentZoomScale = currentZoomScale;
        this.availableHeightPx = availableHeightPx;
    }
}