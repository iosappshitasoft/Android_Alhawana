package com.app.alhawanajd;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.app.alhawanajd.R;
import com.app.alhawanajd.profile.MyProfileActivity;
import com.app.alhawanajd.profile.OthersProfileActivity;
import com.app.alhawanajd.search.adapter.SearchUserAdapter;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.model.FollowRequest;
import com.app.model.GetSet;
import com.app.model.UserList;
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

public class UsersListActivity extends BaseFragmentActivity implements AdapterItemClickListener, View.OnClickListener {

    private static final String TAG = UsersListActivity.class.getSimpleName();

    private ImageView btnBack;
    private TextView txtTitle;
    private TextView txtSubTitle;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RelativeLayout nullLay;
    private ImageView nullImage;
    private TextView nullText;

    private ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
    private SearchUserAdapter adapter;
    private final ArrayList<UserList.Result> usersList = new ArrayList<>();
    private LinearLayoutManager itemManager;
    private int limit = 10, offset = 0;
    private boolean isLoading, isScroll = false;
    private int lastVisibleItem, totalItemCount;
    private int visibleThreshold = 1;
    private Context mContext;
    private String from, title, type;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);
        mContext = this;
        if (getIntent().hasExtra(Constants.TAG_TITLE))
            title = getIntent().getStringExtra(Constants.TAG_TITLE);
        if (getIntent().hasExtra(Constants.TAG_TYPE))
            type = getIntent().getStringExtra(Constants.TAG_TYPE);
        findViews();
        initView();
    }

    private void findViews() {
        txtTitle = findViewById(R.id.txtTitle);
        txtSubTitle = findViewById(R.id.txtSubTitle);
        btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        nullLay = findViewById(R.id.nullLay);
        nullImage = findViewById(R.id.nullImage);
        nullText = findViewById(R.id.nullText);
    }

    public void initView() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);
        txtSubTitle.setVisibility(View.GONE);
        if (title != null) {
            txtTitle.setText(title);
        }
        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }
        btnBack.setOnClickListener(this);
        nullImage.setImageResource(R.drawable.no_user);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new SearchUserAdapter(UsersListActivity.this, usersList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        adapter.notifyDataSetChanged();
        float horizontalMargin = getResources().getDimension(R.dimen.activity_horizontal_margin);
        float verticalMargin = getResources().getDimension(R.dimen.activity_vertical_margin);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int position = parent.getChildViewHolder(view).getAbsoluteAdapterPosition();
                int itemCount = state.getItemCount();
                outRect.top = (int) verticalMargin;
                if (position == (itemCount - 1)) {
                    outRect.bottom = (int) verticalMargin;
                } else {
                    outRect.bottom = 0;
                }
                outRect.left = (int) horizontalMargin;
                outRect.right = (int) horizontalMargin;
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(() -> swipeRefresh(true));

        setScrollMethod();
        usersList.clear();
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
                Timber.d("onScrolled: %d", dy);
                if ((dy > 0 || lastVisibleItem == usersList.size() - 1) && !isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    usersList.add(null);
                    adapter.notifyItemInserted(usersList.size() - 1);
                    offset++;
                    isScroll = true;
                    getUsersList();
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
            getUsersList();
        }
    }

    private void getUsersList() {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> requestMap = new HashMap<>();
            requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
            requestMap.put(Constants.TAG_TYPE, type);
            requestMap.put(Constants.TAG_LIMIT, "" + limit);
            requestMap.put(Constants.TAG_OFFSET, "" + (offset * limit));
            Timber.tag(TAG).d("getUsersList: %s", App.getGsonPrettyInstance().toJson(requestMap));

            Call<UserList> call = apiInterface.getUsersList(requestMap);
            call.enqueue(new Callback<UserList>() {
                @Override
                public void onResponse(Call<UserList> call, Response<UserList> response) {
                    swipeRefresh(false);
                    if (response.isSuccessful()) {
                        if (isScroll) {
                            removeLoadMore();
                        }
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            if (offset == 0) {
                                usersList.clear();
                                adapter.notifyDataSetChanged();
                            }
                            if (response.body().getResult().size() > 0) {
                                List<UserList.Result> resultList = response.body().getResult();
//                            Timber.d("getUsersList: %s", resultList.size());
                                usersList.addAll(resultList);
                                recyclerView.setVisibility(View.VISIBLE);
                                adapter.notifyDataSetChanged();
                            }

                            if(response.body().getResult().size() > 0&& response.body().getResult().size()>=limit)
                                isLoading =false;
                            else isLoading = true;

                        } else {
                            isLoading = true;
                        }
                    } else {
                        isLoading = true;
                    }
                    setNullLay();
                }

                @Override
                public void onFailure(Call<UserList> call, Throwable t) {
                    t.printStackTrace();
                    swipeRefresh(false);
                    if (offset != 0)
                        offset--;
                    removeLoadMore();
                    call.cancel();
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

    private void setNullLay() {
        if (usersList.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            nullImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.no_user));
            nullText.setText(mContext.getString(R.string.no_user_txt));
            nullLay.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            nullLay.setVisibility(View.GONE);
        }
    }

    private void removeLoadMore() {
        if (usersList.size() > 0 && usersList.get(usersList.size() - 1) == null) {
            usersList.remove(usersList.size() - 1);
            adapter.notifyItemRemoved(usersList.size());
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }

    @Override
    public void onItemClicked(Object o, int position, String type) {
        if (type.equals(Constants.TAG_FOLLOW)) {
            UserList.Result finalItem = (UserList.Result) o;
            followUnFollowUser(finalItem.getUserId(), finalItem.isFollow());
        } else if (type.equals(Constants.TAG_USERS)) {
            UserList.Result finalItem = (UserList.Result) o;
            if (finalItem.getUserId().equals(GetSet.getUserId())) {
                Intent profile = new Intent(mContext, MyProfileActivity.class);
                profile.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                profile.putExtra(Constants.TAG_EDIT, true);
                profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(profile);
            } else {
                Intent profile = new Intent(mContext, OthersProfileActivity.class);
                profile.putExtra(Constants.TAG_USER_ID, finalItem.getUserId());
                profile.putExtra(Constants.TAG_INTENT_DATA, new Gson().toJson(finalItem));
                profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                mContext.startActivity(profile);
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

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnBack) {
            onBackPressed();
        }
    }
}
