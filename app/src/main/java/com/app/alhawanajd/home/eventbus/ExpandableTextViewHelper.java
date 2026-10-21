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
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.app.alhawanajd.VideoListActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.view.ViewTreeObserver;


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
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpandableTextViewHelper {

    private static final int MAX_LINES = 2;

    public static void setExpandableText(final TextView shortTextView,
                                         final TextView fullTextView,
                                         final TextView toggleView,
                                         final String fullText,
                                         final boolean isExpanded) {

        if (TextUtils.isEmpty(fullText)) {
            shortTextView.setText("");
            fullTextView.setText("");
            toggleView.setVisibility(View.GONE);
            return;
        }

        // Full TextView
        fullTextView.setText(fullText);
        //fullTextView.setVisibility(View.GONE);
        fullTextView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        makeLinksAndHashtagsClickable(fullTextView, fullText);

        // Short TextView
        shortTextView.setMaxLines(MAX_LINES);
        shortTextView.setText(fullText);
        //newly added
        makeLinksAndHashtagsClickable(shortTextView, fullText);
        shortTextView.setVisibility(isExpanded ? View.GONE : View.VISIBLE);

        /*shortTextView.post(() -> {
            Layout layout = shortTextView.getLayout();
            if (layout == null) return;

            // Calculate the truncated text for 2 lines
            int lineEndIndex = layout.getLineEnd(Math.min(MAX_LINES - 1, layout.getLineCount() - 1));
            lineEndIndex = Math.min(lineEndIndex, fullText.length());
            CharSequence truncatedText = fullText.subSequence(0, lineEndIndex);

            // Ellipsize to fit MAX_LINES exactly
            CharSequence ellipsized = TextUtils.ellipsize(
                    truncatedText,
                    shortTextView.getPaint(),
                    shortTextView.getWidth() * MAX_LINES,
                    TextUtils.TruncateAt.END
            );

            shortTextView.setText(ellipsized);

            // Check if toggle needed
            if (fullText.length() > ellipsized.length()) {
                toggleView.setVisibility(View.VISIBLE);
                toggleView.setText("More");
                makeLinksAndHashtagsClickable(shortTextView, ellipsized.toString());
            } else {
                toggleView.setVisibility(View.GONE);
                makeLinksAndHashtagsClickable(shortTextView, fullText);
            }

            // RTL support
            shortTextView.setTextDirection(View.TEXT_DIRECTION_ANY_RTL);
            shortTextView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        });*/

        // Wait until layout is ready to measure lines
        shortTextView.post(() -> {
            Layout layout = shortTextView.getLayout();
            if (layout == null) return;

            if (layout.getLineCount() > MAX_LINES) {
                toggleView.setVisibility(View.VISIBLE);
                toggleView.setText(isExpanded ? "Less" : "More");

                if (!isExpanded) {
                    // Truncate text to MAX_LINES for shortTextView
                    int lineEndIndex = layout.getLineEnd(MAX_LINES - 1);
                    lineEndIndex = Math.min(lineEndIndex, fullText.length());
                    String visibleText = fullText.substring(0, lineEndIndex).trim();
                    shortTextView.setText(visibleText);
                    makeLinksAndHashtagsClickable(shortTextView, visibleText);
                }
            } else {
                toggleView.setVisibility(View.GONE);
            }
        });

        toggleView.setOnClickListener(v -> {
            if (fullTextView.getVisibility() == View.GONE) {
                // Show full text
                shortTextView.setVisibility(View.GONE);
                fullTextView.setVisibility(View.VISIBLE);
                makeLinksAndHashtagsClickable(fullTextView, fullText);
                toggleView.setText("Less");
            } else {
                // Show short text
                shortTextView.setVisibility(View.VISIBLE);
                fullTextView.setVisibility(View.GONE);
                // Re-truncate short text
                shortTextView.post(() -> {
                    Layout layout = shortTextView.getLayout();
                    if (layout == null) return;
                    int lineEndIndex = layout.getLineEnd(MAX_LINES - 1);
                    lineEndIndex = Math.min(lineEndIndex, fullText.length());
                    String visibleText = fullText.substring(0, lineEndIndex).trim();
                    shortTextView.setText(visibleText);
                    makeLinksAndHashtagsClickable(shortTextView, visibleText);
                });
               /*

               shortTextView.post(() -> {
                    Layout layout = shortTextView.getLayout();
                    if (layout == null) return;
                    int lineEndIndex = layout.getLineEnd(Math.min(MAX_LINES - 1, layout.getLineCount() - 1));
                    lineEndIndex = Math.min(lineEndIndex, fullText.length());
                    CharSequence truncatedText = fullText.subSequence(0, lineEndIndex);
                    CharSequence ellipsized = TextUtils.ellipsize(
                            truncatedText,
                            shortTextView.getPaint(),
                            shortTextView.getWidth() * MAX_LINES,
                            TextUtils.TruncateAt.END
                    );
                    shortTextView.setText(ellipsized);
                    makeLinksAndHashtagsClickable(shortTextView, ellipsized.toString());
                });*/
                toggleView.setText("More");
            }
        });
    }

    private static void makeLinksAndHashtagsClickable(TextView textView, String text) {
        SpannableString spannable = new SpannableString(text);
        Context context = textView.getContext();

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

        textView.setText(spannable);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        // RTL support
        textView.setTextDirection(View.TEXT_DIRECTION_ANY_RTL);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
    }
}

