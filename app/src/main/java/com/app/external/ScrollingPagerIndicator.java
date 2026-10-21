package com.app.external;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.app.alhawanajd.R;

public class ScrollingPagerIndicator extends View {

    private int infiniteDotCount;

    private int dotNormalSize;
    private int dotSelectedSize;
    private int spaceBetweenDotCenters;
    private int visibleDotCount;
    private int visibleDotThreshold;

    private float visibleFramePosition;
    private float visibleFrameWidth;

    private float firstDotOffset;
    private SparseArray<Float> dotScale;

    private int itemCount;

    private final Paint paint;
    private final ArgbEvaluator colorEvaluator = new ArgbEvaluator();

    @ColorInt
    private int dotColor;

    @ColorInt
    private int selectedDotColor;

    private boolean looped;

    private Runnable attachRunnable;
    private PagerAttacher<?> currentAttacher;

    private boolean dotCountInitialized;

    public ScrollingPagerIndicator(Context context) {
        this(context, null);
    }

    public ScrollingPagerIndicator(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollingPagerIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // DEFAULTS (sane defaults — change with setters)
        // dp -> px helper
        final float density = getResources().getDisplayMetrics().density;
        dotNormalSize = Math.round(6 * density);      // default 6dp
        dotSelectedSize = Math.round(10 * density);   // default 10dp
        spaceBetweenDotCenters = Math.round(8 * density) + dotNormalSize; // 8dp gap + dot size
        visibleDotCount = 5;                          // must be odd
        visibleDotThreshold = 2;
        looped = false;

        // Default colors: try to read sensible theme attributes (no custom attrs used).
        // dotColor <- android:textColor (fallback)
        // selectedDotColor <- colorAccent (fallback to dotColor)
        dotColor = 0xFFCCCCCC; // light gray
        selectedDotColor = 0xFF2196F3; // material blue

        if (attrs != null) {
            // If a style resource is applied on the View (style="@style/...") we try to read
            // standard attributes from that style: android:textColorLink and colorAccent.
            // This is a *conservative* attempt — it uses only standard/theme attributes.
            int styleResId = 0;
            try {
                styleResId = attrs.getAttributeResourceValue(null, "style", 0);
            } catch (Exception ignored) {
                styleResId = 0;
            }

            if (styleResId != 0) {
                // attributes to try to read from style (standard/theme attributes only)
                final int[] styleAttrs = new int[]{
                        android.R.attr.textColor,      // index 0 -> dot color fallback
                        android.R.attr.textColorLink,  // index 1 -> hyperlink color (unused usually)
                        android.R.attr.colorAccent             // index 2 -> selected dot color fallback
                };
                try {
                    TypedArray ta = getContext().obtainStyledAttributes(styleResId, styleAttrs);
                    ColorStateList textColorCs = ta.getColorStateList(0);
                    ColorStateList linkColorCs = ta.getColorStateList(1);
                    ColorStateList accentCs = ta.getColorStateList(2);
                    if (textColorCs != null) {
                        dotColor = textColorCs.getDefaultColor();
                    }
                    if (accentCs != null) {
                        selectedDotColor = accentCs.getDefaultColor();
                    } else if (linkColorCs != null) {
                        selectedDotColor = linkColorCs.getDefaultColor();
                    }
                    ta.recycle();
                } catch (Exception ignored) {
                }
            } else {
                // Fallback: try theme direct resolution
                TypedValue tv = new TypedValue();
                if (getContext().getTheme().resolveAttribute(android.R.attr.colorAccent, tv, true)) {
                    try {
                        int color = getContext().getResources().getColor(tv.resourceId);
                        selectedDotColor = color;
                    } catch (Exception ignored) {
                    }
                }
                if (getContext().getTheme().resolveAttribute(android.R.attr.textColorPrimary, tv, true)
                        || getContext().getTheme().resolveAttribute(android.R.attr.textColor, tv, true)) {
                    try {
                        int resId = tv.resourceId;
                        if (resId != 0) {
                            ColorStateList cs = getContext().getResources().getColorStateList(resId);
                            if (cs != null) dotColor = cs.getDefaultColor();
                        }
                    } catch (Exception ignored) {}
                }
            }
        }

        paint = new Paint();
        paint.setAntiAlias(true);

        // if editing in layout editor show example
        if (isInEditMode()) {
            setDotCount(visibleDotCount);
            onPageScrolled(visibleDotCount / 2, 0);
        }
    }

