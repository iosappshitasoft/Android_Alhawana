package com.app.alhawanajd;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.app.alhawanajd.profile.MyProfileActivity;
import com.app.alhawanajd.profile.OthersProfileActivity;
import com.app.alhawanajd.search.BannerAdapter;
import com.app.alhawanajd.search.HashtagAdapter;
import com.app.alhawanajd.search.MusicAdapter;
import com.app.alhawanajd.search.SearchActivity;
import com.app.alhawanajd.search.StreamsAdapter;
import com.app.db.DBHelper;
import com.app.external.AutoScrollViewPager;
import com.app.external.avloading.AVLoadingIndicatorView;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.helper.callback.OnNetworkChangedListener;
import com.app.model.FollowRequest;
import com.app.model.GetSet;
import com.app.model.MusicListResponse;
import com.app.model.SearchPageResponse;
import com.app.model.UserList;
import com.app.model.VideoListResponse;
import com.app.model.event.GetSearchEvent;
import com.app.model.event.VideoDeleteEvent;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.google.android.material.appbar.AppBarLayout;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class SearchFragmentActivity extends BaseFragmentActivity implements OnNetworkChangedListener {

    private static final String TAG = SearchFragmentActivity.class.getSimpleName();
    private TextView edtSearch;
    private AppBarLayout appBarLayout;
    private ConstraintLayout parentLay;
    private RecyclerView rvSearch;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView nullImage;
    private TextView nullText;
    AppCompatImageView backBtn;
    private RelativeLayout nullLay , searchcontainer;

    private ApiInterface apiInterface;
    private Context context;
    private Typeface typeface;
    private String from;
    private SearchAdapter searchAdapter;
    private final List<SearchPageResponse.Result> searchList = new ArrayList<>();
    public AppUtils appUtils;
    public List<GradientDrawable> countryBgList = new ArrayList<>();
    private int offset = 0;
    private boolean isLoading, isScroll = false;
    private int lastVisibleItem, totalItemCount;
    private int visibleThreshold = 1;
    Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onStart() {
        super.onStart();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_search_new);

        Window window = getWindow();

        // Set your status and nav bar color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorBlack));
        window.setNavigationBarColor(ContextCompat.getColor(this, R.color.colorBlack));

        // Ensure light/dark icons based on background
        WindowInsetsControllerCompat insetsController =
                new WindowInsetsControllerCompat(window, window.getDecorView());

        // For white icons on dark status bar
        insetsController.setAppearanceLightStatusBars(false);

        context=SearchFragmentActivity.this;
        Bundle bundle = getIntent().getExtras();
        from = bundle.getString(Constants.TAG_FROM);
        EventBus.getDefault().register(this);
        appUtils = new AppUtils(context);
        countryBgList = appUtils.getCountryBGList();
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = getResources().getFont(R.font.font_light);
        } else {
            typeface = ResourcesCompat.getFont(context, R.font.font_light);
        }
        findViews();
        initView();
    }


    private void findViews() {
        edtSearch = findViewById(R.id.edtSearch);
        searchcontainer = findViewById(R.id.search_container);
        appBarLayout = findViewById(R.id.appBarLayout);
        parentLay = findViewById(R.id.parentLay);
        rvSearch =findViewById(R.id.rvSearch);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        nullImage = findViewById(R.id.nullImage);
        nullText = findViewById(R.id.nullText);
        nullLay = findViewById(R.id.nullLay);
        backBtn = findViewById(R.id.btnBack);

    }

    private void initView() {
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        if (LocaleManager.isRTL()) {
            backBtn.setRotation(180);
        } else {
            backBtn.setRotation(0);
        }

        backBtn.setOnClickListener(v->finish());

        nullImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.no_videos));
        nullText.setText(getString(R.string.no_broadcast_found));

        searchAdapter = new SearchAdapter(context, searchList);
        rvSearch.setAdapter(searchAdapter);

        RecyclerView.OnItemTouchListener listener = (new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                int action = e.getAction();
                if (rv.canScrollHorizontally(RecyclerView.FOCUS_FORWARD)) {
                    if (action == MotionEvent.ACTION_MOVE) {
                        rv.getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    return false;
                } else {
                    if (action == MotionEvent.ACTION_MOVE) {
                        rv.getParent().requestDisallowInterceptTouchEvent(false);
                    }
                    rv.removeOnItemTouchListener(this);
                    return true;
                }
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
        rvSearch.addOnItemTouchListener(listener);
//        setScrollMethod();

        searchAdapter.notifyDataSetChanged();

        edtSearch.setOnClickListener(view -> {
            Intent intent = new Intent(context, SearchActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });

        searchcontainer.setOnClickListener(view -> {
            Log.d("SearchFragment", "Search icon clicked");
            Intent intent = new Intent(context, SearchActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });

        /*popularLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AllCountriesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

        btnCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popularLay.performClick();
            }
        });*/

        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefresh(true);
        });
        swipeRefresh(true);
    }

    private void setScrollMethod() {
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) rvSearch.getLayoutManager();
        rvSearch.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int topRowVerticalPosition =
                        (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(topRowVerticalPosition >= 0);

                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                Log.e(TAG, "onScrolled: ::::"+(!swipeRefreshLayout.isRefreshing())+"::::"+(dy >= 0)+
                        ":::"+(!isLoading)+"::::"+totalItemCount+":::"+lastVisibleItem+"::::"+visibleThreshold+":::::"+(totalItemCount <= (lastVisibleItem + visibleThreshold))+"\n"+
                        (!swipeRefreshLayout.isRefreshing() && dy >= 0 && !isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)));

                if (!swipeRefreshLayout.isRefreshing() && dy >= 0 && !isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    searchList.add(null);
                    searchAdapter.notifyItemInserted(searchList.size() - 1);
                    offset++;
                    isScroll = true;
                    getSearchList(offset,"setScrollMethod");
                    isLoading = true;
                }
            }
        });
    }

    private void swipeRefresh(boolean refresh) {
        swipeRefreshLayout.setRefreshing(refresh);
        if (refresh) {
            isScroll = false;
            getSearchList(offset = 0,"refresh");
            /*HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_OFFSET, "" + offset);
            map.put(Constants.TAG_TEXT, "");
            Timber.tag(TAG).i("getSearchPage %s", App.getGsonPrettyInstance().toJson(map));
            List<Observable<SearchPageResponse>> requests = new ArrayList<>();
            requests.add(apiInterface.getSearchList(map));
            offset++;
            map.put(Constants.TAG_OFFSET, "" + offset);
            requests.add(apiInterface.getSearchList(map));
            offset++;
            map.put(Constants.TAG_OFFSET, "" + offset);
            requests.add(apiInterface.getSearchList(map));
            disposable = Observable.zip(requests, objects -> {

                return objects;
            });
            isScroll = false;*/
        }
    }

    int previousOffset = -1;
    private void getSearchList(final int offset,String from) {

        if (previousOffset != offset) {
            previousOffset = offset;
            if (NetworkReceiver.isConnected()) {
                HashMap<String, String> map = new HashMap<>();
                map.put(Constants.TAG_USER_ID, GetSet.getUserId());
                map.put(Constants.TAG_OFFSET, "" + offset);
                map.put(Constants.TAG_TEXT, "");
                if (LocaleManager.isRTL()) {
                    map.put(Constants.TAG_LANGUAGE_CODE, "ar");
                } else {
                    map.put(Constants.TAG_LANGUAGE_CODE, "en");
                }
                Timber.tag(TAG).i("getSearchPage %s %s", from, App.getGsonPrettyInstance().toJson(map));

                Call<SearchPageResponse> call = apiInterface.getSearchList(/*GetSet.getAuthToken(),*/ map);
                call.enqueue(new Callback<SearchPageResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<SearchPageResponse> call, @NotNull Response<SearchPageResponse> response) {
                        swipeRefresh(false);
                        if (isScroll) {
                            searchList.remove(searchList.size() - 1);
                            searchAdapter.notifyItemRemoved(searchList.size());
                        }

                        if (response.isSuccessful()) {
                            Timber.tag(TAG).i("getSearchPage %s %s", from, App.getGsonPrettyInstance().toJson(response.body()));

                            if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                                if (offset == 0) {
                                    searchList.clear();
                                    searchAdapter.notifyDataSetChanged();
                                }
                                if (response.body().getResult().size() > 0) {
                                    int tempPosition = searchList.size();
                                    searchList.addAll(response.body().getResult());
                                    isLoading = false;
                                    searchAdapter.notifyItemRangeInserted(tempPosition, response.body().getResult().size());
                                    mainHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {

                                            int finalOffset = offset + 1;
                                            getSearchList(finalOffset, "api");
                                        }
                                    }, 1000);
                                } else {
                                    isLoading = true;
                                }
                            } else if (response.body().getStatus().equals(Constants.TAG_ACCOUNT_BLOCKED)) {
                                App.makeToast(getString(R.string.Account_block));
                                GetSet.reset();
                                SharedPref.clearAll();
                                DBHelper.getInstance(context).clearDB();
                                Intent logout = new Intent(context, LoginActivity.class);
                                logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(logout);
                            }
                        }
                        setNullLay(false);
                    }

                    @Override
                    public void onFailure(Call<SearchPageResponse> call, Throwable t) {
                        swipeRefresh(false);
                        call.cancel();
                        t.printStackTrace();
                        setNullLay(true);
                    }
                });
            } else {
                swipeRefresh(false);
                //  singleBanner.setVisibility(View.GONE);
                rvSearch.setVisibility(View.GONE);
                nullLay.setVisibility(View.VISIBLE);
                nullText.setText(R.string.no_internet_connection);
            }
        }

    }
    private void setNullLay(boolean isServerError) {
        if (!isServerError) {
            if (searchList.size() == 0) {
                nullImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.no_videos));
                nullText.setText(getString(R.string.no_broadcast_found));
                nullLay.setVisibility(View.VISIBLE);
                rvSearch.setVisibility(View.GONE);
            } else {
                rvSearch.setVisibility(View.VISIBLE);
                nullLay.setVisibility(View.GONE);
            }
        } else {
            if (searchList.size() == 0) {
                nullImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.no_videos));
                nullText.setText(getString(R.string.unable_to_reach_server));
                nullLay.setVisibility(View.VISIBLE);
                rvSearch.setVisibility(View.GONE);
            } else {
                nullLay.setVisibility(View.GONE);
                rvSearch.setVisibility(View.VISIBLE);
            }
        }
    }

  /*  @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof MainActivity) {
            ((MainActivity) context).compositeOnNetworkChangedListener.register(this);
        }
    }*/

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getLiveStreams(GetSearchEvent searchEvent) {
        updateFragments();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnVideoDeleteEvent(VideoDeleteEvent deleteEvent) {
        Timber.d("OnVideoDeleteEvent: %s", deleteEvent.getVideoId());
        swipeRefresh(true);
    }

    private void updateFragments() {
        getSearchList(offset = 0,"update");
    }

    @Override
    public void onResume() {
        super.onResume();
        isScroll = false;
        getSearchList(offset = 0,"resume");
    }
/*
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }*/

    @Override
    public void onNetworkChanged(Boolean connect) {
        if (connect && searchList.size() == 0) {
            if (nullLay != null && nullLay.getVisibility() == View.VISIBLE)
                getSearchList(offset = 0,"network");
        }
    }

    public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final Context mContext;
        private List<SearchPageResponse.Result> searchList = new ArrayList<>();
        private final Random random = new Random();
        private final RecyclerView.RecycledViewPool viewPool;
        private static final int VIEW_TYPE_BANNER = 0;
        private static final int VIEW_TYPE_USER = 1;
        private static final int VIEW_TYPE_HASHTAG = 2;
        private static final int VIEW_TYPE_VIDEOS = 3;
        private static final int VIEW_TYPE_MUSIC = 4;
        private static final int VIEW_TYPE_FOOTER = 5;


        public SearchAdapter(Context context, List<SearchPageResponse.Result> searchList) {
            this.mContext = context;
            this.searchList = searchList;
            viewPool = new RecyclerView.RecycledViewPool();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case VIEW_TYPE_BANNER: {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_banner, parent, false);
                    return new BannerViewHolder(v);
                }
                case VIEW_TYPE_USER: {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_users, parent, false);
                    return new UserViewHolder(v);
                }
                case VIEW_TYPE_VIDEOS: {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_users, parent, false);
                    return new StreamViewHolder(v);
                }
                case VIEW_TYPE_HASHTAG: {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_users, parent, false);
                    return new HashTagViewHolder(v);
                }
                case VIEW_TYPE_MUSIC: {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_users, parent, false);
                    return new MusicViewHolder(v);
                }
                case VIEW_TYPE_FOOTER: {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.livza_progress, parent, false);
                    return new LoadingViewHolder(v);
                }
            }
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            if (viewHolder instanceof BannerViewHolder) {
                BannerViewHolder holder = (BannerViewHolder) viewHolder;
                List<SearchPageResponse.Result.Banner> item = searchList.get(holder.getAbsoluteAdapterPosition()).getBanner();
                if (!item.isEmpty()) {
                    holder.itemView.setVisibility(View.VISIBLE);
                    RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    holder.itemView.setLayoutParams(params);
                    BannerAdapter bannerAdapter = new BannerAdapter(mContext, item,
                            new AdapterItemClickListener() {
                                @Override
                                public void onItemClicked(Object o, int position, String type) {
                                    if (type.equals(Constants.TAG_USER)) {
                                        SearchPageResponse.Result.Banner finalItem = (SearchPageResponse.Result.Banner) o;

                                        if (finalItem.getUserId().equals(GetSet.getUserId())) {
                                            Intent intent = new Intent(mContext, MyProfileActivity.class);
                                            intent.putExtra(Constants.TAG_USER_ID, finalItem.getUserId());
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                            mContext.startActivity(intent);
                                        } else {
                                            Intent intent = new Intent(mContext, OthersProfileActivity.class);
                                            intent.putExtra(Constants.TAG_USER_ID, finalItem.getUserId());
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                            mContext.startActivity(intent);
                                        }
                                    } else if (type.equals(Constants.TAG_HASHTAGS)) {
                                        SearchPageResponse.Result.Banner finalItem = (SearchPageResponse.Result.Banner) o;
                                        Intent intent = new Intent(mContext, VideoListActivity.class);
                                        intent.putExtra(Constants.TAG_TITLE, finalItem.getHashtagName());
                                        intent.putExtra(Constants.TAG_FROM, Constants.TAG_RELATED_HASHTAG);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        mContext.startActivity(intent);
                                    }
                                }
                            });
                    float height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 160,
                            mContext.getResources().getDisplayMetrics());
                    int margin = (int) mContext.getResources().getDimension(R.dimen.margin_medium_large);
                    holder.viewPager.getLayoutParams().height = (int) height;
                    holder.viewPager.setPageMargin((int) (mContext.getResources().getDisplayMetrics().widthPixels * -0.33));
                    holder.viewPager.setPadding(0, margin, 0, 0);
                    holder.viewPager.setOffscreenPageLimit(item.size());
                    holder.viewPager.startAutoScroll();
                    holder.viewPager.setPageTransformer(false, new ViewPager.PageTransformer() {
                        @Override
                        public void transformPage(View page, float position) {
                            float scaleX = 0.88f - Math.abs(position * 0.7f);
                            if (!Double.isNaN(scaleX)) {
                                page.setScaleX(scaleX);
                            }
                            //                        page.setScaleY(0.8f - Math.abs(position * 0.6f));
                            page.setAlpha(1.0f - Math.abs(position * 0.5f));
                        }
                    });
                    holder.viewPager.setAdapter(bannerAdapter);
                    bannerAdapter.notifyDataSetChanged();
                } else {
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                }
            }
            else if (viewHolder instanceof UserViewHolder) {
                UserViewHolder holder = (UserViewHolder) viewHolder;
                List<UserList.Result> item = searchList.get(holder.getAbsoluteAdapterPosition()).getUsers();
                if (item.size() == 0) {
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                    Timber.tag(TAG).d("HashTagViewHolder: %s", item.size());
                    holder.itemLay.setVisibility(View.GONE);
                } else {
                    holder.itemView.setVisibility(View.VISIBLE);
                    RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    int topMargin = (int) mContext.getResources().getDimension(R.dimen.margin_large);
                    params.topMargin = (int) topMargin;
                    holder.itemView.setLayoutParams(params);
                    holder.itemLay.setVisibility(View.VISIBLE);
                    holder.txtTitle.setText(searchList.get(holder.getAbsoluteAdapterPosition()).getTitle());
                    if (item.size() > 10) {
                        //below i change viewAllLay to  viewAllLay_Arrow
                        holder.viewAllLay_Arrow.setVisibility(View.VISIBLE);
                    } else {
                      //  holder.viewAllLay.setVisibility(View.VISIBLE);
                    }
                    //below i change viewAllLay to viewAllLay_Arrow
                    holder.viewAllLay_Arrow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(mContext, UsersListActivity.class);
                            intent.putExtra(Constants.TAG_TITLE, searchList.get(holder.getAbsoluteAdapterPosition()).getTitle());
                            intent.putExtra(Constants.TAG_TYPE, searchList.get(holder.getAbsoluteAdapterPosition()).getUserType());
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            mContext.startActivity(intent);
                        }
                    });
                    UsersAdapter usersAdapter = new UsersAdapter(mContext,
                            item,
                            new AdapterItemClickListener() {
                                @Override
                                public void onItemClicked(Object o, int position, String type) {
                                    if (type.equals(Constants.TAG_FOLLOW)) {
                                        UserList.Result finalItem = (UserList.Result) o;
                                        followUnFollowUser(finalItem.getUserId(), finalItem.isFollow());
                                    } else if (type.equals(Constants.TAG_USERS)) {
                                        UserList.Result finalItem = (UserList.Result) o;
                                        if (finalItem.getUserId().equals(GetSet.getUserId())) {
                                            Intent profile = new Intent(mContext, MyProfileActivity.class);
                                            profile.putExtra(Constants.TAG_EDIT, true);
                                            profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                            mContext.startActivity(profile);
                                        } else {
                                            Intent profile = new Intent(mContext, OthersProfileActivity.class);
                                            profile.putExtra(Constants.TAG_USER_ID, finalItem.getUserId());
                                            profile.putExtra(Constants.TAG_INTENT_DATA, new Gson().toJson(finalItem));
                                            profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                            mContext.startActivity(profile);
                                        }
                                    }
                                }
                            });
                    holder.rvUsers.setAdapter(usersAdapter);
                    usersAdapter.notifyDataSetChanged();
                }
            } else if (viewHolder instanceof StreamViewHolder) {
                StreamViewHolder holder = (StreamViewHolder) viewHolder;
                List<VideoListResponse.Result> item = searchList.get(holder.getAbsoluteAdapterPosition()).getStreams();
                if (item.size() == 0) {
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                    Timber.tag(TAG).d("HashTagViewHolder: %s", item.size());
                    holder.itemLay.setVisibility(View.GONE);
                } else {
                    holder.itemView.setVisibility(View.VISIBLE);
                    RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    int topMargin = (int) mContext.getResources().getDimension(R.dimen.margin_large);
                    params.topMargin = (int) topMargin;
                    holder.itemView.setLayoutParams(params);
                    holder.itemLay.setVisibility(View.VISIBLE);
                    holder.txtTitle.setText(searchList.get(holder.getAbsoluteAdapterPosition()).getTitle());
                    holder.viewAllLay.setVisibility(View.GONE);
                    if (item.size() > 10) {
                        holder.viewAllLay_Arrow.setVisibility(View.VISIBLE);
                    } else {
                       // holder.viewAllLay.setVisibility(View.VISIBLE);
                    }
                    holder.viewAllLay_Arrow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(mContext, VideoListActivity.class);
                            intent.putExtra(Constants.TAG_TITLE, searchList.get(holder.getAbsoluteAdapterPosition()).getTitle());
                            intent.putExtra(Constants.TAG_TYPE, searchList.get(holder.getAbsoluteAdapterPosition()).getVideoType());
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            mContext.startActivity(intent);
                        }
                    });
                    StreamsAdapter streamsAdapter = new StreamsAdapter(mContext, item,
                            new AdapterItemClickListener() {
                                @Override
                                public void onItemClicked(Object o, int position, String type) {

                                    VideoListResponse.Result item = (VideoListResponse.Result) o;
                                    //Intent intent = new Intent(getContext(), SingleVideoActivity.class);
                                    Intent intent = new Intent(getApplicationContext(), MultipleVideoActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    intent.putExtra(Constants.TAG_TYPE, searchList.get(holder.getAbsoluteAdapterPosition()).getType());
                                    Log.d(TAG, "onItemClicked_type_search: "+type);
                                    intent.putExtra(Constants.TAG_TAGID, searchList.get(holder.getAbsoluteAdapterPosition()).getTitle());
                                    intent.putExtra(Constants.TAG_VIDEO_ID, item.getVideoId());
                                    intent.putExtra(Constants.TAG_POSITION, item.getPosition());
                                    intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, item.getStreamThumbnail());
                                    startActivity(intent);
                                }
                            });
                    holder.rvUsers.setAdapter(streamsAdapter);
                    streamsAdapter.notifyDataSetChanged();
                }
            } else if (viewHolder instanceof HashTagViewHolder) {
                HashTagViewHolder holder = (HashTagViewHolder) viewHolder;
                List<VideoListResponse.Result> item = searchList.get(holder.getAbsoluteAdapterPosition()).getStreams();
//                Timber.tag(TAG).d("HashTagViewHolder: %s", new Gson().toJson(item));
                if (item.size() == 0) {
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                    Timber.tag(TAG).d("HashTagViewHolder: %s", item.size());
                    holder.itemLay.setVisibility(View.GONE);
                } else {
                    holder.itemView.setVisibility(View.VISIBLE);
                    RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    int topMargin = (int) mContext.getResources().getDimension(R.dimen.margin_large);
                    params.topMargin = topMargin;
                    holder.itemView.setLayoutParams(params);
                    holder.itemLay.setVisibility(View.VISIBLE);
                    holder.txtTitle.setText(searchList.get(holder.getAbsoluteAdapterPosition()).getTitle());
                    holder.txtTitle.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });
                    //holder.viewAllLay.setVisibility(View.VISIBLE);
                    if (item.size() > 10) {
                        holder.viewAllLay_Arrow.setVisibility(View.VISIBLE);
                    } else {
                       // holder.viewAllLay.setVisibility(View.VISIBLE);
                    }
                    holder.viewAllLay_Arrow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(mContext, VideoListActivity.class);
                            intent.putExtra(Constants.TAG_TITLE, searchList.get(holder.getAbsoluteAdapterPosition()).getTitle());
                            intent.putExtra(Constants.TAG_TYPE, searchList.get(holder.getAbsoluteAdapterPosition()).getVideoType());
                            intent.putExtra(Constants.TAG_FROM, Constants.TAG_HASHTAGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            mContext.startActivity(intent);
                        }
                    });
                    HashtagAdapter hashtagAdapter = new HashtagAdapter(mContext, item,
                            new AdapterItemClickListener() {
                                @Override
                                public void onItemClicked(Object o, int position, String type) {
                                    VideoListResponse.Result item = (VideoListResponse.Result) o;
                                    /*Intent intent = new Intent(getContext(), SingleVideoActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    intent.putExtra(Constants.TAG_VIDEO_ID, item.getVideoId());
                                    intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, item.getStreamThumbnail());
                                    startActivity(intent);*/
                                    Intent intent = new Intent(getApplicationContext(), MultipleVideoActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    intent.putExtra(Constants.TAG_TYPE, searchList.get(holder.getAbsoluteAdapterPosition()).getType());
                                    String tagid = searchList.get(holder.getAbsoluteAdapterPosition()).getTitle();
                                    if (tagid.contains("#")) {
                                        tagid = searchList.get(holder.getAbsoluteAdapterPosition()).getTitle().replace("#", "");
                                    }
                                    intent.putExtra(Constants.TAG_TAGID, tagid);
                                    intent.putExtra(Constants.TAG_VIDEO_ID, item.getVideoId());
                                    intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, item.getStreamThumbnail());
                                    intent.putExtra(Constants.TAG_POSITION, item.getPosition());
                                    startActivity(intent);
                                }
                            });
                    holder.rvUsers.setAdapter(hashtagAdapter);
                    hashtagAdapter.notifyDataSetChanged();
                }
            } else if (viewHolder instanceof MusicViewHolder) {
                MusicViewHolder holder = (MusicViewHolder) viewHolder;
                List<MusicListResponse.Result> item = searchList.get(holder.getAbsoluteAdapterPosition()).getMusic();
                if (item.size() == 0) {
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                    Timber.tag(TAG).d("HashTagViewHolder: %s", item.size());
                    holder.itemLay.setVisibility(View.GONE);
                } else {
                    holder.itemView.setVisibility(View.VISIBLE);
                    RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    int topMargin = (int) mContext.getResources().getDimension(R.dimen.margin_large);
                    params.topMargin = (int) topMargin;
                    holder.itemView.setLayoutParams(params);
                    holder.itemLay.setVisibility(View.VISIBLE);
                    holder.txtTitle.setText(searchList.get(holder.getAbsoluteAdapterPosition()).getTitle());
                    if (item.size() > 10) {
                        holder.viewAllLay_Arrow.setVisibility(View.VISIBLE);
                    } else {
                      //  holder.viewAllLay.setVisibility(View.VISIBLE);
                    }
                    holder.viewAllLay_Arrow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(mContext, MusicListActivity.class);
                            intent.putExtra(Constants.TAG_TITLE, searchList.get(holder.getAbsoluteAdapterPosition()).getTitle());
                            intent.putExtra(Constants.TAG_TYPE, searchList.get(holder.getAbsoluteAdapterPosition()).getMusicType());
                            intent.putExtra(Constants.TAG_FROM, Constants.TAG_MUSIC);

                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            mContext.startActivity(intent);
                        }
                    });
                    MusicAdapter musicAdapter = new MusicAdapter(mContext, item,
                            new AdapterItemClickListener() {
                                @Override
                                public void onItemClicked(Object o, int position, String type) {
                                    Intent intent = new Intent(mContext, VideoListActivity.class);
                                    intent.putExtra(Constants.TAG_TITLE, searchList.get(holder.getAbsoluteAdapterPosition()).getTitle());
                                    intent.putExtra(Constants.TAG_TYPE, searchList.get(holder.getAbsoluteAdapterPosition()).getVideoType());
                                    intent.putExtra(Constants.TAG_FROM, Constants.TAG_MUSIC);
                                    intent.putExtra(Constants.TAG_INTENT_DATA, new Gson().toJson(item.get(position)));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    mContext.startActivity(intent);
                                }
                            });
                    holder.rvUsers.setAdapter(musicAdapter);
                    musicAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public int getItemCount() {
            return searchList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (searchList.get(position) == null)
                return VIEW_TYPE_FOOTER;

            switch (searchList.get(position).getType()) {
                case Constants.TAG_BANNER:
                    return VIEW_TYPE_BANNER;
                case Constants.TAG_USERS:
                    return VIEW_TYPE_USER;
                case Constants.TAG_HASHTAGS:
                    return VIEW_TYPE_HASHTAG;
                case Constants.TAG_VIDEOS:
                    return VIEW_TYPE_VIDEOS;

                case Constants.TAG_MUSIC:
                default:
                    return VIEW_TYPE_MUSIC;
            }
        }
    }

    private void followUnFollowUser(String otherUserId, boolean follow) {
        if (NetworkReceiver.isConnected()) {
            FollowRequest followRequest = new FollowRequest();
            if (otherUserId != null) followRequest.setUserId(otherUserId);
            followRequest.setFollowerId(GetSet.getUserId());
            if (follow)
                followRequest.setType(Constants.TAG_FOLLOW_USER);
            else
                followRequest.setType(Constants.TAG_UNFOLLOW_USER);

            Call<Map<String, String>> call = apiInterface.followUser(followRequest);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {

                }

                @Override
                public void onFailure
                        (@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private class BannerViewHolder extends RecyclerView.ViewHolder {
        AutoScrollViewPager viewPager;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            viewPager = itemView.findViewById(R.id.viewPager);
        }
    }

    private static class UserViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout itemLay;
        private RecyclerView rvUsers;
        private LinearLayout viewAllLay;
        private ImageView viewAllLay_Arrow;
        private TextView txtTitle, txtViewAll, txtSubtitle;
        private ImageView btnViewAll;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            itemLay = itemView.findViewById(R.id.itemLay);
            rvUsers = itemView.findViewById(R.id.rvUsers);
            viewAllLay = itemView.findViewById(R.id.viewAllLay1);
            txtViewAll = itemView.findViewById(R.id.txtViewAll);
            btnViewAll = itemView.findViewById(R.id.btnViewAll);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            viewAllLay_Arrow = itemView.findViewById(R.id.viewAllLay);
            txtSubtitle = itemView.findViewById(R.id.txtSubtitle);

            if (LocaleManager.isRTL()) {
                btnViewAll.setRotation(180);
            } else {
                btnViewAll.setRotation(0);
            }
        }
    }

    private static class StreamViewHolder extends RecyclerView.ViewHolder {

        private LinearLayout itemLay;
        private RecyclerView rvUsers;
        private LinearLayout viewAllLay;
        private TextView txtViewAll, txtTitle;
        private ImageView btnViewAll;
        private ImageView viewAllLay_Arrow;

        public StreamViewHolder(@NonNull View itemView) {
            super(itemView);
            itemLay = itemView.findViewById(R.id.itemLay);
            rvUsers = itemView.findViewById(R.id.rvUsers);
            viewAllLay = itemView.findViewById(R.id.viewAllLay1);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtViewAll = itemView.findViewById(R.id.txtViewAll);
            btnViewAll = itemView.findViewById(R.id.btnViewAll);
            viewAllLay_Arrow = itemView.findViewById(R.id.viewAllLay);

            if (LocaleManager.isRTL()) {
                btnViewAll.setRotation(180);
            } else {
                btnViewAll.setRotation(0);
            }
        }
    }

    private static class HashTagViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout itemLay;
        private RecyclerView rvUsers;
        private LinearLayout viewAllLay;
        private TextView txtTitle, txtViewAll;
        private ImageView btnViewAll;
        private ImageView viewAllLay_Arrow;

        public HashTagViewHolder(@NonNull View itemView) {
            super(itemView);
            itemLay = itemView.findViewById(R.id.itemLay);
            rvUsers = itemView.findViewById(R.id.rvUsers);
            viewAllLay = itemView.findViewById(R.id.viewAllLay1);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtViewAll = itemView.findViewById(R.id.txtViewAll);
            btnViewAll = itemView.findViewById(R.id.btnViewAll);
            viewAllLay_Arrow = itemView.findViewById(R.id.viewAllLay);

            if (LocaleManager.isRTL()) {
                btnViewAll.setRotation(180);
            } else {
                btnViewAll.setRotation(0);
            }
        }
    }

    private class MusicViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout itemLay;
        private RecyclerView rvUsers;
        private LinearLayout viewAllLay;
        private TextView txtTitle, txtViewAll;
        private ImageView btnViewAll;
        private ImageView viewAllLay_Arrow;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            itemLay = itemView.findViewById(R.id.itemLay);
            rvUsers = itemView.findViewById(R.id.rvUsers);
            viewAllLay = itemView.findViewById(R.id.viewAllLay1);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtViewAll = itemView.findViewById(R.id.txtViewAll);
            btnViewAll = itemView.findViewById(R.id.btnViewAll);
            viewAllLay_Arrow = itemView.findViewById(R.id.viewAllLay);


            if (LocaleManager.isRTL()) {
                btnViewAll.setRotation(180);
            } else {
                btnViewAll.setRotation(0);
            }
        }
    }

    public static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public AVLoadingIndicatorView progressBar;
        FrameLayout progressLay;

        public LoadingViewHolder(View view) {
            super(view);
            progressBar = view.findViewById(R.id.progressBarNew);
            progressLay = view.findViewById(R.id.progressLay);
        }
    }

}
