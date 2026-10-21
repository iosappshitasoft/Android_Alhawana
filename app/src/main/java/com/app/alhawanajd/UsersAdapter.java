package com.app.alhawanajd;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.app.alhawanajd.R;
import com.app.db.DBHelper;
import com.app.helper.NetworkReceiver;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.model.GetSet;
import com.app.model.UserList;
import com.app.utils.Constants;
import com.app.utils.DevicePref;
import com.app.utils.SharedPref;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    private static final String TAG = UsersAdapter.class.getSimpleName();
    private Context mContext;
    private List<UserList.Result> usersList = new ArrayList<>();
    private AdapterItemClickListener clickListener;

    public UsersAdapter(Context mContext, List<UserList.Result> usersList, AdapterItemClickListener clickListener) {
        this.mContext = mContext;
        this.usersList = usersList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public UsersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_users, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int index) {
        int position = holder.getAbsoluteAdapterPosition();
        UserList.Result item = usersList.get(position);
//        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemLay.getLayoutParams();
//        params.width = (int) (DevicePref.getInstance(mContext).getInt(DevicePref.SCREEN_WIDTH, 0) / 2);
//        params.height = (int) (DevicePref.getInstance(mContext).getInt(DevicePref.SCREEN_WIDTH, 0) / 2);
//        int margin = (int) mContext.getResources().getDimension(R.dimen.tiny);
//        params.setMargins(margin, 0, 0, 0);
//        if (position == usersList.size() - 1) {
//            params.setMargins(margin, 0, margin, 0);
//        }
//        holder.itemLay.setLayoutParams(params);

//        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemLay.getLayoutParams();
//        params.width = (int) (DevicePref.getInstance(mContext).getInt(DevicePref.SCREEN_WIDTH, 0) * 0.45);
//        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
//        holder.itemLay.setLayoutParams(params);

        int screenWidth = DevicePref.getInstance(mContext).getInt(DevicePref.SCREEN_WIDTH, 0);
        int itemWidth = (int) (screenWidth * 0.43); // 80% of screen width

        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemLay.getLayoutParams();
        params.width = itemWidth;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        holder.itemLay.setLayoutParams(params);



        Glide.with(mContext)
                .load(item.getUserImage())
                .centerCrop()
                .placeholder(R.drawable.social_icon_svg)
                .error(R.drawable.social_icon_svg)
                .into(holder.profileImage);
        holder.premiumImage.setVisibility(item.isPrimeMember() ? View.VISIBLE : View.GONE);
        holder.txtName.setText(item.getName());
        holder.txtUserName.setText(item.getUserName());

        if (Constants.Login_method.equals("skip")){
            holder.btnFollow.setVisibility(View.GONE);
        }else {
            holder.btnFollow.setVisibility(View.VISIBLE);
        }
//        if (item.getUserId().equals(GetSet.getUserId())) {
//            holder.btnFollow.setSelected(false);
//            holder.btnFollow.setText(mContext.getString(R.string.view));
//        } else {
//            holder.btnFollow.setSelected(item.isFollow());
//            if (item.isFollow()) {
//                holder.btnFollow.setText(mContext.getString(R.string.following));
//            } else {
//                holder.btnFollow.setText(mContext.getString(R.string.follow_new));
//            }
//        }

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


        holder.itemLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.preventMultipleClick(holder.itemLay);
                clickListener.onItemClicked(item, position, Constants.TAG_USERS);
            }
        });

        holder.btnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NetworkReceiver.isConnected()) {
                    if (Constants.Login_method.equals("skip")) {
                        GetSet.reset();
                        SharedPref.clearAll();
                        DBHelper.getInstance(mContext).clearDB();
                        Intent logout = new Intent(mContext, LoginActivity.class);
                        logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(logout);
                    } else {
                        if (item.getUserId().equals(GetSet.getUserId())) {
                            clickListener.onItemClicked(item, position, Constants.TAG_USERS);
                        } else {
                            item.setFollow(!item.isFollow());
                            notifyItemChanged(position);
                            clickListener.onItemClicked(item, position, Constants.TAG_FOLLOW);
                        }
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        FrameLayout itemLay;
        ImageView profileImage, premiumImage;
        TextView txtName,txtUserName;
//        Button btnFollow;
        LinearLayout btnFollow;
        TextView txtFollow;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemLay = itemView.findViewById(R.id.itemLay);
            profileImage = itemView.findViewById(R.id.profileImage);
            premiumImage = itemView.findViewById(R.id.premiumImage);
            txtName = itemView.findViewById(R.id.txtName);
            txtUserName = itemView.findViewById(R.id.txtName1);
            btnFollow = itemView.findViewById(R.id.btnFollow);
            progressBar = itemView.findViewById(R.id.progressBar);
            txtFollow = itemView.findViewById(R.id.txtFollow);
        }
    }
}