/*


public class ExpandableTextViewHelper {

    private static final int MAX_LINES = 2;

    public static void setExpandableText(final TextView shortTextView,
                                         final TextView fullTextView,
                                         final TextView toggleView,
                                         final String fullText) {

        if (TextUtils.isEmpty(fullText)) {
            shortTextView.setText("");
            fullTextView.setText("");
            toggleView.setVisibility(View.GONE);
            return;
        }

        // Full TextView setup
        fullTextView.setText(fullText);
        fullTextView.setVisibility(View.GONE);
        makeLinksAndHashtagsClickable(fullTextView, fullText);

        // Short TextView setup
        shortTextView.setMaxLines(MAX_LINES);
        shortTextView.setText(fullText);
        makeLinksAndHashtagsClickable(shortTextView, fullText);

        shortTextView.post(() -> {
            Layout layout = shortTextView.getLayout();
            if (layout == null) return;

            // Determine if text exceeds 2 lines
            if (layout.getLineCount() > MAX_LINES) {
                toggleView.setVisibility(View.VISIBLE);
                toggleView.setText("More");

                // Truncate text to MAX_LINES
                int lineEndIndex = layout.getLineEnd(MAX_LINES - 1);
                lineEndIndex = Math.min(lineEndIndex, fullText.length());
                String visibleText = fullText.substring(0, lineEndIndex).trim();
                shortTextView.setText(visibleText);
                makeLinksAndHashtagsClickable(shortTextView, fullText);
            } else {
                toggleView.setVisibility(View.GONE);
                shortTextView.setText(fullText);
                makeLinksAndHashtagsClickable(shortTextView, fullText);
            }
        });

        toggleView.setOnClickListener(v -> {
            if (fullTextView.getVisibility() == View.GONE) {
                shortTextView.setVisibility(View.GONE);
                fullTextView.setVisibility(View.VISIBLE);
                makeLinksAndHashtagsClickable(fullTextView, fullText);
                toggleView.setText("Less");
            } else {
                shortTextView.setVisibility(View.VISIBLE);
                fullTextView.setVisibility(View.GONE);
                makeLinksAndHashtagsClickable(shortTextView, fullText);
                toggleView.setText("More");
            }
        });
    }

    */
/**
     * Make hashtags (#tag) and URLs clickable with RTL support
     *//*

    private static void makeLinksAndHashtagsClickable(TextView textView, String text) {
        SpannableString spannable = new SpannableString(text);
        Context context = textView.getContext();

        // Hashtags
        Pattern hashtagPattern = Pattern.compile("#([\\p{L}\\p{N}_]+)");
        Matcher matcher = hashtagPattern.matcher(text);
        while (matcher.find()) {
            final String tag = matcher.group(1);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    // Handle hashtag click
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

        textView.setText(spannable);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        // RTL support
        textView.setTextDirection(View.TEXT_DIRECTION_ANY_RTL);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
    }
}

*/

