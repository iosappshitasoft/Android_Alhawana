package com.app.alhawanajd.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.alhawanajd.R;
import com.app.audiotrimmer.views.CircleImageView;
import com.app.audiotrimmer.views.CustomTextView;
import com.app.model.ShareItem;
import com.bumptech.glide.Glide;

import java.util.List;

public class ShareAdapter extends RecyclerView.Adapter<ShareAdapter.ViewHolder> {

    public List<ShareItem> list;
    OnShareClick listener;

    public interface OnShareClick {
        void onClick(ShareItem item);
    }

    public ShareAdapter(List<ShareItem> list, OnShareClick listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.share_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShareItem item = list.get(position);

        if (item.hasImageUrl()) {
            // Load URL (recent chats)
            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.default_profile_image)
                    .into(holder.icon);
        } else {
            // Load drawable (apps + info options)
            holder.icon.setImageResource(item.getIconRes());
        }

        holder.name.setText(item.getName());

        holder.itemView.setOnClickListener(v -> listener.onClick(item));
    }


    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView icon;
        CustomTextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.itemIcon);
            name = itemView.findViewById(R.id.itemName);
        }
    }
}
