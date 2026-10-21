package com.app.alhawanajd.search.adapter;

import static com.app.utils.Constants.TAG_OWN_PROFILE;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.alhawanajd.App;
import com.app.alhawanajd.MultipleVideoActivity;
import com.app.alhawanajd.R;
import com.app.alhawanajd.SingleVideoActivity;
import com.app.model.GetSet;
import com.app.model.ProfileRequest;
import com.app.model.ProfileResponse;
import com.app.model.UserList;
import com.app.utils.Constants;
import com.app.utils.api.ApiInterface;
import com.bumptech.glide.Glide;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class RecentPostVideoAdapter extends RecyclerView.Adapter<RecentPostVideoAdapter.Holder> {

    Context context;

    String TAG = "RecentPostVideoAdapter";
    List<UserList.Post> list;
    ApiInterface apiInterface;

    public RecentPostVideoAdapter(Context context, List<UserList.Post> list, ApiInterface apiInterface) {
        this.context = context;
        this.list = list;
        this.apiInterface =apiInterface;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.video_preview, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {

        UserList.Post item = list.get(position);

        if (item == null) return;

        String thumb = item.getThumbnail();

        if (thumb == null || thumb.isEmpty()) {
            holder.img.setImageResource(R.drawable.video_placeholder);
            return;
        }

        Log.e("RecentPostVideoAdapter", "onBindViewHolder: "+"https://d3pqf70msuvtmp.cloudfront.net/img/streams/" + thumb);

        Glide.with(context)
                .load("https://d3pqf70msuvtmp.cloudfront.net/img/streams/" + thumb)
                .placeholder(R.drawable.video_placeholder)
                .error(R.drawable.video_placeholder)
                .into(holder.img);

        holder.itemView.setOnClickListener(view -> getProfile(item.getPublisher_id(),Constants.TAG_OTHER_PROFILE,item,position));
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    class Holder extends RecyclerView.ViewHolder {

        ImageView img;

        public Holder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgBackground);
        }
    }

//    getProfile params: {
//        "profile_id": "69296a44ed2779bd3481ca9a",
//                "user_id": "68c1160bb15944e6b34eb364"
//    }

    private void getProfile(String getUserId, String type, UserList.Post item, int position) {
        if (GetSet.getUserId() != null && apiInterface != null) {
            ProfileRequest request = new ProfileRequest();
            request.setUserId(getUserId);
            request.setProfileId(getUserId);
            Timber.d("getProfile params: %s", App.getGsonPrettyInstance().toJson(request));
            Call<ProfileResponse> call = apiInterface.getProfile(request);

            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(@NotNull Call<ProfileResponse> call, @NotNull Response<ProfileResponse> response) {
                    ProfileResponse profile = response.body();

                    String blocked_status = profile.getBlockedByMe();
                    Log.d(TAG, "onResponse_blockedstatus: "+blocked_status);

                    //6929e55412c93b7153412521
                    if (profile.getStatus().equals(Constants.TAG_TRUE)) {

                        if (blocked_status ==null ||blocked_status.equals(Constants.TAG_FALSE)){
                           // VideoListResponse.Result item = (VideoListResponse.Result) o;
                            //Log.e(TAG,"click item :" + new Gson().toJson(item));
//                            Intent intent = new Intent(context, MultipleVideoActivity.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                            intent.putExtra(Constants.TAG_TYPE, type);
//                            intent.putExtra(Constants.TAG_TAGID, GetSet.getUserId() );
//                            intent.putExtra(Constants.TAG_VIDEO_ID, item.getId());
//                            intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, item.getThumbnail());
//                            intent.putExtra(Constants.TAG_POSITION, (position+1));
//                            context.startActivity(intent);


                            Intent intent = new Intent(context, SingleVideoActivity.class);
                            intent.putExtra(Constants.TAG_VIDEO_ID, item.getId());
                            intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, item.getThumbnail());
                            intent.putExtra(Constants.TAG_FROM, Constants.TAG_NOTIFICATION_PAGE);
                            context.startActivity(intent);

                        }else{
                            App.makeToast(context.getString(R.string.unblock_description));
                        }
                    }

                }

                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable t) {

                }


            });
        }
    }
}