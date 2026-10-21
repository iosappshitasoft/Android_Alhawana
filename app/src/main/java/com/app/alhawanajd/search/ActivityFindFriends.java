package com.app.alhawanajd.search;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.alhawanajd.App;
import com.app.alhawanajd.BaseFragmentActivity;
import com.app.alhawanajd.R;
import com.app.alhawanajd.profile.MyProfileActivity;
import com.app.alhawanajd.profile.OthersProfileActivity;
import com.app.alhawanajd.search.adapter.SearchUserAdapter;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.model.FollowRequest;
import com.app.model.GetSet;
import com.app.model.ProfileResponse;
import com.app.model.UserList;
import com.app.utils.Constants;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class ActivityFindFriends extends BaseFragmentActivity implements AdapterItemClickListener {

    private static final String TAG = ActivityFindFriends.class.getSimpleName();
    private ApiInterface apiInterface;
    private RecyclerView recyclerView;
    private ConstraintLayout nullLay;
    private Button btn_continue;
    private ImageView no_comment;
    private TextView tv_title;
    private TextView tv_subtitle;
    private LinearLayout layout_syncing;
    private List<UserList.Result> usersList = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SearchUserAdapter itemAdapter;
    private LinearLayoutManager itemManager;
    private int limit = Constants.MAX_LIMIT, offset = 0;
    private boolean isLoading, isScroll = false;
    private int lastVisibleItem, totalItemCount;
    private int visibleThreshold = 1;
    private int selectedPosition = -1;
    private ActivityResultLauncher<Intent> profileResultLauncher;
    private EditText edtSearch;


    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private String currentQuery = "";
    private Call<UserList> currentCall; // to cancel previous retrofit call
    ImageView btnBack;

    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_find_friends_new);

        visibleThreshold = Constants.MAX_LIMIT;
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        findViews();
        initView();
        Log.d(TAG, "onCreate: ActivityFindFriends created.");
    }

    private void findViews() {
        recyclerView = findViewById(R.id.recyclerView);
        nullLay = findViewById(R.id.nullLay);
        btn_continue = findViewById(R.id.btn_continue);
        edtSearch = findViewById(R.id.edtSearch);
        btnBack = findViewById(R.id.btnBack);
        // Initial visibility
        nullLay.setVisibility(View.GONE);
        tv_title = nullLay.findViewById(R.id.tv_title);
        no_comment = nullLay.findViewById(R.id.nocontact);
        tv_subtitle = nullLay.findViewById(R.id.tv_subtitle);
        layout_syncing = nullLay.findViewById(R.id.layout_syncing);
        edtSearch.setVisibility(View.GONE); // Hide search bar initially
        Log.d(TAG, "findViews: All views initialized.");

        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    private void initView() {
        // set up RecyclerView + adapter (keep your existing adapter if already configured)
        itemManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(itemManager);

        itemAdapter = new SearchUserAdapter(this, usersList, this::onItemClicked);
        recyclerView.setAdapter(itemAdapter);

        Log.d(TAG, "initView: RecyclerView and adapter set up.");

        // initialize permission launcher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    Log.d(TAG, "requestPermissionLauncher: Permission granted status: " + isGranted);
                    if (isGranted) {
                        nullLay.setVisibility(View.GONE);
                        layout_syncing.setVisibility(View.GONE);
                        Log.d(TAG, "requestPermissionLauncher: Contact permission granted.");
                        // Permission granted, now trigger contact collection
                        collectContactsAndFindFriends();
                    } else {
                        nullLay.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        layout_syncing.setVisibility(View.GONE);
                        if (tv_title != null) tv_title.setText(getString(R.string.permission_required_contacts));
                        if (tv_subtitle != null) tv_subtitle.setText(getString(R.string.helpcontent));
                        if (tv_title != null) tv_title.setVisibility(View.VISIBLE);
                        if (tv_subtitle != null) tv_subtitle.setVisibility(View.VISIBLE);
                        if (btn_continue != null) btn_continue.setVisibility(View.VISIBLE);
                        if (no_comment != null) no_comment.setVisibility(View.GONE);
                        Log.d(TAG, "requestPermissionLauncher: Contact permission denied. Showing nullLay.");
                    }
                });

        // Initialize profileResultLauncher
        profileResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && selectedPosition != -1) {
                            Intent data = result.getData();
                            if (data != null && data.hasExtra(Constants.TAG_FOLLOW)) {
                                boolean isFollow = data.getBooleanExtra(Constants.TAG_FOLLOW, false);
                                usersList.get(selectedPosition).setFollow(isFollow);
                                itemAdapter.notifyItemChanged(selectedPosition);
                            }
                        }
                    }
                });

        btn_continue.setOnClickListener(v -> {
            Log.d(TAG, "btn_continue clicked.");
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "btn_continue: Permission already granted. Collecting contacts.");
                collectContactsAndFindFriends();
            } else {
                Log.d(TAG, "btn_continue: Permission not granted. Requesting permission.");
                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
            }
        });

        // Initial check on app entry
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "initView: Initial check - permission already granted. Hiding nullLay.");
            nullLay.setVisibility(View.GONE);
            layout_syncing.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE); // Hide RecyclerView initially, will be shown if contacts are found
            collectContactsAndFindFriends(); // Trigger contact collection immediately
        } else {
            Log.d(TAG, "initView: Initial check - permission not granted. Showing nullLay.");
            nullLay.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            layout_syncing.setVisibility(View.GONE);
            if (tv_title != null) tv_title.setText(getString(R.string.permission_required_contacts));
            if (tv_subtitle != null) tv_subtitle.setText(getString(R.string.helpcontent));
            if (tv_title != null) tv_title.setVisibility(View.VISIBLE);
            if (tv_subtitle != null) tv_subtitle.setVisibility(View.VISIBLE);
            if (btn_continue != null) btn_continue.setVisibility(View.VISIBLE);
            if (no_comment != null) no_comment.setVisibility(View.GONE);
        }
    }

    private void setNullLay() {
        // This method is no longer needed with the simplified logic
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

    private void askPermissionIfNeeded() {
        // This method is no longer needed as the logic is in btn_continue click listener
    }

    private void collectContactsAndFindFriends() {
        Log.d(TAG, "collectContactsAndFindFriends: Starting contact collection and find friends process.");
        runOnUiThread(() -> {
            btn_continue.setEnabled(false);
            nullLay.setVisibility(View.VISIBLE);
            layout_syncing.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            if (tv_title != null) tv_title.setText(getString(R.string.syncfind_contact));
            if (tv_subtitle != null) tv_subtitle.setText(getString(R.string.msg_please_wait));
            if (tv_title != null) tv_title.setVisibility(View.GONE);
            if (tv_subtitle != null) tv_subtitle.setVisibility(View.GONE);
            if (btn_continue != null) btn_continue.setVisibility(View.GONE);
            Log.d(TAG, "collectContactsAndFindFriends: UI updated for syncing. Main elements hidden.");
        });

        new Thread(() -> {
            Log.d(TAG, "collectContactsAndFindFriends: Running contact collection on background thread.");
            JsonArray contacts = collectContactsJsonArray();
            Log.d(TAG, "collectContactsAndFindFriends: Collected contacts JSON: " + contacts.toString());

            if (contacts.size() == 0) {
                Log.d(TAG, "collectContactsAndFindFriends: No local contacts found.");
                runOnUiThread(() -> {
                    btn_continue.setEnabled(true);
                    layout_syncing.setVisibility(View.GONE);
                    if (tv_title != null) tv_title.setText(getString(R.string.no_phone_contacts_found));
                    if (tv_subtitle != null) tv_subtitle.setText(""); // Clear subtitle for no contacts
                    nullLay.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    if (tv_title != null) tv_title.setVisibility(View.VISIBLE);
                    if (tv_subtitle != null) tv_subtitle.setVisibility(View.VISIBLE);
                    if (btn_continue != null) btn_continue.setVisibility(View.VISIBLE);
                    if (no_comment != null) no_comment.setVisibility(View.GONE);
                    Toast.makeText(ActivityFindFriends.this, getString(R.string.no_phone_contacts_found), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "collectContactsAndFindFriends: No local contacts found. UI updated.");
                });
                return;
            }

            // build payload
            JsonObject payload = new JsonObject();
            payload.add("contacts", contacts);
            String userId = GetSet.getUserId() == null ? "" : GetSet.getUserId();
            payload.addProperty("user_id", userId);
            Log.d(TAG, "collectContactsAndFindFriends: API Request Payload: " + payload.toString());

            RequestBody body = RequestBody.create(payload.toString(), MediaType.get("application/json; charset=utf-8"));

            // cancel any previous call
            if (currentCall != null && !currentCall.isCanceled()) currentCall.cancel();

            // call API
            currentCall = apiInterface.findFriends(body);
            Log.d(TAG, "collectContactsAndFindFriends: Making API call to findFriends.");
            runOnUiThread(() -> {
                if (tv_title != null) tv_title.setText(getString(R.string.syncfind_contact));
                if (tv_subtitle != null) tv_subtitle.setText("Syncing Contacts...");
                nullLay.setVisibility(View.VISIBLE);
                layout_syncing.setVisibility(View.VISIBLE);
                btn_continue.setEnabled(false);
                if (tv_title != null) tv_title.setVisibility(View.GONE);
                if (tv_subtitle != null) tv_subtitle.setVisibility(View.GONE);
                if (btn_continue != null) btn_continue.setVisibility(View.GONE);
            });

            currentCall.enqueue(new Callback<UserList>() {
                @Override
                public void onResponse(@NonNull Call<UserList> call, @NonNull Response<UserList> response) {
                    Log.d(TAG, "onResponse: API call successful. Response code: " + response.code() + ", body: " + (response.body() != null ? new Gson().toJson(response.body()) : "null"));
                    runOnUiThread(() -> {
                        btn_continue.setEnabled(true);
                        layout_syncing.setVisibility(View.GONE);
                        if (tv_title != null) tv_title.setText("");
                        if (tv_subtitle != null) tv_subtitle.setText("");
                        if (tv_title != null) tv_title.setVisibility(View.VISIBLE);
                        if (tv_subtitle != null) tv_subtitle.setVisibility(View.VISIBLE);
                        if (btn_continue != null) btn_continue.setVisibility(View.VISIBLE);
                    });

                    if (response.isSuccessful() && response.body() != null && Constants.TAG_TRUE.equals(response.body().getStatus())) {
                        List<UserList.Result> result = response.body().getResult();
                        runOnUiThread(() -> {
                            if (result != null && !result.isEmpty()) {
                                Log.d(TAG, "onResponse: API returned matched friends. Updating RecyclerView.");
                                usersList.clear();
                                usersList.addAll(result);
                                itemAdapter.notifyDataSetChanged();
                                recyclerView.setVisibility(View.VISIBLE);
                                nullLay.setVisibility(View.GONE);
                                edtSearch.setVisibility(View.VISIBLE);
                                if (tv_title != null) tv_title.setVisibility(View.GONE);
                                if (tv_subtitle != null) tv_subtitle.setVisibility(View.GONE);
                                if (btn_continue != null) btn_continue.setVisibility(View.GONE);
                            } else {
                                Log.d(TAG, "onResponse: API returned no matched friends. Showing 'no friends found' message.");
                                usersList.clear();
                                itemAdapter.notifyDataSetChanged();
                                nullLay.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                                edtSearch.setVisibility(View.GONE);
                                if (tv_title != null) tv_title.setText(getString(R.string.no_matches_found));
                                if (tv_subtitle != null) tv_subtitle.setText(getString(R.string.no_friends_found));
                                if (tv_title != null) tv_title.setVisibility(View.VISIBLE);
                                if (tv_subtitle != null) tv_subtitle.setVisibility(View.VISIBLE);
                                if (btn_continue != null) btn_continue.setVisibility(View.VISIBLE);
                                if (no_comment != null) no_comment.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        Log.e(TAG, "onResponse: API returned false status or null body. Showing server error.");
                        runOnUiThread(() -> {
                            usersList.clear();
                            itemAdapter.notifyDataSetChanged();
                            nullLay.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                            edtSearch.setVisibility(View.GONE);
                            if (tv_title != null) tv_title.setText(getString(R.string.server_error));
                            if (tv_subtitle != null) tv_subtitle.setText(getString(R.string.wecound_not_find_suggestions));
                            if (tv_title != null) tv_title.setVisibility(View.VISIBLE);
                            if (tv_subtitle != null) tv_subtitle.setVisibility(View.VISIBLE);
                            if (btn_continue != null) btn_continue.setVisibility(View.GONE);
                            if (no_comment != null) no_comment.setVisibility(View.VISIBLE);
                            Toast.makeText(ActivityFindFriends.this, getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                            btn_continue.setEnabled(true);
                        });
                    }
                }

                @Override
                public void onFailure(@NonNull Call<UserList> call, @NonNull Throwable t) {
                    Log.e(TAG, "onFailure: API call failed: " + t.getMessage(), t);
                    runOnUiThread(() -> {
                        usersList.clear();
                        itemAdapter.notifyDataSetChanged();
                        nullLay.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        layout_syncing.setVisibility(View.GONE);
                        edtSearch.setVisibility(View.GONE);
                        if (tv_title != null) tv_title.setText(getString(R.string.network_error));
                        if (tv_subtitle != null) tv_subtitle.setText("");
                        if (tv_title != null) tv_title.setVisibility(View.VISIBLE);
                        if (tv_subtitle != null) tv_subtitle.setVisibility(View.VISIBLE);
                        if (btn_continue != null) btn_continue.setVisibility(View.VISIBLE);
                        if (no_comment != null) no_comment.setVisibility(View.GONE);
                        Toast.makeText(ActivityFindFriends.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                        btn_continue.setEnabled(true);
                    });
                }
            });
        }).start();
    }

    private JsonArray collectContactsJsonArray() {
        Set<String> dedup = new HashSet<>();
        JsonArray arr = new JsonArray();
        int rawContactCount = 0;

        ContentResolver cr = getContentResolver();
        Cursor cur = null;
        try {
            cur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                    null, null, null);

            if (cur != null) {
                rawContactCount = cur.getCount();
                Log.d(TAG, "collectContactsJsonArray: Raw contacts found: " + rawContactCount);
                while (cur.moveToNext()) {
                    String rawNumber = cur.getString(0);
                    // optionally get name: String name = cur.getString(1);
                    if (rawNumber == null) continue;
                    String phone = normalizePhone(rawNumber);
                    if (TextUtils.isEmpty(phone)) continue;
                    if (dedup.contains(phone)) continue;
                    dedup.add(phone);

                    JsonObject obj = new JsonObject();
                    obj.addProperty("value", phone);
                    obj.addProperty("type", "phone");
                    arr.add(obj);
                }
                cur.close();
            }
            Log.d(TAG, "collectContactsJsonArray: Normalized and deduped contacts count: " + arr.size());
        } catch (SecurityException se) {
            Log.e(TAG, "collectContactsJsonArray: Permission denied: " + se.getMessage(), se);
        } catch (Exception e) {
            Log.e(TAG, "collectContactsJsonArray: Error collecting contacts: " + e.getMessage(), e);
        } finally {
            if (cur != null && !cur.isClosed()) cur.close();
        }
        return arr;
    }

    private String normalizePhone(String raw) {
        if (raw == null) return null;
        // Simple normalization: keep + and digits only
        String p = raw.replaceAll("[^+\\d]", "");
        // Optionally: further normalize with libphonenumber for international format
        return p;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == Constants.PROFILE_OWN_SEARCH_CODE) {
       //     swipeRefresh(true);
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
