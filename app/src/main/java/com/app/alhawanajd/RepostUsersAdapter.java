package com.app.alhawanajd;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.model.GetSet;
import com.app.model.HomeResponse;
import com.bumptech.glide.Glide;

import java.util.List;

public class RepostUsersAdapter extends RecyclerView.Adapter<RepostUsersAdapter.ViewHolder> {

    private Context context;
    private List<HomeResponse.Result.RepostedData> list;

    public RepostUsersAdapter(Context context, List<HomeResponse.Result.RepostedData> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_repost_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HomeResponse.Result.RepostedData data = list.get(position);

        if (GetSet.getUserId().equals(data.getUserId())) {
            holder.desc.setVisibility(View.VISIBLE);
            holder.chatIcon.setVisibility(View.GONE);
        } else {
            holder.desc.setVisibility(View.GONE);
            holder.chatIcon.setVisibility(View.VISIBLE);
        }

        holder.name.setText(data.getDisplayName());
        holder.desc.setText(data.getDescription());

        Glide.with(context)
                .load(data.getUserImage())
                .placeholder(R.drawable.default_profile_image)
                .into(holder.profile);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profile,chatIcon;
        TextView name, desc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profile = itemView.findViewById(R.id.profileImage);
            chatIcon = itemView.findViewById(R.id.chatIcons);
            name = itemView.findViewById(R.id.userName);
            desc = itemView.findViewById(R.id.repostedText);
        }
    }
}
