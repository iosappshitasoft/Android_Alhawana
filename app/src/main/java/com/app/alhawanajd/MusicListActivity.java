package com.app.alhawanajd;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.alhawanajd.databinding.ActivityMusicListBinding;
import com.google.gson.Gson;
import com.app.alhawanajd.R;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.model.GetSet;
import com.app.model.MusicListResponse;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.app.utils.Constants;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class MusicListActivity extends BaseFragmentActivity implements AdapterItemClickListener {

    private static final String TAG = MusicListActivity.class.getSimpleName();
    private Context mContext;
    /*@BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtSubTitle)
    TextView txtSubTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.nullLay)
    RelativeLayout nullLay;
    @BindView(R.id.nullImage)
    ImageView nullImage;
    @BindView(R.id.nullText)
    TextView nullText;*/

    private ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
    private MusicListAdapter adapter;
    private final List<MusicListResponse.Result> musicList = new ArrayList<>();
    private int limit = 10, offset = 0;
    private int visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold;
    private boolean isLoading = true;
    private String from, title, type, hashtag;
    private ActivityResultLauncher<Intent> videoResultLauncher;
    private ActivityMusicListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*setContentView(R.layout.activity_music_list);
        ButterKnife.bind(this);*/
        binding = ActivityMusicListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mContext = this;
        if (getIntent().hasExtra(Constants.TAG_TITLE))
            title = getIntent().getStringExtra(Constants.TAG_TITLE);
        if (getIntent().hasExtra(Constants.TAG_TYPE))
            type = getIntent().getStringExtra(Constants.TAG_TYPE);
        if (getIntent().hasExtra(Constants.TAG_FROM))
            from = getIntent().getStringExtra(Constants.TAG_FROM);
        if (from != null && from.equals(Constants.TAG_HASHTAGS)) {
            hashtag = getIntent().getStringExtra(Constants.TAG_TITLE);
        }
        initView();
    }

    public void initView() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .primaryColor(getResources().getColor(R.color.colorWhite))
                .secondaryColor(getResources().getColor(R.color.colorWhite))
                .build();
        Slidr.attach(this, config);
        binding.toolbar.txtSubTitle.setVisibility(View.GONE);
        if (title != null) {
            binding.toolbar.txtTitle.setText(title);
        }
//        layoutManager = new NpaGridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);
        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.recyclerView.getLayoutManager();
        binding.recyclerView.setLayoutManager(layoutManager);

        adapter = new MusicListAdapter(MusicListActivity.this, musicList, this);
        binding.recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        float spacing = getResources().getDimension(R.dimen.activity_horizontal_margin);
        float smallMargin = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                5,
                mContext.getResources().getDisplayMetrics()
        );
        binding.swipeRefreshLayout.setOnRefreshListener(() -> getMusicList(offset = 0));

        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {//check for scroll down
                    visibleItemCount = recyclerView.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

                    synchronized (this) {
                        if (isLoading) {
                            if (totalItemCount > previousTotal) {
                                isLoading = false;
                                previousTotal = totalItemCount;
                            }
                        }

                        if ((visibleItemCount + firstVisibleItem) >= totalItemCount
                                && firstVisibleItem >= 0) {
                            offset++;
                            isLoading = true;
                            getMusicList(offset);
                        }
                    }
                }
            }
        });
        if (LocaleManager.isRTL()) {
            binding.toolbar.btnBack.setRotation(180);
        } else {
            binding.toolbar.btnBack.setRotation(0);
        }

        binding.toolbar.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        initActivityResultLauncher();
        swipeRefresh(true);
    }

    private void initActivityResultLauncher() {
        videoResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

            }
        });
    }

    private void getMusicList(int offset) {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> requestMap = new HashMap<>();
            requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
            requestMap.put(Constants.TAG_TYPE, type);
            requestMap.put(Constants.TAG_LIMIT, "" + limit);
            requestMap.put(Constants.TAG_OFFSET, "" + (offset * limit));
            Timber.tag(TAG).d("getMusicList: %s", App.getGsonPrettyInstance().toJson(requestMap));

            Call<MusicListResponse> call = apiInterface.getMusicList(requestMap);
            call.enqueue(new Callback<MusicListResponse>() {
                @Override
                public void onResponse(Call<MusicListResponse> call, Response<MusicListResponse> response) {
                    swipeRefresh(false);
                    if (response.isSuccessful()) {
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            if (offset == 0) {
                                musicList.clear();
                            }
                            musicList.addAll(response.body().getResult());
                            adapter.notifyDataSetChanged();
                        }
                    }
                    setNullLay();
                }

                @Override
                public void onFailure(Call<MusicListResponse> call, Throwable t) {
                    Timber.tag(TAG).e("getMusicList: %s", t.getMessage());
                }
            });
        }
    }

    private void setNullLay() {
        if (musicList.size() == 0) {
            binding.nullLay1.nullLay.setVisibility(View.VISIBLE);
            binding.nullLay1.nullImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.music));
            binding.nullLay1.nullText.setText(getString(R.string.no_videos_found));
        } else {
            binding.nullLay1.nullLay.setVisibility(View.GONE);
        }
    }

    private void swipeRefresh(final boolean refresh) {
        binding.swipeRefreshLayout.setRefreshing(refresh);
        if (refresh) {
            previousTotal = 0;
            offset = 0;
            totalItemCount = 0;
            visibleItemCount = 0;
            firstVisibleItem = 0;
            getMusicList(offset);
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }



    @Override
    public void onItemClicked(Object o, int position, String type) {
        if (type.equals(Constants.TAG_VIEW)) {
            MusicListResponse.Result item = (MusicListResponse.Result) o;
            Intent intent = new Intent(this, VideoListActivity.class);
            intent.putExtra(Constants.TAG_INTENT_DATA, new Gson().toJson(item));
            intent.putExtra(Constants.TAG_FROM, Constants.TAG_MUSIC);
            intent.putExtra(Constants.TAG_TYPE, this.type);
            startActivity(intent);
        } else if (type.equals(Constants.TAG_FAV)) {
            MusicListResponse.Result item = (MusicListResponse.Result) o;
            addFavourite(item.getSoundId());
        }
    }

    private void addFavourite(String soundId) {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_TOKEN, GetSet.getAuthToken());
            map.put(Constants.TAG_ADDIDTOFAV, soundId);
            map.put(Constants.TAG_FAV_TYPE, "sound");

            Call<Map<String, String>> call = apiInterface.setFav(map);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {

                    } else {
                        App.makeToast(getString(R.string.something_went_wrong));
                    }

                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    call.cancel();
                    App.makeToast(getString(R.string.something_went_wrong));
                }
            });
        } else {
            App.makeToast(getString(R.string.no_internet_connection));
        }
    }
}