package com.app.alhawanajd.search;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.app.alhawanajd.R;
import com.app.alhawanajd.App;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.model.VideoListResponse;
import com.app.utils.Constants;
import com.app.utils.DevicePref;

import java.util.ArrayList;
import java.util.List;

public class StreamsAdapter extends RecyclerView.Adapter<StreamsAdapter.ViewHolder> {
    private static final String TAG = StreamsAdapter.class.getSimpleName();
    private Context mContext;
    private List<VideoListResponse.Result> streamList = new ArrayList<>();
    private AdapterItemClickListener clickListener;

    public StreamsAdapter(Context mContext, List<VideoListResponse.Result> streamList, AdapterItemClickListener clickListener) {
        this.mContext = mContext;
        this.streamList = streamList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public StreamsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_streams, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        int position = holder.getAbsoluteAdapterPosition();
        VideoListResponse.Result item = streamList.get(holder.getAbsoluteAdapterPosition());

        int screenWidth = DevicePref.getInstance(mContext).getInt(DevicePref.SCREEN_WIDTH, 0);
        int itemWidth = (int) (screenWidth * 0.35); // 80% of screen width

        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemLay.getLayoutParams();
        params.width = itemWidth;
        int heightInDp = 250; // or whatever height you want in dp

        float itemHeightPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                heightInDp,
                mContext.getResources().getDisplayMetrics()
        );

        params.height = (int) itemHeightPx;

        holder.itemLay.setLayoutParams(params);
        holder.likeLay.setVisibility(View.VISIBLE);
//        holder.ivLike.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.heart));
//        holder.itemLay.setLayoutParams(params);
        holder.txtLikeCount.setText("" + item.getLikes());
        holder.txtUserName.setText("" + item.getUserName());

        Glide.with(mContext)
                .load(item.getStreamThumbnail())
                .placeholder(R.drawable.bg_card_curve)
                .error(R.drawable.bg_card_curve)
                .into(holder.ivThumb);

        Glide.with(mContext)
                .load(item.getUserImage())
                .centerCrop()
                .placeholder(R.drawable.default_profile_image)
                .error(R.drawable.default_profile_image)
                .into(holder.ivUser);

        holder.ivLike.setVisibility(View.VISIBLE);
        holder.txtLikeCount.setText("" + item.getLikes());
        holder.txtUserName.setText("" + item.getUserName());
//        holder.btnFollow.setText(item.getName());
        holder.itemLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.preventMultipleClick(holder.itemLay);
                clickListener.onItemClicked(item, position, Constants.TAG_SINGLE_VIDEO);
            }
        });
    }

    @Override
    public int getItemCount() {
        return streamList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout itemLay;
        private ImageView ivThumb, ivLike, ivUser;
        private LinearLayout likeLay;
        private TextView txtLikeCount,txtUserName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemLay = itemView.findViewById(R.id.itemLay);
            ivThumb = itemView.findViewById(R.id.ivThumb);
            likeLay = itemView.findViewById(R.id.likeLay);
            ivLike = itemView.findViewById(R.id.ivLike);
            txtLikeCount = itemView.findViewById(R.id.txtLikeCount);
            ivUser = itemView.findViewById(R.id.ivUser);
            txtUserName = itemView.findViewById(R.id.userName);
        }
    }
}
