package com.app.alhawanajd.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;
import com.app.alhawanajd.R;
import com.app.external.TimeAgo;
import com.app.alhawanajd.App;
import com.app.helper.NetworkReceiver;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.model.GetSet;
import com.app.model.VideoCommentResponse;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.view.CustomSpannableTextView;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private static final String TAG = CommentAdapter.class.getSimpleName();
    private ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
    Context mContext;
    ArrayList<VideoCommentResponse.Result> commendList;
    String getVideoId, publisherId;
    int getPosition;
    TimeAgo timeAgo;
    private AdapterItemClickListener clickListener;
    private String loadMoreCheckVideoCommand = "initial";

    public CommentAdapter(Context mContext, ArrayList<VideoCommentResponse.Result> commendList, String publisherId, String videoId, int adapterPosition, AdapterItemClickListener clickListener) {
        this.mContext = mContext;
        this.commendList = commendList;
        this.getVideoId = videoId;
        this.getPosition = adapterPosition;
        this.publisherId = publisherId;
        this.clickListener = clickListener;
        timeAgo = new TimeAgo(mContext);
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new CommentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int pos) {
        int position = holder.getAbsoluteAdapterPosition();
       /* Glide.with(mContext)
                .load(commendList.get(position).getUserImage())
                .placeholder(R.drawable.default_profile_image)
                .error(R.drawable.default_profile_image)
                .into(holder.profileImage);*/
        Glide.with(mContext)
                .load(commendList.get(position).getUserImage())
                .placeholder(R.drawable.default_profile_image)
                .error(R.drawable.default_profile_image)
                .into(holder.profileImageNew);

        holder.userName.setText(commendList.get(position).getName());

        /*holder.userComment.setText(commendList.get(position).getComment());*/

        String timeText = timeAgo.timeAgo(AppUtils.utcToMillis(commendList.get(position).getCommentPostedTime()));
        String getHash = commendList.get(position).getComment();
        holder.messageDateandTime.setText(timeText);
        holder.userComment.setText(getHash + "\t" + " " /*+ timeText*/);
        VideoCommentResponse.Result data = getItem(position);
        final List<Pair<String, View.OnClickListener>> tagsList = new ArrayList<>();
        for (int i = 0; i < data.getTaggedUser().size(); i++) {
            String tagItem = data.getTaggedUser().get(i);
            if (!TextUtils.isEmpty(tagItem)) {
                tagsList.add(Pair.create("@" + tagItem, v -> {
                    clickListener.onItemClicked(tagItem, position, Constants.TAG_USER_NAME);
                }));
            }
        }
        if (tagsList.size() > 0) {
            holder.userComment.makeLinks(tagsList);
        }


        if (commendList.get(position).getUserId().equals(GetSet.getUserId()) || publisherId.equals(GetSet.getUserId()))
            holder.deleteIcon.setVisibility(View.VISIBLE);
        else holder.deleteIcon.setVisibility(View.GONE);

        holder.profileImage.setOnClickListener(v -> {
            clickListener.onItemClicked(commendList.get(position).getUserId(), position, Constants.TAG_USERS);
        });

        holder.profileImageNew.setOnClickListener(v -> {
            clickListener.onItemClicked(commendList.get(position).getUserId(), position, Constants.TAG_USERS);
        });

        holder.userName.setOnClickListener(v -> {
            clickListener.onItemClicked(commendList.get(position).getUserId(), position, Constants.TAG_USERS);
        });


    }

    private VideoCommentResponse.Result getItem(int position) {
        return commendList.get(position);
    }

    @Override
    public int getItemCount() {
        return commendList.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {

        RoundedImageView profileImage;
        ImageView deleteIcon;
        TextView userName;
        TextView messageDateandTime;
        CustomSpannableTextView userComment;
        ShapeableImageView profileImageNew;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.profileImage);
            profileImageNew = itemView.findViewById(R.id.profileImageNew);
            deleteIcon = itemView.findViewById(R.id.deleteIcon);
            userName = itemView.findViewById(R.id.userName);
            userComment = itemView.findViewById(R.id.userComment);
            messageDateandTime = itemView.findViewById(R.id.messageDateandTime);

            deleteIcon.setOnClickListener(v -> {
                App.preventMultipleClick(deleteIcon);
                if (NetworkReceiver.isConnected()) {
                    Map<String, String> map = new HashMap<>();
                    map.put("user_id", GetSet.getUserId());
                    map.put("token", GetSet.getAuthToken());
                    map.put("video_id", getVideoId);
                    map.put("comment_id", commendList.get(getAbsoluteAdapterPosition()).getCommentId());


                    Timber.d("delete Comments: params %s", map);

                    loadMoreCheckVideoCommand = "initial";
                    //  clickListener.onItemClicked(true, getAbsoluteAdapterPosition(), "refresh");
                    Call<Map<String, String>> call3 = apiInterface.deleteComment(map);
                    call3.enqueue(new Callback<Map<String, String>>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                            try {

                                Map<String, String> data = response.body();

                                Timber.d("delete onResponse: %s", new Gson().toJson(response.body()));

                                if (data != null && data.get(Constants.TAG_STATUS).equals("true")) {
                                  /*  commendList.remove(getAbsoluteAdapterPosition());
                                    *//*commendAdapter.notifyItemRemoved(getAdapterPosition());*//*
                                    notifyItemRangeRemoved(getAbsoluteAdapterPosition(), 1);
*/

                                    int pos = getAbsoluteAdapterPosition();
                                    if (pos >= 0 && pos < commendList.size()) {
                                        commendList.remove(pos);
                                        notifyItemRemoved(pos);
                                        // optional: update indices for remaining items
                                        notifyItemRangeChanged(pos, commendList.size() - pos);
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, String>> call, Throwable t) {
                            call.cancel();
                            Timber.d("onFailure: %s", "response- failure");
                        }
                    });

                } else {
                    Toasty.error(mContext, mContext.getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
                }
            });

            /*profileImage.setOnClickListener(v -> {
                clickListener.onItemClicked(commendList.get(getAbsoluteAdapterPosition()), getAbsoluteAdapterPosition(), Constants.TAG_USERS);
            });

            userName.setOnClickListener(v -> {
                clickListener.onItemClicked(commendList.get(getAbsoluteAdapterPosition()), getAbsoluteAdapterPosition(), Constants.TAG_USERS);
            });*/
        }
    }

    // replace entire list (initial load)
    public void replaceComments(List<VideoCommentResponse.Result> newComments) {
        commendList.clear();
        if (newComments != null && !newComments.isEmpty()) {
            commendList.addAll(newComments);
        }
        // initial load ok to refresh fully (you can optimize later)
        notifyDataSetChanged();
    }

    // append more comments (pagination)
    public void addComments(List<VideoCommentResponse.Result> more) {
        if (more == null || more.isEmpty()) return;
        int start = commendList.size();
        commendList.addAll(more);
        notifyItemRangeInserted(start, more.size());
    }
}