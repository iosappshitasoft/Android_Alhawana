package com.app.addon.livestream.external.avloading;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;

public class AVLoadingIndicatorView2 extends View {

    private static final LineScalePulseOutIndicator DEFAULT_INDICATOR = new LineScalePulseOutIndicator();

    private static final int MIN_SHOW_TIME = 500; // ms
    private static final int MIN_DELAY = 500; // ms

    private long mStartTime = -1;
    private boolean mPostedHide = false;
    private boolean mPostedShow = false;
    private boolean mDismissed = false;
    private boolean mShouldStartAnimationDrawable = false;

    private int mMinWidth, mMaxWidth, mMinHeight, mMaxHeight;
    private Indicator mIndicator;
    private int mIndicatorColor = Color.WHITE;

    private final Runnable mDelayedHide = () -> {
        mPostedHide = false;
        mStartTime = -1;
        setVisibility(View.GONE);
    };

    private final Runnable mDelayedShow = () -> {
        mPostedShow = false;
        if (!mDismissed) {
            mStartTime = System.currentTimeMillis();
            setVisibility(View.VISIBLE);
        }
    };

    public AVLoadingIndicatorView2(Context context) {
        super(context, null, 0, android.R.style.Widget);
        initDefaults(context);
    }

    public AVLoadingIndicatorView2(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.progressBarStyleSmall);
        initDefaults(context);
    }

    public AVLoadingIndicatorView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDefaults(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AVLoadingIndicatorView2(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initDefaults(context);
    }

    private void initDefaults(Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        mMinWidth = (int) (24 * density);
        mMaxWidth = (int) (48 * density);
        mMinHeight = (int) (24 * density);
        mMaxHeight = (int) (48 * density);

        setIndicator(DEFAULT_INDICATOR);
        setIndicatorColor(mIndicatorColor);
    }

    public void setIndicator(Indicator indicator) {
        if (mIndicator != indicator) {
            if (mIndicator != null) {
                mIndicator.setCallback(null);
                unscheduleDrawable(mIndicator);
            }
            mIndicator = indicator;
            if (indicator != null) {
                indicator.setCallback(this);
                indicator.setColor(mIndicatorColor);
            }
            postInvalidate();
        }
    }

    public void setIndicatorColor(int color) {
        mIndicatorColor = color;
        if (mIndicator != null) {
            mIndicator.setColor(color);
        }
        invalidate();
    }

    public void show() {
        mStartTime = -1;
        mDismissed = false;
        removeCallbacks(mDelayedHide);
        if (!mPostedShow) {
            postDelayed(mDelayedShow, MIN_DELAY);
            mPostedShow = true;
        }
    }

    public void hide() {
        mDismissed = true;
        removeCallbacks(mDelayedShow);
        long diff = System.currentTimeMillis() - mStartTime;
        if (diff >= MIN_SHOW_TIME || mStartTime == -1) {
            setVisibility(View.GONE);
        } else if (!mPostedHide) {
            postDelayed(mDelayedHide, MIN_SHOW_TIME - diff);
            mPostedHide = true;
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == mIndicator || super.verifyDrawable(who);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mIndicator != null) {
            final int saveCount = canvas.save();
            canvas.translate(getPaddingLeft(), getPaddingTop());
            mIndicator.draw(canvas);
            canvas.restoreToCount(saveCount);
            if (mShouldStartAnimationDrawable && mIndicator instanceof Animatable) {
                ((Animatable) mIndicator).start();
                mShouldStartAnimationDrawable = false;
            }
        }
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int dw = Math.max(mMinWidth, Math.min(mMaxWidth, mIndicator != null ? mIndicator.getIntrinsicWidth() : 0));
        int dh = Math.max(mMinHeight, Math.min(mMaxHeight, mIndicator != null ? mIndicator.getIntrinsicHeight() : 0));
        dw += getPaddingLeft() + getPaddingRight();
        dh += getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(resolveSizeAndState(dw, widthMeasureSpec, 0),
                resolveSizeAndState(dh, heightMeasureSpec, 0));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopAnimation();
        super.onDetachedFromWindow();
    }

    private void startAnimation() {
        if (getVisibility() == VISIBLE && mIndicator instanceof Animatable) {
            mShouldStartAnimationDrawable = true;
            invalidate();
        }
    }

    private void stopAnimation() {
        if (mIndicator instanceof Animatable) {
            ((Animatable) mIndicator).stop();
            mShouldStartAnimationDrawable = false;
        }
    }
}
