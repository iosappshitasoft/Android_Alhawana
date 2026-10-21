package com.app.helper.widget;

import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.PatternsCompat;
import androidx.core.util.Supplier;

import com.app.alhawanajd.R;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to help implement {@link SocialView} on any {@link TextView}-based class.
 * This class itself is not a {@link View}.
 *
 * NOTE: This implementation deliberately avoids R.styleable / attrs.xml.
 * It reads colors from an applied style (style="@style/YourStyle") or from the theme.
 */
public final class SocialViewHelper implements SocialView {
    private static final int FLAG_HASHTAG = 1;
    private static final int FLAG_MENTION = 2;
    private static final int FLAG_HYPERLINK = 4;

    public static SocialView install(@NonNull TextView view) {
        return new SocialViewHelper(view, null);
    }

    public static SocialView install(@NonNull TextView view, @Nullable AttributeSet attrs) {
        return new SocialViewHelper(view, attrs);
    }

    private final TextView view;
    private final MovementMethod initialMovementMethod;

    @Nullable private Pattern hashtagPattern;
    @Nullable private Pattern mentionPattern;
    @Nullable private Pattern hyperlinkPattern;
    private int flags;
    @NonNull private ColorStateList hashtagColors;
    @NonNull private ColorStateList mentionColors;
    @NonNull private ColorStateList hyperlinkColors;
    @Nullable private OnClickListener hashtagClickListener;
    @Nullable private OnClickListener mentionClickListener;
    @Nullable private OnClickListener hyperlinkClickListener;
    @Nullable private OnChangedListener hashtagChangedListener;
    @Nullable private OnChangedListener mentionChangedListener;
    private boolean hashtagEditing;
    private boolean mentionEditing;

