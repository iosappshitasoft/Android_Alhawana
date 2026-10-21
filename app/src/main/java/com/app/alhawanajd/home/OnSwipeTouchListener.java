package com.app.alhawanajd.home;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;


import com.google.android.exoplayer2.ui.StyledPlayerView;

public class OnSwipeTouchListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;
    private final ScaleGestureDetector scaleGestureDetector;
    private long swipeData = 0;

    // Zoom related variables
    private View videoSurfaceView;
    private float scaleFactor = 1.0f;
    private static final float MIN_ZOOM = 0.1f;
    private static final float MAX_ZOOM = 3.0f;

    // Long press for 2x speed variables
    private Handler longPressHandler;
    private Runnable longPressRunnable;
    private boolean isLongPressing = false;
    private boolean isSpeedChanged = false;
    private static final long LONG_PRESS_TIMEOUT = 500; // 500ms for long press
    private float initialTouchX = 0f;
    private float initialTouchY = 0f;
    private static final float TOUCH_SLOP = 50f; // Movement threshold to cancel long press

    public OnSwipeTouchListener(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener());
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        longPressHandler = new Handler(Looper.getMainLooper());
    }

    // Constructor with video surface view for zoom functionality
    public OnSwipeTouchListener(Context context, View videoSurfaceView) {
        this(context);
        this.videoSurfaceView = videoSurfaceView;
    }

    // Set the long value to pass during swipe
    public void setSwipeData(long swipeData) {
        this.swipeData = swipeData;
    }

    // Set video surface view for zoom functionality
    public void setVideoSurfaceView(StyledPlayerView videoSurfaceView) {
        this.videoSurfaceView = videoSurfaceView;
    }

    @Override
    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        // Handle scale gesture first
        boolean scaleHandled = scaleGestureDetector.onTouchEvent(motionEvent);

        // Handle long press for 2x speed
        handleLongPressForSpeed(view, motionEvent);

        // Request parent to not intercept touch events during scaling or long pressing
        if (scaleGestureDetector.isInProgress() || isLongPressing) {
            view.getParent().requestDisallowInterceptTouchEvent(true);
        } else {
            // Check for touch event end to reset parent intercept
            if (motionEvent.getAction() == MotionEvent.ACTION_UP ||
                    motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                view.getParent().requestDisallowInterceptTouchEvent(false);
            }
        }

        // If not scaling and not long pressing, handle other gestures
        if (!scaleGestureDetector.isInProgress() && !isLongPressing) {
            return gestureDetector.onTouchEvent(motionEvent) || scaleHandled;
        }

        return scaleHandled || isLongPressing;
    }

    private void handleLongPressForSpeed(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialTouchX = motionEvent.getX();
                initialTouchY = motionEvent.getY();

                // Check if touch is on the left side of the view
                if (isLeftSideTouch(view, motionEvent)) {
                    startLongPressTimer();
                }
                break;

            case MotionEvent.ACTION_MOVE:
                // Cancel long press if user moves finger too much
                if (longPressRunnable != null) {
                    float deltaX = Math.abs(motionEvent.getX() - initialTouchX);
                    float deltaY = Math.abs(motionEvent.getY() - initialTouchY);

                    if (deltaX > TOUCH_SLOP || deltaY > TOUCH_SLOP) {
                        cancelLongPress();
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                cancelLongPress();
                if (isSpeedChanged) {
                    onSpeedChangeEnd(); // Reset to normal speed
                    isSpeedChanged = false;
                }
                isLongPressing = false;
                break;
        }
    }

    private boolean isLeftSideTouch(View view, MotionEvent motionEvent) {
        float touchX = motionEvent.getX();
        float viewWidth = view.getWidth();

        // Check if layout is RTL
        boolean isRTL = view.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;

        if (isRTL) {
            // For RTL, long press should work on right side (which is the start side in RTL)
            return touchX > (viewWidth * 0.5f);
        } else {
            // For LTR, long press works on left side
            return touchX < (viewWidth * 0.5f);
        }
    }

    private void startLongPressTimer() {
        if (longPressRunnable != null) {
            longPressHandler.removeCallbacks(longPressRunnable);
        }

        longPressRunnable = new Runnable() {
            @Override
            public void run() {
                // Long press detected on left side
                isLongPressing = true;
                isSpeedChanged = true;
                onSpeedChangeStart(); // Change to 2x speed
            }
        };

        longPressHandler.postDelayed(longPressRunnable, LONG_PRESS_TIMEOUT);
    }

    private void cancelLongPress() {
        if (longPressRunnable != null) {
            longPressHandler.removeCallbacks(longPressRunnable);
            longPressRunnable = null;
        }
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // Don't trigger single tap if we're in speed change mode
            if (!isSpeedChanged) {
                singleTab();
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // Don't trigger double tap if we're in speed change mode
            if (!isSpeedChanged) {
                doubleTab();
            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        // Detect swipe direction and call appropriate method
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // Don't trigger swipe if we're in speed change mode
            if (isSpeedChanged || isLongPressing) {
                return false;
            }

            boolean result = false;

            try {
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                if (Math.abs(diffX) > Math.abs(diffY)) {
                    // Horizontal swipe
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight(swipeData);
                        } else {
                            onSwipeLeft(swipeData);
                        }
                        result = true;
                    }
                } else {
                    // Vertical swipe
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeDown();
                        } else {
                            onSwipeUp();
                        }
                        result = true;
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }

            return result;
        }
    }

    // Scale gesture listener for pinch zoom
    private final class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();

            // Limit zoom range
            scaleFactor = Math.min(scaleFactor, MAX_ZOOM);

            // Apply scale to the video surface view
            if (videoSurfaceView != null) {
                videoSurfaceView.setScaleX(scaleFactor);
                videoSurfaceView.setScaleY(scaleFactor);
                onZoomChanged(scaleFactor); // Callback for zoom change
            }

            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            // Cancel long press if scaling starts
            cancelLongPress();
            onZoomStart();
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            // Auto-reset zoom out to normal size when user lifts fingers
            if (scaleFactor < 1.0f) {
                animateZoomToNormal();
            }
            onZoomEnd();
        }
    }

    // Zoom utility methods
    public void resetZoom() {
        scaleFactor = 1.0f;
        if (videoSurfaceView != null) {
            videoSurfaceView.setScaleX(scaleFactor);
            videoSurfaceView.setScaleY(scaleFactor);
            onZoomChanged(scaleFactor);
        }
    }

    // Animate zoom to normal size (for zoom out auto-reset)
    private void animateZoomToNormal() {
        if (videoSurfaceView == null) return;

        // Create smooth animation back to normal size
        videoSurfaceView.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(300) // 300ms animation
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        scaleFactor = 1.0f;
                        onZoomChanged(scaleFactor);
                    }
                })
                .start();
    }

    public float getCurrentZoom() {
        return scaleFactor;
    }

    public void setZoom(float zoom) {
        scaleFactor = Math.max(MIN_ZOOM, Math.min(zoom, MAX_ZOOM));
        if (videoSurfaceView != null) {
            videoSurfaceView.setScaleX(scaleFactor);
            videoSurfaceView.setScaleY(scaleFactor);
            onZoomChanged(scaleFactor);
        }
    }

    // Speed control utility methods
    public boolean isInSpeedMode() {
        return isSpeedChanged;
    }

    public void forceStopSpeedMode() {
        cancelLongPress();
        if (isSpeedChanged) {
            onSpeedChangeEnd();
            isSpeedChanged = false;
        }
        isLongPressing = false;
    }

    // Methods to override
    public void onSwipeRight(long data) {}
    public void onSwipeLeft(long data) {}
    public void onSwipeUp() {}
    public void onSwipeDown() {}
    public void doubleTab() {}
    public void singleTab() {}

    // Zoom callback methods to override
    public void onZoomChanged(float zoomLevel) {}
    public void onZoomStart() {}
    public void onZoomEnd() {}

    // New speed change callback methods to override
    public void onSpeedChangeStart() {} // Called when long press triggers 2x speed
    public void onSpeedChangeEnd() {}   // Called when user releases and speed returns to normal
}