/*

public class ExpandableTextViewHelper {

    public static void setExpandableText(final TextView shortTextView,
                                         final TextView fullTextView,
                                         final TextView toggleView,
                                         final String fullText) {

        if (TextUtils.isEmpty(fullText)) {
            shortTextView.setText("");
            fullTextView.setText("");
            toggleView.setVisibility(View.GONE);
            return;
        }

        // Full TextView setup
        fullTextView.setText(fullText);
        fullTextView.setVisibility(View.GONE);
        fullTextView.setMovementMethod(LinkMovementMethod.getInstance());
        setHashtagAndLinks(fullTextView, fullText);

        // Short TextView initial setup
        shortTextView.setMaxLines(2);
        shortTextView.setMovementMethod(LinkMovementMethod.getInstance());
        shortTextView.setText(fullText);

        // Wait until layout is ready to measure lines
     */
/*   shortTextView.post(() -> {
            Layout layout = shortTextView.getLayout();
            if (layout == null) return;

            if (layout.getLineCount() > 2) {
                toggleView.setVisibility(View.VISIBLE);
                toggleView.setText("More");
                int lineEndIndex = layout.getLineEnd(1); // end of 2nd line
                lineEndIndex = Math.min(lineEndIndex, fullText.length()); // clamp to avoid crash
                String visibleText = fullText.substring(0, lineEndIndex).trim();

                shortTextView.setText(visibleText);
              //  setHashtagAndLinks(shortTextView, visibleText);


                setHashtagAndLinks(shortTextView, fullText);
            } else {
                setHashtagAndLinks(shortTextView, fullText);
                toggleView.setVisibility(View.GONE);
            }
        });*//*


        shortTextView.post(() -> {
            Layout layout = shortTextView.getLayout();
            if (layout == null) return;

            if (layout.getLineCount() > 2) {   // if lines exceed 2 → show toggle
                toggleView.setVisibility(View.VISIBLE);
                toggleView.setText("More");
            } else {
                toggleView.setVisibility(View.GONE);
            }

            // Always set hashtags/links
            setHashtagAndLinks(shortTextView, fullText);
        });

        // Toggle click listener
        toggleView.setOnClickListener(v -> {
            if (fullTextView.getVisibility() == View.GONE) {
                shortTextView.setVisibility(View.GONE);
                fullTextView.setVisibility(View.VISIBLE);
                setHashtagAndLinks(fullTextView, fullText);
                fullTextView.setMovementMethod(LinkMovementMethod.getInstance());
                toggleView.setText("Less");
            } else {

                shortTextView.setVisibility(View.VISIBLE);
                fullTextView.setVisibility(View.GONE);
                setHashtagAndLinks(shortTextView, fullText);
                shortTextView.setMovementMethod(LinkMovementMethod.getInstance());
                toggleView.setText("More");
            }
        });
    }

  */
/*  private static void setHashtagAndLinks(TextView textView, String text) {
        SpannableString spannable = new SpannableString(text);
        Context context = textView.getContext();

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

        textView.setText(spannable);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }*//*


    private static void setHashtagAndLinks(TextView textView, String text) {
        SpannableString spannable = new SpannableString(text);
        Context context = textView.getContext();

        // Hashtags
        Pattern hashtagPattern = Pattern.compile("#([\\p{L}\\p{N}_]+)");
        Matcher matcher = hashtagPattern.matcher(text);
        while (matcher.find()) {
            final String tag = matcher.group(1);
            spannable.setSpan(new ClickableSpan() {
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
            }, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // URLs
        Matcher urlMatcher = Patterns.WEB_URL.matcher(text);
        while (urlMatcher.find()) {
            final String url = urlMatcher.group();
            spannable.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        context.startActivity(intent);
                    } catch (Exception e) { e.printStackTrace(); }
                }
                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    ds.setColor(Color.BLUE);
                    ds.setUnderlineText(true);
                }
            }, urlMatcher.start(), urlMatcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        textView.setText(spannable);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

}
*/
