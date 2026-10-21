package com.app.alhawanajd;import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.alhawanajd.databinding.ActivityLanguageBinding; // 1. Import Activity binding
import com.app.alhawanajd.databinding.ItemLanguageBinding;   // 1. Import Item binding
import com.app.helper.NetworkReceiver;
import com.app.model.CommonResponse;
import com.app.model.GetSet;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.app.utils.api.CommonApi;
import com.google.gson.Gson;
import com.app.helper.LocaleManager;
import com.app.model.LanguageData;
import com.app.utils.Constants;
import com.app.utils.SharedPref;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// No more ButterKnife imports

public class LanguageActivity extends BaseFragmentActivity {

    private static final String TAG = LanguageActivity.class.getSimpleName();

    // 2. Declare the binding object for the Activity
    private ActivityLanguageBinding binding;

    private String[] resourceArray;
    List<LanguageData> languageList = new ArrayList<>();
    LanguageAdapter languageAdapter;
    private ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 3. Inflate the layout using View Binding
        binding = ActivityLanguageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        hideLoading();
        resourceArray = getResources().getStringArray(R.array.language_array);
        Log.d(TAG, "onCreate_resourceArray: "+new Gson().toJson(resourceArray));
        List<String> tempList = Arrays.asList(resourceArray);
        for (String list : tempList) {
            String[] lang = list.split("-");
            String langTitle = lang[0];
            String langCode = lang[1];
            LanguageData data = new LanguageData();
            data.language = langTitle;
            data.languageCode = langCode;
            languageList.add(data);
        }

        // 4. Access views through the binding object
        binding.includeHeader.txtTitle.setText(R.string.language);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(LanguageActivity.this);
        binding.recyclerView.setLayoutManager(layoutManager);
        languageAdapter = new LanguageAdapter(this, languageList);
        binding.recyclerView.setAdapter(languageAdapter);

        if (LocaleManager.isRTL()) {
            binding.includeHeader.btnBack.setRotation(180);
        } else {
            binding.includeHeader.btnBack.setRotation(0);
        }
        Constants.SHOULD_RECREATE_ACTIVITIES_ON_RESUME = false;

        binding.includeHeader.btnBack.setOnClickListener(v -> {
            onBackPressed();
        });

        LocaleManager.getLanguageName(this);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        // You can show a snackbar here if needed, e.g.,
        // AppUtils.showSnack(this, binding.getRoot(), isConnected);
    }

    private void showLoading() {
        //  binding.progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        //  binding.progressBar.setVisibility(View.GONE);
    }

    public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.MyViewHolder> {
        Context context;
        List<LanguageData> languageList;

        public LanguageAdapter(Context context, List<LanguageData> languageList) {
            this.languageList = languageList;
            this.context = context;
        }

        @NotNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // 5. Inflate the item layout using View Binding
            ItemLanguageBinding itemBinding = ItemLanguageBinding.inflate(LayoutInflater.from(context), parent, false);
            return new MyViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final LanguageData data = languageList.get(position);

            // 7. Access item views through the holder's binding object
            holder.binding.helpTitle.setText(data.language);
            holder.binding.tickRadio.setChecked(data.languageCode.equals(LocaleManager.getLanguageCode(context)));

           // View.OnClickListener listener = v -> setNewLocale(context, data.languageCode, data.language);
            String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            View.OnClickListener listener = v -> {
                changeLangugae(context, deviceId, data.language, data.languageCode, data.language);
                setNewLocale(context, data.languageCode, data.language);
            };

            holder.binding.mainLay.setOnClickListener(listener);
            holder.binding.tickRadio.setOnClickListener(listener);
        }

        private void setNewLocale(Context context, String languageCode, String languageName) {
            // This method seems to handle the core logic of changing the language
            CommonApi.getAppDefaultData(LanguageActivity.this);
            languageAdapter.notifyDataSetChanged();
            SharedPref.putString(Constants.TAG_LANGUAGE_CODE, languageCode);
            LocaleManager.setNewLocale(context, languageCode, languageName);
            Log.i(TAG, "setNewLocale: " + languageCode + "" + languageName);
            Constants.SHOULD_RECREATE_ACTIVITIES_ON_RESUME = true;
            ((LanguageActivity) context).recreate();
        }

        @Override
        public int getItemCount() {
            return languageList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            // 6. Hold a reference to the item's binding, not individual views
            private final ItemLanguageBinding binding;

            public MyViewHolder(ItemLanguageBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
                // No more ButterKnife.bind() needed
            }
        }
    }

    private void changeLangugae(Context context ,String deviceId, String language, String languageCode, String languageName) {
        if (NetworkReceiver.isConnected()) {
            showLoading();
            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put("device_id", deviceId);
            map.put("language", language);
            Log.d(TAG, "changeLangugae: params" + new Gson().toJson(map));

            Call<CommonResponse> call = apiInterface.changeLangugae(GetSet.getToken(), map);
            call.enqueue(new Callback<CommonResponse>() {
                @Override
                public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                    hideLoading();
                    Log.d(TAG, "changeLangugae: res" + new Gson().toJson(response.body()));
                    if (response.isSuccessful() && response.body() != null) {
                        if(response.body().getStatus().equalsIgnoreCase(Constants.TAG_TRUE)){
                            languageAdapter.notifyDataSetChanged();
                            LocaleManager.setNewLocale(context, languageCode, languageName);
                            Log.i(TAG, "setNewLocale: " + languageCode + "," + languageName);
                            Constants.SHOULD_RECREATE_ACTIVITIES_ON_RESUME = true;

                            CommonApi.getAppDefaultData(LanguageActivity.this);

                            SharedPref.putString(Constants.TAG_LANGUAGE_CODE, languageCode);
                            App.recreateAllActivities();


                        }
                    }
                }

                @Override
                public void onFailure(Call<CommonResponse> call, Throwable t) {
                    hideLoading();
                }
            });
        }
    }
}
