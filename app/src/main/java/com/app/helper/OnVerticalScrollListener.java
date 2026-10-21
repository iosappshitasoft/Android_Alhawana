package com.app.helper;

import androidx.recyclerview.widget.RecyclerView;

public class OnVerticalScrollListener extends RecyclerView.OnScrollListener {

    @Override
    public final void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (!recyclerView.canScrollVertically(-1)) {
            onScrolledToTop();
        } else if (!recyclerView.canScrollVertically(1)) {
            onScrolledToBottom();
        } else if (dy < 0) {
            onScrolledUp(dx, dy);
        } else if (dy > 0) {
            onScrolledDown(dx, dy);
        }
    }

    public void onScrolledUp(int dx, int dy) {}

    public void onScrolledDown(int dx, int dy) {}

    public void onScrolledToTop() {}

    public void onScrolledToBottom() {}
}