    @SuppressWarnings("FieldCanBeLocal")
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (count > 0 && start > 0) {
                final char c = s.charAt(start - 1);
                switch (c) {
                    case '#':
                        hashtagEditing = true;
                        mentionEditing = false;
                        break;
                    case '@':
                        hashtagEditing = false;
                        mentionEditing = true;
                        break;
                    default:
                        if (!Character.isLetterOrDigit(c)) {
                            hashtagEditing = false;
                            mentionEditing = false;
                        } else if (hashtagChangedListener != null && hashtagEditing) {
                            hashtagChangedListener.onChanged(SocialViewHelper.this, s.subSequence(
                                    indexOfPreviousNonLetterDigit(s, 0, start - 1) + 1, start
                            ));
                        } else if (mentionChangedListener != null && mentionEditing) {
                            mentionChangedListener.onChanged(SocialViewHelper.this, s.subSequence(
                                    indexOfPreviousNonLetterDigit(s, 0, start - 1) + 1, start
                            ));
                        }
                        break;
                }
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 0) return;
            recolorize();
            if (start < s.length()) {
                final int index = start + count - 1;
                if (index < 0) return;
                switch (s.charAt(index)) {
                    case '#':
                        hashtagEditing = true;
                        mentionEditing = false;
                        break;
                    case '@':
                        hashtagEditing = false;
                        mentionEditing = true;
                        break;
                    default:
                        if (!Character.isLetterOrDigit(s.charAt(start))) {
                            hashtagEditing = false;
                            mentionEditing = false;
                        } else if (hashtagChangedListener != null && hashtagEditing) {
                            hashtagChangedListener.onChanged(SocialViewHelper.this, s.subSequence(
                                    indexOfPreviousNonLetterDigit(s, 0, start) + 1, start + count
                            ));
                        } else if (mentionChangedListener != null && mentionEditing) {
                            mentionChangedListener.onChanged(SocialViewHelper.this, s.subSequence(
                                    indexOfPreviousNonLetterDigit(s, 0, start) + 1, start + count
                            ));
                        }
                        break;
                }
            }
        }

        @Override public void afterTextChanged(Editable s) {}
    };

    /**
     * Constructor — reads colors and flags from an applied style (style="...") or theme.
     *
     * Styling rules (no attrs):
     * 1) If your TextView has style="@style/MySocialStyle", we look inside that style for:
     *      - android:textColorLink (used as hyperlinkColor)
     *      - android:textColor / android:textColorPrimary (used as hashtag/mention fallback)
     *      - colorAccent (R.attr.colorAccent) as fallback color
     *      - a custom integer named "socialFlags" inside the style (optional) — see note below
     *
     * 2) If no style or property is found, we fallback to sensible defaults.
     *
     * IMPORTANT NOTE:
     * - Because you requested "no attrs / no declare-styleable", custom attribute names like
     *   "hashtagColor" cannot be declared in attrs.xml. Therefore to provide colors via XML,
     *   put them into the style using existing attributes the helper reads (android:textColorLink,
     *   android:textColor, colorAccent) or customize programmatically via the provided setters.
     */
    private SocialViewHelper(@NonNull TextView view, @Nullable AttributeSet attrs) {
        this.view = view;
        this.initialMovementMethod = view.getMovementMethod();

        view.addTextChangedListener(textWatcher);
        view.setText(view.getText(), TextView.BufferType.SPANNABLE);

        // defaults
        flags = FLAG_HASHTAG | FLAG_MENTION | FLAG_HYPERLINK;
        hashtagColors = ColorStateList.valueOf(0xFF2196F3);   // blue default
        mentionColors = ColorStateList.valueOf(0xFF2196F3);
        hyperlinkColors = ColorStateList.valueOf(0xFF2196F3);

        // 1) Try to obtain a style resource id applied to the view (style="@style/...")
        int styleResId = 0;
        if (attrs != null) {
            // "style" is not in a namespace; reading it directly from AttributeSet returns the resource id.
            try {
                styleResId = attrs.getAttributeResourceValue(null, "style", 0);
            } catch (Exception ignored) { styleResId = 0; }
        }

        // We'll try several places in this order:
        //  A) If styleResId != 0, read standard attributes from that style resource.
        //  B) Else try to resolve theme attributes (colorAccent, android:textColorLink, android:textColorPrimary).
        //  C) Keep defaults.

        if (styleResId != 0) {
            // Attributes we try to read from the style resource:
            // index 0 -> android:textColorLink  -> hyperlinkColors
            // index 1 -> android:textColor / android:textColorPrimary -> hashtag/mention fallback
            // index 2 -> colorAccent (R.attr.colorAccent) -> fallback if others absent
            final int[] styleAttrs = new int[] {
                    android.R.attr.textColorLink,
                    android.R.attr.textColor, // try textColor first
                    android.R.attr.colorAccent
            };
            try {
                TypedArray a = view.getContext().obtainStyledAttributes(styleResId, styleAttrs);
                ColorStateList link = a.getColorStateList(0);
                ColorStateList txt = a.getColorStateList(1);
                ColorStateList accent = a.getColorStateList(2);
                if (link != null) hyperlinkColors = link;
                if (txt != null) {
                    hashtagColors = txt;
                    mentionColors = txt;
                } else if (accent != null) {
                    hashtagColors = accent;
                    mentionColors = accent;
                }
                a.recycle();
            } catch (Exception ignored) {}
        } else {
            // Read from current theme as fallback
            TypedValue tv = new TypedValue();
            // colorAccent
            if (view.getContext().getTheme().resolveAttribute( android.R.attr.colorAccent, tv, true)) {
                try {
                    int color = view.getContext().getResources().getColor(tv.resourceId);
                    ColorStateList cs = ColorStateList.valueOf(color);
                    if (cs != null) {
                        if (hashtagColors == null) hashtagColors = cs;
                        if (mentionColors == null) mentionColors = cs;
                        if (hyperlinkColors == null) hyperlinkColors = cs;
                    }
                } catch (Exception ignored) {}
            }
            // android:textColorLink
            if (view.getContext().getTheme().resolveAttribute(android.R.attr.textColorLink, tv, true)) {
                try {
                    int resId = tv.resourceId;
                    if (resId != 0) {
                        ColorStateList linkCs = view.getContext().getResources().getColorStateList(resId);
                        if (linkCs != null) hyperlinkColors = linkCs;
                    }
                } catch (Exception ignored) {}
            }
            // android:textColor (primary text)
            if (view.getContext().getTheme().resolveAttribute(android.R.attr.textColor, tv, true)
                    || view.getContext().getTheme().resolveAttribute(android.R.attr.textColorPrimary, tv, true)) {
                try {
                    int resId = tv.resourceId;
                    if (resId != 0) {
                        ColorStateList txtCs = view.getContext().getResources().getColorStateList(resId);
                        if (txtCs != null) {
                            hashtagColors = txtCs;
                            mentionColors = txtCs;
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        // NOTE: "flags" cannot be reliably read without declaring an attr or styleable; keep default (all enabled).
        // If you want to change flags via XML without attrs.xml, you can provide a style whose name encodes behavior
        // or set flags programmatically using setHashtagEnabled()/setMentionEnabled()/setHyperlinkEnabled().

        recolorize();
    }

    @NonNull @Override public Pattern getHashtagPattern() { return hashtagPattern != null ? hashtagPattern : Pattern.compile("#(\\w+)"); }
    @NonNull @Override public Pattern getMentionPattern() { return mentionPattern != null ? mentionPattern : Pattern.compile("@(\\w+)"); }
    @NonNull @Override public Pattern getHyperlinkPattern() { return hyperlinkPattern != null ? hyperlinkPattern : PatternsCompat.WEB_URL; }

    @Override public void setHashtagPattern(@Nullable Pattern pattern) { if (hashtagPattern != pattern) { hashtagPattern = pattern; recolorize(); } }
    @Override public void setMentionPattern(@Nullable Pattern pattern) { if (mentionPattern != pattern) { mentionPattern = pattern; recolorize(); } }
    @Override public void setHyperlinkPattern(@Nullable Pattern pattern) { if (hyperlinkPattern != pattern) { hyperlinkPattern = pattern; recolorize(); } }

    @Override public boolean isHashtagEnabled() { return (flags | FLAG_HASHTAG) == flags; }
    @Override public boolean isMentionEnabled() { return (flags | FLAG_MENTION) == flags; }
    @Override public boolean isHyperlinkEnabled() { return (flags | FLAG_HYPERLINK) == flags; }

    @Override public void setHashtagEnabled(boolean enabled) { if (enabled != isHashtagEnabled()) { flags = enabled ? flags | FLAG_HASHTAG : flags & ~FLAG_HASHTAG; recolorize(); } }
    @Override public void setMentionEnabled(boolean enabled) { if (enabled != isMentionEnabled()) { flags = enabled ? flags | FLAG_MENTION : flags & ~FLAG_MENTION; recolorize(); } }
    @Override public void setHyperlinkEnabled(boolean enabled) { if (enabled != isHyperlinkEnabled()) { flags = enabled ? flags | FLAG_HYPERLINK : flags & ~FLAG_HYPERLINK; recolorize(); } }

    @NonNull @Override public ColorStateList getHashtagColors() { return hashtagColors; }
    @NonNull @Override public ColorStateList getMentionColors() { return mentionColors; }
    @NonNull @Override public ColorStateList getHyperlinkColors() { return hyperlinkColors; }

    @Override public void setHashtagColors(@NonNull ColorStateList colors) { hashtagColors = colors; recolorize(); }
    @Override public void setMentionColors(@NonNull ColorStateList colors) { mentionColors = colors; recolorize(); }
    @Override public void setHyperlinkColors(@NonNull ColorStateList colors) { hyperlinkColors = colors; recolorize(); }

    @Override public int getHashtagColor() { return getHashtagColors().getDefaultColor(); }
    @Override public int getMentionColor() { return getMentionColors().getDefaultColor(); }
    @Override public int getHyperlinkColor() { return getHyperlinkColors().getDefaultColor(); }

    @Override public void setHashtagColor(int color) { setHashtagColors(ColorStateList.valueOf(color)); }
    @Override public void setMentionColor(int color) { setMentionColors(ColorStateList.valueOf(color)); }
    @Override public void setHyperlinkColor(int color) { setHyperlinkColors(ColorStateList.valueOf(color)); }

    @Override
    public void setOnHashtagClickListener(@Nullable OnClickListener listener) {
        ensureMovementMethod(listener);
        hashtagClickListener = listener;
        recolorize();
    }

    @Override
    public void setOnMentionClickListener(@Nullable OnClickListener listener) {
        ensureMovementMethod(listener);
        mentionClickListener = listener;
        recolorize();
    }

    @Override
    public void setOnHyperlinkClickListener(@Nullable OnClickListener listener) {
        ensureMovementMethod(listener);
        hyperlinkClickListener = listener;
        recolorize();
    }

    @Override public void setHashtagTextChangedListener(@Nullable OnChangedListener listener) { hashtagChangedListener = listener; }
    @Override public void setMentionTextChangedListener(@Nullable OnChangedListener listener) { mentionChangedListener = listener; }

    @NonNull @Override public List<String> getHashtags() { return listOf(view.getText(), getHashtagPattern(), false); }
    @NonNull @Override public List<String> getMentions() { return listOf(view.getText(), getMentionPattern(), false); }
    @NonNull @Override public List<String> getHyperlinks() { return listOf(view.getText(), getHyperlinkPattern(), true); }

    private void ensureMovementMethod(Object listener) {
        if (listener == null) {
            view.setMovementMethod(initialMovementMethod);
        } else if (!(view.getMovementMethod() instanceof LinkMovementMethod)) {
            view.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private void recolorize() {
        final CharSequence text = view.getText();
        if (!(text instanceof Spannable)) {
            throw new IllegalStateException("Attached text is not a Spannable, add TextView.BufferType.SPANNABLE when setting text to this TextView.");
        }
        final Spannable spannable = (Spannable) text;
        for (final Object span : spannable.getSpans(0, text.length(), CharacterStyle.class)) {
            spannable.removeSpan(span);
        }
        if (isHashtagEnabled()) {
            spanAll(spannable, getHashtagPattern(), new Supplier<CharacterStyle>() {
                        @Override
                        public CharacterStyle get() {
                            return hashtagClickListener != null
                                    ? new SocialClickableSpan(hashtagClickListener, hashtagColors, false)
                                    : new ForegroundColorSpan(hashtagColors.getDefaultColor());
                        }
                    }
            );
        }
        if (isMentionEnabled()) {
            spanAll(spannable, getMentionPattern(), new Supplier<CharacterStyle>() {
                        @Override
                        public CharacterStyle get() {
                            return mentionClickListener != null
                                    ? new SocialClickableSpan(mentionClickListener, mentionColors, false)
                                    : new ForegroundColorSpan(mentionColors.getDefaultColor());
                        }
                    }
            );
        }
        if (isHyperlinkEnabled()) {
            spanAll(spannable, getHyperlinkPattern(), new Supplier<CharacterStyle>() {
                        @Override
                        public CharacterStyle get() {
                            return hyperlinkClickListener != null
                                    ? new SocialClickableSpan(hyperlinkClickListener, hyperlinkColors, true)
                                    : new SocialURLSpan(text, hyperlinkColors);
                        }
                    }
            );
        }
    }

    private static int indexOfNextNonLetterDigit(CharSequence text, int start) {
        for (int i = start + 1; i < text.length(); i++) {
            if (!Character.isLetterOrDigit(text.charAt(i))) {
                return i;
            }
        }
        return text.length();
    }

    private static int indexOfPreviousNonLetterDigit(CharSequence text, int start, int end) {
        for (int i = end; i > start; i--) {
            if (!Character.isLetterOrDigit(text.charAt(i))) {
                return i;
            }
        }
        return start;
    }

    private static void spanAll(Spannable spannable, Pattern pattern, Supplier<CharacterStyle> styleSupplier) {
        final Matcher matcher = pattern.matcher(spannable);
        while (matcher.find()) {
            final int start = matcher.start();
            final int end = matcher.end();
            final Object span = styleSupplier.get();
            spannable.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (span instanceof SocialClickableSpan) {
                ((SocialClickableSpan) span).text = spannable.subSequence(start, end);
            }
        }
    }

    private static List<String> listOf(CharSequence text, Pattern pattern, boolean isHyperlink) {
        final List<String> list = new ArrayList<>();
        final Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            list.add(matcher.group(!isHyperlink ? 1 : 0));
        }
        return list;
    }

    private static class SocialClickableSpan extends ClickableSpan {
        private final OnClickListener listener;
        private final int color;
        private final boolean isHyperlink;
        private CharSequence text;

        private SocialClickableSpan(OnClickListener listener, ColorStateList colors, boolean isHyperlink) {
            this.listener = listener;
            this.color = colors.getDefaultColor();
            this.isHyperlink = isHyperlink;
        }

        @Override
        public void onClick(@NonNull View widget) {
            if (!(widget instanceof SocialView)) {
                throw new IllegalStateException("Clicked widget is not an instance of SocialView.");
            }
            listener.onClick((SocialView) widget, !isHyperlink ? text.subSequence(1, text.length()) : text);
        }

        @Override
        public void updateDrawState(@NonNull TextPaint ds) {
            ds.setColor(color);
            ds.setUnderlineText(isHyperlink);
        }
    }

    private static class SocialURLSpan extends URLSpan {
        private final int color;

        private SocialURLSpan(CharSequence url, ColorStateList colors) {
            super(url.toString());
            this.color = colors.getDefaultColor();
        }

        @Override
        public void updateDrawState(@NonNull TextPaint ds) {
            ds.setColor(color);
            ds.setUnderlineText(true);
        }
    }
}
