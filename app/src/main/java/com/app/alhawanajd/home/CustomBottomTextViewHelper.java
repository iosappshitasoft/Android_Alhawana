package com.app.alhawanajd.home;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

// Custom Bottom TextView Helper Class
public class CustomBottomTextViewHelper {

    private ViewGroup rootContainer;
    private TextView customTextView;
    private Context context;
    private boolean isShowing = false;

    public CustomBottomTextViewHelper(Context context, ViewGroup rootContainer) {
        this.context = context;
        this.rootContainer = rootContainer;
    }

    /**
     * Show custom TextView at bottom middle of screen (default bias 0.9)
     * @param message Text to display
     */
    public void showBottomText(String message) {
        showBottomText(message, 0.9f, null);
    }

    /**
     * Show custom TextView with custom styling at bottom (default bias 0.9)
     * @param message Text to display
     * @param config Custom configuration (can be null for default)
     */
    public void showBottomText(String message, TextViewConfig config) {
        showBottomText(message, 0.9f, config);
    }

    /**
     * Show custom TextView with vertical bias
     * @param message Text to display
     * @param verticalBias Vertical position bias (0.0 = top, 1.0 = bottom)
     */
    public void showBottomText(String message, float verticalBias) {
        showBottomText(message, verticalBias, null);
    }

    /**
     * Show custom TextView with vertical bias and custom styling
     * @param message Text to display
     * @param verticalBias Vertical position bias (0.0 = top, 1.0 = bottom)
     * @param config Custom configuration (can be null for default)
     */
    public void showBottomText(String message, float verticalBias, TextViewConfig config) {
        // Remove existing TextView if any
        dismissBottomText();

        if (rootContainer == null || context == null) return;

        // Clamp vertical bias between 0.0 and 1.0
        verticalBias = Math.max(0.0f, Math.min(1.0f, verticalBias));

        // Use default config if none provided
        if (config == null) {
            config = new TextViewConfig();
        }

        // Create the TextView
        customTextView = new TextView(context);
        customTextView.setText(message);

        // Calculate gravity based on vertical bias
        int gravity = Gravity.CENTER_HORIZONTAL;

        if (verticalBias <= 0.1f) {
            // Top area (0.0 - 0.1)
            gravity |= Gravity.TOP;
        } else if (verticalBias >= 0.9f) {
            // Bottom area (0.9 - 1.0)
            gravity |= Gravity.BOTTOM;
        } else {
            // Center vertically for middle range
            gravity |= Gravity.CENTER_VERTICAL;
        }

        // Set layout parameters with calculated gravity
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                gravity
        );

        // Calculate margins based on vertical bias and screen height
        int screenHeight = rootContainer.getHeight();
        if (screenHeight == 0) {
            // If height not available, use display metrics
            android.util.DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            screenHeight = displayMetrics.heightPixels;
        }

        // Apply margins based on bias
        if (verticalBias <= 0.1f) {
            // Top positioning
            params.topMargin = (int)(screenHeight * verticalBias) + config.verticalMargin;
        } else if (verticalBias >= 0.9f) {
            // Bottom positioning
            params.bottomMargin = (int)(screenHeight * (1.0f - verticalBias)) + config.verticalMargin;
        } else {
            // Center positioning with bias offset
            int centerOffset = (int)(screenHeight * (verticalBias - 0.5f));
            params.topMargin = centerOffset + config.verticalMargin;
        }

        // Apply horizontal margins
        params.leftMargin = config.sideMargin;
        params.rightMargin = config.sideMargin;

        customTextView.setLayoutParams(params);

        // Apply styling
        applyTextViewStyling(customTextView, config);

        // Add to root container
        rootContainer.addView(customTextView);
        isShowing = true;

