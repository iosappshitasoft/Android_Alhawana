package com.app.alhawanajd;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.recyclerview.widget.RecyclerView;

import com.app.helper.BlurUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.load.resource.gif.GifOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.app.alhawanajd.R;
import com.app.external.avloading.AVLoadingIndicatorView;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.model.VideoListResponse;
import com.app.utils.DevicePref;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import timber.log.Timber;

public class VideosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = VideosAdapter.class.getSimpleName();
    private Context mContext;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_FOOTER = 1;
    List<VideoListResponse.Result> videoList;
    private AdapterItemClickListener clickListener;
    private String wayType;

    public VideosAdapter(Context mContext, List<VideoListResponse.Result> videoList, AdapterItemClickListener clickListener, String type) {
        this.mContext = mContext;
        this.videoList = videoList;
        this.clickListener = clickListener;
        this.wayType = type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_streams, parent, false);
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
            int position = viewHolder.getAbsoluteAdapterPosition();
            VideoListResponse.Result item = videoList.get(holder.getAbsoluteAdapterPosition());
//            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemLay.getLayoutParams();
            float startMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.4f, mContext.getResources().getDisplayMetrics());
//            params.width = (int) (DevicePref.getInstance(mContext).getInt(DevicePref.SCREEN_WIDTH, 0) / 3);
           /* params.height = (int) ((DevicePref.getInstance(mContext).getInt(DevicePref.SCREEN_WIDTH, 0) / 3) * 1.4);
            if (position % 3 == 0) {
                params.setMargins(0, 0, (int) startMargin, (int) startMargin);
            } else if ((position + 1) % 3 == 0) {
                params.setMargins((int) startMargin, 0, 0, (int) startMargin);
            } else {
                params.setMargins(0, 0, 0, (int) startMargin);
            }--->previously working fine*/

//            float centerMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, mContext.getResources().getDisplayMetrics());
//           // params.height = 180;
//            params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 180, mContext.getResources().getDisplayMetrics());
//
//            if (position % 2 == 0) { // Left column
//                params.setMargins(0, 0, (int) (centerMargin / 2), (int) centerMargin); // Add half of the center margin to the right
//            } else { // Right column
//                params.setMargins((int) (centerMargin / 2), 0, 0, (int) centerMargin); // Add half of the center margin to the left
//            }
//            holder.itemLay.setLayoutParams(params);

            int screenWidth = DevicePref.getInstance(mContext).getInt(DevicePref.SCREEN_WIDTH, 0);
            int itemWidth = (int) (screenWidth * 0.28); // 30% of screen width

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemLay.getLayoutParams();
            params.width = itemWidth;

            int heightInDp = 200;
            float itemHeightPx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    heightInDp,
                    mContext.getResources().getDisplayMetrics()
            );
            params.height = (int) itemHeightPx;

