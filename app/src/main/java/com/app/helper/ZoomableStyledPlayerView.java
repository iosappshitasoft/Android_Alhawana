package com.app.helper;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import com.google.android.exoplayer2.ui.StyledPlayerView;

public class ZoomableStyledPlayerView extends StyledPlayerView {

    private ScaleGestureDetector scaleGestureDetector;
    private float scaleFactor = 1.0f;
    private static final float MIN_ZOOM = 0.5f;
    private static final float MAX_ZOOM = 3.0f;
    private final String TAG = ZoomableStyledPlayerView.class.getSimpleName();

    public ZoomableStyledPlayerView(Context context) {
        super(context);
        setupZoom();
    }

    private void setupZoom() {
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        Log.d(TAG, "onTouchEvent: "+event.getEventTime());
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();

            // Limit zoom range
            scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));

            // Apply scale to the view
            setScaleX(scaleFactor);
            setScaleY(scaleFactor);

            return true;
        }
    }

    // Optional: Method to reset zoom
    public void resetZoom() {
        scaleFactor = 1.0f;
        setScaleX(scaleFactor);
        setScaleY(scaleFactor);
    }

    // Optional: Method to get current zoom level
    public float getCurrentZoom() {
        return scaleFactor;
    }
}