    // ========== PUBLIC SETTERS to configure when NOT using attrs.xml ==========
    public void setDotNormalSizePx(int px) {
        this.dotNormalSize = px;
        requestLayout();
        invalidate();
    }

    public void setDotSelectedSizePx(int px) {
        this.dotSelectedSize = px;
        requestLayout();
        invalidate();
    }

    public void setSpaceBetweenDotCentersPx(int px) {
        this.spaceBetweenDotCenters = px;
        requestLayout();
        invalidate();
    }

    public void setDotNormalSizeDp(int dp) {
        setDotNormalSizePx(dpToPx(dp));
    }

    public void setDotSelectedSizeDp(int dp) {
        setDotSelectedSizePx(dpToPx(dp));
    }

    public void setSpaceBetweenDotCentersDp(int dp) {
        setSpaceBetweenDotCentersPx(dpToPx(dp));
    }

    public void setDotColor(@ColorInt int color) {
        this.dotColor = color;
        invalidate();
    }

    public void setSelectedDotColor(@ColorInt int color) {
        this.selectedDotColor = color;
        invalidate();
    }

    public void setVisibleDotCount(int visibleDotCount) {
        if (visibleDotCount % 2 == 0) {
            throw new IllegalArgumentException("visibleDotCount must be odd");
        }
        this.visibleDotCount = visibleDotCount;
        this.infiniteDotCount = visibleDotCount + 2;
        if (attachRunnable != null) reattach();
        else requestLayout();
    }

    public void setVisibleDotThreshold(int visibleDotThreshold) {
        this.visibleDotThreshold = visibleDotThreshold;
        if (attachRunnable != null) reattach();
        else requestLayout();
    }

    public void setLooped(boolean looped) {
        this.looped = looped;
        reattach();
        invalidate();
    }

    // ========== getters ==========

    public int getVisibleDotCount() { return visibleDotCount; }
    public int getVisibleDotThreshold() { return visibleDotThreshold; }
    public int getDotColor() { return dotColor; }
    public int getSelectedDotColor() { return selectedDotColor; }
    public int getDotNormalSizePx() { return dotNormalSize; }
    public int getDotSelectedSizePx() { return dotSelectedSize; }
    public int getSpaceBetweenDotCentersPx() { return spaceBetweenDotCenters; }
    public boolean isLooped() { return looped; }

    // ========== rest of original implementation left intact (only minor name/pack adjustments) ==========

    public void attachToPager(@NonNull ViewPager pager) {
        attachToPager(pager, new ViewPagerAttacher());
    }

    public void attachToRecyclerView(@NonNull RecyclerView recyclerView) {
        attachToPager(recyclerView, new RecyclerViewAttacher());
    }

    public void attachToRecyclerView(@NonNull RecyclerView recyclerView, int currentPageLeftCornerX) {
        attachToPager(recyclerView, new RecyclerViewAttacher(currentPageLeftCornerX));
    }

    public <T> void attachToPager(@NonNull final T pager, @NonNull final PagerAttacher<T> attacher) {
        detachFromPager();
        attacher.attachToPager(this, pager);
        currentAttacher = attacher;

        attachRunnable = new Runnable() {
            @Override
            public void run() {
                itemCount = -1;
                attachToPager(pager, attacher);
            }
        };
    }

    public void detachFromPager() {
        if (currentAttacher != null) {
            currentAttacher.detachFromPager();
            currentAttacher = null;
            attachRunnable = null;
        }
        dotCountInitialized = false;
    }

    public void reattach() {
        if (attachRunnable != null) {
            attachRunnable.run();
            invalidate();
        }
    }

