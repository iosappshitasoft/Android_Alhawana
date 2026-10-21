package com.app.alhawanajd;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
//import com.google.android.gms.ads.AdListener;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.LoadAdError;
import com.app.alhawanajd.R;
import com.app.db.DBHelper;
import com.app.alhawanajd.profile.OthersProfileActivity;
import com.app.helper.AppWebSocket;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.livedata.MessageLiveModel;
import com.app.model.AdminMessageResponse;
import com.app.model.ChatResponse;
import com.app.model.GetSet;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.Logging;
import com.makeramen.roundedimageview.RoundedImageView;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends BaseFragmentActivity implements AppWebSocket.WebSocketChannelEvents {

    private static String TAG = ChatFragment.class.getSimpleName();
    ApiInterface apiInterface;

    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtSubTitle)
    TextView txtSubTitle;
    @BindView(R.id.btnSettings)
    ImageView btnSettings;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    //    @BindView(R.id.adView)
//    AdView adView;
    @BindView(R.id.noDataImage)
    LottieAnimationView noDataImage;

    private Context mContext;
    LinearLayoutManager mLayoutManager;
    ChatAdapter adapter;
    private boolean isLoading = true;
    private int visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold = 20;
    int currentPage = 0, limit = 20;
    private DBHelper dbHelper;
    private MessageLiveModel liveModel;
    private List<ChatResponse> messageList = new ArrayList<>();
    Handler handler = new Handler();
    int delay = 15000; //milliseconds


    private String sharedType = null;
    private String sharedText = null;
    private String sharedUri = null;
    private ArrayList<Uri> sharedUris = null;
    private boolean isShareIntent = false;


    Runnable onlineRunnable = new Runnable() {
        @Override
        public void run() {
            if (NetworkReceiver.isConnected()) {
                try {
                    if (messageList.size() > 0) {
                        JSONObject jsonObject = new JSONObject();
                        JSONArray jsonArray = new JSONArray();
                        for (ChatResponse chatResponse : messageList) {
                            jsonObject.put(Constants.TAG_TYPE, Constants._ONLINE_LIST);
                            jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
                            if (chatResponse.getChatType() != null && !chatResponse.getChatType().equals(Constants.TAG_ADMIN)
                                    && !chatResponse.getBlockStatus())
                                jsonArray.put(chatResponse.getUserId());
                        }

                        jsonObject.put(Constants.TAG_USERS_LIST, jsonArray);
                        if (jsonArray.length() > 0) {
                            Logging.i(TAG, "getOnlineStatus: " + jsonObject);
                            AppWebSocket.getInstance(mContext).send(jsonObject.toString());
                        }

                    }
                } catch (JSONException e) {
                    Logging.e(TAG, "getOnlineStatus: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            handler.postDelayed(onlineRunnable, delay);
        }
    };
    private Observer<List<ChatResponse>> observer;

    public ChatFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_chat);
        ButterKnife.bind(this);
        mContext = this;
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        dbHelper = DBHelper.getInstance(this);
        liveModel = new ViewModelProvider(ChatFragment.this).get(MessageLiveModel.class);
        initView();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("content_type")) {
            isShareIntent = true;
            sharedType = intent.getStringExtra("content_type");
            sharedText = intent.getStringExtra("shared_text");
            sharedUri = intent.getStringExtra("shared_uri");
            sharedUris = intent.getParcelableArrayListExtra("shared_uris");

            Log.e(TAG,"sharedType" + sharedType);
            Log.e(TAG,"sharedText" + sharedText);
            Log.e(TAG,"sharedUri" + sharedUri);
            Log.e(TAG,"sharedUris" + sharedUris);


            // Optionally show banner: “Select a chat to share this message”
            // Toast.makeText(this, "Select a user to share the content", Toast.LENGTH_SHORT).show();
        }
    }

    private void initView() {
        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }
        btnBack.setVisibility(View.VISIBLE);

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);
        swipeRefreshLayout.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        txtTitle.setText(getString(R.string.message));


