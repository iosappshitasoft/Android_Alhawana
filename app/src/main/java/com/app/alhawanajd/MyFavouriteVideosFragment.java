package com.app.alhawanajd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.helper.NetworkReceiver;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.model.FavVideoResponse;
import com.app.model.GetSet;
import com.app.model.PostviedoRefresh;
import com.app.model.ProfileRequest;
import com.app.model.ProfileResponse;
import com.app.model.Request.GetVideosRequest;
import com.app.model.VideoListResponse;
import com.app.model.event.VideoDeleteEvent;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.DevicePref;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;


public class MyFavouriteVideosFragment extends Fragment implements AdapterItemClickListener {

    private static final String TAG = MyFavouriteVideosFragment.class.getSimpleName();
    private ApiInterface apiInterface;
    private Context mContext;
    private RecyclerView recyclerView;
    private RelativeLayout nullLay;
    private ImageView nullImage;
    private TextView nullText;
    NestedScrollView nested_scroll;

    private final ArrayList<VideoListResponse.Result> videoList = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private VideosAdapter2 videosAdapter;
    private int limit = Constants.MAX_LIMIT, offset = 0;
    private boolean isLoading, isScroll = false;
    private int lastVisibleItem, totalItemCount;
    private int visibleThreshold = 1;
    AppUtils appUtils;
    int screenHalf;
    int displayWidth = 0, displayHeight = 0;
    GridLayoutManager layoutManager;
    ProgressBar loadMoreProgress;
    private ActivityResultLauncher<Intent> videoResultLauncher;

    private String getProfileId;
    private  String nameValue;
    private String from;
    private ProfileResponse profile;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public MyFavouriteVideosFragment() {
    }
    public MyFavouriteVideosFragment(String profileId, String title, String getFrom) {
        getProfileId = profileId;
        from = getFrom;
    }
    public MyFavouriteVideosFragment(String profileId, String title, String getFrom , String name) {
        getProfileId = profileId;
        from = getFrom;
        nameValue = name;
        Log.d(TAG, "MyFavouriteVideosFragment: "+name);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mContext == null) mContext = getActivity();
        displayWidth = DevicePref.getInstance(mContext).getInt(DevicePref.SCREEN_WIDTH, 0);
        displayHeight = DevicePref.getInstance(mContext).getInt(DevicePref.SCREEN_HEIGHT, 0);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        screenHalf = displayWidth - App.dpToPx(mContext, 0);

