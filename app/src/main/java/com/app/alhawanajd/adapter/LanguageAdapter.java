package com.app.alhawanajd.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.alhawanajd.LoginActivity;
import com.app.alhawanajd.databinding.ItemLanguageBinding; // 1. Import the binding class
import com.app.helper.LocaleManager;
import com.app.model.LanguageData;
import com.app.utils.Constants;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiInterface;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

// No more ButterKnife imports
// import butterknife.BindView;
// import butterknife.ButterKnife;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.MyViewHolder> {

    Activity context;
    List<LanguageData> languageList = new ArrayList<>();
    private ApiInterface apiInterface;

    public LanguageAdapter(Activity context, List<LanguageData> languageList) {
        this.languageList = languageList;
        this.context = context;
    }

    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 2. Inflate the layout using View Binding
        ItemLanguageBinding binding = ItemLanguageBinding.inflate(LayoutInflater.from(context), parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final LanguageData data = languageList.get(position);

        // 4. Access views through the holder's binding object
        holder.binding.helpTitle.setText(data.language);
        holder.binding.tickRadio.setChecked(data.languageCode.equals(LocaleManager.getLanguageCode(context)));

        View.OnClickListener listener = v -> {
            Log.i("LanguageAdapter", "langonBindViewHoldersmai: " + data.language + "Code" + data.languageCode);
            setNewLocale(context, data.languageCode, data.language);
        };

        holder.binding.mainLay.setOnClickListener(listener);
        holder.binding.tickRadio.setOnClickListener(listener);
    }

    private void setNewLocale(Activity context, String languageCode, String languageName) {
        notifyDataSetChanged();
        SharedPref.putString(Constants.TAG_LANGUAGE_CODE, languageCode);
        LocaleManager.setNewLocale(context, languageCode, languageName);
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        context.finish();
    }

    @Override
    public int getItemCount() {
        return languageList.size();
    }

    @SuppressLint("NonConstantResourceId")
    public class MyViewHolder extends RecyclerView.ViewHolder {

        // 3. Hold a reference to the item's binding, not individual views
        private final ItemLanguageBinding binding;

        public MyViewHolder(ItemLanguageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            // No more ButterKnife.bind() needed
        }
    }
}