//        loadAd();
        adapter = new ChatAdapter(this);
        mLayoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        removeBlinkAnimationWhenNewItemAdded();

        observer = new Observer<List<ChatResponse>>() {
            @Override
            public void onChanged(List<ChatResponse> chatResponses) {
                /*if (chatResponses != null && chatResponses.size() == 0) {
                    noDataImage.setAnimationFromJson("no_data");
                    noDataImage.playAnimation();
                    noDataImage.setVisibility(View.VISIBLE);
                } else {
                    noDataImage.cancelAnimation();
                    noDataImage.setVisibility(View.GONE);
                }*/

                adapter.setData(chatResponses);
            }
        };

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAdminMessages();
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(final RecyclerView rv, final int dx, final int dy) {
                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                if (dy > 0) {//check for scroll down
                    if (isLoading) {
                        if (totalItemCount > previousTotal) {
                            isLoading = false;
                            previousTotal = totalItemCount;
                            currentPage++;
                        }
                    }

                    if (!isLoading && (totalItemCount - visibleItemCount)
                            <= (firstVisibleItem + visibleThreshold)) {
                        // End has been reached
                        getMessageList(currentPage);
                        isLoading = true;
                    }
                }
            }
        });
    }

//    private void loadAd() {
//        if (AdminData.isAdEnabled()) {
    ////            MobileAds.initialize(context, AdminData.googleAdsId);
