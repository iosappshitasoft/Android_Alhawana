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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.helper.NetworkReceiver;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.model.GetSet;
import com.app.model.Request.GetVideosRequest;
import com.app.model.VideoListResponse;
import com.app.model.event.VideoDeleteEvent;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RepostedVideosFragment extends Fragment implements AdapterItemClickListener {

    private static final String TAG = RepostedVideosFragment.class.getSimpleName();
    private Context mContext;
    private ApiInterface apiInterface;
    private RecyclerView recyclerView;
    private RelativeLayout nullLay;
    private ImageView nullImage;
    private TextView nullText;
    private ArrayList<VideoListResponse.Result> videoList = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private VideosAdapter2 videosAdapter;
    private int limit = Constants.MAX_LIMIT, offset = 0;
    private boolean isLoading, isScroll = false;
    private int lastVisibleItem, totalItemCount;
    private int visibleThreshold = 1;
    AppUtils appUtils;
    ProgressBar loadMoreProgress;
    private String getProfileId, from;
    private GridLayoutManager layoutManager;
    private ActivityResultLauncher<Intent> videoResultLauncher;

    public RepostedVideosFragment() {
    }

    public RepostedVideosFragment(String profileId, String string, String getFrom) {
        getProfileId = profileId;
        from = getFrom;
    }

/*    public static LikedVideosFragment newInstance(String param1, String param2) {
        LikedVideosFragment fragment = new LikedVideosFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }*/

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
        View view = inflater.inflate(R.layout.fragment_liked_videos, container, false);
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
        loadMoreProgress.setVisibility(View.GONE);
        recyclerView.setHasFixedSize(true);

        //To set Grid Layout manager
        recyclerView.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(mContext, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);

        videosAdapter = new VideosAdapter2(mContext, videoList, this, Constants.TAG_REPOSTEDVIDEOS);
        recyclerView.setAdapter(videosAdapter);


        int spacingInDp = 7;
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

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefresh(true);
        });

        setScrollMethod();
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

    public void getVideo() {
        if (NetworkReceiver.isConnected()) {
            GetVideosRequest request = new GetVideosRequest();
            request.setUserId(GetSet.getUserId());
            request.setType(Constants.TAG_REPOSTEDVIDEOS);
            request.setProfile_id(getProfileId);
            request.setLimit("" + Constants.MAX_LIMIT);
            request.setOffset("" + (Constants.MAX_LIMIT * offset));
//            Timber.d("getVideo: %s", App.getGsonPrettyInstance().toJson(request));

            Call<VideoListResponse> call = apiInterface.getvideos(request);
            call.enqueue(new Callback<VideoListResponse>() {
                @Override
                public void onResponse(Call<VideoListResponse> call, Response<VideoListResponse> response) {
                    swipeRefresh(false);
                    if (response.isSuccessful()) {
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
            swipeRefresh(false);
            nullImage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            nullLay.setVisibility(View.VISIBLE);
            nullText.setText(mContext.getString(R.string.no_internet_connection));
        }
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
            nullText.setText(mContext.getString(R.string.no_videos_found));
            nullImage.setVisibility(View.VISIBLE);
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
        Log.d(TAG, "onItemClicked2222: "+type);
        Intent intent = new Intent(getContext(), MultipleVideoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(Constants.TAG_TYPE, type);
        intent.putExtra(Constants.TAG_TAGID, getProfileId);
        intent.putExtra(Constants.TAG_VIDEO_ID, item.getVideoId());
        intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, item.getStreamThumbnail());
        intent.putExtra(Constants.TAG_POSITION, item.getPosition());
        videoResultLauncher.launch(intent);
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
