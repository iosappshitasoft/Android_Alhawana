package com.app.helper;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;

// TODO: 23/10/21 @VishnuKumar
public class CustomLinearLayoutManager extends LinearLayoutManager {
    private boolean isScrollEnabled = true;
    private BottomSheetDialog sheetDialog;

    public CustomLinearLayoutManager(Context context) {
        super(context);
    }

    public CustomLinearLayoutManager(Context context, BottomSheetDialog sheetDialog) {
        super(context);
        this.sheetDialog = sheetDialog;
    }

    public CustomLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public CustomLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setSheetDialog(BottomSheetDialog sheetDialog) {
        this.sheetDialog = sheetDialog;
    }

    public void setScrollEnabled(boolean flag) {
        this.isScrollEnabled = flag;
    }

    @Override
    public boolean canScrollHorizontally() {
        if (sheetDialog != null && sheetDialog.isShowing()) {
            return false;
        }
        return isScrollEnabled && super.canScrollHorizontally();
    }

    @Override
    public boolean canScrollVertically() {
        if (sheetDialog != null && sheetDialog.isShowing()) {
            return false;
        }
        return isScrollEnabled && super.canScrollVertically();
    }
}