        // Apply entrance animation
        if (config.animateEntrance) {
            applyEntranceAnimation(customTextView, config.animationType);
        }
    }

    public void showBottomText(CharSequence message, TextViewConfig config) {
        dismissBottomText();

        if (rootContainer == null || context == null) return;

        if (config == null) config = new TextViewConfig();

        customTextView = new TextView(context);
        customTextView.setText(message);
        customTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        customTextView.setGravity(Gravity.CENTER);
        customTextView.setSingleLine(false);
        customTextView.setEllipsize(null);
        customTextView.setPadding(8, 4, 8, 4);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL
        );

        // Fixed bottom margin (distance from bottom)
        params.bottomMargin = (int) (85 * context.getResources().getDisplayMetrics().density);
        params.leftMargin = config.sideMargin;
        params.rightMargin = config.sideMargin;

        customTextView.setLayoutParams(params);
        applyTextViewStyling(customTextView, config);

        rootContainer.addView(customTextView);
        isShowing = true;

        if (config.animateEntrance) {
            applyEntranceAnimation(customTextView, config.animationType);
        }
    }

    private void applyTextViewStyling(TextView textView, TextViewConfig config) {
        // Text properties
        textView.setTextColor(config.textColor);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, config.textSize);
        textView.setTypeface(null, config.textStyle);
        textView.setGravity(Gravity.CENTER);

        // Padding
        textView.setPadding(config.paddingHorizontal, config.paddingVertical,
                config.paddingHorizontal, config.paddingVertical);

        // Background
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.RECTANGLE);
        background.setColor(config.backgroundColor);
        background.setCornerRadius(config.cornerRadius);

        // Add stroke if specified
        if (config.strokeWidth > 0) {
            background.setStroke(config.strokeWidth, config.strokeColor);
        }

        // Add shadow if specified
        if (config.shadowRadius > 0) {
            textView.setShadowLayer(config.shadowRadius, config.shadowDx,
                    config.shadowDy, config.shadowColor);
        }

        textView.setBackground(background);

        // Set minimum width and height if specified
        if (config.minWidth > 0) {
            textView.setMinWidth(config.minWidth);
        }
        if (config.minHeight > 0) {
            textView.setMinHeight(config.minHeight);
        }

        // Set max lines
        if (config.maxLines > 0) {
            textView.setMaxLines(config.maxLines);
            textView.setEllipsize(TextUtils.TruncateAt.END);
        }
    }

    private void applyEntranceAnimation(TextView textView, AnimationType animationType) {
        switch (animationType) {
            case SLIDE_UP:
                textView.setTranslationY(200f);
                textView.setAlpha(1f);
                textView.animate()
                        .translationY(0f)
                        .setDuration(300)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
                break;

            case FADE_IN:
                textView.setAlpha(0f);
                textView.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
                break;

            case SCALE_IN:
                textView.setScaleX(0f);
                textView.setScaleY(0f);
                textView.setAlpha(0.8f);
                textView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(300)
                        .setInterpolator(new OvershootInterpolator())
                        .start();
                break;

            case SLIDE_UP_FADE:
                textView.setTranslationY(100f);
                textView.setAlpha(0f);
                textView.animate()
                        .translationY(0f)
                        .alpha(1f)
                        .setDuration(400)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
                break;

            case NONE:
            default:
                // No animation
                break;
        }
    }

    /**
     * Dismiss the bottom TextView with animation
     */
    public void dismissBottomText() {
        dismissBottomText(AnimationType.FADE_OUT);
    }

    /**
     * Dismiss with custom exit animation
     */
    public void dismissBottomText(AnimationType exitAnimation) {
        if (customTextView != null && rootContainer != null && isShowing) {
            applyExitAnimation(customTextView, exitAnimation);
        }
    }

    private void applyExitAnimation(TextView textView, AnimationType animationType) {
        switch (animationType) {
            case SLIDE_DOWN:
                textView.animate()
                        .translationY(200f)
                        .setDuration(300)
                        .setInterpolator(new AccelerateInterpolator())
                        .withEndAction(() -> removeTextView())
                        .start();
                break;

            case FADE_OUT:
                textView.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .setInterpolator(new AccelerateInterpolator())
                        .withEndAction(() -> removeTextView())
                        .start();
                break;

            case SCALE_OUT:
                textView.animate()
                        .scaleX(0f)
                        .scaleY(0f)
                        .alpha(0f)
                        .setDuration(250)
                        .setInterpolator(new AccelerateInterpolator())
                        .withEndAction(() -> removeTextView())
                        .start();
                break;

            case SLIDE_DOWN_FADE:
                textView.animate()
                        .translationY(100f)
                        .alpha(0f)
                        .setDuration(300)
                        .setInterpolator(new AccelerateInterpolator())
                        .withEndAction(() -> removeTextView())
                        .start();
                break;

            case NONE:
            default:
                removeTextView();
                break;
        }
    }

    private void removeTextView() {
        if (customTextView != null && rootContainer != null) {
            rootContainer.removeView(customTextView);
            customTextView = null;
            isShowing = false;
        }
    }

    /**
     * Update the text of currently showing TextView
     */
    public void updateText(String newText) {
        if (customTextView != null && isShowing) {
            customTextView.setText(newText);
        }
    }

    /**
     * Check if TextView is currently showing
     */
    public boolean isShowing() {
        return isShowing && customTextView != null;
    }

    /**
     * Get current TextView instance (can be null)
     */
    public TextView getCurrentTextView() {
        return customTextView;
    }

    // Configuration class for custom styling
    public static class TextViewConfig {
        // Text properties
        public int textColor = Color.WHITE;
        public float textSize = 16f;
        public int textStyle = Typeface.BOLD;
        public int maxLines = 2;

        // Background properties
        public int backgroundColor = Color.parseColor("#E91E63"); // Material Pink
        public float cornerRadius = 25f;

        // Border properties
        public int strokeWidth = 0;
        public int strokeColor = Color.TRANSPARENT;

        // Padding and margins
        public int paddingHorizontal = 32;
        public int paddingVertical = 16;
        public int verticalMargin = 50;  // Changed from bottomMargin to verticalMargin
        public int sideMargin = 32;

        // Size constraints
        public int minWidth = 0;
        public int minHeight = 0;

        // Shadow properties
        public float shadowRadius = 2f;
        public float shadowDx = 1f;
        public float shadowDy = 1f;
        public int shadowColor = Color.argb(100, 0, 0, 0);

        // Animation
        public boolean animateEntrance = true;
        public AnimationType animationType = AnimationType.SLIDE_UP_FADE;

        // Predefined configurations
        public static TextViewConfig success() {
            TextViewConfig config = new TextViewConfig();
            config.backgroundColor = Color.parseColor("#4CAF50"); // Green
            config.textColor = Color.WHITE;
            return config;
        }

        public static TextViewConfig error() {
            TextViewConfig config = new TextViewConfig();
            config.backgroundColor = Color.parseColor("#F44336"); // Red
            config.textColor = Color.WHITE;
            return config;
        }

        public static TextViewConfig warning() {
            TextViewConfig config = new TextViewConfig();
            config.backgroundColor = Color.parseColor("#FF9800"); // Orange
            config.textColor = Color.WHITE;
            return config;
        }

        public static TextViewConfig info() {
            TextViewConfig config = new TextViewConfig();
            config.backgroundColor = Color.parseColor("#2196F3"); // Blue
            config.textColor = Color.WHITE;
            return config;
        }

        public static TextViewConfig minimal() {
            TextViewConfig config = new TextViewConfig();
            config.backgroundColor = Color.argb(200, 0, 0, 0); // Semi-transparent black
            config.textColor = Color.WHITE;
            config.cornerRadius = 15f;
            config.paddingHorizontal = 20;
            config.paddingVertical = 10;
            config.textSize = 14f;
            return config;
        }
    }

    // Animation types
    public enum AnimationType {
        NONE,
        FADE_IN,
        FADE_OUT,
        SLIDE_UP,
        SLIDE_DOWN,
        SLIDE_UP_FADE,
        SLIDE_DOWN_FADE,
        SCALE_IN,
        SCALE_OUT
    }
}

