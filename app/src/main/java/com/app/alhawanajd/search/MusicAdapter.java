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
import com.app.helper.callback.AdapterItemClickListener;
import com.app.model.MusicListResponse;
import com.app.utils.Constants;
import com.app.utils.DevicePref;

import java.util.ArrayList;
import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {
    private static final String TAG = MusicAdapter.class.getSimpleName();
    private Context mContext;
    private List<MusicListResponse.Result> streamList = new ArrayList<>();
    private AdapterItemClickListener clickListener;

    public MusicAdapter(Context mContext, List<MusicListResponse.Result> streamList, AdapterItemClickListener clickListener) {
        this.mContext = mContext;
        this.streamList = streamList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public MusicAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_music, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        int position = holder.getAbsoluteAdapterPosition();
        MusicListResponse.Result item = streamList.get(holder.getAbsoluteAdapterPosition());
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemLay.getLayoutParams();
        params.width = (int) (DevicePref.getInstance(mContext).getInt(DevicePref.SCREEN_WIDTH, 0) / 4.2);
        params.height = (int) (params.width * 1.3);
        int margin = (int) mContext.getResources().getDimension(R.dimen.activity_horizontal_margin);
        float startMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, mContext.getResources().getDisplayMetrics());

        if (position == 0) {
            params.setMargins(margin, 0, 0, 0);
        } else if (position == (streamList.size() - 1)) {
            params.setMargins((int) startMargin, 0, margin, 0);
        } else {
            params.setMargins((int) startMargin, 0, 0, 0);
        }
        holder.itemLay.setLayoutParams(params);

        Glide.with(mContext)
                .load(item.getCoverImage())
                .centerCrop()
                .placeholder(R.drawable.novideo_placeholder)
                .error(R.drawable.novideo_placeholder)
                .into(holder.ivThumb);
        /*holder.txtTitle.setText(item.getTitle());
        if (item.getIsFavorite()) {
            holder.txtSave.setText(mContext.getString(R.string.saved));
            holder.ivSave.setVisibility(View.GONE);
        } else {
            holder.txtSave.setText(mContext.getString(R.string.save));
            holder.ivSave.setVisibility(View.VISIBLE);
        }*/
        holder.itemLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.onItemClicked(item, position, Constants.TAG_VIDEO);
            }
        });
    }

    @Override
    public int getItemCount() {
        return streamList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout itemLay;
        ImageView ivThumb, ivMusic, ivLike, ivUser, ivSave;
        private LinearLayout likeLay, nameLay, saveLay;
        TextView txtLikeCount, txtTitle, txtArthurName, txtSave;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemLay = itemView.findViewById(R.id.itemLay);
            ivThumb = itemView.findViewById(R.id.ivThumb);
            ivMusic = itemView.findViewById(R.id.ivMusic);
            likeLay = itemView.findViewById(R.id.likeLay);
            nameLay = itemView.findViewById(R.id.nameLay);
            saveLay = itemView.findViewById(R.id.saveLay);
            ivSave = itemView.findViewById(R.id.ivSave);
            ivLike = itemView.findViewById(R.id.ivLike);
            txtLikeCount = itemView.findViewById(R.id.txtLikeCount);
            ivUser = itemView.findViewById(R.id.ivUser);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtArthurName = itemView.findViewById(R.id.txtArthurName);
            txtSave = itemView.findViewById(R.id.txtSave);
        }
    }
}
