package com.app.helper.viewpageranimation;

import android.view.View;

import androidx.viewpager.widget.ViewPager;

public class FlipHorizontalPageTransformer implements ViewPager.PageTransformer {
    @Override
    public void transformPage(View page, float pos) {
        final float rotation = 180f * pos;

        page.setAlpha(rotation > 90f || rotation < -90f ? 0 : 1);
        page.setPivotX(page.getWidth() * 0.5f);
        page.setPivotY(page.getHeight() * 0.5f);
        page.setRotationY(rotation);
    }
}
