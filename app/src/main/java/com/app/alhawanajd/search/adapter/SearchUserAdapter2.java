package com.app.alhawanajd.search.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.alhawanajd.App;
import com.app.alhawanajd.R;
import com.app.external.avloading.AVLoadingIndicatorView;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.model.GetSet;
import com.app.model.UserList;
import com.app.utils.Constants;
import com.app.utils.api.ApiInterface;
import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SearchUserAdapter2 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = SearchUserAdapter2.class.getSimpleName();
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_FOOTER = 1;
    List<UserList.Result> usersList = new ArrayList<>();
    private boolean showLoading = false;
    private Context mContext;
    private AdapterItemClickListener clickListener;
    ApiInterface apiInterface;

    public SearchUserAdapter2(Context context, List<UserList.Result> peopleLists, AdapterItemClickListener clickListener, ApiInterface apiInterface) {
        this.usersList = peopleLists;
        this.mContext = context;
        this.clickListener = clickListener;
        this.apiInterface =apiInterface;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_user_2, parent, false);
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
                        holder.txtFollow.setBackgroundResource(R.drawable.bg_following); // 🔁 set followed bg
                    } else {
                        holder.txtFollow.setText(mContext.getString(R.string.follow_new));
                        holder.txtFollow.setBackgroundResource(R.drawable.bg_follow1); // 🔁 set default bg
                    }
                }
            }
            // HORIZONTAL IMPORTANT
            LinearLayoutManager manager = new LinearLayoutManager(holder.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false);

            holder.recyclerView.setLayoutManager(manager);

            RecentPostVideoAdapter adapter = new RecentPostVideoAdapter(holder.itemView.getContext(), item.getRecentPost(),apiInterface);
            holder.recyclerView.setAdapter(adapter);

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
            holder.txtFollow.setOnClickListener(view -> {
                if (item.getUserId().equals(GetSet.getUserId())) {
                    clickListener.onItemClicked(item, position, Constants.TAG_USERS);
                } else {
                    item.setFollow(!item.isFollow());
                    notifyItemChanged(position);
                    clickListener.onItemClicked(item, position, Constants.TAG_FOLLOW);
                }
            });

            holder.btnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    usersList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, usersList.size());
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
        private TextView txtFollow,btnRemove;
        RecyclerView recyclerView ;

        public MyViewHolder(View view) {
            super(view);
            userImage = view.findViewById(R.id.userImage);
            premiumImage = view.findViewById(R.id.premiumImage);
            fullName = view.findViewById(R.id.fullName);
            userName = view.findViewById(R.id.userName);
            itemCardView = view.findViewById(R.id.itemCardView);
            btnFollow = view.findViewById(R.id.btnFollow);
            btnRemove = view.findViewById(R.id.txtRemove);
            txtFollow = view.findViewById(R.id.txtFollow);
            recyclerView = view.findViewById(R.id.recyclerView2);
        }
    }
}