package com.app.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;

import com.app.alhawanajd.R;

public class CheckableCardView extends CardView {

    private Context mContext;
    private boolean checked = false;

    public CheckableCardView(Context context) {
        this(context, null);
    }

    public CheckableCardView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public CheckableCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(@NonNull Context context) {
        mContext = context;


    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        if (checked) {
            setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.bg_primary_curved, null));
        } else {
            setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.bg_primary_curved_outline, null));
        }
        invalidate();
    }

    public boolean isChecked() {
        return checked;
    }
}