//            AdRequest adRequest = new AdRequest.Builder().build();
//            adView.loadAd(adRequest);
//            adView.setAdListener(new AdListener() {
//                @Override
//                public void onAdLoaded() {
//                    // Code to be executed when an ad finishes loading.
//                }
//
//                @Override
//                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                    super.onAdFailedToLoad(loadAdError);
//                }
//
//                @Override
//                public void onAdOpened() {
//                    // Code to be executed when an ad opens an overlay that
//                    // covers the screen.
//                }
//
//                @Override
//                public void onAdClicked() {
//                    // Code to be executed when the user clicks on an ad.
//                }
//
//                @Override
//                public void onAdClosed() {
//                    // Code to be executed when the user is about to return
//                    // to the app after tapping on an ad.
//                }
//            });
//        }
//    }

    public void getMessageList(int offset) {
        if (offset > 0) {
            adapter.showLoading(true);
        }
        if (liveModel != null) {
            liveModel.getRecentChats(limit, offset * 20).observe(ChatFragment.this, observer);
        }
        if (offset == 0) {
            getOnlineStatus();
        }
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefresh(false);
            isLoading = true;
        }
        adapter.showLoading(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        getAdminMessages();
        AppWebSocket.setCallEvents(ChatFragment.this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProfileLoaded(String userId) {
        getMessageList(currentPage = 0);
    }

    public void getOnlineStatus() {
        handler.post(onlineRunnable);
    }

    private void getAdminMessages() {
        if (GetSet.getUserId() != null && GetSet.getCreatedAt() != null) {
            if (NetworkReceiver.isConnected()) {
                long lastTime = 0L;
                ChatResponse lastMessage;
                lastMessage = dbHelper.getLastAdminMessage();
                if (lastMessage != null && lastMessage.getCreatedAt() != null) {
                    lastTime = Long.parseLong(lastMessage.getCreatedAt());
                }
                Call<AdminMessageResponse> call = apiInterface.getAdminMessages(GetSet.getUserId(), Constants.TAG_ANDROID, GetSet.getCreatedAt(), lastTime);
                call.enqueue(new Callback<AdminMessageResponse>() {
                    @Override
                    public void onResponse(Call<AdminMessageResponse> call, Response<AdminMessageResponse> response) {
                        if (response.isSuccessful()) {
                            if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                                for (AdminMessageResponse.MessageData messageData : response.body().getMessageData()) {
                                    if (messageData.getMsgPlatform()!=null){
                                        if (messageData.getMsgPlatform().equals(Constants.TAG_ANDROID)) {
                                            dbHelper.addAdminMessage(messageData);
                                            int unseenCount = dbHelper.getUnseenMessagesCount(GetSet.getUserId());
                                            unseenCount = unseenCount + 1;
                                            dbHelper.addAdminRecentMessage(messageData, unseenCount);
                                        }
                                    }

                                }
                            }
                        }
                        getMessageList(currentPage = 0);
                    }

                    @Override
                    public void onFailure(Call<AdminMessageResponse> call, Throwable t) {
                        call.cancel();
                        getMessageList(currentPage = 0);
                    }
                });
            } else {
                getMessageList(currentPage = 0);
                //Toasty.error(getContext(), getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(onlineRunnable);
        AppWebSocket.setCallEvents(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        if (!NetworkReceiver.isConnected()) {
            AppUtils.showSnack(this, findViewById(R.id.parentLay), false);
        }
    }

    @OnClick({R.id.btnBack, R.id.btnSettings})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                App.preventMultipleClick(btnBack);
                onBackPressed();
                break;
            case R.id.btnSettings:
                App.preventMultipleClick(btnSettings);
                break;
        }
    }

    private void swipeRefresh(final boolean refresh) {
        swipeRefreshLayout.setRefreshing(refresh);
    }

    @Override
    public void onWebSocketConnected() {

    }

    @Override
    public void onWebSocketMessage(String message) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(message);
            String type = jsonObject.getString(Constants.TAG_TYPE);
            switch (type) {
                case Constants.TAG_lISTEN_TYPING:
                    liveModel.changeTypingStatus(jsonObject);
                    break;
                case Constants.TAG_ONLINE_LIST_STATUS:
                    JSONArray usersArray = jsonObject.getJSONArray(Constants.TAG_USERS_LIST);
                    liveModel.changeLiveStatus(usersArray);
                    break;
                case Constants.TAG_RECEIVE_CHAT:
                    liveModel.getRecents();
                    break;
                case Constants.TAG_OFFLINE_CHATS:
                    liveModel.getRecents();
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWebSocketClose() {

    }

    @Override
    public void onWebSocketError(String description) {

    }


    public class ChatAdapter extends RecyclerView.Adapter {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_ADMIN = 1;
        private final int VIEW_TYPE_FOOTER = 2;
        private final Context context;

        private boolean showLoading = false;
        private RecyclerView.ViewHolder viewHolder;

        public ChatAdapter(Context context) {
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
                viewHolder = new MyViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_ADMIN) {
                View itemView = LayoutInflater.from(context).inflate(R.layout.item_admin_message, parent, false);
                viewHolder = new AdminViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_FOOTER) {
                View v = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
                viewHolder = new FooterViewHolder(v);
            }

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                ChatResponse chat = messageList.get(position);
                ((MyViewHolder) holder).iconPhoto.setVisibility(View.VISIBLE);
                if (chat.getMessageType() != null && chat.getMessageType().equals(Constants.TAG_IMAGE)) {
                    ((MyViewHolder) holder).iconPhoto.setImageDrawable(context.getDrawable(R.drawable.image_placeholder));
                } else if (chat.getMessageType() != null && chat.getMessageType().equals(Constants.TAG_MISSED)) {
                    ((MyViewHolder) holder).iconPhoto.setImageDrawable(context.getDrawable(R.drawable.missed));
                } else if (chat.getMessageType() != null && chat.getMessageType().equals(Constants.TAG_LIVE)) {
                    ((MyViewHolder) holder).iconPhoto.setImageDrawable(context.getDrawable(R.drawable.my_video));
                } else {
                    ((MyViewHolder) holder).iconPhoto.setVisibility(View.GONE);
                }

                ((MyViewHolder) holder).iconOnline.setVisibility(chat.getOnlineStatus() ? View.VISIBLE : View.INVISIBLE);
//                ((MyViewHolder) holder).iconOnline.setImageDrawable(chat.getOnlineStatus() ? context.getDrawable(R.drawable.online) : null);

                if (chat.getTypingStatus() == null) {
                    ((MyViewHolder) holder).txtTyping.setVisibility(View.GONE);
                    ((MyViewHolder) holder).msgLay.setVisibility(View.VISIBLE);
                } else if (chat.getTypingStatus().equals(Constants.TAG_TYPING)) {
                    ((MyViewHolder) holder).txtTyping.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).msgLay.setVisibility(View.INVISIBLE);
                } else {
                    ((MyViewHolder) holder).txtTyping.setVisibility(View.GONE);
                    ((MyViewHolder) holder).msgLay.setVisibility(View.VISIBLE);
                }
                Glide.with(context)
                        .load(chat.getUserImage())
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                ((MyViewHolder) holder).profileImage.setImageDrawable(resource);
                                return false;
                            }
                        }).apply(new RequestOptions().error(R.drawable.loginscreen_bg).placeholder(R.drawable.loginscreen_bg).dontAnimate())
                        .into(((MyViewHolder) holder).profileImage);

                if (dbHelper.getUnseenMessagesCount(chat.getChatId()) > 0) {
                    ((MyViewHolder) holder).txtName.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                    ((MyViewHolder) holder).txtMessage.setTextColor(ContextCompat.getColor(context, R.color.grayclr));
                    ((MyViewHolder) holder).txtChatTime.setTextColor(ContextCompat.getColor(context, R.color.grayclr));
                } else {
                    ((MyViewHolder) holder).txtName.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                    ((MyViewHolder) holder).txtMessage.setTextColor(ContextCompat.getColor(context, R.color.grayclr));
                    ((MyViewHolder) holder).txtChatTime.setTextColor(ContextCompat.getColor(context, R.color.grayclr));
                }
                Log.i(TAG, "onBindViewHolder: " + chat.getUserName());
                ((MyViewHolder) holder).txtName.setText(chat.getUserName());
                if (chat.getMessageType() != null && chat.getMessageType().equals(Constants.TAG_MISSED)) {
                    ((MyViewHolder) holder).txtMessage.setText(getString(R.string.missed_call));
                } else if (chat.getMessageType() != null && chat.getMessageType().equals(Constants.TAG_LIVE)) {
                    ((MyViewHolder) holder).txtName.setText(chat.getUserName());
                    ((MyViewHolder) holder).txtMessage.setText(getString(R.string.shared_a_stream));
                } else {
                    ((MyViewHolder) holder).txtMessage.setText(chat.getMessageType() != null && chat.getMessageType().equals(Constants.TAG_IMAGE) ? getString(R.string.image) : chat.getMessage());
                }

                //Addon Voice Message and Emoji & Gif in chat
                if (chat.getMessageType() != null && chat.getMessageType().equals(Constants.TAG_IMAGE)) {
                    ((MyViewHolder) holder).txtMessage.setText(chat.getMessageType() != null && chat.getMessageType().equals(Constants.TAG_IMAGE) ? getString(R.string.image) : chat.getMessage());
                } else if (chat.getMessageType() != null && chat.getMessageType().equals(Constants.TAG_AUDIO)) {
                    ((MyViewHolder) holder).txtMessage.setText(getString(R.string.audio));
                } else if (chat.getMessageType() != null && chat.getMessageType().equals(Constants.TAG_GIF)) {
                    ((MyViewHolder) holder).txtMessage.setText(getString(R.string.gif));
                }

                if (TextUtils.isEmpty(("" + ((MyViewHolder) holder).txtMessage.getText()).trim())) {
                    ((MyViewHolder) holder).txtChatTime.setVisibility(View.GONE);
                } else {
                    ((MyViewHolder) holder).txtChatTime.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).txtChatTime.setText(AppUtils.getTimeAgo(context, chat.getChatTime()));
                }

            }
            else if (holder instanceof AdminViewHolder) {

                ChatResponse chat = messageList.get(position);
                ((AdminViewHolder) holder).iconPhoto.setVisibility(chat.getMessageType() != null && chat.getMessageType().equals(Constants.TAG_IMAGE) ? View.VISIBLE : View.GONE);
//                ((AdminViewHolder) holder).adminImage.setImageDrawable(context.getDrawable(R.mipmap.ic_launcher));
                Glide.with(context)
                        .load(context.getDrawable(R.drawable.loginscreen_bg))
                        .apply(new RequestOptions().dontAnimate().error(R.drawable.loginscreen_bg).placeholder(R.drawable.loginscreen_bg))
                        .into(((AdminViewHolder) holder).adminImage);
                int count = dbHelper.getUnseenMessagesCount(chat.getChatId());
                if (count > 0) {
                    ((AdminViewHolder) holder).txtName.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                    ((AdminViewHolder) holder).txtMessage.setTextColor(ContextCompat.getColor(context, R.color.grayclr));
                    ((AdminViewHolder) holder).txtChatTime.setTextColor(ContextCompat.getColor(context, R.color.grayclr));
                } else {
                    ((AdminViewHolder) holder).txtName.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                    ((AdminViewHolder) holder).txtMessage.setTextColor(ContextCompat.getColor(context, R.color.grayclr));
                    ((AdminViewHolder) holder).txtChatTime.setTextColor(ContextCompat.getColor(context, R.color.grayclr));
                }
                ((AdminViewHolder) holder).txtName.setText(chat.getUserName());
                ((AdminViewHolder) holder).txtMessage.setText(chat.getMessageType() != null && chat.getMessageType().equals(Constants.TAG_IMAGE) ? getString(R.string.image) : chat.getMessage());
                if (chat.getMessage() == null || chat.getMessage().equals("")) {
                    ((AdminViewHolder) holder).txtChatTime.setVisibility(View.INVISIBLE);
                } else {
                    ((AdminViewHolder) holder).txtChatTime.setVisibility(View.VISIBLE);
                    ((AdminViewHolder) holder).txtChatTime.setText(AppUtils.getTimeAgo(context, chat.getChatTime()));
                }

            }
            else if (holder instanceof FooterViewHolder) {
                FooterViewHolder footerHolder = (FooterViewHolder) holder;
                footerHolder.progressBar.setIndeterminate(true);
                footerHolder.progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (showLoading && isPositionFooter(position))
                return VIEW_TYPE_FOOTER;
            else if (messageList.get(position).getChatType() != null && messageList.get(position).getChatType().equals(Constants.TAG_ADMIN))
                return VIEW_TYPE_ADMIN;
            else
                return VIEW_TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            int itemCount = messageList.size();
            if (showLoading)
                itemCount++;
            return itemCount;
        }

        public boolean isPositionFooter(int position) {
            return position == getItemCount() - 1 && showLoading;
        }

        public void showLoading(boolean value) {
            showLoading = value;
        }

        public void setData(List<ChatResponse> newData) {
            /*if (messageList != null) {
                PostDiffCallback postDiffCallback = new PostDiffCallback(messageList, newData);
                DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(postDiffCallback);
                messageList.clear();
                messageList.addAll(newData);
                diffResult.dispatchUpdatesTo(adapter);
            } else {
                // first initialization
                messageList = newData;
            }*/

            messageList.clear();
            messageList.addAll(newData);
            adapter.notifyDataSetChanged();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.profileImage)
            RoundedImageView profileImage;
            @BindView(R.id.txtName)
            TextView txtName;
            @BindView(R.id.txtMessage)
            TextView txtMessage;
            @BindView(R.id.txtChatTime)
            TextView txtChatTime;
            @BindView(R.id.itemLay)
            LinearLayout itemLay;
            @BindView(R.id.iconPhoto)
            ImageView iconPhoto;
            @BindView(R.id.msgLay)
            LinearLayout msgLay;
            @BindView(R.id.txtTyping)
            TextView txtTyping;
            @BindView(R.id.iconOnline)
            ImageView iconOnline;

            public MyViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

            @OnClick({R.id.itemLay, R.id.profileImage})
            public void onViewClicked(View view) {
                if (getAdapterPosition() <= (messageList.size() - 1)) {
                    if (view.getId() == R.id.itemLay) {
                        App.preventMultipleClick(itemLay);
                        if (getAdapterPosition() != -1) {
                            Intent chatIntent = new Intent(mContext, ChatActivity.class);
                            chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            chatIntent.putExtra(Constants.TAG_PARTNER_ID, messageList.get(getAdapterPosition()).getUserId());
                            chatIntent.putExtra(Constants.TAG_PARTNER_IMAGE, messageList.get(getAdapterPosition()).getUserImage());
                            chatIntent.putExtra(Constants.TAG_PARTNER_NAME, messageList.get(getAdapterPosition()).getUserName());
                            startActivity(chatIntent);

                        }
                    } else if (view.getId() == R.id.profileImage) {
                        /*Intent profile = new Intent(context, OthersProfileActivity.class);
                        profile.putExtra(Constants.TAG_PARTNER_ID, messageList.get(getAdapterPosition()).getUserId());
                        profile.putExtra(Constants.TAG_PARTNER_NAME, messageList.get(getAdapterPosition()).getUserName());
                        profile.putExtra(Constants.TAG_PARTNER_IMAGE, messageList.get(getAdapterPosition()).getUserImage());
                        profile.putExtra(Constants.TAG_FROM, Constants.TAG_MESSAGE);
                        startActivity(profile);*/
                        App.preventMultipleClick(profileImage);
                        Intent profile = new Intent(mContext, OthersProfileActivity.class);
                        profile.putExtra(Constants.TAG_USER_ID, messageList.get(getAdapterPosition()).getUserId());
                        profile.putExtra(Constants.TAG_USER_IMAGE, messageList.get(getAdapterPosition()).getUserImage());
                        startActivity(profile);
                    }
                }
            }
        }

        public class AdminViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.adminImage)
            ImageView adminImage;
            @BindView(R.id.txtName)
            TextView txtName;
            @BindView(R.id.iconPhoto)
            ImageView iconPhoto;
            @BindView(R.id.txtMessage)
            TextView txtMessage;
            @BindView(R.id.msgLay)
            LinearLayout msgLay;
            @BindView(R.id.txtChatTime)
            TextView txtChatTime;
            @BindView(R.id.contentLay)
            RelativeLayout contentLay;
            @BindView(R.id.itemLay)
            LinearLayout itemLay;

            public AdminViewHolder(View parent) {
                super(parent);
                ButterKnife.bind(this, parent);
            }

            @OnClick({R.id.itemLay})
            public void onViewClicked(View view) {
                if (view.getId() == R.id.itemLay) {
                    App.preventMultipleClick(itemLay);
                    Intent chatIntent = new Intent(mContext, ChatActivity.class);
                    chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    chatIntent.putExtra(Constants.TAG_FROM, Constants.TAG_ADMIN);
                    chatIntent.putExtra(Constants.TAG_PARTNER_NAME, messageList.get(getAbsoluteAdapterPosition()).getUserName());
                    startActivity(chatIntent);
                }
            }
        }

        public class FooterViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.progressBar)
            ProgressBar progressBar;

            public FooterViewHolder(View parent) {
                super(parent);
                ButterKnife.bind(this, parent);
            }
        }
    }

    private void removeBlinkAnimationWhenNewItemAdded() {
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator != null) {
            animator.setChangeDuration(0);
            if (animator instanceof SimpleItemAnimator) {
                ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
            }
            animator.endAnimations();
        }
    }

}


