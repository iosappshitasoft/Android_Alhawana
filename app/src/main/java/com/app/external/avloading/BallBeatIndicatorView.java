package com.app.external.avloading;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.app.addon.livestream.external.avloading.BallBeatIndicator;
import com.app.addon.livestream.external.avloading.Indicator;
import com.app.alhawanajd.R;


public class BallBeatIndicatorView extends View {

    private static final com.app.addon.livestream.external.avloading.BallBeatIndicator DEFAULT_INDICATOR = new BallBeatIndicator();

    private static final int MIN_SHOW_TIME = 500; // ms
    private static final int MIN_DELAY = 500; // ms

    private long mStartTime = -1;
    private boolean mPostedHide = false;
    private boolean mPostedShow = false;
    private boolean mDismissed = false;
    private boolean mShouldStartAnimationDrawable = false;

    private int mMinWidth;
    private int mMaxWidth;
    private int mMinHeight;
    private int mMaxHeight;

    private com.app.addon.livestream.external.avloading.Indicator mIndicator;
    private int mIndicatorColor = Color.WHITE;

    private final Runnable mDelayedHide = new Runnable() {
        @Override
        public void run() {
            mPostedHide = false;
            mStartTime = -1;
            setVisibility(View.GONE);
        }
    };

    private final Runnable mDelayedShow = new Runnable() {
        @Override
        public void run() {
            mPostedShow = false;
            if (!mDismissed) {
                mStartTime = System.currentTimeMillis();
                setVisibility(View.VISIBLE);
            }
        }
    };

    public BallBeatIndicatorView(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public BallBeatIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public BallBeatIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BallBeatIndicatorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Initialize defaults and optionally read standard Android styleable attributes
     * from the provided style (android:minWidth, android:minHeight, android:background, android:tint).
     */
    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        // defaults in dp -> px
        mMinWidth = dpToPx(24, context);
        mMaxWidth = dpToPx(48, context);
        mMinHeight = dpToPx(24, context);
        mMaxHeight = dpToPx(48, context);
        mIndicatorColor = Color.WHITE;

        // Read standard android attributes from style if provided
        // We'll request android:minWidth, android:minHeight, android:background, android:tint
        int[] androidAttrs = new int[] {
                android.R.attr.minWidth,
                android.R.attr.minHeight,
                android.R.attr.background,
                android.R.attr.tint
        };

        TypedArray a = null;
        try {
            a = context.obtainStyledAttributes(attrs, androidAttrs, defStyleAttr, defStyleRes);
            // index 0 -> minWidth, index 1 -> minHeight, index 3 -> tint
            if (a.hasValue(0)) {
                mMinWidth = a.getDimensionPixelSize(0, mMinWidth);
            }
            if (a.hasValue(1)) {
                mMinHeight = a.getDimensionPixelSize(1, mMinHeight);
            }
            if (a.hasValue(3)) {
                // android:tint - can be color or ColorStateList - read default color
                mIndicatorColor = a.getColor(3, mIndicatorColor);
            }
            // Note: android:maxWidth / maxHeight are not universally available as attrs, keep defaults or set via setter
        } catch (Exception ignored) {
            // Safe fallback to defaults if styled attributes cannot be read
        } finally {
            if (a != null) a.recycle();
        }

        // Set default indicator
        setIndicator(DEFAULT_INDICATOR);
        setIndicatorColor(mIndicatorColor);
    }

    // dp helper
    private int dpToPx(int dp, Context ctx) {
        float density = ctx.getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    // ===== Public API =====

    public void setIndicator(com.app.addon.livestream.external.avloading.Indicator d) {
        if (mIndicator != d) {
            if (mIndicator != null) {
                mIndicator.setCallback(null);
                unscheduleDrawable(mIndicator);
            }
            mIndicator = d;
            if (d != null) {
                d.setCallback(this);
                d.setColor(mIndicatorColor);
            }
            postInvalidate();
        }
    }

    /**
     * Set indicator by simple class name (will use same package + .indicators) or full class name.
     * Example: "BallBeatIndicator" or "com.app.addon.livestream.external.avloading.indicators.BallBeatIndicator"
     */
    public void setIndicator(String indicatorName) {
        if (indicatorName == null || TextUtils.isEmpty(indicatorName)) return;
        try {
            String className = indicatorName.contains(".")
                    ? indicatorName
                    : getClass().getPackage().getName() + ".indicators." + indicatorName;
            Class<?> cls = Class.forName(className);
            com.app.addon.livestream.external.avloading.Indicator indicator = (com.app.addon.livestream.external.avloading.Indicator) cls.newInstance();
            setIndicator(indicator);
        } catch (Exception e) {
            // ignore or log
        }
    }

    public Indicator getIndicator() {
        return mIndicator;
    }

    public void setIndicatorColor(int color) {
        mIndicatorColor = color;
        if (mIndicator != null) {
            mIndicator.setColor(color);
        }
        invalidate();
    }

    public int getIndicatorColor() {
        return mIndicatorColor;
    }

    // show/hide with min delay logic
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

    // ===== view lifecycle =====

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == mIndicator || super.verifyDrawable(who);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final Drawable d = mIndicator;
        if (d != null) {
            final int save = canvas.save();
            canvas.translate(getPaddingLeft(), getPaddingTop());
            d.draw(canvas);
            canvas.restoreToCount(save);
            if (mShouldStartAnimationDrawable && d instanceof Animatable) {
                ((Animatable) d).start();
                mShouldStartAnimationDrawable = false;
            }
        }
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int dw = mMinWidth;
        int dh = mMinHeight;
        final Drawable d = mIndicator;
        if (d != null) {
            dw = Math.max(mMinWidth, Math.min(mMaxWidth > 0 ? mMaxWidth : Integer.MAX_VALUE, d.getIntrinsicWidth()));
            dh = Math.max(mMinHeight, Math.min(mMaxHeight > 0 ? mMaxHeight : Integer.MAX_VALUE, d.getIntrinsicHeight()));
        }
        dw += getPaddingLeft() + getPaddingRight();
        dh += getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(resolveSizeAndState(dw, widthMeasureSpec, 0),
                resolveSizeAndState(dh, heightMeasureSpec, 0));
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mIndicator != null && mIndicator.isStateful()) {
            mIndicator.setState(getDrawableState());
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimationIfNeeded();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopAnimationIfNeeded();
        super.onDetachedFromWindow();
    }

    private void startAnimationIfNeeded() {
        if (getVisibility() == VISIBLE && mIndicator instanceof Animatable) {
            mShouldStartAnimationDrawable = true;
            invalidate();
        }
    }

    private void stopAnimationIfNeeded() {
        if (mIndicator instanceof Animatable) {
            ((Animatable) mIndicator).stop();
            mShouldStartAnimationDrawable = false;
        }
    }
}
