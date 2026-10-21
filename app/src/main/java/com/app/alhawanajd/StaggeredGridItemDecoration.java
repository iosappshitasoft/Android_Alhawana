package com.app.alhawanajd;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// 5. Alternative fix - Add ItemDecoration to ensure proper spacing
public class StaggeredGridItemDecoration extends RecyclerView.ItemDecoration {
    private final int spacing;

    public StaggeredGridItemDecoration(int spacing) {
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {

        int position = parent.getChildAdapterPosition(view); // item position
        int spanCount = 2; // since you are using 2 columns in StaggeredGridLayoutManager
        int column = position % spanCount; // item column

        outRect.left = spacing - column * spacing / spanCount;
        outRect.right = (column + 1) * spacing / spanCount;
        outRect.top = spacing;
        outRect.bottom = spacing;
    }
}
