package com.app.addon.livestream.liveVideoPlayer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.app.alhawanajd.R;
import com.app.addon.livestream.PlayerActivity;
import com.app.addon.livestream.external.avloading.AVLoadingIndicatorView2;
import com.app.addon.livestream.external.helper.NetworkReceiver;
import com.app.addon.livestream.external.helper.Utils;
import com.app.addon.livestream.external.model.LiveStreamRequest;
import com.app.addon.livestream.external.model.LiveStreamResponse;
import com.app.addon.livestream.external.model.StreamDetails;
import com.app.addon.livestream.external.utils.ApiClient;
import com.app.addon.livestream.external.utils.Constants;
import com.app.model.GetSet;
import com.app.addon.livestream.external.utils.StreamConstants;
import com.app.alhawanajd.BaseFragmentActivity;
import com.app.helper.LocaleManager;
import com.app.utils.api.ApiInterface;


import java.util.ArrayList;

import java.util.List;
import java.util.Map;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserVideoActivity extends BaseFragmentActivity {

    private static final String TAG = UserVideoActivity.class.getSimpleName();

    ImageView userBackground;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    ImageView nullImage;
    TextView nullText;
    LinearLayout nullLay;
    ImageView btnBack;
    TextView txtTitle;
    /*    TextView txtSubTitle;*/
    ImageView btnSettings;
    Toolbar toolbar;


    private final ArrayList<StreamDetails> streamLists = new ArrayList<>();
    private RecyclerViewAdapter itemAdapter;
    private LinearLayoutManager itemManager;
    private ApiInterface apiInterface;
    //    private AppUtils appUtils;
    private int currentPage = 0, visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold;
    private boolean isLoading = true, isVideoDeleted = false;
    private String publisherId, publisherName, publisherImage, from;
    private Long selectedPosition;
    private final List<Call<LiveStreamResponse>> liveStreamApiCall = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_user_video);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        /*        appUtils = new AppUtils(this);*/
        getFromIntent();
        initView();
    }

    private void getFromIntent() {
        publisherId = getIntent().getStringExtra(Constants.TAG_PARTNER_ID);
        publisherName = getIntent().getStringExtra(Constants.TAG_PARTNER_NAME);
        publisherImage = getIntent().getStringExtra(Constants.TAG_PARTNER_IMAGE);
        from = getIntent().getStringExtra(Constants.TAG_FROM);
        if (getIntent().hasExtra(Constants.IS_VIDEO_DELETED)) {
            isVideoDeleted = getIntent().getBooleanExtra(Constants.IS_VIDEO_DELETED, false);
        }
    }

    private void initView() {
/*
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);
*/

        userBackground = findViewById( R.id.userBackground);
        recyclerView = findViewById( R.id.recyclerView);
        swipeRefreshLayout = findViewById( R.id.swipeRefreshLayout);
        nullImage = findViewById( R.id.nullImage);
        nullText = findViewById( R.id.nullText);
        nullLay = findViewById( R.id.nullLay);
        btnBack = findViewById( R.id.btnBack);
        txtTitle = findViewById( R.id.txtTitle);

        btnSettings = findViewById( R.id.btnSettings);

        toolbar = findViewById( R.id.toolbar);

        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary), ContextCompat.getColor(this, R.color.colorPrimary));
        btnBack.setImageDrawable(getDrawable( R.drawable.arrow_w_l));
        btnSettings.setImageDrawable(getDrawable( R.drawable.delete));

        Glide.with(this)
                .load(GetSet.getImageUrl())
                .error( R.drawable.default_profile_image)
                .into(userBackground);

        /*if (from != null && from.equals(Constants.TAG_HISTORY)) {
            txtTitle.setText(getString(R.string.recent_videos));
        } else if (from != null && from.equals(Constants.TAG_OTHER_PROFILE)) {
            txtTitle.setText(getString(R.string.videos_txt));
        } else {
            txtTitle.setText(getString(R.string.my_video));
        }*/


        txtTitle.setText(getString(R.string.my_videos));
        txtTitle.setTextColor(ContextCompat.getColor(this, R.color.white));

        itemAdapter = new RecyclerViewAdapter(this, streamLists);
        itemManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setAdapter(itemAdapter);
        recyclerView.setLayoutManager(itemManager);
        itemAdapter.notifyDataSetChanged();
        //loadAd();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                itemAdapter.showLoading(false);
                streamLists.clear();
                itemAdapter.notifyDataSetChanged();
                for (Call<LiveStreamResponse> liveStreamResponseCall : liveStreamApiCall) {
                    liveStreamResponseCall.cancel();
                }
                liveStreamApiCall.clear();
                previousTotal = 0;
                currentPage = 0;
                totalItemCount = 0;
                visibleItemCount = 0;
                firstVisibleItem = 0;
                getLiveStreams(currentPage);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = itemManager.getItemCount();
                firstVisibleItem = itemManager.findFirstVisibleItemPosition();

                if (dy > 0) {//check for scroll down
                    if (isLoading) {
                        Log.i(TAG, "onScrolled: " + totalItemCount + ", " + previousTotal);
                        if (totalItemCount > previousTotal) {
                            isLoading = false;
                            previousTotal = totalItemCount;
                            currentPage++;
                        }
                    }

                    if (!isLoading && (totalItemCount - visibleItemCount)
                            <= (firstVisibleItem + visibleThreshold)) {
                        // End has been reached
                        isLoading = true;
                        getLiveStreams(currentPage);
                    }
                }
            }
        });

        streamLists.clear();
        if (!isVideoDeleted) {
            swipeRefresh(true);
        } else {
            setNullLay();
        }


        btnBack.setOnClickListener(v -> {
            finish();
        });
    }

    private void swipeRefresh(final boolean refresh) {
        swipeRefreshLayout.setRefreshing(refresh);
        if (refresh) {
            previousTotal = 0;
            currentPage = 0;
            totalItemCount = 0;
            visibleItemCount = 0;
            firstVisibleItem = 0;
            getLiveStreams(currentPage);
        }
    }

    public void getLiveStreams(final int offset) {
        if (NetworkReceiver.isConnected()) {
            if (!swipeRefreshLayout.isRefreshing()) {
                itemAdapter.showLoading(true);
            }

            Call<LiveStreamResponse> call;
            if (from != null && from.equals(Constants.TAG_HISTORY)) {
                call = apiInterface.getRecentWatched2(GetSet.getUserId(), "" + Constants.MAX_LIMIT, "" + (Constants.MAX_LIMIT * offset));
            } else {
                LiveStreamRequest request = new LiveStreamRequest();
                request.setUserId(GetSet.getUserId());
                request.setProfileId(GetSet.getUserId());
                request.setSortBy(Constants.TAG_RECENT);
                request.setType(Constants.TAG_USER);
                request.setLimit("" + Constants.MAX_LIMIT);
                request.setOffset("" + (Constants.MAX_LIMIT * offset));
                request.setFilterApplied("" + false);


                Log.d(TAG, "getLiveStreams: params" + new Gson().toJson(request));

                call = apiInterface.getCurrentStreams(GetSet.getToken(), request);
            }
            liveStreamApiCall.add(call);
            call.enqueue(new Callback<LiveStreamResponse>() {
                @Override
                public void onResponse(Call<LiveStreamResponse> call, Response<LiveStreamResponse> response) {
                    if (response.isSuccessful()) {
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            LiveStreamResponse data = response.body();
                            Log.d(TAG, "getLiveStreams: response" + new Gson().toJson(data) +"Authorization" + response.headers());
                            streamLists.addAll(data.result);
                        }
                    }
                    setNullLay();
                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefresh(false);
                        isLoading = true;
                    }
                    itemAdapter.showLoading(false);
                    itemAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Call<LiveStreamResponse> call, Throwable t) {
                    Log.e(TAG, "onFailure: " + t.getMessage());
                    call.cancel();
                    if (currentPage != 0) {
                        currentPage--;
                    }
                }
            });
        } else {
            if (currentPage != 0)
                currentPage--;
            if (!swipeRefreshLayout.isRefreshing()) {
                itemAdapter.showLoading(false);
            } else {
                setNullLay();
                swipeRefresh(false);
            }
        }
    }

    private void setNullLay() {
        if (streamLists.size() == 0) {
            isVideoDeleted = true;
            nullImage.setImageResource( R.drawable.no_video);
            if (LocaleManager.isRTL()) {
                btnBack.setRotation(180);
            } else {
                btnBack.setRotation(0);

            }
            nullText.setText(getString( R.string.no_videos_found));
            txtTitle.setText(getString(R.string.no_videos_found));
            nullLay.setVisibility(View.VISIBLE);
        } else {
            nullLay.setVisibility(View.GONE);
        }
        if (from != null && from.equals(Constants.TAG_HISTORY)) {
            btnSettings.setVisibility(streamLists.size() > 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

/*    private void loadAd() {
        if (AdminData.isAdEnabled()) {
            MobileAds.initialize(this,
                    AdminData.googleAdsId);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                    Log.i(TAG, "onAdLoaded: ");
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    // Code to be executed when an ad request fails.
                    Log.e(TAG, "onAdFailedToLoad: " + errorCode);
                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                    Log.i(TAG, "onAdOpened: ");
                }

                @Override
                public void onAdClicked() {
                    // Code to be executed when the user clicks on an ad.
                }

                @Override
                public void onAdLeftApplication() {
                    // Code to be executed when the user has left the app.
                }

                @Override
                public void onAdClosed() {
                    // Code to be executed when the user is about to return
                    // to the app after tapping on an ad.
                }
            });
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        appUtils.showSnack(this, findViewById(R.id.parentLay),isConnected);
    }*/

/*    @OnClick({R.id.btnBack, R.id.btnSettings})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.btnSettings:
                openDeleteDialog(getString(R.string.really_remove_from_list));
                break;
        }
    }*/


    @Override
    public void onBackPressed() {
        if (from != null && from.equals(Constants.TAG_HISTORY)) {
            Intent intent = new Intent();
            intent.putExtra(Constants.IS_VIDEO_DELETED, isVideoDeleted);
            setResult(RESULT_OK, intent);
            super.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }

    private void openDeleteDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserVideoActivity.this);
        builder.setMessage(message);
        builder.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                deleteWatchHistory();
            }
        });
        builder.setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        Typeface typeface = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = getResources().getFont( R.font.font_light);
        } else {
            typeface = ResourcesCompat.getFont(this, R.font.font_light);
        }
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setTypeface(typeface);

        Button btn1 = dialog.findViewById(android.R.id.button1);
        btn1.setTypeface(typeface);

        Button btn2 = dialog.findViewById(android.R.id.button2);
        btn2.setTypeface(typeface);
    }

    public void deleteWatchHistory() {
        Call<Map<String, String>> call = apiInterface.clearWatched(GetSet.getUserId());
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    if (response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                        streamLists.clear();
                        itemAdapter.notifyDataSetChanged();
                        //App.makeToast(getString(R.string.history_cleared_successfully));
                    } else {
                        //App.makeToast(getString(R.string.something_went_wrong));
                    }
                    setNullLay();
                } else {
                    //App.makeToast(getString(R.string.something_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_FOOTER = 1;
        List<StreamDetails> broadcastLists;
        private boolean showLoading = false;
        Context context;

        public RecyclerViewAdapter(Context context, List<StreamDetails> broadcastLists) {
            this.broadcastLists = broadcastLists;
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(parent.getContext()).inflate( R.layout.item_video, parent, false);
                return new MyViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_FOOTER) {
                View view = LayoutInflater.from(parent.getContext()).inflate( R.layout.livestreaming_progress, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
            if (viewHolder instanceof MyViewHolder) {
                MyViewHolder holder = (MyViewHolder) viewHolder;
                final StreamDetails broadCast = broadcastLists.get(position);
                holder.txtUserName.setVisibility(View.VISIBLE);
                holder.txtUserName.setText(broadCast.getPostedBy());


                /*holder.btnPrivate.setVisibility(broadCast.getMode().equals(Constants.TAG_PUBLIC) ?
                        View.GONE : View.VISIBLE);*/

               /* if (LocaleManager.isRTL()) {
                    holder.liveTxtLay.setBackground(getResources().getDrawable(R.drawable.live_status_bg_rtl));
                    holder.liveCountLay.setBackground(getResources().getDrawable(R.drawable.live_count_bg_rtl));
                } else {
                    holder.liveTxtLay.setBackground(getResources().getDrawable(R.drawable.live_status_bg));
                    holder.liveCountLay.setBackground(getResources().getDrawable(R.drawable.live_count_bg));
                }*/

                if (broadCast.type.equals(Constants.TAG_LIVE)) {
                    holder.liveStatusLay.setVisibility(View.VISIBLE);
                    holder.txtLiveCount.setText(broadCast.getWatchCount());
                } else {
                    holder.liveStatusLay.setVisibility(View.GONE);
                }

                holder.txtTitle.setText(broadCast.title);
                Glide.with(context).load(broadCast.getPublisherImage())
                        .apply(new RequestOptions().error( R.drawable.alhawana).placeholder(R.drawable.alhawana).centerCrop())
                        .into(holder.iconThumb);


                holder.txtUploadTime.setText(Utils.getRecentDate(Utils.getTimeFromUTC(broadCast.getStreamedOn())));

                holder.parentLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (broadCast.type.equals(Constants.TAG_LIVE)) {
                            pauseExternalAudio(UserVideoActivity.this);
                            selectedPosition = (long) position;
                            Intent i = new Intent(UserVideoActivity.this, SubscribeActivity.class);
                            Bundle arguments = new Bundle();
                            arguments.putSerializable(StreamConstants.TAG_STREAM_DATA, broadCast);
                            i.putExtra(Constants.TAG_FROM, Constants.TAG_OTHER_PROFILE);
                            i.putExtras(arguments);
                            startActivityForResult(i, StreamConstants.STREAM_REQUEST_CODE);
                        } else {
                            pauseExternalAudio(UserVideoActivity.this);
                            selectedPosition = (long) position;
                            Intent intent = new Intent(UserVideoActivity.this, PlayerActivity.class);
                            intent.putExtra(StreamConstants.TAG_STREAM_DATA, broadCast);
                            intent.putExtra(Constants.TAG_FROM, from);
                            startActivityForResult(intent, StreamConstants.DELETE_REQUEST_CODE);
                        }
                    }
                });
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (showLoading && isPositionFooter(position))
                return VIEW_TYPE_FOOTER;
            return VIEW_TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            int itemCount = broadcastLists.size();
            if (showLoading)
                itemCount++;
            return itemCount;
        }


        public void pauseExternalAudio(Context context) {
            AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (manager.isMusicActive()) {
                Constants.isExternalPlay = true;
                Intent i = new Intent("com.android.music.musicservicecommand");
                i.putExtra("command", "pause");
                context.sendBroadcast(i);
            }
        }

        public boolean isPositionFooter(int position) {
            return position == getItemCount() - 1 && showLoading;
        }

        public void showLoading(boolean value) {
            showLoading = value;
        }

        public class LoadingViewHolder extends RecyclerView.ViewHolder {
            AVLoadingIndicatorView2 progressBar;
            FrameLayout progressLay;

            public LoadingViewHolder(View view) {
                super(view);
                progressBar = view.findViewById( R.id.progressBarNew);
                progressLay = view.findViewById( R.id.progressLay);
            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            RelativeLayout parentLay;

            RelativeLayout liveStatusLay;

            RelativeLayout liveCountLay;

            FrameLayout liveTxtLay;

            ImageView iconThumb;


            TextView txtLiveCount;

            TextView txtTitle;

            TextView txtUploadTime;

            TextView txtUserName;

            public MyViewHolder(View view) {
                super(view);

                parentLay = view.findViewById( R.id.parentLay);
                liveStatusLay = view.findViewById( R.id.liveStatusLay);
                liveCountLay = view.findViewById( R.id.liveCountLay);
                liveTxtLay = view.findViewById( R.id.liveTxtLay);
                iconThumb = view.findViewById( R.id.iconThumb);
                txtLiveCount = view.findViewById( R.id.txtLiveCount);
                txtTitle = view.findViewById( R.id.txtTitle);
                txtUploadTime = view.findViewById( R.id.txtUploadTime);
                txtUserName = view.findViewById( R.id.txtUserName);

                txtTitle.setTextColor(ContextCompat.getColor(context, R.color.white));
                txtUserName.setTextColor(ContextCompat.getColor(context, R.color.white));
                txtUploadTime.setTextColor(ContextCompat.getColor(context, R.color.colorSecondaryText));
                /*txtUploadTime.setTextColor(ContextCompat.getColor(context, R.color.colorBlack));*/

            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == StreamConstants.DELETE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (selectedPosition != null) {
                streamLists.remove(selectedPosition.intValue());
                itemAdapter.notifyItemRemoved(selectedPosition.intValue());
                itemAdapter.notifyItemRangeChanged(selectedPosition.intValue(), (itemAdapter.getItemCount() - selectedPosition.intValue()));
                selectedPosition = null;
            }
        } else if (requestCode == StreamConstants.STREAM_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (selectedPosition != null) {
                if (streamLists != null && streamLists.size() > 0) {
                    streamLists.get(selectedPosition.intValue()).setType(Constants.TAG_RECORDED);
                    itemAdapter.notifyItemChanged(selectedPosition.intValue());
                    selectedPosition = null;
                }
            }
        }
    }
}