    public void onPageScrolled(int page, float offset) {
        if (offset < 0 || offset > 1) {
            throw new IllegalArgumentException("Offset must be [0, 1]");
        } else if (page < 0 || page != 0 && page >= itemCount) {
            throw new IndexOutOfBoundsException("page must be [0, adapter.getItemCount())");
        }

        if (!looped || itemCount <= visibleDotCount && itemCount > 1) {
            dotScale.clear();

            scaleDotByOffset(page, offset);

            if (page < itemCount - 1) {
                scaleDotByOffset(page + 1, 1 - offset);
            } else if (itemCount > 1) {
                scaleDotByOffset(0, 1 - offset);
            }

            invalidate();
        }
        adjustFramePosition(offset, page);
        invalidate();
    }

    public void setDotCount(int count) {
        initDots(count);
    }

    public void setCurrentPosition(int position) {
        if (position != 0 && (position < 0 || position >= itemCount)) {
            throw new IndexOutOfBoundsException("Position must be [0, adapter.getItemCount()]");
        }
        if (itemCount == 0) return;
        adjustFramePosition(0, position);
        updateScaleInIdleState(position);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth;
        if (isInEditMode()) {
            measuredWidth = (visibleDotCount - 1) * spaceBetweenDotCenters + dotSelectedSize;
        } else {
            measuredWidth = itemCount >= visibleDotCount
                    ? (int) visibleFrameWidth
                    : (itemCount - 1) * spaceBetweenDotCenters + dotSelectedSize;
        }

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int desiredHeight = dotSelectedSize;
        int measuredHeight;

        switch (heightMode) {
            case MeasureSpec.EXACTLY:
                measuredHeight = heightSize;
                break;
            case MeasureSpec.AT_MOST:
                measuredHeight = Math.min(desiredHeight, heightSize);
                break;
            case MeasureSpec.UNSPECIFIED:
            default:
                measuredHeight = desiredHeight;
        }

        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int dotCount = getDotCount();
        if (dotCount < visibleDotThreshold) {
            return;
        }

        float scaleDistance = (spaceBetweenDotCenters + (dotSelectedSize - dotNormalSize) / 2f) * 0.7f;
        float smallScaleDistance = dotSelectedSize / 2f;
        float centerScaleDistance = 6f / 7f * spaceBetweenDotCenters;

        int firstVisibleDotPos = (int) (visibleFramePosition - firstDotOffset) / spaceBetweenDotCenters;
        int lastVisibleDotPos = firstVisibleDotPos
                + (int) (visibleFramePosition + visibleFrameWidth - getDotOffsetAt(firstVisibleDotPos))
                / spaceBetweenDotCenters;

        if (firstVisibleDotPos == 0 && lastVisibleDotPos + 1 > dotCount) {
            lastVisibleDotPos = dotCount - 1;
        }

        for (int i = firstVisibleDotPos; i <= lastVisibleDotPos; i++) {
            float dot = getDotOffsetAt(i);
            if (dot >= visibleFramePosition && dot < visibleFramePosition + visibleFrameWidth) {
                float diameter;
                float scale;

                if (looped && itemCount > visibleDotCount) {
                    float frameCenter = visibleFramePosition + visibleFrameWidth / 2f;
                    if (dot >= frameCenter - centerScaleDistance && dot <= frameCenter) {
                        scale = (dot - frameCenter + centerScaleDistance) / centerScaleDistance;
                    } else if (dot > frameCenter && dot < frameCenter + centerScaleDistance) {
                        scale = 1 - (dot - frameCenter) / centerScaleDistance;
                    } else {
                        scale = 0;
                    }
                } else {
                    scale = getDotScaleAt(i);
                }
                diameter = dotNormalSize + (dotSelectedSize - dotNormalSize) * scale;

                if (itemCount > visibleDotCount) {
                    float currentScaleDistance;
                    if (!looped && (i == 0 || i == dotCount - 1)) {
                        currentScaleDistance = smallScaleDistance;
                    } else {
                        currentScaleDistance = scaleDistance;
                    }

                    if (dot - visibleFramePosition < currentScaleDistance) {
                        float calculatedDiameter = diameter * (dot - visibleFramePosition) / currentScaleDistance;
                        if (calculatedDiameter < diameter) diameter = calculatedDiameter;
                    } else if (dot - visibleFramePosition > canvas.getWidth() - currentScaleDistance) {
                        float calculatedDiameter = diameter * (-dot + visibleFramePosition + canvas.getWidth()) / currentScaleDistance;
                        if (calculatedDiameter < diameter) diameter = calculatedDiameter;
                    }
                }

                paint.setColor(calculateDotColor(scale));
                canvas.drawCircle(dot - visibleFramePosition,
                        getMeasuredHeight() / 2f,
                        diameter / 2f,
                        paint);
            }
        }
    }

