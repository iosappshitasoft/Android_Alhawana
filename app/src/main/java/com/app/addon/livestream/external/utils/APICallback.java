package com.app.addon.livestream.external.utils;

import androidx.annotation.NonNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Class can be used for all API calls for additional error handling
 * @param <T>
 *
 * @author EpicDev@Hts
 * @since 21 Oct 2019
 */
public abstract class APICallback<T> implements Callback<T> {

    @Override
    final public void onResponse(@NonNull Call<T> call,

                           @NonNull Response<T> response) {
        if (!response.isSuccessful() || response.body() == null) {
            onFailure(call, new Exception("Unexpected response from server: " + response.code() + "\n" +
                    "for " + call.request().url().toString()));
            return;
        }
        // deliver results
        onSuccessResponse(response.body());
    }

    @Override
    final public void onFailure(@NonNull Call<T> call, Throwable t) {
        t.printStackTrace();

        // error occurred
        onErrorResponse(call, t.getMessage());
    }

    /**
     * Gives values obtained from {@link Response} where {@link Response#code()} is 200
     * @param body - response received from the call
     */
    abstract public void onSuccessResponse(@NonNull T body);

    /**
     * Gives messages if {@link Response#code()} is not in [200, 300..)
     * @param message - cause of the error
     */
    abstract public void onErrorResponse(@NonNull Call<T> call, @NonNull String message);
}