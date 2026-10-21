package com.app.alhawanajd.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.app.alhawanajd.R;
import com.app.alhawanajd.App;
import com.app.alhawanajd.MusicListAdapter;
import com.app.alhawanajd.VideoListActivity;
import com.app.helper.NetworkReceiver;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.model.FavSoundResponse;
import com.app.model.GetSet;
import com.app.model.MusicListResponse;
import com.app.model.Request.GetVideosRequest;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.app.utils.AppUtils;
import com.app.utils.Constants;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;


public class FavSoundFragment extends Fragment implements AdapterItemClickListener {

    private static final String TAG = FavSoundFragment.class.getSimpleName();
    private Context mContext;
    private ApiInterface apiInterface;
    private RecyclerView recyclerView;
    private RelativeLayout nullLay;
    private ImageView nullImage;
    private TextView nullText;
    private ArrayList<MusicListResponse.Result> soundList = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private MusicListAdapter soundAdapter;
    private LinearLayoutManager itemManager;
    private int limit = Constants.MAX_LIMIT, offset = 0;
    private boolean isLoading, isScroll = false;
    private int lastVisibleItem, totalItemCount;
    private int visibleThreshold = 1;
    AppUtils appUtils;
    private ActivityResultLauncher<Intent> audioResultLauncher;
    MaterialButton uploadButton;

    public FavSoundFragment(String profileId) {
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

        View view = inflater.inflate(R.layout.fragment_fav_sound, container, false);
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
        uploadButton = rootView.findViewById(R.id.uplaodButton);
        recyclerView.setHasFixedSize(true);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        visibleThreshold = Constants.MAX_LIMIT;

        itemManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(itemManager);

        soundAdapter = new MusicListAdapter(getActivity(), soundList, this);
        recyclerView.setAdapter(soundAdapter);

        ((SimpleItemAnimator) Objects.requireNonNull(recyclerView.getItemAnimator())).setSupportsChangeAnimations(false);

        mSwipeRefreshLayout.setOnRefreshListener(() -> swipeRefresh(true));
        setScrollMethod();
        soundList.clear();
        swipeRefresh(true);
    }

    private void initActivityResultLauncher() {
        audioResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    swipeRefresh(true);
                }
            }
        });
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
                    soundList.add(null);
                    soundAdapter.notifyItemInserted(soundList.size() - 1);
                    offset++;
                    isScroll = true;
                    mSwipeRefreshLayout.setEnabled(false);
                    getSound();
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
            isLoading = true;
            getSound();
        }
    }

    public void getSound() {
        if (NetworkReceiver.isConnected()) {
            GetVideosRequest request = new GetVideosRequest();
            request.setUserId(GetSet.getUserId());
            request.setProfile_id(GetSet.getUserId());
            request.setType(Constants.TAG_SOUND);
            request.setLimit("" + limit);
            request.setOffset("" + (limit * offset));
            Timber.d("getVideo: params %s", App.getGsonPrettyInstance().toJson(request));

            Call<FavSoundResponse> call3 = apiInterface.getfavsounds(request);
            call3.enqueue(new Callback<FavSoundResponse>() {
                @Override
                public void onResponse(@NotNull Call<FavSoundResponse> call, @NotNull Response<FavSoundResponse> response) {
                    mSwipeRefreshLayout.setEnabled(true);
                    swipeRefresh(false);
                    if (response.isSuccessful()) {
                        if (response.body().getStatus().equals("true")) {
                            if (isScroll) {
                                removeLoadMore();
                            }
                            if (offset == 0) {
                                soundList.clear();
                            }
                            List<MusicListResponse.Result> resultList = response.body().getSounds();
                            soundList.addAll(resultList);
                            recyclerView.setVisibility(View.VISIBLE);
                            if (resultList.size() > 0) {
                                isLoading = false;
                                soundAdapter.notifyDataSetChanged();
                            } else
                                isLoading = true;
                            setNullLay();
                        } else {
                            //Remove bottom loading item
                            if (isScroll) {
                                removeLoadMore();
                            }
                            if (offset == 0) {
                                soundList.clear();
                            }
                            isLoading = true;
                            setNullLay();
                        }
                    }
                    if (mSwipeRefreshLayout.isRefreshing()) {
                        swipeRefresh(false);
                        isLoading = true;
                    }
                    soundAdapter.notifyDataSetChanged();

                }

                @Override
                public void onFailure(Call<FavSoundResponse> call, Throwable t) {
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
            nullImage.setVisibility(View.GONE);
            nullLay.setVisibility(View.VISIBLE);
            nullText.setText(getString(R.string.no_internet_connection));
        }
    }

    private void setNullLay() {
        if (soundList.size() == 0) {
            nullImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.music));
            nullText.setText(mContext.getString(R.string.no_sound));
            nullLay.setVisibility(View.VISIBLE);
        } else {
            nullLay.setVisibility(View.GONE);
        }
    }

    private void removeLoadMore() {
        if (soundList.size() > 0 && soundList.get(soundList.size() - 1) == null) {
            soundList.remove(soundList.size() - 1);
            soundAdapter.notifyItemRemoved(soundList.size());
        }
    }

    @Override
    public void onItemClicked(Object o, int position, String type) {
        if (type.equals(Constants.TAG_FAV)) {
            MusicListResponse.Result item = (MusicListResponse.Result) o;
            favApi(item.getSoundId());
        } else if (type.equals(Constants.TAG_VIEW)) {
            MusicListResponse.Result item = (MusicListResponse.Result) o;
            Intent intent = new Intent(mContext, VideoListActivity.class);
            intent.putExtra(Constants.TAG_INTENT_DATA, new Gson().toJson(item));
            intent.putExtra(Constants.TAG_FROM, Constants.TAG_MUSIC);
            audioResultLauncher.launch(intent);
        }
    }

    private void favApi(String soundId) {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_TOKEN, GetSet.getAuthToken());
            map.put(Constants.TAG_ADDIDTOFAV, soundId);
            map.put(Constants.TAG_FAV_TYPE, "sound");
            Timber.d("setFav params=> %s", new Gson().toJson(map));

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
    public void onPause() {
        super.onPause();
    }
}