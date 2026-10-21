package com.app.utils;

import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AliasingDrawableWrapper extends DrawableWrapper {
    private static final DrawFilter DRAW_FILTER =
            new PaintFlagsDrawFilter(Paint.FILTER_BITMAP_FLAG, 0);

    /**
     * Creates a new wrapper around the specified drawable.
     *
     * @param wrapped the drawable to wrap
     */
    public AliasingDrawableWrapper(@Nullable Drawable wrapped) {
        super(wrapped);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        DrawFilter oldDrawFilter = canvas.getDrawFilter();
        canvas.setDrawFilter(DRAW_FILTER);
        super.draw(canvas);
        canvas.setDrawFilter(oldDrawFilter);
    }
}