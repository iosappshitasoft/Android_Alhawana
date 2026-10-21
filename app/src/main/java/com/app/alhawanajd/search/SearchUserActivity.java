package com.app.alhawanajd.search;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.alhawanajd.App;
import com.app.alhawanajd.BaseFragmentActivity;
import com.app.alhawanajd.LoginActivity;
import com.app.alhawanajd.R;
import com.app.alhawanajd.profile.MyProfileActivity;
import com.app.alhawanajd.profile.OthersProfileActivity;
import com.app.alhawanajd.search.adapter.SearchUserAdapter;
import com.app.alhawanajd.search.adapter.SearchUserAdapter2;
import com.app.db.DBHelper;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.model.FollowRequest;
import com.app.model.GetSet;
import com.app.model.ProfileRequest;
import com.app.model.ProfileResponse;
import com.app.model.UserList;
import com.app.newlib.ComposeChangeScreen;
import com.app.qrpage.QrCode;
import com.app.qrpage.QrScanActivity;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class SearchUserActivity extends BaseFragmentActivity implements AdapterItemClickListener {

    private static final String TAG = SearchUserActivity.class.getSimpleName();
    private ApiInterface apiInterface;
    private RecyclerView recyclerView;
    private RelativeLayout nullLay;
    private ImageView nullImage,scanBt;
    private TextView nullText;
    private List<UserList.Result> usersList = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SearchUserAdapter2 itemAdapter;
    private LinearLayoutManager itemManager;
    private int limit = Constants.MAX_LIMIT, offset = 0;
    private boolean isLoading, isScroll = false;
    private int lastVisibleItem, totalItemCount;
    private int visibleThreshold = 1;
    private int selectedPosition = -1;
    private ActivityResultLauncher<Intent> profileResultLauncher;
    private LinearLayout linearSearch,linearFindContacts,linearInviteFriends,btnQrcode;
    private EditText edtSearch;


    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private String currentQuery = "";
    private Call<UserList> currentCall; // to cancel previous retrofit call
    ImageView btnBack;
    ProfileResponse profileResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_find_friends);

        visibleThreshold = Constants.MAX_LIMIT;
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        findViews();
        initResultLauncher();
        initView();
    }

    private void findViews() {
        recyclerView = findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        nullLay = findViewById(R.id.nullLay);
        nullImage = findViewById(R.id.nullImage);
        nullText = findViewById(R.id.nullText);
        linearSearch = findViewById(R.id.linear_search);
        linearFindContacts = findViewById(R.id.linearFindContacts);
        linearInviteFriends = findViewById(R.id.linearInviteFriends);
        edtSearch = findViewById(R.id.edtSearch);
        btnBack = findViewById(R.id.btnBack);
        btnQrcode = findViewById(R.id.btnQrcode);
        scanBt = findViewById(R.id.scanBt);

        scanBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchUserActivity.this, QrScanActivity.class);
                startActivity(intent);
            }
        });

        btnQrcode.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), QrCode.class);
            intent.putExtra("screenType", "QrCode");   // 👈 passing your value
            intent.putExtra("profile_line", profileResponse.getShare_profile_link());   // 👈 passing your value
            startActivity(intent);
        });
        // Initial visibility
        //linearSearch.setVisibility(View.VISIBLE);
       // mSwipeRefreshLayout.setVisibility(View.GONE);
        nullLay.setVisibility(View.GONE);

        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }

        linearFindContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchUserActivity.this, ActivityFindFriends.class);
                startActivity(intent);
            }
        });

        linearInviteFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.preventMultipleClick(linearInviteFriends);
                shareMyProfile(profileResponse);
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // Add text change listener
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

        /*    @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    // Show initial placeholder
                    linearSearch.setVisibility(View.VISIBLE);
                    mSwipeRefreshLayout.setVisibility(View.GONE);
                    nullLay.setVisibility(View.GONE);

                    usersList.clear();
                    itemAdapter.notifyDataSetChanged();
                } else {
                    // Hide placeholder, show RecyclerView
                    linearSearch.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                    swipeRefresh(true); // triggers search API
                }
            }*/
        @Override
        public void afterTextChanged(Editable s) {
            String query = s.toString().trim();
            currentQuery = query;

            // cancel pending runnable
            if (searchRunnable != null) {
                searchHandler.removeCallbacks(searchRunnable);
            }

            // if cleared -> show initial placeholder and cancel any running API call
            if (query.isEmpty()) {
              //  linearSearch.setVisibility(View.VISIBLE);
                mSwipeRefreshLayout.setVisibility(View.VISIBLE); // Show refresh layout to display the full list
                nullLay.setVisibility(View.GONE);

                // cancel ongoing network request
                if (currentCall != null && !currentCall.isCanceled()) {
                    currentCall.cancel();
                }

                // Call swipeRefresh to reload the full list when search query is empty
                swipeRefresh(true);
                return;
            }

            // show list UI and start (debounced) search
            linearSearch.setVisibility(View.GONE);
            mSwipeRefreshLayout.setVisibility(View.VISIBLE);

            // debounce: wait 350ms after last keystroke
            searchRunnable = () -> swipeRefresh(true);
            searchHandler.postDelayed(searchRunnable, 350);
        }

        });
    }

    private void initResultLauncher() {
        profileResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            ProfileResponse profile = null;
                            if (selectedPosition != -1 && result.getData() != null) {
                                String jsonData = result.getData().getStringExtra(Constants.TAG_INTENT_DATA);
                                profile = new Gson().fromJson(jsonData, ProfileResponse.class);
                                usersList.get(selectedPosition).setFollow(Boolean.parseBoolean(profile.getFollow()));
                                itemAdapter.notifyItemChanged(selectedPosition);
                            }
                        }
                        selectedPosition = -1;
                    }
                });
    }

    private void initView() {

        recyclerView.setHasFixedSize(true);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary));

        itemManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(itemManager);

        itemAdapter = new SearchUserAdapter2(this, usersList, this,apiInterface);
        recyclerView.setAdapter(itemAdapter);

        float horizontalMargin = getResources().getDimension(R.dimen.activity_horizontal_margin);
        float verticalMargin = getResources().getDimension(R.dimen.activity_vertical_margin);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int position = parent.getChildViewHolder(view).getAbsoluteAdapterPosition();
                int itemCount = state.getItemCount();
                outRect.top = (int) verticalMargin;
                outRect.bottom = (position == itemCount - 1) ? (int) verticalMargin : 0;
                outRect.left = (int) horizontalMargin;
                outRect.right = (int) horizontalMargin;
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(() -> swipeRefresh(true));
        setScrollMethod();

        usersList.clear();
        swipeRefresh(true);
        getProfile();
    }

    private void shareMyProfile(ProfileResponse profile) {
        String userId = GetSet.getUserId(); // or get from arguments
        if (userId != null) {
            String profileLink = profile.getShare_profile_link(); // App Link
            String shareMsg = "Hi, check my  profile ---> " + profileLink;
            Log.d(TAG, "shareMyProfilelinkkk: "+profileLink);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, shareMsg);
            startActivity(Intent.createChooser(intent, getString(R.string.share_link)));
        }
    }

    private void getProfile() {
        if (GetSet.getUserId() != null && apiInterface != null) {
            ProfileRequest request = new ProfileRequest();
            request.setUserId(GetSet.getUserId());
            request.setProfileId(GetSet.getUserId());

            Call<ProfileResponse> call = apiInterface.getProfile(request);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(@NotNull Call<ProfileResponse> call, @NotNull Response<ProfileResponse> response) {
                    if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        profileResponse = response.body();
                    }
                }

                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable t) {
                   call.cancel();
                }
            });
        }
    }

    private void setScrollMethod() {
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (dy >= 0 && !isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    usersList.add(null);
                    itemAdapter.notifyItemInserted(usersList.size() - 1);
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
            selectedPosition = -1;
            offset = 0;
            totalItemCount = 0;
            lastVisibleItem = 0;
            getUsersList();
        }
    }

    private void getUsersList() {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId() == null ? "" : GetSet.getUserId());
            map.put(Constants.TAG_TOKEN, GetSet.getAuthToken() == null ? "" : GetSet.getAuthToken());
            map.put(Constants.TAG_LIMIT, String.valueOf(limit));
            map.put(Constants.TAG_OFFSET, String.valueOf(limit * offset));
            map.put(Constants.TAG_TYPE, Constants.TAG_SEARCH_USER);

            if (!TextUtils.isEmpty(currentQuery)) {
                map.put(Constants.TAG_TEXT, TextUtils.isEmpty(currentQuery) ? "" : currentQuery);
            }

            Timber.tag(TAG).d("getUsersList: %s", App.getGsonPrettyInstance().toJson(map));

            Call<UserList> call = apiInterface.getSearchUser(map);
            call.enqueue(new Callback<UserList>() {
                @Override
                public void onResponse(@NotNull Call<UserList> call, @NotNull Response<UserList> response) {
                    swipeRefresh(false);
                    if (response.isSuccessful() && response.body() != null) {
                        if (Constants.TAG_TRUE.equals(response.body().getStatus())) {
                            if (isScroll) removeLoadMore();
                            if (offset == 0) usersList.clear();

                            List<UserList.Result> resultList = response.body().getResult();
                            usersList.addAll(resultList);
                            recyclerView.setVisibility(View.VISIBLE);

                            if (resultList.size() > 0 && resultList.size() >= Constants.MAX_LIMIT) {
                                isLoading = false;
                            } else {
                                isLoading = true;
                            }
                            itemAdapter.notifyDataSetChanged();
                            setNullLay();
                        } else {
                            if (isScroll) removeLoadMore();
                            isLoading = true;
                            setNullLay();
                        }
                    }
                }

                @Override
                public void onFailure(@NotNull Call<UserList> call, @NotNull Throwable t) {
                    t.printStackTrace();
                    swipeRefresh(false);
                    if (offset != 0) offset--;
                    removeLoadMore();
                   // call.cancel();
                }
            });
        } else {
            swipeRefresh(false);
            nullImage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            nullLay.setVisibility(View.VISIBLE);
            nullText.setText(getString(R.string.no_internet_connection));
        }
    }

    private void setNullLay() {
        if (usersList.isEmpty()) {
            nullImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.no_follow));
            nullText.setText(getString(R.string.no_user_txt));
            nullLay.setVisibility(View.VISIBLE);
        } else {
            nullLay.setVisibility(View.GONE);
        }
    }

    private void removeLoadMore() {
        if (!usersList.isEmpty() && usersList.get(usersList.size() - 1) == null) {
            usersList.remove(usersList.size() - 1);
            itemAdapter.notifyItemRemoved(usersList.size());
        }
    }

    @Override
    public void onItemClicked(Object o, int position, String type) {
        if (Constants.TAG_FOLLOW.equals(type)) {
            UserList.Result finalItem = (UserList.Result) o;
            followUnFollowUser(finalItem.getUserId(), finalItem.isFollow());
        } else if (Constants.TAG_USERS.equals(type)) {
            UserList.Result finalItem = (UserList.Result) o;
            if (finalItem.getUserId().equals(GetSet.getUserId())) {
                Intent profile = new Intent(this, MyProfileActivity.class);
                profile.putExtra(Constants.TAG_EDIT, true);
                profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityForResult(profile, Constants.PROFILE_OWN_SEARCH_CODE);
            } else {
                selectedPosition = position;
                Intent profile = new Intent(this, OthersProfileActivity.class);
                profile.putExtra(Constants.TAG_USER_ID, finalItem.getUserId());
                profile.putExtra(Constants.TAG_INTENT_DATA, new Gson().toJson(finalItem));
                profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                profileResultLauncher.launch(profile);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == Constants.PROFILE_OWN_SEARCH_CODE) {
            swipeRefresh(true);
        }
    }

    private void followUnFollowUser(String otherUserId, boolean follow) {
        if (NetworkReceiver.isConnected()) {
            FollowRequest followRequest = new FollowRequest();
            followRequest.setUserId(otherUserId);
            followRequest.setFollowerId(GetSet.getUserId());
            followRequest.setType(follow ? Constants.TAG_FOLLOW_USER : Constants.TAG_UNFOLLOW_USER);

            Call<Map<String, String>> call = apiInterface.followUser(followRequest);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {}

                @Override
                public void onFailure(@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
        if (currentCall != null && !currentCall.isCanceled()) currentCall.cancel();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }
}
