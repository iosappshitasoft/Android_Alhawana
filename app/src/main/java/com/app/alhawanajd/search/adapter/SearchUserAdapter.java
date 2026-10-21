package com.app.alhawanajd.search.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.app.alhawanajd.R;
import com.app.external.avloading.AVLoadingIndicatorView;
import com.app.alhawanajd.App;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.model.GetSet;
import com.app.model.UserList;
import com.app.utils.Constants;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SearchUserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = SearchUserAdapter.class.getSimpleName();
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_FOOTER = 1;
    List<UserList.Result> usersList = new ArrayList<>();
    private boolean showLoading = false;
    private Context mContext;
    private AdapterItemClickListener clickListener;

    public SearchUserAdapter(Context context, List<UserList.Result> peopleLists, AdapterItemClickListener clickListener) {
        this.usersList = peopleLists;
        this.mContext = context;
        this.clickListener = clickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_user, parent, false);
            return new MyViewHolder(view);
        } else if (viewType == VIEW_TYPE_FOOTER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.livza_progress, parent, false);
            return new LoadingViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.@NotNull ViewHolder viewHolder, int pos) {
        int position = viewHolder.getAbsoluteAdapterPosition();
        if (viewHolder instanceof MyViewHolder) {
            final UserList.Result item = usersList.get(position);
//            Log.d(TAG, "onBindViewHolder: " + new Gson().toJson(item));
            MyViewHolder holder = (MyViewHolder) viewHolder;
            holder.fullName.setText(item.getName());
            holder.userName.setText(String.format("@%s", item.getUserName()));

//            if (item.getUserId().equals(GetSet.getUserId())) {
//                holder.btnFollow.setSelected(false);
//                holder.txtFollow.setText(mContext.getString(R.string.view));
//            } else {
//                if (Constants.Login_method.equals("skip")){
//                    holder.btnFollow.setVisibility(View.GONE);
//                }else {
//                    holder.btnFollow.setSelected(item.isFollow());
//                    if (item.isFollow()) {
//                        holder.txtFollow.setText(mContext.getString(R.string.following));
//                        holder.btnFollow.setBackgroundResource(R.drawable.bg_following);
//                    } else {
//                        holder.txtFollow.setText(mContext.getString(R.string.follow_new));
//                        holder.btnFollow.setBackgroundResource(R.drawable.bg_card_curve1);
//                    }
//                }
//            }

            if (item.getUserId().equals(GetSet.getUserId())) {
                holder.btnFollow.setSelected(false);
                holder.txtFollow.setText(mContext.getString(R.string.view_txt));
                holder.btnFollow.setBackgroundResource(R.drawable.bg_following); // default bg
            } else {
                if (Constants.Login_method.equals("skip")){
                    holder.btnFollow.setVisibility(View.GONE);
                } else {
                    holder.btnFollow.setSelected(item.isFollow());
                    if (item.isFollow()) {
                        Log.d(TAG, "onBindViewHolderyess: ");
                        holder.txtFollow.setText(mContext.getString(R.string.following));
                        holder.btnFollow.setBackgroundResource(R.drawable.bg_following); // 🔁 set followed bg
                    } else {
                        holder.txtFollow.setText(mContext.getString(R.string.follow_new));
                        holder.btnFollow.setBackgroundResource(R.drawable.bg_follow1); // 🔁 set default bg
                    }
                }
            }


            Glide.with(mContext).load(item.getUserImage())
                    .placeholder(R.drawable.social_icon_svg)
                    .error(R.drawable.social_icon_svg)
//                    .override(holder.userImage.getWidth(), holder.userImage.getHeight())
                    .into(holder.userImage);
            holder.premiumImage.setVisibility(item.isPrimeMember() ? View.VISIBLE : View.GONE);

            holder.itemCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    App.preventMultipleClick(holder.itemCardView);
                    clickListener.onItemClicked(item, position, Constants.TAG_USERS);
                }
            });
            holder.btnFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (item.getUserId().equals(GetSet.getUserId())) {
                        clickListener.onItemClicked(item, position, Constants.TAG_USERS);
                    } else {
                        item.setFollow(!item.isFollow());
                        notifyItemChanged(position);
                        clickListener.onItemClicked(item, position, Constants.TAG_FOLLOW);
                    }
                }
            });

        }
    }

    @Override
    public int getItemViewType(int position) {
        return usersList.get(position) == null ? VIEW_TYPE_FOOTER : VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return usersList == null ? 0 : usersList.size();
    }

    public boolean isPositionFooter(int position) {
        return position == getItemCount() - 1 && showLoading;
    }

    public void showLoading(boolean value) {
        showLoading = value;
    }

    public class LoadingViewHolder extends RecyclerView.ViewHolder {
        public AVLoadingIndicatorView progressBar;
        FrameLayout progressLay;

        public LoadingViewHolder(View view) {
            super(view);
            progressBar = view.findViewById(R.id.progressBarNew);
            progressLay = view.findViewById(R.id.progressLay);
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView userImage, premiumImage;
        TextView fullName, userName;
        FrameLayout itemCardView;
//        private Button btnFollow;
        private LinearLayout btnFollow;
        private TextView txtFollow;

        public MyViewHolder(View view) {
            super(view);
            userImage = view.findViewById(R.id.userImage);
            premiumImage = view.findViewById(R.id.premiumImage);
            fullName = view.findViewById(R.id.fullName);
            userName = view.findViewById(R.id.userName);
            itemCardView = view.findViewById(R.id.itemCardView);
            btnFollow = view.findViewById(R.id.btnFollow);
            txtFollow = view.findViewById(R.id.txtFollow);
        }
    }
}