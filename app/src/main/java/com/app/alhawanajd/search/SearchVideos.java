package com.app.alhawanajd.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.alhawanajd.R;
import com.app.alhawanajd.MultipleVideoActivity;
import com.app.alhawanajd.SpacesItemDecoration;
import com.app.alhawanajd.VideosAdapter;
import com.app.alhawanajd.VideosAdapter1;
import com.app.helper.NetworkReceiver;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.model.GetSet;
import com.app.model.VideoListResponse;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.app.utils.Constants;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;


public class SearchVideos extends Fragment implements Observer, AdapterItemClickListener {

    private static final String TAG = SearchVideos.class.getSimpleName();
    private Context mContext;
    private ApiInterface apiInterface;
    private RecyclerView recyclerView;
    private RelativeLayout nullLay;
    private ImageView nullImage;
    private TextView nullText;
    private ArrayList<VideoListResponse.Result> videoList = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private VideosAdapter1 videosAdapter;
    private int limit = Constants.MAX_LIMIT, offset = 0;
    private boolean isLoading, isScroll = false;
    private int lastVisibleItem, totalItemCount;
    private int visibleThreshold = 1;
    private GridLayoutManager layoutManager;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public SearchVideos() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search_videos, container, false);
        initView(view);
        return view;
    }

    private void initView(View rootView) {
        nullLay = rootView.findViewById(R.id.nullLay);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        nullImage = rootView.findViewById(R.id.nullImage);
        nullText = rootView.findViewById(R.id.nullText);

        nullImage.setImageResource(R.drawable.no_videos);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        recyclerView.setHasFixedSize(true);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        visibleThreshold = Constants.MAX_LIMIT;

        layoutManager = new GridLayoutManager(mContext, 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.getLayoutManager().setAutoMeasureEnabled(true);
//        recyclerView.setNestedScrollingEnabled(false);

        videosAdapter = new VideosAdapter1(getActivity(), videoList, this, "search");
        recyclerView.setAdapter(videosAdapter);


        int spacingInDp = 5;
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

        videoList.clear();

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            offset = 0;
            swipeRefresh(true);
        });

        setScrollMethod();
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
                    getSearchVideo();
                    isLoading = true;
                }
            }
        });
    }

    private void getSearchVideo() {

        if (NetworkReceiver.isConnected()) {

            HashMap<String, String> map = new HashMap<>();
            if (GetSet.getUserId() == null){
                map.put(Constants.TAG_USER_ID, "");
            }else {
                map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            }

            if (GetSet.getAuthToken() == null){
                map.put(Constants.TAG_TOKEN, "");
            }else {
                map.put(Constants.TAG_TOKEN, GetSet.getAuthToken());
            }
            map.put(Constants.TAG_LIMIT, "" + limit);
            map.put(Constants.TAG_OFFSET, "" + (offset * limit));
            map.put(Constants.TAG_TYPE, Constants.TAG_SEARCH_VIDEO);

            if (!TextUtils.isEmpty(SearchActivity.searchQuery)) {
                map.put(Constants.TAG_TEXT, SearchActivity.searchQuery);
            }

            if (offset > 0) {
                enableSwipeRefresh(false);
            }
//            Timber.d("getSearchVideoPage: params=> %s", App.getGsonPrettyInstance().toJson(map));
            Call<VideoListResponse> call = apiInterface.getSearchVideo(map);
            call.enqueue(new Callback<VideoListResponse>() {
                @Override
                public void onResponse(@NotNull Call<VideoListResponse> call, @NotNull Response<VideoListResponse> response) {
                    swipeRefresh(false);
                    if (response.isSuccessful()) {
                        if (offset == 0) {
                            videoList.clear();
                            videosAdapter.notifyDataSetChanged();
                        }
                        if (response.body().getStatus().equals("true")) {
                            if (isScroll) {
                                removeLoadMore();
                            }
                            List<VideoListResponse.Result> resultList = response.body().getResult();
                            videoList.addAll(resultList);
                            if (resultList.size() > 0) {
                                isLoading = false;
                                videosAdapter.notifyDataSetChanged();
                            } else
                                isLoading = true;
                        } else {
                            //Remove bottom loading item
                            if (isScroll) {
                                removeLoadMore();
                            }
                            isLoading = true;
                        }
                        Timber.d("totalItems: %d", videoList.size());
                    } else {
                        isLoading = true;
                    }
                    enableSwipeRefresh(true);
                    setNullLay();
                }

                @Override
                public void onFailure(@NotNull Call<VideoListResponse> call, @NotNull Throwable t) {
                    call.cancel();
                    t.printStackTrace();
                    enableSwipeRefresh(true);
                    swipeRefresh(false);
                    if (offset != 0)
                        offset--;
                    removeLoadMore();
                }
            });
        } else {
            //AppUtils.showSnack(this, findViewById(R.id.parentLay), false);
            swipeRefresh(false);
            nullImage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            nullLay.setVisibility(View.VISIBLE);
            nullText.setText(getResources().getText(R.string.no_internet_connection));
        }
    }

    private void enableSwipeRefresh(boolean enable) {
        mSwipeRefreshLayout.setEnabled(enable);
    }

    private void swipeRefresh(final boolean refresh) {
        mSwipeRefreshLayout.setRefreshing(refresh);
        if (refresh) {
            offset = 0;
            totalItemCount = 0;
            lastVisibleItem = 0;
            getSearchVideo();
        }
    }

    private void removeLoadMore() {
        if (videoList.size() > 0 && videoList.get(videoList.size() - 1) == null) {
            videoList.remove(videoList.size() - 1);
            videosAdapter.notifyItemRemoved(videoList.size());
        }
    }

    private void setNullLay() {
        if (videoList.size() == 0) {
            nullImage.setImageResource(R.drawable.no_videos);
            nullText.setText(getString(R.string.no_videos_found));
            nullLay.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            nullLay.setVisibility(View.GONE);
        }
    }

    @Override
    public void update(Observable observable, Object o) {
        swipeRefresh(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            swipeRefresh(true);
        }
    }

    @Override
    public void onItemClicked(Object o, int position, String type) {
        VideoListResponse.Result item = (VideoListResponse.Result) o;
        Intent intent = new Intent(getContext(), MultipleVideoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(Constants.TAG_TYPE, "most_liked");
        intent.putExtra(Constants.TAG_TAGID, SearchActivity.searchQuery);
        intent.putExtra(Constants.TAG_VIDEO_ID, item.getVideoId());
        intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, item.getStreamThumbnail());
        intent.putExtra(Constants.TAG_POSITION, item.getPosition());
        startActivity(intent);
    }
}