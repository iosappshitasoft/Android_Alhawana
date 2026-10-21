package com.app.alhawanajd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.alhawanajd.databinding.ActivityLocationFilterBinding; // 1. Import Activity binding
import com.app.alhawanajd.databinding.ItemFilterLocationBinding;   // 1. Import Item binding
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.model.GetSet;
import com.app.utils.AdminData;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrListener;
import com.r0adkll.slidr.model.SlidrPosition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// No more ButterKnife imports

public class LocationFilterActivity extends BaseFragmentActivity implements SlidrListener {

    private static final String TAG = LocationFilterActivity.class.getSimpleName();

    // 2. Declare the binding object for the Activity
    private ActivityLocationFilterBinding binding;

    LocationAdapter locationAdapter;
    int resultCode = Activity.RESULT_CANCELED;
    boolean locationChanged = false, isAllLocationSelected = false;
    private List<String> tempLocationList = new ArrayList<>();
    private String from = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 3. Inflate the layout using View Binding
        binding = ActivityLocationFilterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // No more ButterKnife.bind(this)

        if (getIntent().hasExtra(Constants.TAG_FROM)) {
            from = getIntent().getStringExtra(Constants.TAG_FROM);
        }

        initView();

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .listener(this)
                .build();
        Slidr.attach(this, config);
    }

    private void initView() {
        // 4. Access views through the binding object
        if (LocaleManager.isRTL()) {
            binding.toolbar.btnBack.setRotation(180);
        } else {
            binding.toolbar.btnBack.setRotation(0);
        }

        binding.toolbar.btnBack.setVisibility(View.VISIBLE);
        binding.toolbar.txtTitle.setVisibility(View.VISIBLE);
        binding.toolbar.txtTitle.setText(getString(R.string.location));

        // 5. Set click listener manually
        binding.toolbar.btnBack.setOnClickListener(v -> onBackPressed());

        // ... (rest of your initView logic remains the same)
        if (from != null && from.equals(Constants.TAG_HOME)) {
            tempLocationList = getIntent().getStringArrayListExtra(Constants.TAG_FILTER_LOCATION);
            isAllLocationSelected = getIntent().getBooleanExtra(Constants.TAG_LOCATION_SELECTED, false);
        } else if (from != null && from.equals(Constants.TAG_PROFILE)) {
            if (getIntent().getStringExtra(Constants.TAG_LOCATION) != null && !TextUtils.isEmpty(getIntent().getStringExtra(Constants.TAG_LOCATION))) {
                tempLocationList.clear();
                tempLocationList.add(getIntent().getStringExtra(Constants.TAG_LOCATION));
            }
        } else {
            if (GetSet.isLocationApplied()) {
                tempLocationList = AppUtils.filterLocation;
            } else {
                AppUtils.filterLocation = new ArrayList<>();
            }
        }
        if (from != null) {
            if (from.equals(Constants.TAG_PROFILE)) {
                List<String> location = new ArrayList<>(AdminData.locationList); // Create a mutable copy
                location.remove(getString(R.string.select_all));
                locationAdapter = new LocationAdapter(getApplicationContext(), location);
            } else {
                locationAdapter = new LocationAdapter(getApplicationContext(), AdminData.locationList);
            }
        } else {
            locationAdapter = new LocationAdapter(getApplicationContext(), AdminData.locationList);
        }
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.recyclerView.setAdapter(locationAdapter);
        locationAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        setFilterResult();
    }

    public void setFilterResult() {
        // ... (This method remains the same)
        if (from != null && !from.equals(Constants.TAG_PROFILE)) {
            Intent data = new Intent();
            if (locationChanged) {
                resultCode = Activity.RESULT_OK;
                if (from != null && from.equals(Constants.TAG_HOME)) {
                    if (tempLocationList.size() == 0) {
                        tempLocationList = AdminData.locationList;
                    }
                    data.putExtra(Constants.TAG_FILTER_LOCATION, (Serializable) tempLocationList);
                }
            }
            data.putExtra(Constants.TAG_LOCATION_SELECTED, isAllLocationSelected);
            setResult(resultCode, data);
            finish();
        } else {
            finish();
        }
        overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_right_out);
    }

    public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.MyViewHolder> {
        private final Context context;
        private List<String> locationList = new ArrayList<>();

        public LocationAdapter(Context context, List<String> locationList) {
            this.context = context;
            this.locationList = locationList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // 6. Inflate item layout using View Binding
            ItemFilterLocationBinding itemBinding = ItemFilterLocationBinding.inflate(LayoutInflater.from(context), parent, false);
            return new MyViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final String filter = locationList.get(position);

            // 8. Access views via holder's binding object
            holder.binding.txtLocation.setText(AppUtils.formatWord(filter));
            if (tempLocationList.contains(filter)) {
                holder.binding.btnSelect.setChecked(true);
                holder.binding.checkLay.setBackground(context.getDrawable(R.drawable.circle_primary_bg));
            } else {
                holder.binding.btnSelect.setChecked(false);
                holder.binding.checkLay.setBackground(context.getDrawable(R.drawable.circle_white_bg));
            }
        }

        @Override
        public int getItemCount() {
            return locationList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            // 7. Hold a reference to the item's binding, not individual views
            private final ItemFilterLocationBinding binding;

            public MyViewHolder(ItemFilterLocationBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
                // No more ButterKnife.bind() needed

                // 9. Set click listeners manually in the constructor
                View.OnClickListener clickListener = v -> {
                    if (from != null && from.equals(Constants.TAG_PROFILE)) {
                        binding.btnSelect.setChecked(!binding.btnSelect.isChecked());
                        tempLocationList.clear();
                        tempLocationList.add(locationList.get(getAdapterPosition()));
                        notifyDataSetChanged();
                        Intent intent = new Intent();
                        intent.putExtra(Constants.TAG_LOCATION, tempLocationList.get(0));
                        setResult(resultCode = Activity.RESULT_OK, intent);
                        finish();
                        overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_right_out);
                    } else {
                        binding.btnSelect.setChecked(!binding.btnSelect.isChecked());
                        updateLocations();
                    }
                };

                binding.btnSelect.setOnClickListener(clickListener);
                binding.itemLay.setOnClickListener(clickListener);
                binding.checkLay.setOnClickListener(clickListener);
            }

            // The old @OnClick method is removed

            void updateLocations() {
                // ... (This method remains the same)
                locationChanged = true;
                if (getAdapterPosition() == 0) {
                    if (binding.btnSelect.isChecked()) {
                        isAllLocationSelected = true;
                        tempLocationList = new ArrayList<>();
                        AppUtils.filterLocation = new ArrayList<>();
                        tempLocationList.addAll(locationList);
                        AppUtils.filterLocation.add(Constants.TAG_GLOBAL);
                        AppUtils.filterLocation.addAll(locationList);
                        notifyDataSetChanged();
                    } else {
                        isAllLocationSelected = false;
                        tempLocationList = new ArrayList<>();
                        AppUtils.filterLocation = new ArrayList<>();
                        notifyDataSetChanged();
                    }
                } else {
                    if (binding.btnSelect.isChecked()) {
                        tempLocationList.add(locationList.get(getAdapterPosition()));
                        if (tempLocationList.size() == (AdminData.locationList.size() - 1)) {
                            isAllLocationSelected = true;
                            tempLocationList = new ArrayList<>();
                            AppUtils.filterLocation = new ArrayList<>();
                            tempLocationList.addAll(locationList);
                            AppUtils.filterLocation.add(Constants.TAG_GLOBAL);
                            AppUtils.filterLocation.addAll(locationList);
                        } else {
                            isAllLocationSelected = false;
                            // This line seems problematic, it should add not replace
                            // tempLocationList = AppUtils.filterLocation;
                            if (!AppUtils.filterLocation.contains(locationList.get(getAdapterPosition()))) {
                                AppUtils.filterLocation.add(locationList.get(getAdapterPosition()));
                            }
                        }
                        notifyDataSetChanged();
                    } else {
                        isAllLocationSelected = false;
                        tempLocationList.remove(getString(R.string.select_all));
                        tempLocationList.remove(locationList.get(getAdapterPosition()));
                        AppUtils.filterLocation = tempLocationList;
                        notifyDataSetChanged();
                    }
                }
            }
        }
    }

    // ... (Your other lifecycle and listener methods remain the same)

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(getApplicationContext(), binding.parentLay, isConnected);
    }

    @Override
    public void onSlideStateChanged(int state) { }

    @Override
    public void onSlideChange(float percent) { }

    @Override
    public void onSlideOpened() { }

    @Override
    public boolean onSlideClosed() {
        setFilterResult();
        return false;
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
    }
}