// Overall margin (left, top, right, bottom)
//            int marginInDp = 8;
//            int marginPx = (int) TypedValue.applyDimension(
//                    TypedValue.COMPLEX_UNIT_DIP,
//                    marginInDp,
//                    mContext.getResources().getDisplayMetrics()
//            );
//            params.setMargins(marginPx, marginPx, marginPx, marginPx);

            holder.itemLay.setLayoutParams(params);



            holder.txtLikeCount.setText("" + item.getLikes());

            Timber.tag(TAG).d("item.getGifUrl: %s", item.getGifUrl());
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.set(GifOptions.DISABLE_ANIMATION, false);


            String thumb_nall = item.getStreamThumbnail();
            Log.d(TAG, "onBindViewHolder_thumb_nall: "+thumb_nall);
            Log.d(TAG, "onBindViewHolder_thumb_nall name: "+ item.getName() +"," + item.getPostedBy() +"," +item.getStreamName() +"," );




            /*Glide.with(mContext)
                    .asGif()
                    .apply(requestOptions)
                    .load(item.getGifUrl())
                    .centerCrop()
                    .placeholder(R.drawable.video_placeholder)
                    .error(R.drawable.video_placeholder)
//                    .override(params.width, params.height)
                    .into(holder.ivThumb);*/
            Glide.with(mContext)
                    .load(item.getUserImage())
                    .centerCrop()
                    .placeholder(R.drawable.default_profile_image)
                    .error(R.drawable.default_profile_image)
                    .into(holder.ivUser);
            holder.ivLike.setVisibility(View.VISIBLE);
            holder.txtLikeCount.setText("" + item.getLikes());

            holder.userName.setText("UserName");
            if (item.getVideoStatus() == null || item.getVideoStatus().equals("Done")) {

                //  holder.overLay.setVisibility(View.GONE);
                holder.headerLinearLay.setVisibility(View.GONE);
                holder.likeLay.setVisibility(View.VISIBLE);
                holder.ivUser.setVisibility(View.VISIBLE);
                Glide.with(mContext)
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
                        .into(holder.ivThumb);
            } else {
                //  holder.overLay.setVisibility(View.VISIBLE);
                holder.headerLinearLay.setVisibility(View.VISIBLE);
                holder.likeLay.setVisibility(View.GONE);
                holder.ivUser.setVisibility(View.GONE);
                Glide.with(mContext)
                        .load(item.getStreamThumbnail())
                        .error(R.drawable.novideo_placeholder)
                        .centerCrop()
                        .into(holder.ivThumb);
            }
            holder.itemLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //clickListener.onItemClicked(item, position, Constants.TAG_VIDEO);
                    if (item.getVideoStatus() == null || item.getVideoStatus().equals("Done")) {
                        clickListener.onItemClicked(item, position, wayType);
                    } else {
                        clickListener.onItemClicked(item, position, wayType);
                        //  Toasty.error(mContext, mContext.getString(R.string.video_not_uploaded), Toasty.LENGTH_SHORT).show();
                    }
                }
            });


            // Capture the background of userLay
          /*  Glide.with(mContext)
                    .asBitmap() // Load the image as a Bitmap
                    .load(item.getGifUrl()) // Background image URL
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            holder.itemLay.post(() -> {
                                // Get the position of userLay relative to ivThumb
                                int[] userLayLocation = new int[2];
                                holder.userLay.getLocationOnScreen(userLayLocation);

                                int[] ivThumbLocation = new int[2];
                                holder.ivThumb.getLocationOnScreen(ivThumbLocation);

                                int left = userLayLocation[0] - ivThumbLocation[0];
                                int top = userLayLocation[1] - ivThumbLocation[1];
                                int width = holder.userLay.getWidth();
                                int height = holder.userLay.getHeight();

                                // Safeguard against invalid dimensions
                                left = Math.max(0, left);
                                top = Math.max(0, top);

                                if (left + width > resource.getWidth()) {
                                    width = resource.getWidth() - left;
                                }

                                if (top + height > resource.getHeight()) {
                                    height = resource.getHeight() - top;
                                }

                                // Ensure width and height are valid
                                if (width > 0 && height > 0) {
                                    // Crop the specific portion of the image for userLay
                                    Bitmap croppedBitmap = Bitmap.createBitmap(resource, left, top, width, height);

                                    // Apply blur to the cropped portion
                                    Bitmap blurredBitmap = BlurUtil.blur(mContext, croppedBitmap, 15f);

                                    // Create a rounded drawable from the blurred bitmap
                                    RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(mContext.getResources(), blurredBitmap);
                                    roundedDrawable.setCornerRadius(20f); // Set the desired corner radius
                                    roundedDrawable.setAntiAlias(true);

                                    // Set the rounded drawable as the background of userLay
                                    holder.userLay.setBackground(roundedDrawable);
                                } else {
                                    // Handle the case where width or height is invalid (e.g., fallback or log)
                                    Log.e(TAG, "Invalid crop dimensions: width=" + width + ", height=" + height);
                                }
                            });
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // Clear resources if needed
                        }
                    });*/

          /*  Glide.with(mContext)
                    .asBitmap() // Load the image as a Bitmap
                    .load(item.getGifUrl()) // Background image URL
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            holder.itemLay.post(() -> {
                                // Get the position of userLay relative to ivThumb
                                int[] userLayLocation = new int[2];
                                holder.likeLay.getLocationOnScreen(userLayLocation);

                                int[] ivThumbLocation = new int[2];
                                holder.ivThumb.getLocationOnScreen(ivThumbLocation);

                                int left = userLayLocation[0] - ivThumbLocation[0];
                                int top = userLayLocation[1] - ivThumbLocation[1];
                                int width = holder.likeLay.getWidth();
                                int height = holder.likeLay.getHeight();

                                // Safeguard against invalid dimensions
                                left = Math.max(0, left);
                                top = Math.max(0, top);

                                if (left + width > resource.getWidth()) {
                                    width = resource.getWidth() - left;
                                }

                                if (top + height > resource.getHeight()) {
                                    height = resource.getHeight() - top;
                                }

                                // Ensure width and height are valid
                                if (width > 0 && height > 0) {
                                    // Crop the specific portion of the image for userLay
                                    Bitmap croppedBitmap = Bitmap.createBitmap(resource, left, top, width, height);

                                    // Apply blur to the cropped portion
                                    Bitmap blurredBitmap = BlurUtil.blur(mContext, croppedBitmap, 15f);

                                    // Create a rounded drawable from the blurred bitmap
                                    RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(mContext.getResources(), blurredBitmap);
                                    roundedDrawable.setCornerRadius(20f); // Set the desired corner radius
                                    roundedDrawable.setAntiAlias(true);

                                    // Set the rounded drawable as the background of userLay
                                    holder.likeLay.setBackground(roundedDrawable);
                                } else {
                                    // Handle the case where width or height is invalid (e.g., fallback or log)
                                    Log.e(TAG, "Invalid crop dimensions: width=" + width + ", height=" + height);
                                }
                            });
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // Clear resources if needed
                        }
                    });*/

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