package com.app.alhawanajd.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.alhawanajd.R;
import com.app.alhawanajd.SpacesItemDecoration;
import com.app.db.DBHelper;
import com.app.alhawanajd.App;
import com.app.alhawanajd.LoginActivity;
import com.app.alhawanajd.MultipleVideoActivity;
import com.app.alhawanajd.VideosAdapter;
import com.app.helper.NetworkReceiver;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.model.FavVideoResponse;
import com.app.model.GetSet;
import com.app.model.Request.GetVideosRequest;
import com.app.model.VideoListResponse;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FavVideoFragment extends Fragment implements AdapterItemClickListener {

    private static final String TAG = FavVideoFragment.class.getSimpleName();
    private Context mContext;
    private ApiInterface apiInterface;
    private RecyclerView recyclerView;
    private RelativeLayout nullLay;
    private ImageView nullImage;
    private TextView nullText;
    private ArrayList<VideoListResponse.Result> videoList = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private VideosAdapter videosAdapter;
    private int limit = Constants.MAX_LIMIT, offset = 0;
    private boolean isLoading, isScroll = false;
    private int lastVisibleItem, totalItemCount;
    private int visibleThreshold = 1;
    AppUtils appUtils;
    GridLayoutManager layoutManager;
    private String profileId;
    private ActivityResultLauncher<Intent> videoResultLauncher;

    public FavVideoFragment(String profileId) {
        this.profileId = profileId;
    }

    public FavVideoFragment(String profileId, String string) {
        this.profileId = profileId;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_fav_video, container, false);

        View view = inflater.inflate(R.layout.fragment_fav_videos, container, false);
        appUtils = new AppUtils(getActivity());
        initActivityResultLauncher();
        initView(view);
        return view;
    }

    private void initView(View rootView) {
        nullLay = rootView.findViewById(R.id.nullLay);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        nullImage = rootView.findViewById(R.id.nullImage);
        nullText = rootView.findViewById(R.id.nullText);
        recyclerView.setHasFixedSize(true);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        visibleThreshold = Constants.MAX_LIMIT;
        //To set Grid Layout manager
        recyclerView.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(mContext, 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        videosAdapter = new VideosAdapter(mContext, videoList, this, "fav");
        recyclerView.setAdapter(videosAdapter);

        int spacingInDp = 4;
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


        setScrollMethod();
        initActivityResultLauncher();
        videoList.clear();
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
                    mSwipeRefreshLayout.setEnabled(false);
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

    public void getVideo() {
        if (NetworkReceiver.isConnected()) {
            GetVideosRequest request = new GetVideosRequest();
            request.setUserId(GetSet.getUserId());
            request.setProfile_id(GetSet.getUserId());
            request.setType(Constants.TAG_VIDEO);
            request.setLimit("" + Constants.MAX_LIMIT);
            request.setOffset("" + (Constants.MAX_LIMIT * offset));
            //Timber.d("getVideo: params %s", App.getGsonPrettyInstance().toJson(request));

            Call<FavVideoResponse> call3 = apiInterface.getfavvideos(request);
            call3.enqueue(new Callback<FavVideoResponse>() {
                @Override
                public void onResponse(@NotNull Call<FavVideoResponse> call, @NotNull Response<FavVideoResponse> response) {
                    mSwipeRefreshLayout.setEnabled(true);
                    swipeRefresh(false);
                    if (response.isSuccessful()) {
                        if (response.body().getStatus().equals("true")) {
                            if (isScroll) {
                                removeLoadMore();
                            }
                            if (offset == 0) {
                                videoList.clear();
                            }
                            List<VideoListResponse.Result> resultList = response.body().getVideos();
                            videoList.addAll(resultList);
                            recyclerView.setVisibility(View.VISIBLE);
                            if (resultList.size() > 0) {
                                isLoading = false;
                                videosAdapter.notifyDataSetChanged();
                            } else
                                isLoading = true;
                            setNullLay();
                        } else if(response.body().getStatus().equals(Constants.TAG_ACCOUNT_BLOCKED)) {
                            App.makeToast(getString(R.string.Account_block));
                            GetSet.reset();
                            SharedPref.clearAll();
                            DBHelper.getInstance(mContext).clearDB();
                            Intent logout = new Intent(mContext, LoginActivity.class);
                            logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(logout);
                        }

                        else {
                            //Remove bottom loading item
                            if (isScroll) {
                                removeLoadMore();
                            }
                            if (offset == 0) {
                                videoList.clear();
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
                public void onFailure(Call<FavVideoResponse> call, Throwable t) {
                    t.printStackTrace();
                    mSwipeRefreshLayout.setEnabled(true);
                    swipeRefresh(false);
                    if (offset != 0)
                        offset--;
                    removeLoadMore();
                    call.cancel();
                }
            });
        } else {
            swipeRefresh(false);
            nullLay.setVisibility(View.VISIBLE);
            nullImage.setVisibility(View.GONE);
            nullText.setText(getString(R.string.no_internet_connection));
        }
    }

    private void swipeRefresh(final boolean refresh) {
        mSwipeRefreshLayout.setRefreshing(refresh);
        if (refresh) {
            offset = 0;
            totalItemCount = 0;
            lastVisibleItem = 0;
            isLoading = true;
            getVideo();
        }
    }

    private void setNullLay() {
        if (videoList.size() == 0) {
            nullImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.no_videos));
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
        VideoListResponse.Result item = (VideoListResponse.Result) o;
        Intent intent = new Intent(mContext, MultipleVideoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(Constants.TAG_TYPE, Constants.TAG_FAV);
        intent.putExtra(Constants.TAG_TAGID, GetSet.getUserId());
        intent.putExtra(Constants.TAG_VIDEO_ID, item.getVideoId());
        intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, item.getStreamThumbnail());
        intent.putExtra(Constants.TAG_POSITION, item.getPosition());
        videoResultLauncher.launch(intent);
    }

}