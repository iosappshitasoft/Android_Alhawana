package com.app.alhawanajd;

import static com.app.alhawanajd.App.dpToPx;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.textview.MaterialTextView;
import com.app.alhawanajd.R;
import com.app.db.DBHelper;
import com.app.external.TimeAgo;
import com.app.alhawanajd.profile.OthersProfileActivity;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.livedata.MessageLiveModel;
import com.app.model.ChatResponse;
import com.app.model.GetSet;
import com.app.model.NotificationResponse;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.makeramen.roundedimageview.RoundedImageView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;


public class NotificationFragment extends Fragment {

    private static final String TAG = NotificationFragment.class.getSimpleName();

    private ImageView btnBack;
    private TextView txtTitle;
    private TextView txtSubTitle;
//    private ShimmerFrameLayout shimmerLayout;
    private ImageView btnSettings;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout nullLay;
    private ImageView nullImage;
    private TextView nullText;
    String deleted_status;
    private Context mContext;
    ApiInterface apiInterface;
    LinearLayoutManager mLayoutManager;
    NotificationAdapter notificationAdapter;
    boolean isLoading = true;
    int visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold = 20, currentPage = 0;
    ArrayList<NotificationResponse.Result> listNotification = new ArrayList<>();
    ArrayList<NotificationResponse.Result> displayedNotifications = new ArrayList<>();
    private List<ChatResponse> messageList = new ArrayList<>();
    private ChatInboxAdapter chatAdapter;
    private MessageLiveModel liveModel;

    private LinearLayout tabMessages, tabAlerts;
    private TextView txtTabMessages, txtTabAlerts;
    private View indicatorMessages, indicatorAlerts;
    private View chipScroll;
    private TextView chipAll, chipSystem, chipBroadcast, chipAdvertisements;
    private SwipeRefreshLayout messagesSwipeRefresh;
    private RecyclerView messagesRecyclerView;
    private boolean isMessagesTab = true;
    private String notifFilter = "all";

    private String from = "";

