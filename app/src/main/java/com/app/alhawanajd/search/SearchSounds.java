package com.app.alhawanajd.search;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.app.alhawanajd.R;
import com.app.external.MediaPlayerUtils;
import com.app.alhawanajd.App;
import com.app.alhawanajd.MusicListAdapter;
import com.app.alhawanajd.VideoListActivity;
import com.app.helper.NetworkReceiver;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.model.GetSet;
import com.app.model.MusicListResponse;
import com.app.model.SearchSoundRes;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.app.utils.AppUtils;
import com.app.utils.Constants;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by hitasoft on 26/2/18.
 */

public class SearchSounds extends Fragment implements Observer, AdapterItemClickListener {

    private static final String TAG = SearchSounds.class.getSimpleName();
    private ApiInterface apiInterface;
    private RecyclerView recyclerView;
    private RelativeLayout nullLay;
    private ImageView nullImage;
    private TextView nullText;
    private ArrayList<MusicListResponse.Result> soundList = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private MusicListAdapter musicListAdapter;
    private LinearLayoutManager itemManager;
    private int limit = 10, offset = 0;
    private boolean isLoading, isScroll = false;
    private int lastVisibleItem, totalItemCount;
    private int visibleThreshold = 1;
    private int selectedPosition = -1;
    AppUtils appUtils;
    private Context mContext;
    private ActivityResultLauncher<Intent> updateResultLauncher;

    public SearchSounds() {
        // Required empty public constructor
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
        appUtils = new AppUtils(getActivity());
        initActivityResultLauncher();
    }

    private void initActivityResultLauncher() {
        updateResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (selectedPosition != -1 && result.getData() != null) {
                        soundList.get(selectedPosition).setIsFavorite(result.getData().getBooleanExtra(Constants.TAG_FAV, false));
                        musicListAdapter.notifyItemChanged(selectedPosition);
                    }
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);
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

        //nullImage.setImageResource(R.drawable.sound);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        visibleThreshold = Constants.MAX_LIMIT;

        itemManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(itemManager);