    @ColorInt
    private int calculateDotColor(float dotScale) {
        return (Integer) colorEvaluator.evaluate(dotScale, dotColor, selectedDotColor);
    }

    private void updateScaleInIdleState(int currentPos) {
        if (!looped || itemCount < visibleDotCount) {
            dotScale.clear();
            dotScale.put(currentPos, 1f);
            invalidate();
        }
    }

    private void initDots(int itemCount) {
        if (this.itemCount == itemCount && dotCountInitialized) return;
        this.itemCount = itemCount;
        dotCountInitialized = true;
        dotScale = new SparseArray<>();

        if (itemCount < visibleDotThreshold) {
            requestLayout();
            invalidate();
            return;
        }

        firstDotOffset = looped && this.itemCount > visibleDotCount ? 0 : dotSelectedSize / 2f;
        visibleFrameWidth = (visibleDotCount - 1) * spaceBetweenDotCenters + dotSelectedSize;

        requestLayout();
        invalidate();
    }

    private int getDotCount() {
        if (looped && itemCount > visibleDotCount) return infiniteDotCount;
        else return itemCount;
    }

    private void adjustFramePosition(float offset, int pos) {
        if (itemCount <= visibleDotCount) {
            visibleFramePosition = 0;
        } else if (!looped && itemCount > visibleDotCount) {
            float center = getDotOffsetAt(pos) + spaceBetweenDotCenters * offset;
            visibleFramePosition = center - visibleFrameWidth / 2f;

            int firstCenteredDotIndex = visibleDotCount / 2;
            float lastCenteredDot = getDotOffsetAt(getDotCount() - 1 - firstCenteredDotIndex);
            if (visibleFramePosition + visibleFrameWidth / 2f < getDotOffsetAt(firstCenteredDotIndex)) {
                visibleFramePosition = getDotOffsetAt(firstCenteredDotIndex) - visibleFrameWidth / 2f;
            } else if (visibleFramePosition + visibleFrameWidth / 2f > lastCenteredDot) {
                visibleFramePosition = lastCenteredDot - visibleFrameWidth / 2f;
            }
        } else {
            float center = getDotOffsetAt(infiniteDotCount / 2) + spaceBetweenDotCenters * offset;
            visibleFramePosition = center - visibleFrameWidth / 2f;
        }
    }

    private void scaleDotByOffset(int position, float offset) {
        if (dotScale == null || getDotCount() == 0) return;
        setDotScaleAt(position, 1 - Math.abs(offset));
    }

    private float getDotOffsetAt(int index) {
        return firstDotOffset + index * spaceBetweenDotCenters;
    }

    private float getDotScaleAt(int index) {
        Float scale = dotScale.get(index);
        return scale != null ? scale : 0f;
    }

    private void setDotScaleAt(int index, float scale) {
        if (scale == 0) dotScale.remove(index);
        else dotScale.put(index, scale);
    }

    private int dpToPx(int dp) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics()));
    }

    public interface PagerAttacher<T> {
        void attachToPager(@NonNull com.app.external.ScrollingPagerIndicator indicator, @NonNull T pager);
        void detachFromPager();
    }
}