        //   EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //  EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProfilevideoUpdated(PostviedoRefresh postviedoRefresh) {
        Timber.d("onProfileUpdated: getVideo");
        getVideo();
        swipeRefresh(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_videos, container, false);
        appUtils = new AppUtils(getActivity());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View rootView) {
        nullLay = rootView.findViewById(R.id.nullLay);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        nullImage = rootView.findViewById(R.id.nullImage);
        nullText = rootView.findViewById(R.id.nullText);
        loadMoreProgress = rootView.findViewById(R.id.loadMoreProgress);
        nested_scroll = rootView.findViewById(R.id.nested_scroll);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(mContext, R.color.colorPrimary));
        visibleThreshold = Constants.MAX_LIMIT;

        //To set Grid Layout manager
        recyclerView.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(mContext, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);

        videosAdapter = new VideosAdapter2(getActivity(), videoList, this, "favourite");
        recyclerView.setAdapter(videosAdapter);

        int spacingInDp = 6;
        int spacingInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                spacingInDp,
                mContext.getResources().getDisplayMetrics()
        );
        recyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPx));


        videosAdapter.notifyDataSetChanged();
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return videosAdapter.isFooter(position) ? layoutManager.getSpanCount() : 1;
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(() -> swipeRefresh(true));
      //  setScrollMethod();

        nested_scroll.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(@NonNull NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                /*if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                    Timber.d("onScrollChange: %s",scrollY);
                    offset++;
                    isScroll = true;
                    getVideo();
                    isLoading = true;
                }*/

                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                    Timber.d("onScrollChange: bottom reached");
                    if (!isLoading) {
                        isLoading = true;
                        isScroll = true;
                        getVideo(); // just call once
                    }
                }
            }
        });
        initActivityResultLauncher();
        swipeRefresh(true);
    }

    private void setScrollMethod() {
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (dy >= 0 && !isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    videoList.add(null);
                    videosAdapter.notifyItemInserted(videoList.size() - 1);
                    offset++;
                    isScroll = true;
                    getVideo();
                    isLoading = true;
                }
            }
        });
    }

    private void initActivityResultLauncher() {
        videoResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    swipeRefresh(true);
                }
            }
        });
    }

   /* public void getVideo() {
        if (NetworkReceiver.isConnected()) {
            GetVideosRequest request = new GetVideosRequest();
            if (GetSet.getUserId() == null){
                request.setUserId("");
            }else {
                request.setUserId(GetSet.getUserId());
            }

            request.setType(Constants.TAG_MYVIDEOS);
            request.setProfile_id(getProfileId);
            request.setLimit("" + Constants.MAX_LIMIT);
            request.setOffset("" + (Constants.MAX_LIMIT * offset));
            Timber.d("getVideo_params: %s", App.getGsonPrettyInstance().toJson(request));

            Call<VideoListResponse> call = apiInterface.getvideos(request);
            call.enqueue(new Callback<VideoListResponse>() {
                @Override
                public void onResponse(@NotNull Call<VideoListResponse> call, @NotNull Response<VideoListResponse> response) {
                    if (response.isSuccessful()) {
                       // Timber.tag(TAG).d("getvideos_res: %s", App.getGsonPrettyInstance().toJson(response.body()));
                        if (response.body().getStatus().equals("true")) {
                            if (isScroll) {
                                removeLoadMore();
                            }
                            if (offset == 0) {
                                videoList.clear();
                            }
                            List<VideoListResponse.Result> resultList = response.body().getResult();
                            videoList.addAll(resultList);
                            recyclerView.setVisibility(View.VISIBLE);
                            if (resultList.size() > 0) {
                                isLoading = false;
                                videosAdapter.notifyDataSetChanged();
                            } else
                                isLoading = true;
                            setNullLay();
                        } else {
                            //Remove bottom loading item
                            if (isScroll) {
                                removeLoadMore();
                            }
                            isLoading = true;
                            setNullLay();
                        }
                    }

                    if (mSwipeRefreshLayout.isRefreshing()) {
                        swipeRefresh(false);
                        isLoading = true;
                    }
                    videosAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(Call<VideoListResponse> call, Throwable t) {
                    t.printStackTrace();
                    swipeRefresh(false);
                    if (offset != 0)
                        offset--;
                    removeLoadMore();
                    call.cancel();
                }
            });
        } else {
//            recyclerView.setVisibility(View.GONE);
            swipeRefresh(false);
            nullLay.setVisibility(View.VISIBLE);
            nullImage.setVisibility(View.GONE);
            nullText.setText(mContext.getString(R.string.no_internet_connection));
        }
    }

   */

    public void getVideo() {
        if (!NetworkReceiver.isConnected()) {
            swipeRefresh(false);
            nullLay.setVisibility(View.VISIBLE);
            nullImage.setVisibility(View.GONE);
            nullText.setText(mContext.getString(R.string.no_internet_connection));
            loadMoreProgress.setVisibility(View.GONE);
            return;
        }

        loadMoreProgress.setVisibility(View.VISIBLE);

        GetVideosRequest request = new GetVideosRequest();
        request.setUserId(GetSet.getUserId() == null ? "" : GetSet.getUserId());
        request.setType(Constants.TAG_VIDEO);
        request.setProfile_id(getProfileId);
        request.setLimit(String.valueOf(Constants.MAX_LIMIT));
        request.setOffset(String.valueOf(offset)); // ✅ FIXED: pass actual offset (not multiplied)
        Timber.d("getVideo_params: %s", App.getGsonPrettyInstance().toJson(request));

        apiInterface.getfavvideos(request).enqueue(new Callback<FavVideoResponse>() {
            @Override
            public void onResponse(@NotNull Call<FavVideoResponse> call, @NotNull Response<FavVideoResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getStatus().equals("true")) {

                    if (isScroll) removeLoadMore();
                    if (offset == 0) videoList.clear();

                    List<VideoListResponse.Result> resultList = response.body().getVideos();

                    if (resultList != null && !resultList.isEmpty()) {
                        videoList.addAll(resultList);
                        recyclerView.setVisibility(View.VISIBLE);
                        videosAdapter.notifyDataSetChanged();

                        // ✅ increment offset by limit (not ++)
                        offset += Constants.MAX_LIMIT;
                        isLoading = false;
                    } else {
                        isLoading = true; // no more data
                    }
                    setNullLay();
                } else {
                    if (isScroll) removeLoadMore();
                    isLoading = true;
                    setNullLay();
                }

                if (mSwipeRefreshLayout.isRefreshing()) {
                    swipeRefresh(false);
                }
                loadMoreProgress.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<FavVideoResponse> call, Throwable t) {
                t.printStackTrace();
                swipeRefresh(false);
                if (offset >= Constants.MAX_LIMIT) offset -= Constants.MAX_LIMIT;
                removeLoadMore();
                call.cancel();
                isLoading = false;
                loadMoreProgress.setVisibility(View.GONE);
            }
        });
    }

    private void swipeRefresh(final boolean refresh) {
        mSwipeRefreshLayout.setRefreshing(refresh);
        if (refresh) {
            offset = 0;
            totalItemCount = 0;
            lastVisibleItem = 0;
            getVideo();
        }
    }

    private void setNullLay() {
        if (videoList.size() == 0) {
            nullImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.no_videos));
            nullImage.setVisibility(View.VISIBLE);
            nullText.setText(mContext.getString(R.string.no_videos_found));
            nullLay.setVisibility(View.VISIBLE);
        } else {
            nullLay.setVisibility(View.GONE);
        }
    }

    private void removeLoadMore() {
        if (videoList.size() > 0 && videoList.get(videoList.size() - 1) == null) {
            videoList.remove(videoList.size() - 1);
            videosAdapter.notifyItemRemoved(videoList.size());
        }
    }

    @Override
    public void onItemClicked(Object o, int position, String type) {
        //  VideoListResponse.Result item = (VideoListResponse.Result) o;
        // blockUnBlockUser(getProfileId);
        Log.e(TAG,"onItemClicked : " + position);
        getProfile(getProfileId, nameValue , type, o, position);
        /*if (block_status.equals(Constants.TAG_FALSE) || block_status.equals("profile")) {
            VideoListResponse.Result item = (VideoListResponse.Result) o;
            Intent intent = new Intent(getContext(), MultipleVideoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra(Constants.TAG_TYPE, type);
            intent.putExtra(Constants.TAG_TAGID, getProfileId);
            intent.putExtra(Constants.TAG_VIDEO_ID, item.getVideoId());
            intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, item.getStreamThumbnail());
            intent.putExtra(Constants.TAG_POSITION, item.getPosition());
            videoResultLauncher.launch(intent);
        }
        else{
            App.makeToast(mContext.getString(R.string.unblock_description));
        }*/
    }

    private void getProfile(String getUserId, String userName , String type , Object o, int position) {
        if (GetSet.getUserId() != null && apiInterface != null) {
            ProfileRequest request = new ProfileRequest();
            request.setUserId(GetSet.getUserId());
            request.setProfileId(getUserId);
            Timber.d("getProfile params: %s", App.getGsonPrettyInstance().toJson(request));
            Call<ProfileResponse> call = apiInterface.getProfile(request);

            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(@NotNull Call<ProfileResponse> call, @NotNull Response<ProfileResponse> response) {
                    ProfileResponse profile = response.body();

                    String blocked_status = profile.getBlockedByMe();
                    Log.d(TAG, "onResponse_blockedstatus: "+blocked_status);

                    if (profile.getStatus().equals(Constants.TAG_TRUE)) {

                        if (blocked_status ==null ||blocked_status.equals(Constants.TAG_FALSE)){
                            VideoListResponse.Result item = (VideoListResponse.Result) o;
                            Log.e(TAG,"click item :" + new Gson().toJson(item));
                            Intent intent = new Intent(getContext(), MultipleVideoActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            intent.putExtra(Constants.TAG_TYPE, type);
                            intent.putExtra(Constants.TAG_TAGID, getProfileId);
                            intent.putExtra(Constants.TAG_VIDEO_ID, item.getVideoId());
                            intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, item.getStreamThumbnail());
                            /*intent.putExtra(Constants.TAG_POSITION, item.getPosition());
*/
                            intent.putExtra(Constants.TAG_POSITION, (position+1));

                            Log.d(TAG, "onItemClicked88: "+type);
                            Log.d(TAG, "onItemClicked89 profile is: "+getProfileId);
                            Log.d(TAG, "onItemClicked87 videoid: "+item.getVideoId());
                            Log.d(TAG, "onItemClicked86 thubnai: "+item.getStreamThumbnail());
                            Log.d(TAG, "onItemClicked85 position: "+position);

                            videoResultLauncher.launch(intent);
                        }else{
                            App.makeToast(mContext.getString(R.string.unblock_description));
                        }
                    }

                }

                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable t) {

                }


            });
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(VideoDeleteEvent event) {
        swipeRefresh(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}