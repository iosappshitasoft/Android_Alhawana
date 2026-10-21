package com.app.helper.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.app.model.GetSet;
import com.app.model.ProfileRequest;
import com.app.model.ProfileResponse;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.app.utils.Constants;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class SettingsUpdateWorker extends ListenableWorker {
    private static final String TAG = SettingsUpdateWorker.class.getSimpleName();
    private Context context;

    public SettingsUpdateWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @Override
    public ListenableFuture<Result> startWork() {
        // Do the work here--in this case, download the file.
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        String requestData = getInputData().getString(Constants.TAG_PROFILE_DATA);
        Timber.tag(TAG).d("startWork: %s", requestData);
        ProfileRequest request = new Gson().fromJson(requestData, ProfileRequest.class);
        request.setUserId(GetSet.getUserId());
        request.setProfileId(GetSet.getUserId());
//        Timber.d("updateSettings: %s", new Gson().toJson(request));

        return CallbackToFutureAdapter.getFuture(completer -> {
            Call<ProfileResponse> call = apiInterface.getProfile(request);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(@NotNull Call<ProfileResponse> call, @NotNull Response<ProfileResponse> response) {
                    ProfileResponse profile = response.body();
                    if (profile.getStatus().equals(Constants.TAG_TRUE)) {
//                        Timber.d("updateSettings: %s", new Gson().toJson(profile));
                        completer.set(Result.success(createOutputData(new Gson().toJson(profile))));
                    }
                }

                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable t) {
                    call.cancel();
                    t.getMessage();
                    setError(completer);
                }
            });
            // Indicate whether the work finished successfully with the Result
            return completer;
        });
    }

    private Data createOutputData(String filePath) {
        Data.Builder builder = new Data.Builder();
        builder.putString(Constants.TAG_PROFILE_DATA, filePath);
        return builder.build();
    }

    private void setError(CallbackToFutureAdapter.Completer<Result> completer) {
        completer.set(Result.failure(Data.EMPTY));
    }

}