        musicListAdapter = new MusicListAdapter(mContext, soundList, this);
        recyclerView.setAdapter(musicListAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefresh(true);
            }
        });

        setScrollMethod();

        soundList.clear();
        swipeRefresh(true);
    }

    @Override
    public void update(Observable observable, Object o) {
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
                if (dy > 0 && !isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    soundList.add(null);
                    musicListAdapter.notifyItemInserted(soundList.size() - 1);
                    offset++;
                    isScroll = true;
                    getSearchSound();
                    isLoading = true;
                }
            }
        });
    }

    private void swipeRefresh(final boolean refresh) {
        mSwipeRefreshLayout.setRefreshing(refresh);
        if (refresh) {
            offset = 0;
            totalItemCount = 0;
            lastVisibleItem = 0;
            getSearchSound();
        }
    }


    private void getSearchSound() {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<>();
            if (GetSet.getUserId() == null){
                map.put(Constants.TAG_USER_ID, "" );
            }else {
                map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            }

            if (GetSet.getAuthToken() == null){
                map.put(Constants.TAG_TOKEN, "");
            }else {
                map.put(Constants.TAG_TOKEN, GetSet.getAuthToken());
            }
            map.put(Constants.TAG_LIMIT, "" + limit);
            map.put(Constants.TAG_OFFSET, String.valueOf(limit * offset));
            map.put(Constants.TAG_TYPE, Constants.TAG_SEARCH_SOUND);
            if (!TextUtils.isEmpty(SearchActivity.searchQuery)) {
                map.put(Constants.TAG_TEXT, SearchActivity.searchQuery);
            }

            if (offset > 0) {
                enableSwipeRefresh(false);
            }
            Timber.d("getSearchSound: params=> %s", App.getGsonPrettyInstance().toJson(map));
            Call<SearchSoundRes> call = apiInterface.getSearchSound(map);
            call.enqueue(new Callback<SearchSoundRes>() {
                @Override
                public void onResponse(@NotNull Call<SearchSoundRes> call, @NotNull Response<SearchSoundRes> response) {
                    swipeRefresh(false);
                    if (response.isSuccessful()) {
                        if (offset == 0) {
                            soundList.clear();
                            musicListAdapter.notifyDataSetChanged();
                        }
                        if (response.body().getStatus().equals("true")) {
                            if (isScroll) {
                                removeLoadMore();
                            }
                            List<MusicListResponse.Result> resultList = response.body().getResult();
                            soundList.addAll(resultList);
                            if (resultList.size() > 0) {
                                recyclerView.setVisibility(View.VISIBLE);
                                isLoading = false;
                                musicListAdapter.notifyDataSetChanged();
                            } else
                                isLoading = true;
                        } else {
                            //Remove bottom loading item
                            if (isScroll) {
                                removeLoadMore();
                            }
                            isLoading = true;
                        }
                    } else {
                        isLoading = true;
                    }
                    enableSwipeRefresh(true);
                    setItemLay();
                }

                @Override
                public void onFailure(@NotNull Call<SearchSoundRes> call, @NotNull Throwable t) {
                    t.printStackTrace();
                    enableSwipeRefresh(true);
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
            nullText.setText(getResources().getText(R.string.no_internet_connection));
        }
    }

    private void enableSwipeRefresh(boolean enable) {
        mSwipeRefreshLayout.setEnabled(enable);
    }

    private void setItemLay() {
        if (soundList.size() == 0) {
            nullImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.no_music));
            nullText.setText(mContext.getString(R.string.no_sound));
            nullLay.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            nullLay.setVisibility(View.GONE);
        }
    }

    private void removeLoadMore() {
        if (soundList.size() > 0 && soundList.get(soundList.size() - 1) == null) {
            soundList.remove(soundList.size() - 1);
            musicListAdapter.notifyItemRemoved(soundList.size());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MediaPlayerUtils.pauseMediaPlayer();
        musicListAdapter.notifyDataSetChanged();
    }

    /*public void getLive(final int offset) {
        if (NetworkReceiver.isConnected()) {
            if (!mSwipeRefreshLayout.isRefreshing()) {
                itemAdapter.showLoading(true);
            }
            LiveStreamRequest request = new LiveStreamRequest();
            request.setUserId(GetSet.getUserId());
            request.setProfileId(GetSet.getUserId());
            request.setSortBy(Constants.TAG_RECENT);
            request.setType(Constants.TAG_LIVE);
            request.setLimit("" + Constants.MAX_LIMIT);
            request.setOffset("" + (Constants.MAX_LIMIT * offset));
            request.setFilterApplied("" + false);
            if (!TextUtils.isEmpty(SearchActivity.searchQuery)) {
                request.setSearchKey(SearchActivity.searchQuery);
            }

            Call<LiveStreamResponse> call3 = apiInterface.getCurrentStreams(request);
            call3.enqueue(new Callback<LiveStreamResponse>() {
                @Override
                public void onResponse(Call<LiveStreamResponse> call, Response<LiveStreamResponse> response) {
                    try {
                        if (mSwipeRefreshLayout.isRefreshing() || offset == 0) {
                            broadcastLists.clear();
                        }
                        if (response.isSuccessful()) {
                            LiveStreamResponse data = response.body();
                            if (data.status.equals("true")) {
                                nullLay.setVisibility(View.GONE);
                                broadcastLists.addAll(data.result);
                            }
                            if (broadcastLists.size() == 0) {
                                nullImage.setImageResource(R.drawable.no_video);
                                nullText.setText(getString(R.string.no_videos_found));
                                nullLay.setVisibility(View.VISIBLE);
                            }
                        }

                        if (mSwipeRefreshLayout.isRefreshing()) {
                            swipeRefresh(false);
                            isLoading = true;
                        }
                        itemAdapter.showLoading(false);
                        itemAdapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        broadcastLists.clear();
                        itemAdapter.notifyDataSetChanged();
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(Call<LiveStreamResponse> call, Throwable t) {
                    if (currentPage != 0)
                        currentPage--;

                    if (!mSwipeRefreshLayout.isRefreshing()) {
                        itemAdapter.showLoading(false);
                    } else {
                        if (broadcastLists.size() == 0) {
                            nullText.setVisibility(View.VISIBLE);
                            nullText.setText(getString(R.string.something_went_wrong));
                        }
                        swipeRefresh(false);
                    }
                    call.cancel();
                }
            });
        }
    }*/

    @Override
    public void onItemClicked(Object o, int position, String type) {
        if (type.equals(Constants.TAG_VIEW)) {
            selectedPosition = position;
            MusicListResponse.Result item = (MusicListResponse.Result) o;
            Intent intent = new Intent(mContext, VideoListActivity.class);
            intent.putExtra(Constants.TAG_INTENT_DATA, new Gson().toJson(item));
            intent.putExtra(Constants.TAG_FROM, Constants.TAG_MUSIC);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            updateResultLauncher.launch(intent);
        } else if (type.equals(Constants.TAG_FAV)) {
            MusicListResponse.Result item = (MusicListResponse.Result) o;
            favApi(item.getSoundId());
        }
    }

    private void favApi(String soundId) {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_TOKEN, GetSet.getAuthToken());
            map.put(Constants.TAG_ADDIDTOFAV, soundId);
            map.put(Constants.TAG_FAV_TYPE, "sound");
//            Timber.d("setFav params=> %s", new Gson().toJson(map));

            Call<Map<String, String>> call = apiInterface.setFav(map);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Map<String, String> reportResponse = response.body();
                        if (reportResponse.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            /*if (reportResponse.get(Constants.TAG_MESSAGE).equals(Constants.TAG_UNN_FAV)) {

                            } else if (reportResponse.get(Constants.TAG_MESSAGE).equals(Constants.TAG_SOUND_FAV)) {

                            }*/
                        } else
                            App.makeToast(getString(R.string.something_went_wrong));
                    } else {
                        App.makeToast(getString(R.string.something_went_wrong));
                    }
                }

                @Override
                public void onFailure(@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                    call.cancel();
                    App.makeToast(getString(R.string.something_went_wrong));
                }
            });
        } else {
            Toasty.error(mContext, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if (requestCode == StreamConstants.DELETE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (selectedPosition != null) {
                broadcastLists.remove(selectedPosition.intValue());
                itemAdapter.notifyItemRemoved(selectedPosition.intValue());
                itemAdapter.notifyItemRangeChanged(selectedPosition.intValue(), (itemAdapter.getItemCount() - selectedPosition.intValue()));
                selectedPosition = null;
            }
        } else if (requestCode == StreamConstants.LIVE_STATUS_UPDATE_CODE && resultCode == Activity.RESULT_OK) {
            if (selectedPosition != null) {
                if (broadcastLists != null && broadcastLists.size() > 0) {
                    broadcastLists.get(selectedPosition.intValue()).setType(Constants.TAG_RECORDED);
                    itemAdapter.notifyItemChanged(selectedPosition.intValue());
                    selectedPosition = null;
                }
            }
        }*/
    }
}
