package com.app.alhawanajd;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.app.external.avloading.AVLoadingIndicatorView;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.model.VideoListResponse;
import com.app.utils.DevicePref;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.load.resource.gif.GifOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import timber.log.Timber;

public class VideosAdapter2 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = VideosAdapter2.class.getSimpleName();
    private Context mContext;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_FOOTER = 1;
    List<VideoListResponse.Result> videoList;
    private AdapterItemClickListener clickListener;
    private String wayType;

    public VideosAdapter2(Context mContext, List<VideoListResponse.Result> videoList, AdapterItemClickListener clickListener, String type) {
        this.mContext = mContext;
        this.videoList = videoList;
        this.clickListener = clickListener;
        this.wayType = type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_streams2, parent, false);
            return new MyViewHolder(view);
        } else if (viewType == VIEW_TYPE_FOOTER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.livza_progress, parent, false);
            return new LoadingViewHolder(view);
        }
        return null;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(RecyclerView.@NotNull ViewHolder viewHolder, int pos) {
        if (viewHolder instanceof MyViewHolder) {
            MyViewHolder holder = (MyViewHolder) viewHolder;
           // int position = viewHolder.getAbsoluteAdapterPosition();
            // VideoListResponse.Result item = videoList.get(holder.getAbsoluteAdapterPosition());
            int position = holder.getBindingAdapterPosition();
            //Log.e("MyVideosFragment","CLICKED POSITION: "+ position);

            VideoListResponse.Result item = videoList.get(holder.getBindingAdapterPosition());
            int screenWidth = DevicePref.getInstance(mContext).getInt(DevicePref.SCREEN_WIDTH, 0);
            int itemWidth = (int) (screenWidth * 0.43);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemLay.getLayoutParams();
            params.width = itemWidth;

            int heightInDp = 200;
            float itemHeightPx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    heightInDp,
                    mContext.getResources().getDisplayMetrics()
            );
            params.height = (int) itemHeightPx;
            holder.itemLay.setLayoutParams(params);
            holder.txtLikeCount.setText("" + item.getLikes());

            Timber.tag(TAG).d("item.getGifUrl: %s", item.getGifUrl());
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.set(GifOptions.DISABLE_ANIMATION, false);


            String thumb_nall = item.getStreamThumbnail();
            Log.d(TAG, "onBindViewHolder_thumb_nall: "+thumb_nall);
            Log.d(TAG, "onBindViewHolder_thumb_nall name: "+ item.getName() +"," + item.getPostedBy() +"," +item.getStreamName() +"," );

            Glide.with(mContext)
                    .load(item.getUserImage())
                    .centerCrop()
                    .placeholder(R.drawable.default_profile_image)
                    .error(R.drawable.default_profile_image)
                    .into(holder.ivUser);
            holder.ivLike.setVisibility(View.VISIBLE);
            holder.txtLikeCount.setText("" + item.getLikes());

            holder.userName.setText(item.getUserName());
            if (item.getVideoStatus() == null || item.getVideoStatus().equals("Done")) {

                //  holder.overLay.setVisibility(View.GONE);
                holder.headerLinearLay.setVisibility(View.GONE);
                holder.likeLay.setVisibility(View.VISIBLE);
                holder.ivUser.setVisibility(View.VISIBLE);
              /*  Glide.with(mContext)
                        .asGif()
                        .load(item.getGifUrl()) // Local uri present in the device
                        .apply(requestOptions)
                        .sizeMultiplier(0.5f)
                        .placeholder(R.drawable.bg_card_curve)
                        .error(R.drawable.bg_card_curve)
                        .addListener(new RequestListener<GifDrawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                                Timber.e("onLoadFailed: %s", e.getMessage());
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                                Timber.d("onResourceReady: ");
                                return false;
                            }
                        })
                        .centerCrop()
                        .into(holder.ivThumb);*/

                Glide.with(mContext)
                        .load(item.getStreamThumbnail())
                        .into(holder.ivThumb);
            } else {
                //  holder.overLay.setVisibility(View.VISIBLE);
                holder.headerLinearLay.setVisibility(View.VISIBLE);
                holder.likeLay.setVisibility(View.GONE);
                holder.ivUser.setVisibility(View.GONE);
               /* Glide.with(mContext)
                        .load(item.getStreamThumbnail())
                        .error(R.drawable.novideo_placeholder)
                        .centerCrop()
                        .into(holder.ivThumb);*/
                Glide.with(mContext)
                        .load(item.getStreamThumbnail())
                        .error(R.drawable.novideo_placeholder)
                        .into(holder.ivThumb);
            }
            holder.itemLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "sssssss: ");
                    //clickListener.onItemClicked(item, position, Constants.TAG_VIDEO);
                    if (item.getVideoStatus() == null || item.getVideoStatus().equals("Done")) {
                        clickListener.onItemClicked(item, position, wayType);
                    } else {
                        clickListener.onItemClicked(item, position, wayType);
                        //  Toasty.error(mContext, mContext.getString(R.string.video_not_uploaded), Toasty.LENGTH_SHORT).show();
                    }
                }
            });

        } else if (viewHolder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) viewHolder;
            loadingViewHolder.progressBar.setVisibility(View.VISIBLE);
        }
    }

    private Bitmap captureView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    @Override
    public int getItemViewType(int position) {
        return videoList.get(position) == null ? VIEW_TYPE_FOOTER : VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return videoList == null ? 0 : videoList.size();
    }

    public boolean isFooter(int position) {
        return videoList.size() > 0 && videoList.size() - 1 <= position && videoList.get(position) == null;
    }

    public static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public AVLoadingIndicatorView progressBar;
        FrameLayout progressLay;

        public LoadingViewHolder(View view) {
            super(view);
            progressBar = view.findViewById(R.id.progressBarNew);
            progressLay = view.findViewById(R.id.progressLay);
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout itemLay, overLay;
        private ImageView ivThumb, ivLike, ivUser;
        private LinearLayout likeLay,userLay;
        private RelativeLayout headerLinearLay;
        private TextView txtLikeCount , textview_layout,userName;

        public MyViewHolder(View view) {
            super(view);

            itemLay = itemView.findViewById(R.id.itemLay);
            ivThumb = itemView.findViewById(R.id.ivThumb);
            likeLay = itemView.findViewById(R.id.likeLay);
            userLay = itemView.findViewById(R.id.userLay);
            ivLike = itemView.findViewById(R.id.ivLike);
            txtLikeCount = itemView.findViewById(R.id.txtLikeCount);
            ivUser = itemView.findViewById(R.id.ivUser);
            overLay = itemView.findViewById(R.id.overLay);
            headerLinearLay = itemView.findViewById(R.id.headerLinearLay);
            userName = itemLay.findViewById(R.id.userName);
            // textview_layout = itemLay.findViewById(R.id.textview_layout);

        }
    }
}