// Usage Example in your ForYouExoPlayerRecyclerView class:
/* public class ForYouExoPlayerRecyclerView extends RecyclerView {

    private CustomBottomTextViewHelper bottomTextHelper;

    // Initialize the helper (call this from Activity/Fragment)
    public void initializeBottomTextHelper(ViewGroup rootContainer) {
        bottomTextHelper = new CustomBottomTextViewHelper(getContext(), rootContainer);
    }

    // Updated speed indicator method using custom TextView with vertical bias
    private void showSpeedIndicator(boolean show) {
        if (bottomTextHelper == null) return;

        if (show) {
            // Show 2x speed indicator at custom position (0.8 = near bottom)
            CustomBottomTextViewHelper.TextViewConfig config = CustomBottomTextViewHelper.TextViewConfig.success();
            config.animationType = CustomBottomTextViewHelper.AnimationType.SLIDE_UP_FADE;
            bottomTextHelper.showBottomText("🚀 2x Speed", 0.8f, config);
        } else {
            // Dismiss the indicator
            bottomTextHelper.dismissBottomText(CustomBottomTextViewHelper.AnimationType.SLIDE_DOWN_FADE);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // Clean up bottom text when view is detached
        if (bottomTextHelper != null) {
            bottomTextHelper.dismissBottomText();
        }

        // ... other cleanup code
    }
}

// In your Activity/Fragment:
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get root container
        ViewGroup rootContainer = findViewById(android.R.id.content);

        ForYouExoPlayerRecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.initializeBottomTextHelper(rootContainer);

        // Example usage:
        // bottomTextHelper.showBottomText("Hello World!");
        // bottomTextHelper.showBottomText("Custom Message", TextViewConfig.error());
        // bottomTextHelper.updateText("Updated Text");
        // bottomTextHelper.dismissBottomText();
    }
} */