package com.app.alhawanajd.Video;

import android.content.Context;import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.alhawanajd.BaseFragmentActivity;
import com.app.alhawanajd.R;
import com.app.alhawanajd.databinding.ActivityVideoPrivacyBinding; // 1. Import Activity binding
import com.app.alhawanajd.databinding.ItemSimpleListBinding;     // 1. Import Item binding
import com.google.gson.Gson;
import com.app.helper.LocaleManager;
import com.app.model.VideoPrivacyModel;
import com.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

// 2. ButterKnife imports are removed

public class DynamicPrivacyActivity extends BaseFragmentActivity {

    // 3. Declare the Activity's binding object
    private ActivityVideoPrivacyBinding binding;
    private CustomListAdapter listAdapter;
    private List<VideoPrivacyModel> privacyData = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 4. Inflate the layout using View Binding
        binding = ActivityVideoPrivacyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        privacyData = (List<VideoPrivacyModel>) getIntent().getSerializableExtra("data");
        Timber.i("privacyData %s", new Gson().toJson(privacyData));

        // 5. Access views through the binding object
        if (LocaleManager.isRTL()) {
            binding.includeHeader.btnBack.setRotation(180);
        } else {
            binding.includeHeader.btnBack.setRotation(0);
        }

        Timber.i("getTitle %s", getIntent().getStringExtra(Constants.TAG_TITLE));
        binding.includeHeader.txtTitle.setText(getIntent().getStringExtra(Constants.TAG_TITLE));
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listAdapter = new CustomListAdapter(this, privacyData);
        binding.recyclerView.setAdapter(listAdapter);

        // 6. Set click listener on the view from the binding object
        binding.includeHeader.btnBack.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        // Preserved method
    }

    public class CustomListAdapter extends RecyclerView.Adapter<CustomListAdapter.CustomViewHolder> {

        private final Context mContext;
        private final List<VideoPrivacyModel> listData;

        public CustomListAdapter(Context mContext, List<VideoPrivacyModel> listData) {
            this.mContext = mContext;
            this.listData = listData;
        }

        @NonNull
        @Override
        public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // 7. Inflate the item layout using View Binding
            ItemSimpleListBinding itemBinding = ItemSimpleListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new CustomViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
            VideoPrivacyModel item = listData.get(position);

            // 8. Access views via the holder's binding object
            holder.binding.txtReport.setText(item.getPrivacy());
            holder.binding.radioButton.setChecked(item.getState());

            if (item.getState()) {
                holder.binding.radioButton.setVisibility(View.VISIBLE);
            } else {
                holder.binding.radioButton.setVisibility(View.GONE);
            }

            holder.binding.itemReportLay.setOnClickListener(v -> {
                String commentPrivacy;
                if (listData.get(position).getPrivacy().equalsIgnoreCase(mContext.getString(R.string.settings_everyone))) {
                    commentPrivacy = "Everyone";
                } else if (listData.get(position).getPrivacy().equalsIgnoreCase(mContext.getString(R.string.settings_Fans))) {
                    commentPrivacy = "Fans";
                } else {
                    commentPrivacy = "Off";
                }
                holder.binding.radioButton.setVisibility(View.VISIBLE);
                holder.binding.radioButton.setChecked(true);
                Intent intent = getIntent();
                intent.putExtra("privacyName", commentPrivacy);
                setResult(RESULT_OK, intent);
                finish();
                Log.d("TAG", "onBindViewHolder_commentPrivacy: " + commentPrivacy);
            });
        }

        @Override
        public int getItemCount() {
            return listData.size();
        }

        // 9. ViewHolder now holds a binding object instead of individual views
        public class CustomViewHolder extends RecyclerView.ViewHolder {

            // The binding object holds all the views for the item layout
            private final ItemSimpleListBinding binding;

            public CustomViewHolder(ItemSimpleListBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
                // ButterKnife.bind() is removed.
            }
        }
    }
}