    public NotificationFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_notification, container, false);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            from = getArguments().getString(Constants.TAG_FROM, "");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;

            Toolbar toolbar = v.findViewById(R.id.toolbar);
            if (toolbar != null) {
                if ("mainactivity".equalsIgnoreCase(from)) {
                    // Apply padding ONLY for MainActivity
                    toolbar.setPadding(0, topInset, 0, 0);
                } else {
                    // No padding for other sources
                    toolbar.setPadding(0, 0, 0, 0);
                }
            }

            return insets;
        });
        findViews(view);
        initView();
    }

    private void findViews(View rootView) {
        btnBack = rootView.findViewById(R.id.btnBack);
//        shimmerLayout = rootView.findViewById(R.id.shimmerLayout);
//        txtTitle = rootView.findViewById(R.id.txtTitle);
        txtSubTitle = rootView.findViewById(R.id.txtSubTitle);
        btnSettings = rootView.findViewById(R.id.btnSettings);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        nullLay = rootView.findViewById(R.id.nullLay);
        nullImage = rootView.findViewById(R.id.nullImage);
        nullText = rootView.findViewById(R.id.nullText);
        tabMessages = rootView.findViewById(R.id.tabMessages);
        tabAlerts = rootView.findViewById(R.id.tabAlerts);
        txtTabMessages = rootView.findViewById(R.id.txtTabMessages);
        txtTabAlerts = rootView.findViewById(R.id.txtTabAlerts);
        indicatorMessages = rootView.findViewById(R.id.indicatorMessages);
        indicatorAlerts = rootView.findViewById(R.id.indicatorAlerts);
        chipScroll = rootView.findViewById(R.id.chipScroll);
        chipAll = rootView.findViewById(R.id.chipAll);
        chipSystem = rootView.findViewById(R.id.chipSystem);
        chipBroadcast = rootView.findViewById(R.id.chipBroadcast);
        chipAdvertisements = rootView.findViewById(R.id.chipAdvertisements);
        messagesSwipeRefresh = rootView.findViewById(R.id.messagesSwipeRefresh);
        messagesRecyclerView = rootView.findViewById(R.id.messagesRecyclerView);
    }

    private void initView() {
        nullLay.setVisibility(View.GONE);
//        shimmerLayout.setVisibility(View.GONE);

//        txtTitle.setText(mContext.getString(R.string.notification));


        if("mainactivity".equals(from)){
            btnBack.setVisibility(View.INVISIBLE);
        }
        else{
            btnBack.setVisibility(View.VISIBLE);
        }

        btnBack.setOnClickListener(v->{
            if("otherprofile".equals(from)){

            }
        });
        recyclerView.setHasFixedSize(true);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(mContext, R.color.colorPrimary));
        visibleThreshold = Constants.MAX_LIMIT;

        mLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(mLayoutManager);
        notificationAdapter = new NotificationAdapter(mContext, displayedNotifications);
        recyclerView.setAdapter(notificationAdapter);
        setupChatList();
        setupInboxTabs();

        float horizontalMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics());
        float verticalMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics());

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

        swipeRefresh(true);
        swipeRefreshLayout.setOnRefreshListener(() -> swipeRefresh(true));
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> selectInboxTab(true));
        }
        selectInboxTab(true);
    }

    private void swipeRefresh(final boolean refresh) {
        swipeRefreshLayout.setRefreshing(refresh);
        if (refresh) {
            previousTotal = 0;
            currentPage = 0;
            totalItemCount = 0;
            visibleItemCount = 0;
            firstVisibleItem = 0;
            getNotificationData(currentPage);
        }
    }

    private void getNotificationData(int currentPage) {

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

            map.put(Constants.TAG_LIMIT, "10");
            map.put(Constants.TAG_OFFSET, String.valueOf(currentPage));
            if (LocaleManager.isRTL()) {
                map.put(Constants.TAG_LANGUAGE_CODE, "ar");
            }else{
                map.put(Constants.TAG_LANGUAGE_CODE, "en");
            }
            Timber.d("getNotifications(: params=> %s", App.getGsonPrettyInstance().toJson(map));

            retrofit2.Call<NotificationResponse> call = apiInterface.getNotifications(map);
            call.enqueue(new Callback<NotificationResponse>() {
                @Override
                public void onResponse(@NotNull retrofit2.Call<NotificationResponse> call, @NotNull Response<NotificationResponse> response) {
                    swipeRefreshLayout.setRefreshing(false);
                    if (response.isSuccessful()) {
                        NotificationResponse getResponse = response.body();
//                        Timber.d("getNotifications response: %s", App.getGsonPrettyInstance().toJson(getResponse));
                        if (Objects.requireNonNull(getResponse).getStatus().equals(Constants.TAG_TRUE)) {


                            if (swipeRefreshLayout.isRefreshing() || currentPage == 0) {
                                listNotification.clear();
                            }

                            listNotification.addAll(getResponse.getResult());


                            applyNotificationFilter();
                            if (swipeRefreshLayout.isRefreshing()) {
                                swipeRefresh(false);
                                isLoading = true;
                            }
                        } else if (response.body().getStatus().equals(Constants.TAG_ACCOUNT_BLOCKED)) {
                            App.makeToast(getString(R.string.Account_block));
                            GetSet.reset();
                            SharedPref.clearAll();
                            DBHelper.getInstance(mContext).clearDB();
                            Intent logout = new Intent(mContext, LoginActivity.class);
                            logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(logout);
                        }
                        setNullLay();
                    }
                }

                @Override
                public void onFailure
                        (@NotNull Call<NotificationResponse> call, @NotNull Throwable t) {
                    swipeRefreshLayout.setRefreshing(false);
                    call.cancel();
                    t.printStackTrace();
                }
            });
        } else {
            setNullLay();
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void setNullLay() {
        if (isMessagesTab) {
            nullLay.setVisibility(View.GONE);
            return;
        }
        if (displayedNotifications.size() == 0) {
            nullImage.setVisibility(View.VISIBLE);
            nullImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.no_notification));
            nullLay.setVisibility(View.VISIBLE);
            nullText.setText(R.string.notification_empty);
            recyclerView.setVisibility(View.GONE);
        } else {
            nullImage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            nullLay.setVisibility(View.GONE);
        }
    }

    private void setupInboxTabs() {
        tabMessages.setOnClickListener(v -> selectInboxTab(true));
        tabAlerts.setOnClickListener(v -> selectInboxTab(false));
        chipAll.setOnClickListener(v -> selectChip("all"));
        chipSystem.setOnClickListener(v -> selectChip("system"));
        chipBroadcast.setOnClickListener(v -> selectChip("broadcast"));
        chipAdvertisements.setOnClickListener(v -> selectChip("advertisement"));
    }

    private void selectInboxTab(boolean messages) {
        isMessagesTab = messages;
        int selectedColor = ContextCompat.getColor(mContext, R.color.colorWhite);
        int unselectedColor = Color.parseColor("#8F8F8F");
        txtTabMessages.setTextColor(messages ? selectedColor : unselectedColor);
        txtTabAlerts.setTextColor(messages ? unselectedColor : selectedColor);
        txtTabMessages.setTypeface(ResourcesCompat.getFont(mContext, R.font.outfit_semibold));
        txtTabAlerts.setTypeface(ResourcesCompat.getFont(mContext, messages ? R.font.outfit_medium : R.font.outfit_semibold));
        indicatorMessages.setVisibility(messages ? View.VISIBLE : View.INVISIBLE);
        indicatorAlerts.setVisibility(messages ? View.INVISIBLE : View.VISIBLE);
        messagesSwipeRefresh.setVisibility(messages ? View.VISIBLE : View.GONE);
        swipeRefreshLayout.setVisibility(messages ? View.GONE : View.VISIBLE);
        chipScroll.setVisibility(messages ? View.GONE : View.VISIBLE);
        if (messages) {
            nullLay.setVisibility(View.GONE);
            loadChatList();
        } else {
            applyNotificationFilter();
            if (listNotification.isEmpty()) {
                swipeRefresh(true);
            }
        }
    }

    private void selectChip(String filter) {
        notifFilter = filter;
        chipAll.setBackgroundResource(filter.equals("all") ? R.drawable.bg_inbox_chip_selected : R.drawable.bg_inbox_chip_unselected);
        chipSystem.setBackgroundResource(filter.equals("system") ? R.drawable.bg_inbox_chip_selected : R.drawable.bg_inbox_chip_unselected);
        chipBroadcast.setBackgroundResource(filter.equals("broadcast") ? R.drawable.bg_inbox_chip_selected : R.drawable.bg_inbox_chip_unselected);
        chipAdvertisements.setBackgroundResource(filter.equals("advertisement") ? R.drawable.bg_inbox_chip_selected : R.drawable.bg_inbox_chip_unselected);
        applyNotificationFilter();
    }

    private void applyNotificationFilter() {
        displayedNotifications.clear();
        for (NotificationResponse.Result item : listNotification) {
            if (matchesFilter(item)) {
                displayedNotifications.add(item);
            }
        }
        if (notificationAdapter != null) {
            notificationAdapter.notifyDataSetChanged();
        }
        setNullLay();
    }

    private boolean matchesFilter(NotificationResponse.Result item) {
        if (item == null) return false;
        String type = item.getType() == null ? "" : item.getType().toLowerCase();
        switch (notifFilter) {
            case "system":
                return type.contains("system") || type.contains("admin") || type.contains("alert")
                        || type.contains("notice") || type.contains("official");
            case "broadcast":
                return type.contains("broadcast") || type.contains("live") || type.contains("stream");
            case "advertisement":
                return type.contains("ad") || type.contains("promo") || type.contains("sponsor");
            default:
                return true;
        }
    }

    private void setupChatList() {
        liveModel = new ViewModelProvider(this).get(MessageLiveModel.class);
        chatAdapter = new ChatInboxAdapter();
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        messagesRecyclerView.setHasFixedSize(true);
        messagesRecyclerView.setAdapter(chatAdapter);
        messagesSwipeRefresh.setColorSchemeColors(ContextCompat.getColor(mContext, R.color.colorPrimary));
        messagesSwipeRefresh.setOnRefreshListener(this::loadChatList);
        liveModel.getRecentLiveData().observe(getViewLifecycleOwner(), chats -> {
            messageList.clear();
            if (chats != null) {
                messageList.addAll(chats);
            }
            chatAdapter.notifyDataSetChanged();
            messagesSwipeRefresh.setRefreshing(false);
        });
    }

    private void loadChatList() {
        if (liveModel != null) {
            liveModel.getRecents();
        }
        if (messagesSwipeRefresh != null) {
            messagesSwipeRefresh.setRefreshing(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isMessagesTab) {
            loadChatList();
        }
    }

    public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

        Context mContext;
        ArrayList<NotificationResponse.Result> listNotification;
        TimeAgo timeAgo;

        public NotificationAdapter(Context mContext, ArrayList<NotificationResponse.Result> listNotification) {
            this.mContext = mContext;
            this.listNotification = listNotification;
            timeAgo = new TimeAgo(mContext);
        }

        @NonNull
        @Override
        public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new NotificationViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_notification, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull NotificationViewHolder holder, int pos) {
            int position = holder.getAbsoluteAdapterPosition();
            Glide.with(NotificationFragment.this)
                    .load(listNotification.get(position).getUserImage())
                    .placeholder(R.drawable.default_profile_image)
                    .into(holder.profileImage);
            holder.thumbNail.setPadding(dpToPx(mContext, 0), dpToPx(mContext, 0), dpToPx(mContext, 0), dpToPx(mContext, 0));
            String type = listNotification.get(position).getType();
            String videoThumb = listNotification.get(position).getVideoThumbnail();
            boolean showThumb = type != null && !type.equals("follow") && !TextUtils.isEmpty(videoThumb);
            holder.thumbNail.setVisibility(showThumb ? View.VISIBLE : View.GONE);
            if (showThumb) {
                Glide.with(NotificationFragment.this)
                        .load(videoThumb)
                        .placeholder(R.drawable.novideo_placeholder)
                        .into(holder.thumbNail);
            }

            String[] name = listNotification.get(position).getUserName().split("_");
            deleted_status = listNotification.get(position).getDeleted_status();
            Log.d(TAG, "onBindViewHolderdelete: "+deleted_status);

            holder.userName.setText(name[0]);
            if (listNotification.get(position).getType().equals("like") ||
                    listNotification.get(position).getType().equals("gift") ||
                    listNotification.get(position).getType().equals("comment")) {
//                String sourceString = <"<b>"> + listNotification.get(position).getVideo_description() + "</b> ";


//                holder.comment.setText(listNotification.get(position).getMessage() +  " " + listNotification.get(position).getVideo_description());
                holder.comment.setText(getStyledText(listNotification.get(position).getMessage(), listNotification.get(position).getVideo_description(), " "));
            } else holder.comment.setText(listNotification.get(position).getMessage());

            holder.time.setText(AppUtils.getTimeAgo(mContext, listNotification.get(position).getTime()));

        }

        @NonNull
        public SpannableStringBuilder getStyledText(@NonNull String normalText,
                                                    @NonNull String boldText,
                                                    @NonNull String delimiter) {
            SpannableStringBuilder sb = new SpannableStringBuilder();
            if (TextUtils.isEmpty(normalText)) return sb;

            if (!TextUtils.isEmpty(boldText)) {
                sb.append(normalText).append(delimiter).append(boldText);
                final int start = normalText.length() + delimiter.length();
                final int end = start + boldText.length();
                final StyleSpan span = new StyleSpan(Typeface.BOLD);
                sb.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                sb.append(normalText);
            }
            return sb;
        }

        @Override
        public int getItemCount() {
            return listNotification.size();
        }

        public class NotificationViewHolder extends RecyclerView.ViewHolder {

            RoundedImageView profileImage;
            RoundedImageView thumbNail;
            LinearLayout notificationDetailLayout;
            MaterialTextView userName, comment, time;


            public NotificationViewHolder(@NonNull View itemView) {
                super(itemView);
                profileImage = itemView.findViewById(R.id.profileImage);
                thumbNail = itemView.findViewById(R.id.thumbNail);
                notificationDetailLayout = itemView.findViewById(R.id.notificationDetailLayout);
                userName = itemView.findViewById(R.id.userName);
                comment = itemView.findViewById(R.id.txtcomment);
                time = itemView.findViewById(R.id.time);

                //For Arabic addon
                if (LocaleManager.isRTL()) {
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    llp.setMargins(50, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
                    userName.setLayoutParams(llp);
                }

                thumbNail.setOnClickListener(v -> {
                    App.preventMultipleClick(thumbNail);
                    if (listNotification.get(getAbsoluteAdapterPosition()).getType().equals("comment") ||
                            listNotification.get(getAbsoluteAdapterPosition()).getType().equals("mention")) {
                        Intent intent = new Intent(mContext, SingleVideoActivity.class);
                        intent.putExtra(Constants.TAG_VIDEO_ID, listNotification.get(getAbsoluteAdapterPosition()).getVideoId());
                        intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, listNotification.get(getAbsoluteAdapterPosition()).getVideoThumbnail());
                        intent.putExtra(Constants.TAG_COMMENT_OPEN, Constants.TAG_COMMENT_OPEN);
                        intent.putExtra(Constants.TAG_FROM, Constants.TAG_NOTIFICATION_PAGE);
                        startActivityForResult(intent, Constants.NOTIFICATION_ACTIVITY);
                    } else if (listNotification.get(getAbsoluteAdapterPosition()).getType().equals("follow")) {
                        Intent profile = new Intent(mContext, OthersProfileActivity.class);
                        profile.putExtra(Constants.TAG_USER_ID, listNotification.get(getAbsoluteAdapterPosition()).getUserId());
                        profile.putExtra(Constants.TAG_USER_IMAGE, listNotification.get(getAbsoluteAdapterPosition()).getUserImage());
                        startActivity(profile);
                    } else {
                        Intent intent = new Intent(mContext, SingleVideoActivity.class);
                        intent.putExtra(Constants.TAG_VIDEO_ID, listNotification.get(getAbsoluteAdapterPosition()).getVideoId());
                        intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, listNotification.get(getAbsoluteAdapterPosition()).getVideoThumbnail());
                        intent.putExtra(Constants.TAG_FROM, Constants.TAG_NOTIFICATION_PAGE);
                        startActivityForResult(intent, Constants.NOTIFICATION_ACTIVITY);
                    }

                });

                profileImage.setOnClickListener(v -> {
                    if (listNotification.get(getAbsoluteAdapterPosition()).getDeleted_status().equals("0")){
                        App.preventMultipleClick(profileImage);
                        otherProfileActivity();
                    }else{
                        opendeleteDialog();
                    }
                });

                userName.setOnClickListener(v -> {
                    if (listNotification.get(getAbsoluteAdapterPosition()).getDeleted_status().equals("0")){
                        App.preventMultipleClick(userName);
                        otherProfileActivity();
                    }else{
                        opendeleteDialog();
                    }

                });

                comment.setOnClickListener(v -> {
                    if (listNotification.get(getAbsoluteAdapterPosition()).getDeleted_status().equals("0")){
                        App.preventMultipleClick(comment);
                        otherProfileActivity();
                    }else{
                        if (listNotification.get(getAbsoluteAdapterPosition()).getType().equals("comment") ||
                                listNotification.get(getAbsoluteAdapterPosition()).getType().equals("mention")) {
                            Intent intent = new Intent(mContext, SingleVideoActivity.class);
                            intent.putExtra(Constants.TAG_VIDEO_ID, listNotification.get(getAbsoluteAdapterPosition()).getVideoId());
                            intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, listNotification.get(getAbsoluteAdapterPosition()).getVideoThumbnail());
                            intent.putExtra(Constants.TAG_COMMENT_OPEN, Constants.TAG_COMMENT_OPEN);
                            intent.putExtra(Constants.TAG_FROM, Constants.TAG_NOTIFICATION_PAGE);
                            startActivityForResult(intent, Constants.NOTIFICATION_ACTIVITY);
                        } else if (listNotification.get(getAbsoluteAdapterPosition()).getType().equals("follow")) {
                            Intent profile = new Intent(mContext, OthersProfileActivity.class);
                            profile.putExtra(Constants.TAG_USER_ID, listNotification.get(getAbsoluteAdapterPosition()).getUserId());
                            profile.putExtra(Constants.TAG_USER_IMAGE, listNotification.get(getAbsoluteAdapterPosition()).getUserImage());
                            startActivity(profile);
                        } else {
                            Intent intent = new Intent(mContext, SingleVideoActivity.class);
                            intent.putExtra(Constants.TAG_VIDEO_ID, listNotification.get(getAbsoluteAdapterPosition()).getVideoId());
                            intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, listNotification.get(getAbsoluteAdapterPosition()).getVideoThumbnail());
                            intent.putExtra(Constants.TAG_FROM, Constants.TAG_NOTIFICATION_PAGE);
                            startActivityForResult(intent, Constants.NOTIFICATION_ACTIVITY);
                        }
                    }
                    /*Intent intent = new Intent(context, SingleVideoActivity.class);
                    intent.putExtra(Constants.TAG_VIDEO_ID, listNotification.get(getAdapterPosition()).getVideoId());
                    intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, listNotification.get(getAdapterPosition()).getVideoThumbnail());
                    if (listNotification.get(getAdapterPosition()).getType().equals("comment") ||
                            listNotification.get(getAdapterPosition()).getType().equals("mention")) {
                        intent.putExtra(Constants.TAG_COMMENT_OPEN, Constants.TAG_COMMENT_OPEN);
                        intent.putExtra(Constants.TAG_FROM, Constants.TAG_NOTIFICATION_PAGE);
                    } else {
                        intent.putExtra(Constants.TAG_FROM, Constants.TAG_NOTIFICATION_PAGE);
                    }
                    startActivityForResult(intent, Constants.NOTIFICATION_ACTIVITY);*/

                });


            }

            private void opendeleteDialog() {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage(getString(R.string.notification_del_acc));
                builder.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
       /* builder.setNegativeButton(getStringR.string.no_txt), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });*/
                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.setCancelable(false);
                Typeface typeface = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    typeface = getResources().getFont(R.font.font_regular);
                } else {
                    typeface = ResourcesCompat.getFont(getContext(), R.font.font_regular);
                }
                TextView textView = dialog.findViewById(android.R.id.message);
                textView.setTypeface(typeface);

                Button btn1 = dialog.findViewById(android.R.id.button1);
                btn1.setTypeface(typeface);

                Button btn2 = dialog.findViewById(android.R.id.button2);
                btn2.setTypeface(typeface);

            }

            public void otherProfileActivity() {
                Intent profile = new Intent(mContext, OthersProfileActivity.class);
                profile.putExtra(Constants.TAG_USER_ID, listNotification.get(getAbsoluteAdapterPosition()).getUserId());
                profile.putExtra(Constants.TAG_USER_IMAGE, listNotification.get(getAbsoluteAdapterPosition()).getUserImage());
                startActivity(profile);
            }
        }
    }

    private class ChatInboxAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int VIEW_TYPE_ITEM = 0;
        private static final int VIEW_TYPE_ADMIN = 1;

        @Override
        public int getItemViewType(int position) {
            ChatResponse chat = messageList.get(position);
            if (chat.getChatType() != null && chat.getChatType().equals(Constants.TAG_ADMIN)) {
                return VIEW_TYPE_ADMIN;
            }
            return VIEW_TYPE_ITEM;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ADMIN) {
                return new AdminHolder(LayoutInflater.from(mContext).inflate(R.layout.item_admin_message, parent, false));
            }
            return new ChatHolder(LayoutInflater.from(mContext).inflate(R.layout.item_chat, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ChatResponse chat = messageList.get(position);
            if (holder instanceof ChatHolder) {
                ChatHolder h = (ChatHolder) holder;
                h.txtName.setText(chat.getUserName());
                if (chat.getMessageType() != null && chat.getMessageType().equals(Constants.TAG_IMAGE)) {
                    h.txtMessage.setText(getString(R.string.image));
                } else {
                    h.txtMessage.setText(chat.getMessage());
                }
                if (TextUtils.isEmpty(h.txtMessage.getText())) {
                    h.txtChatTime.setVisibility(View.GONE);
                } else {
                    h.txtChatTime.setVisibility(View.VISIBLE);
                    h.txtChatTime.setText(AppUtils.getTimeAgo(mContext, chat.getChatTime()));
                }
                Glide.with(mContext)
                        .load(chat.getUserImage())
                        .placeholder(R.drawable.loginscreen_bg)
                        .error(R.drawable.loginscreen_bg)
                        .into(h.profileImage);
            } else if (holder instanceof AdminHolder) {
                AdminHolder h = (AdminHolder) holder;
                h.txtName.setText(chat.getUserName());
                if (chat.getMessageType() != null && chat.getMessageType().equals(Constants.TAG_IMAGE)) {
                    h.txtMessage.setText(getString(R.string.image));
                } else {
                    h.txtMessage.setText(chat.getMessage());
                }
                if (TextUtils.isEmpty(chat.getMessage())) {
                    h.txtChatTime.setVisibility(View.INVISIBLE);
                } else {
                    h.txtChatTime.setVisibility(View.VISIBLE);
                    h.txtChatTime.setText(AppUtils.getTimeAgo(mContext, chat.getChatTime()));
                }
                Glide.with(mContext)
                        .load(R.mipmap.app_icon_new)
                        .into(h.adminImage);
            }
        }

        @Override
        public int getItemCount() {
            return messageList.size();
        }

        class ChatHolder extends RecyclerView.ViewHolder {
            RoundedImageView profileImage;
            TextView txtName, txtMessage, txtChatTime;

            ChatHolder(@NonNull View itemView) {
                super(itemView);
                profileImage = itemView.findViewById(R.id.profileImage);
                txtName = itemView.findViewById(R.id.txtName);
                txtMessage = itemView.findViewById(R.id.txtMessage);
                txtChatTime = itemView.findViewById(R.id.txtChatTime);
                itemView.setOnClickListener(v -> openChat(getAbsoluteAdapterPosition(), false));
                profileImage.setOnClickListener(v -> {
                    int pos = getAbsoluteAdapterPosition();
                    if (pos < 0 || pos >= messageList.size()) return;
                    Intent profile = new Intent(mContext, OthersProfileActivity.class);
                    profile.putExtra(Constants.TAG_USER_ID, messageList.get(pos).getUserId());
                    profile.putExtra(Constants.TAG_USER_IMAGE, messageList.get(pos).getUserImage());
                    startActivity(profile);
                });
            }
        }

        class AdminHolder extends RecyclerView.ViewHolder {
            ImageView adminImage;
            TextView txtName, txtMessage, txtChatTime;

            AdminHolder(@NonNull View itemView) {
                super(itemView);
                adminImage = itemView.findViewById(R.id.adminImage);
                txtName = itemView.findViewById(R.id.txtName);
                txtMessage = itemView.findViewById(R.id.txtMessage);
                txtChatTime = itemView.findViewById(R.id.txtChatTime);
                itemView.setOnClickListener(v -> openChat(getAbsoluteAdapterPosition(), true));
            }
        }

        private void openChat(int position, boolean admin) {
            if (position < 0 || position >= messageList.size()) return;
            ChatResponse chat = messageList.get(position);
            Intent chatIntent = new Intent(mContext, ChatActivity.class);
            chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            if (admin) {
                chatIntent.putExtra(Constants.TAG_FROM, Constants.TAG_ADMIN);
                chatIntent.putExtra(Constants.TAG_PARTNER_NAME, chat.getUserName());
            } else {
                chatIntent.putExtra(Constants.TAG_PARTNER_ID, chat.getUserId());
                chatIntent.putExtra(Constants.TAG_PARTNER_IMAGE, chat.getUserImage());
                chatIntent.putExtra(Constants.TAG_PARTNER_NAME, chat.getUserName());
            }
            startActivity(chatIntent);
        }
    }

}