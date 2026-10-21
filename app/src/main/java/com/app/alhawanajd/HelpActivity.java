package com.app.alhawanajd;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.alhawanajd.databinding.ActivityHelpBinding; // 1. Import Activity binding
import com.app.alhawanajd.databinding.ItemHelpBinding;     // 1. Import Item binding
import com.app.db.DBHelper;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.model.GetSet;
import com.app.model.HelpResponse;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// 2. All @BindView and @OnClick annotations are removed.
@SuppressLint("NonConstantResourceId")
public class HelpActivity extends BaseFragmentActivity {

    private static final String TAG = HelpActivity.class.getSimpleName();
    ApiInterface apiInterface;

    // 3. A single binding object now holds all views for the activity.
    private ActivityHelpBinding binding;

    HelpAdapter adapter;
    private List<HelpResponse.HelpList> helpList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 4. Inflate the layout using View Binding and set the content view.
        binding = ActivityHelpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // 5. The call to ButterKnife.bind(this) is removed.

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        initView();
    }

    private void initView() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Slidr.attach(this, config);

        // 6. Access views via the binding object.
        // The toolbar views are nested, so we access them like binding.toolbar.txtTitle
        binding.includeHeader.txtTitle.setText(R.string.help);
        adapter = new HelpAdapter(getApplicationContext(), helpList);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        // loadAd(); // Your commented-out code is preserved exactly.

        if (LocaleManager.isRTL()) {
            binding.includeHeader.btnBack.setRotation(180);
        } else {
            binding.includeHeader.btnBack.setRotation(0);
        }

        // 7. The @OnClick is replaced with a standard click listener.
        binding.includeHeader.btnBack.setOnClickListener(v -> onViewClicked());

        getHelpList();
    }

    // Your entire loadAd() method is preserved as commented-out.
//    private void loadAd() {
//        if (AdminData.isAdEnabled()) {
    ////            MobileAds.initialize(this, AdminData.googleAdsId);
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

    private void getHelpList() {
        if (NetworkReceiver.isConnected()) {
            String lang = LocaleManager.getLanguageCode(this);
            Call<HelpResponse> call = apiInterface.getHelps(lang);
            call.enqueue(new Callback<HelpResponse>() {
                @Override
                public void onResponse(Call<HelpResponse> call, Response<HelpResponse> response) {
                    // Added a null check for response.body() to prevent crashes.
                    if (response.body() != null) {
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            helpList.clear();
                            helpList.addAll(response.body().getHelpList());
                            adapter.notifyDataSetChanged();
                        } else if(response.body().getStatus().equals(Constants.TAG_ACCOUNT_BLOCKED)) {
                            App.makeToast(getString(R.string.Account_block));
                            GetSet.reset();
                            SharedPref.clearAll();
                            DBHelper.getInstance(HelpActivity.this).clearDB();
                            Intent logout = new Intent(HelpActivity.this, LoginActivity.class);
                            logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(logout);
                        }
                    }
                }

                @Override
                public void onFailure(Call<HelpResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        // Accessing the root layout via the binding for the snackbar.
        AppUtils.showSnack(getApplicationContext(), binding.parentLay, isConnected);
    }

    // 8. This method is no longer annotated but is preserved to be called by the listener.
    public void onViewClicked() {
        onBackPressed();
    }

    // 9. The Adapter is updated to specify the correct ViewHolder type.
    public class HelpAdapter extends RecyclerView.Adapter<HelpAdapter.MyViewHolder> {
        private final Context context;
        private List<HelpResponse.HelpList> helpList;

        public HelpAdapter(Context context, List<HelpResponse.HelpList> helpList) {
            this.context = context;
            this.helpList = helpList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // 10. Inflate the item layout using its binding class.
            ItemHelpBinding itemBinding = ItemHelpBinding.inflate(LayoutInflater.from(context), parent, false);
            return new MyViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final HelpResponse.HelpList help = helpList.get(position);
            // 11. Access item views through the holder's binding object.
            holder.binding.txtHelpTitle.setText(help.getHelpTitle());
        }

        @Override
        public int getItemCount() {
            return helpList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            // 12. The ViewHolder now holds a single binding object.
            private final ItemHelpBinding binding;

            public MyViewHolder(ItemHelpBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
                // ButterKnife.bind(this, view) is removed.

                // 13. The item's click listener is set directly here.
                binding.itemLay.setOnClickListener(v -> onViewClicked());
            }

            // This method is now called by the listener above.
            public void onViewClicked() {
                Intent intent = new Intent(context, PrivacyActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra(Constants.TAG_TITLE, helpList.get(getAdapterPosition()).getHelpTitle());
                intent.putExtra(Constants.TAG_DESCRIPTION, helpList.get(getAdapterPosition()).getHelpDescrip());
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.showSnack(getApplicationContext(), binding.parentLay, NetworkReceiver.isConnected());
        registerNetworkReceiver();
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
        super.onDestroy();
        // 14. Add this line to prevent memory leaks in activities.
        binding = null;
    }
}
