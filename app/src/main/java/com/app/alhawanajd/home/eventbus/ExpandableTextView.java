package com.app.alhawanajd.home.eventbus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Patterns;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

import com.app.alhawanajd.VideoListActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpandableTextView extends AppCompatTextView {

    private static final int COLLAPSED_MAX_LINES = 2;
    private boolean isExpanded = false;
    private String fullText = "";

    public ExpandableTextView(Context context) {
        super(context);
        init();
    }

    public ExpandableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ExpandableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setMovementMethod(LinkMovementMethod.getInstance());
        setTextDirection(View.TEXT_DIRECTION_ANY_RTL);
        setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
    }

    public void setText(final String text, final OnToggleListener listener) {
        fullText = text;
        isExpanded = false;

        setMaxLines(COLLAPSED_MAX_LINES);
        setHashtagAndLinks(fullText);

        post(() -> {
            Layout layout = getLayout();
            if (layout != null && layout.getLineCount() > COLLAPSED_MAX_LINES) {
                // Append "More"
                showCollapsed(listener);
            } else {
                // Text fits → no toggle
                setText(fullText);
            }
        });

        setOnClickListener(v -> {
            if (getTag() != null && getTag().equals("toggle")) {
                if (isExpanded) {
                    showCollapsed(listener);
                } else {
                    showExpanded(listener);
                }
            }
        });
    }

    private void showCollapsed(OnToggleListener listener) {
        isExpanded = false;
        int lineEnd = getLayout().getLineEnd(COLLAPSED_MAX_LINES - 1);
        lineEnd = Math.min(lineEnd, fullText.length());

        String visibleText = fullText.substring(0, lineEnd).trim() + "... More";
        setHashtagAndLinks(visibleText);

        setTag("toggle");
        if (listener != null) listener.onToggled(false);
    }

    private void showExpanded(OnToggleListener listener) {
        isExpanded = true;
        setHashtagAndLinks(fullText + " Less");

        setTag("toggle");
        if (listener != null) listener.onToggled(true);
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    private void setHashtagAndLinks(String text) {
        SpannableString spannable = new SpannableString(text);
        Context context = getContext();

        // Hashtags
        Pattern hashtagPattern = Pattern.compile("#([\\p{L}\\p{N}_]+)");
        Matcher matcher = hashtagPattern.matcher(text);
        while (matcher.find()) {
            final String tag = matcher.group(1);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Intent intent = new Intent(context, VideoListActivity.class);
                    intent.putExtra("tag", tag);
                    context.startActivity(intent);
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    ds.setColor(Color.CYAN);
                    ds.setUnderlineText(false);
                }
            };
            spannable.setSpan(clickableSpan, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // URLs
        Matcher urlMatcher = Patterns.WEB_URL.matcher(text);
        while (urlMatcher.find()) {
            final String url = urlMatcher.group();
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        context.startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    ds.setColor(Color.BLUE);
                    ds.setUnderlineText(true);
                }
            };
            spannable.setSpan(clickableSpan, urlMatcher.start(), urlMatcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        setText(spannable);
    }

    public interface OnToggleListener {
        void onToggled(boolean expanded);
    }
}

/*

public class ExpandableTextView extends androidx.appcompat.widget.AppCompatTextView {

    private static final String MORE = "...More";
    private static final String LESS = " Less";

    private boolean isExpanded = false;
    private int maxCollapsedLines = 2;
    private String fullText;

    public ExpandableTextView(Context context) {
        super(context);
        init();
    }

    public ExpandableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ExpandableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setMovementMethod(LinkMovementMethod.getInstance());
        setTextDirection(View.TEXT_DIRECTION_ANY_RTL);
        setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        setOnClickListener(v -> toggle());
    }

    public void setText(String text, int maxLines, boolean expanded) {
        this.fullText = text;
        this.maxCollapsedLines = maxLines;
        this.isExpanded = expanded;
        updateText();
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    private void toggle() {
        isExpanded = !isExpanded;
        updateText();
    }

    private void updateText() {
        if (TextUtils.isEmpty(fullText)) {
            setText("");
            return;
        }

        if (!isExpanded) {
            // Collapse
            post(() -> {
                Layout layout = getLayout();
                if (layout == null) return;

                if (layout.getLineCount() > maxCollapsedLines) {
                    int lineEndIndex = layout.getLineEnd(maxCollapsedLines - 1);
                    lineEndIndex = Math.min(lineEndIndex, fullText.length());
                    String visibleText = fullText.substring(0, lineEndIndex).trim() + MORE;

                    SpannableString spannable = new SpannableString(visibleText);
                    makeLinksAndHashtagsClickable(spannable, visibleText);

                    // "More" clickable
                    spannable.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View widget) {
                            isExpanded = true;
                            updateText();
                        }

                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            ds.setColor(Color.CYAN);
                            ds.setUnderlineText(false);
                        }
                    }, visibleText.length() - MORE.length(), visibleText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    setText(spannable);
                } else {
                    // Text fits → just hashtags & links
                    SpannableString spannable = new SpannableString(fullText);
                    makeLinksAndHashtagsClickable(spannable, fullText);
                    setText(spannable);
                }
            });
        } else {
            // Expanded → full text with "Less"
            String displayText = fullText + LESS;
            SpannableString spannable = new SpannableString(displayText);
            makeLinksAndHashtagsClickable(spannable, fullText);

            // "Less" clickable
            spannable.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    isExpanded = false;
                    updateText();
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    ds.setColor(Color.CYAN);
                    ds.setUnderlineText(false);
                }
            }, displayText.length() - LESS.length(), displayText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            setText(spannable);
        }
    }

    private void makeLinksAndHashtagsClickable(SpannableString spannable, String text) {
        Context context = getContext();

        // Hashtags
        Pattern hashtagPattern = Pattern.compile("#([\\p{L}\\p{N}_]+)");
        Matcher matcher = hashtagPattern.matcher(text);
        while (matcher.find()) {
            final String tag = matcher.group(1);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    Intent intent = new Intent(context, VideoListActivity.class);
                    intent.putExtra("tag", tag);
                    context.startActivity(intent);
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    ds.setColor(Color.CYAN);
                    ds.setUnderlineText(false);
                }
            };
            spannable.setSpan(clickableSpan, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // URLs
        Matcher urlMatcher = Patterns.WEB_URL.matcher(text);
        while (urlMatcher.find()) {
            final String url = urlMatcher.group();
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        context.startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    ds.setColor(Color.BLUE);
                    ds.setUnderlineText(true);
                }
            };
            spannable.setSpan(clickableSpan, urlMatcher.start(), urlMatcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}
*/
