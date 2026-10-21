package com.app.alhawanajd.home;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;

import com.app.alhawanajd.VideoListActivity;
import com.app.alhawanajd.profile.OthersProfileActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpandableSpannableHelper {

    private static final int DEFAULT_MAX_LINES = 2;
    private static final String MORE = " ... More";
    private static final String LESS = " Less";

    // Hashtag supports Arabic and other unicode letters
    private static final Pattern HASHTAG_PATTERN = Pattern.compile("(#|＃)([\\p{L}\\p{M}0-9_]+)");
    private static final Pattern MENTION_PATTERN = Pattern.compile("@([\\p{L}\\p{M}0-9_]+)");
    private static final Pattern URL_PATTERN = Patterns.WEB_URL;

    /**
     * Public entry
     */
    public static void setExpandableText(final TextView textView, final String fullText) {
        setExpandableText(textView, fullText, DEFAULT_MAX_LINES);
    }

    public static void setExpandableText(final TextView textView, final String fullText, final int maxLinesBeforeMore) {
        // ensure movement method so ClickableSpans work
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setFocusable(false);
        textView.setClickable(true);

        // Post to ensure layout is measured
        textView.post(new Runnable() {
            @Override
            public void run() {
                // Temporarily set full text to measure lines
                textView.setText(fullText);
                textView.setMaxLines(Integer.MAX_VALUE);

                Layout layout = textView.getLayout();
                if (layout == null) {
                    // fallback: directly set full spannable
                    SpannableString spannable = new SpannableString(fullText);
                    applySpans(textView.getContext(), spannable, 0, spannable.length(), textView);
                    textView.setText(spannable);
                    return;
                }

                int lineCount = layout.getLineCount();
                if (lineCount <= maxLinesBeforeMore) {
                    // No need to collapse
                    SpannableString spannable = new SpannableString(fullText);
                    applySpans(textView.getContext(), spannable, 0, spannable.length(), textView);
                    textView.setText(spannable);
                    textView.setMaxLines(Integer.MAX_VALUE);
                } else {
                    // Need to collapse to maxLinesBeforeMore and append "... More"
                    int endIndex = layout.getLineEnd(maxLinesBeforeMore - 1);
                    if (endIndex > fullText.length()) endIndex = fullText.length();

                    String visible = fullText.substring(0, endIndex).trim();

                    // Avoid breaking a URL/hashtag at the end - trim back to last space if necessary
                    // If last char of visible is part of non-space and the next char in fullText is not a space, attempt trim back
                    if (endIndex < fullText.length()) {
                        // safer trim: back to last whitespace within visible (up to 15 chars)
                        int trimPos = Math.max(0, visible.length() - 1);
                        while (trimPos > 0 && !Character.isWhitespace(visible.charAt(trimPos)) && (visible.length() - trimPos) <= 30) {
                            trimPos--;
                        }
                        if (trimPos > 0) {
                            visible = visible.substring(0, trimPos).trim();
                        }
                    }

                    final String truncatedWithMore = visible + MORE;
                    SpannableString spannable = new SpannableString(truncatedWithMore);

                    // Apply clickable spans for content portion only
                    applySpans(textView.getContext(), spannable, 0, visible.length(), textView);

                    // Add More clickable
                    int moreStart = truncatedWithMore.length() - MORE.length();
                    int moreEnd = truncatedWithMore.length();
                    spannable.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            expandText(textView, fullText, maxLinesBeforeMore);
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setUnderlineText(false);
                        }
                    }, moreStart, moreEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    textView.setText(spannable);
                    textView.setMaxLines(maxLinesBeforeMore);
                }

                // Auto direction alignment (first strong)
                textView.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG);
                textView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            }
        });
    }

    private static void expandText(final TextView textView, final String fullText, final int maxLinesBeforeMore) {
        final String fullWithLess = fullText + LESS;
        SpannableString spannable = new SpannableString(fullWithLess);

        // apply spans to entire fullText
        applySpans(textView.getContext(), spannable, 0, fullText.length(), textView);

        // Add Less clickable
        int lessStart = fullWithLess.length() - LESS.length();
        int lessEnd = fullWithLess.length();
        spannable.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                // collapse
                setExpandableText(textView, fullText, maxLinesBeforeMore);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        }, lessStart, lessEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(spannable);
        textView.setMaxLines(Integer.MAX_VALUE);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
    }

    /**
     * Apply hashtag/mention/url ClickableSpan within the given range [start, end)
     */
    private static void applySpans(Context context, SpannableString spannable, int start, int end, TextView textView) {
        if (start < 0) start = 0;
        if (end > spannable.length()) end = spannable.length();
        String substring = spannable.subSequence(start, end).toString();

        // URLs
        Matcher urlMatcher = URL_PATTERN.matcher(substring);
        while (urlMatcher.find()) {
            final int s = urlMatcher.start() + start;
            final int e = urlMatcher.end() + start;
            final String urlText = spannable.subSequence(s, e).toString();
            spannable.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    try {
                        String urlToOpen = urlText;
                        if (!urlToOpen.startsWith("http://") && !urlToOpen.startsWith("https://")) {
                            urlToOpen = "http://" + urlToOpen;
                        }
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(urlToOpen));
                        context.startActivity(i);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(true);
                }
            }, s, e, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // Hashtags
        Matcher tagMatcher = HASHTAG_PATTERN.matcher(substring);
        while (tagMatcher.find()) {
            final int s = tagMatcher.start() + start;
            final int e = tagMatcher.end() + start;
            final String tagText = spannable.subSequence(s, e).toString(); // includes #
            spannable.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    try {
                        String tag = tagText.replace("#", "").replace("＃", "");
                        Intent intent = new Intent(context, VideoListActivity.class);
                        intent.putExtra("hashtag", tag);
                        context.startActivity(intent);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            }, s, e, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // Mentions
        Matcher mentionMatcher = MENTION_PATTERN.matcher(substring);
        while (mentionMatcher.find()) {
            final int s = mentionMatcher.start() + start;
            final int e = mentionMatcher.end() + start;
            final String mentionText = spannable.subSequence(s, e).toString(); // includes @
            spannable.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    try {
                        String username = mentionText.replace("@", "");
                        Intent intent = new Intent(context, OthersProfileActivity.class);
                        intent.putExtra("username", username);
                        context.startActivity(intent);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            }, s, e, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}
