package com.app.alhawanajd;

import android.content.Context;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.app.alhawanajd.R;
import com.app.external.MediaPlayerUtils;
import com.app.external.avloading.AVLoadingIndicatorView;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.model.MusicListResponse;
import com.app.utils.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class FavMusicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = FavMusicAdapter.class.getSimpleName();
    private Context mContext;
    private List<MusicListResponse.Result> soundsList = new ArrayList<>();
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_FOOTER = 1;
    private AdapterItemClickListener clickListener;
    private int selectedPosition = -1;// no selection by default
    private int lastItemSelectedPos = -1;// no selection by default
    private int bookmarkVisibility = View.VISIBLE, nextVisibility = View.VISIBLE;
    private String soundId;

    public enum PlayState {PLAY, PAUSE}

    public FavMusicAdapter(Context mContext, List<MusicListResponse.Result> streamList, AdapterItemClickListener clickListener, String id) {
        this.mContext = mContext;
        this.soundsList = streamList;
        this.clickListener = clickListener;
        this.soundId = id;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = null;
        if (viewType == VIEW_TYPE_ITEM) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fav_music_list, parent, false);
            return new MyViewHolder(v);
        } else if (viewType == VIEW_TYPE_FOOTER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.livza_progress, parent, false);
            return new LoadingViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int pos) {
        if (viewHolder instanceof MyViewHolder) {
            MyViewHolder holder = (MyViewHolder) viewHolder;
            int position = holder.getAbsoluteAdapterPosition();
            MusicListResponse.Result item = soundsList.get(holder.getAbsoluteAdapterPosition());
            Timber.tag(TAG).d("onBindViewHolder: %s", new Gson().toJson(item));
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemLay.getLayoutParams();
//        params.width = (int) (DevicePref.getInt(DevicePref.SCREEN_WIDTH, 0) / 4.2);
//        params.height = (int) (params.width * 1.3);
            int margin = (int) mContext.getResources().getDimension(R.dimen.activity_horizontal_margin);
            float startMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, mContext.getResources().getDisplayMetrics());
            if (position == (soundsList.size() - 1)) {
                params.setMargins(margin, (int) startMargin, margin, (int) startMargin);
            } else {
                params.setMargins(margin, (int) startMargin, margin, 0);
            }
            holder.itemLay.setLayoutParams(params);

            Glide.with(mContext)
                    .load(item.getCoverImage())
                    .centerCrop()
                    .placeholder(R.drawable.music)
                    .error(R.drawable.music)
                    .into(holder.ivThumb);
            holder.txtLanguage.setVisibility(View.GONE);
            holder.txtArthurName.setVisibility(View.GONE);

            holder.btnBookMark.setSelected(item.getIsFavorite());
            holder.btnBookMark.setColorFilter(ContextCompat.getColor(mContext, R.color.colorWhite), PorterDuff.Mode.MULTIPLY);
            holder.btnBookMark.setVisibility(bookmarkVisibility);
            holder.btnNext.setVisibility(nextVisibility);

            if (soundId == null) {
                holder.tickImage.setVisibility(View.GONE);
            } else if (soundId.equals(item.getSoundId())) {
                holder.tickImage.setVisibility(View.VISIBLE);
            } else holder.tickImage.setVisibility(View.GONE);
            /*if (item.getIsFavorite()) {
                holder.btnBookMark.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.book_active));
            } else {
                holder.btnBookMark.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.book_inactive));
            }*/
            String soundName = "";
            if (!TextUtils.isEmpty(item.getSoundBy())) {
                soundName = mContext.getResources().getString(R.string.original_audio_by) + " " +
                        item.getSoundBy();
            } else {
                soundName = item.getTitle();
            }
            holder.txtTitle.setText(soundName);


//            holder.txtTitle.setText(item.getTitle());
            holder.txtDuration.setText(item.getDuration());
            holder.btnPlay.setImageResource(R.drawable.video_play);
            holder.btnPlay.setVisibility(View.VISIBLE);

            /*if (MediaPlayerUtils.isPlaying()) {
                if (item.getSoundUrl().equals(MediaPlayerUtils.getUrl())) {
                    holder.btnPlay.setImageResource(R.drawable.video_play);
                } else {
                    holder.btnPlay.setImageResource(R.drawable.video_pause);
                }
            } else {
                holder.btnPlay.setImageResource(R.drawable.video_pause);
            }*/

            if (position == lastItemSelectedPos) {
                if (MediaPlayerUtils.isPlaying()) {
                    holder.btnPlay.setImageResource(R.drawable.video_play);
                } else {
                    holder.btnPlay.setImageResource(R.drawable.video_pause);
                }
            }

            holder.btnPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedPosition = position;
                    if (lastItemSelectedPos == selectedPosition) {
                        if (MediaPlayerUtils.isPlaying()) {
                            MediaPlayerUtils.pauseMediaPlayer();
                            holder.btnPlay.setImageResource(R.drawable.video_play);
                        } else {
                            MediaPlayerUtils.playMediaPlayer(MediaPlayerUtils.getCurrentPosition());
                            holder.btnPlay.setImageResource(R.drawable.video_pause);
                        }
                    } else {
                        holder.circularProgress.setVisibility(View.VISIBLE);
                        holder.btnPlay.setVisibility(View.GONE);
                        App.pauseExternalAudio(mContext);
                        try {
                            MediaPlayerUtils.startAndPlayMediaPlayer(mContext, Uri.parse(soundsList.get(selectedPosition).getSoundUrl()), new MediaPlayerUtils.Listener() {
                                @Override
                                public void onAudioComplete() {
                                    holder.btnPlay.setImageResource(R.drawable.video_play);
                                    holder.btnPlay.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onAudioUpdate(int currentPosition) {
                                    holder.circularProgress.setVisibility(View.GONE);
                                    holder.btnPlay.setImageResource(R.drawable.video_pause);
                                    holder.btnPlay.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void setMediaDuration(int duration) {

                                }
                            }, 0);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (lastItemSelectedPos != -1) {
                            notifyItemChanged(lastItemSelectedPos);
                        }
                        lastItemSelectedPos = selectedPosition;
                        notifyItemChanged(selectedPosition);
                    }
                }
            });
            holder.btnBookMark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    item.setIsFavorite(!item.getIsFavorite());
                    holder.btnBookMark.setSelected(item.getIsFavorite());
                    clickListener.onItemClicked(item, position, Constants.TAG_FAV);
                }
            });
            holder.itemLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    soundId = item.getSoundId();
                    notifyDataSetChanged();
                    clickListener.onItemClicked(item, position, Constants.TAG_VIEW);
                }
            });
        }
    }

    public void setBookMarkVisible(int visibility) {
        bookmarkVisibility = visibility;
    }

    public void setNextVisible(int visibility) {
        nextVisibility = visibility;
    }

    @Override
    public int getItemViewType(int position) {
        return soundsList.get(position) == null ? VIEW_TYPE_FOOTER : VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return soundsList == null ? 0 : soundsList.size();
    }

    public boolean isFooter(int position) {
        return soundsList.get(position) == null;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout itemLay;
        ImageView ivThumb, ivMusic, ivLike, ivUser, btnBookMark, btnNext, btnPlay, tickImage;
        private LinearLayout likeLay, nameLay;
        TextView txtLikeCount, txtTitle, txtArthurName, txtDuration, txtLanguage;
        private ProgressBar circularProgress;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            itemLay = itemView.findViewById(R.id.itemLay);
            ivThumb = itemView.findViewById(R.id.ivThumb);
            ivMusic = itemView.findViewById(R.id.ivMusic);
            likeLay = itemView.findViewById(R.id.likeLay);
            nameLay = itemView.findViewById(R.id.nameLay);
            tickImage = itemView.findViewById(R.id.tick);
            btnBookMark = itemView.findViewById(R.id.btnBookMark);
            btnNext = itemView.findViewById(R.id.btnNext);
            ivLike = itemView.findViewById(R.id.ivLike);
            txtLikeCount = itemView.findViewById(R.id.txtLikeCount);
            ivUser = itemView.findViewById(R.id.ivUser);
            btnPlay = itemView.findViewById(R.id.btnPlay);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtArthurName = itemView.findViewById(R.id.txtArthurName);
            txtDuration = itemView.findViewById(R.id.txtDuration);
            txtLanguage = itemView.findViewById(R.id.txtLanguage);
            circularProgress = itemView.findViewById(R.id.circularProgress);
        }
    }

    public class LoadingViewHolder extends RecyclerView.ViewHolder {
        AVLoadingIndicatorView progressBar;
        FrameLayout progressLay;

        public LoadingViewHolder(View view) {
            super(view);
            progressBar = view.findViewById(R.id.progressBarNew);
            progressLay = view.findViewById(R.id.progressLay);
        }
    }
}
