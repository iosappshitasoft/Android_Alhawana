package com.app.view;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.google.android.material.textview.MaterialTextView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CustomSpannableTextView extends MaterialTextView {
    public CustomSpannableTextView(@NonNull @NotNull Context context) {
        super(context);
    }

    public CustomSpannableTextView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSpannableTextView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomSpannableTextView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void makeLinks(List<Pair<String, OnClickListener>> links) {
        SpannableString spannableString = new SpannableString(this.getText());
        for (Pair<String, OnClickListener> it : links) {
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View view) {
                    Selection.setSelection(((Spannable) ((TextView) view).getText()), 0);
                    view.invalidate();
                    it.second.onClick(view);
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            };
            final int startIndexOfLink = this.getText().toString().indexOf(it.first != null ? it.first : null);

            Log.d("CustomTextView", "Index from " + startIndexOfLink + " to " + startIndexOfLink + it.first.length() +
                    "Message--> " + this.getText().toString());
            if (startIndexOfLink != -1) {
                spannableString.setSpan(clickableSpan, startIndexOfLink, startIndexOfLink + it.first.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableString.setSpan(new StyleSpan(Typeface.BOLD), startIndexOfLink, startIndexOfLink + it.first.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

        }

        this.setMovementMethod(LinkMovementMethod.getInstance());
        this.setText(spannableString, BufferType.SPANNABLE);
    }


}
