package com.app.external;

import android.content.Context;
import android.text.Editable;
import android.text.Selection;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.appcompat.widget.AppCompatEditText;

/**
 * Created by hitasoft on 12/2/16.
 */
public class CustomEditText extends AppCompatEditText {

    private Context context;
    private AttributeSet attrs;
    private int defStyle;

    public CustomEditText(Context context) {
        super(context);
        this.context = context;
        // init();
    }

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.attrs = attrs;
        //init();
    }

    public CustomEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        this.attrs = attrs;
        this.defStyle = defStyle;
    }


    public void setSelection(int index) {
        Selection.setSelection(getText(), index);
    }

    @Override
    public Editable getText() {
        return super.getText();
    }

    OnKeyPreImeListener onKeyPreImeListener;

    public void setOnKeyPreImeListener(OnKeyPreImeListener onKeyPreImeListener) {
        this.onKeyPreImeListener = onKeyPreImeListener;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        /*&& event.getAction() == KeyEvent.ACTION_UP*/
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            if (onKeyPreImeListener != null)
                onKeyPreImeListener.onBackPressed();
            return false;
        }
        return super.dispatchKeyEvent(event);
    }

    public interface OnKeyPreImeListener {
        void onBackPressed();
    }

}