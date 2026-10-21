package com.app.addon;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.alhawanajd.databinding.ItemLanguageBinding;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.app.alhawanajd.R;


import com.app.alhawanajd.BaseFragmentActivity;
import com.app.alhawanajd.MainActivity;
import com.app.helper.LocaleManager;
import com.app.model.LanguageData;
import com.app.utils.Constants;
import com.app.utils.SharedPref;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LanguageTranslationAct extends BaseFragmentActivity {


    private static final String TAG = LanguageTranslationAct.class.getSimpleName();
    RecyclerView recyclerView;
    TextView txtTitle;
    ImageView btnBack;
    private String from;
    private String[] resourceArray;
    List<LanguageData> languageList = new ArrayList<>();
    LanguageAdapter languageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        txtTitle = (TextView) findViewById(R.id.txtTitle);
        btnBack = (ImageView) findViewById(R.id.btnBack);

        if (getIntent().hasExtra(Constants.TAG_FROM)) {
            from = getIntent().getStringExtra(Constants.TAG_FROM);
        }
        if (from != null && from.equals(Constants.TAG_CHAT)) {
            LanguageData data = new LanguageData();
            String lang="None";
            String lan="";
            data.language = lang;
            data.languageCode = lan;
            languageList.add(data);
            Log.i(TAG, "onCreatelanga: "+data.language+":"+data.languageCode);
            new GetTranslateLanguages().execute();
        }
        else {
            resourceArray = getResources().getStringArray(R.array.language_array);
            List<String> tempList = Arrays.asList(resourceArray);
            for (String list : tempList) {
                String lang[] = list.split("-");
                String langTitle = lang[0];
                String langCode = lang[1];
                LanguageData data = new LanguageData();
                data.language = langTitle;
                data.languageCode = langCode;
                languageList.add(data);
            }

        }

        txtTitle.setText(R.string.language);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(LanguageTranslationAct.this);
        recyclerView.setLayoutManager(layoutManager);
        languageAdapter = new LanguageAdapter(this,languageList);
        recyclerView.setAdapter(languageAdapter);

        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }

        btnBack.setOnClickListener(v -> {
            onBackPressed();
        });

        LocaleManager.getLanguageName(this);

    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }

    @SuppressLint("StaticFieldLeak")
    private class GetTranslateLanguages extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            HttpURLConnection conn = null;
            StringBuilder sb = new StringBuilder("https://translation.googleapis.com/language/translate/v2/languages");
            sb.append("?key=").append(getString(R.string.google_api_key_));
            Log.d(TAG, "doInBackground_token: "+sb);
            try {
                URL url = new URL(sb.toString());
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setUseCaches(false);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestProperty("Content-Type", "application/json");

                //Create JSONObject here
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("target", Constants.TAG_DEFAULT_LANGUAGE_CODE);

                OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
                out.write(jsonParam.toString());
                out.close();
                int HttpResult = conn.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    StringBuilder resultBuilder = new StringBuilder();
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            conn.getInputStream(), StandardCharsets.UTF_8));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        resultBuilder.append(line).append("\n");
                    }
                    br.close();
                    Log.i(TAG, "doInBackgroundResultlan: " + resultBuilder);
                    JSONObject resultObject = new JSONObject(resultBuilder.toString());
                    JSONObject dataObject = resultObject.getJSONObject("data");
                    JSONArray languageArray = dataObject.getJSONArray("languages");
                    for (int i = 0; i < languageArray.length(); i++) {
                        LanguageData data = new LanguageData();
                        data.language = languageArray.getJSONObject(i).getString("name");
                        data.languageCode = languageArray.getJSONObject(i).getString("language");
                        languageList.add(data);
                        Log.i(TAG, "languageListdoInBackground: "+data);
                    }
                } else {
                    Log.e(TAG, "doInBackgroundlan: " + conn.getResponseMessage());
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            languageAdapter.notifyDataSetChanged();
        }
    }

    public class LanguageAdapter extends RecyclerView.Adapter {
        /*List<String> helpTitleLists;*/
        Context context;
        List<LanguageData> languageList = new ArrayList<>();
        private RecyclerView.ViewHolder viewHolder;

       /* public LanguageAdapter(Context context, List<String> helpTitleLists) {
            this.helpTitleLists = helpTitleLists;
            this.context = context;

        }*/

        public LanguageAdapter(Context context,  List<LanguageData> languageList) {
            this.languageList = languageList;
            this.context = context;

        }

        @Override
        public RecyclerView.@NotNull ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
           /* View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_language, parent, false);
            viewHolder = new MyViewHolder(itemView);
            return viewHolder;*/
            ItemLanguageBinding itemBinding = ItemLanguageBinding.inflate(LayoutInflater.from(context), parent, false);
            return new MyViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final LanguageData data = languageList.get(position);
            MyViewHolder myHolder = (MyViewHolder) holder; // Cast holder once

            myHolder.binding.helpTitle.setText(data.language);

            if (from != null && from.equals(Constants.TAG_CHAT)) {
                Log.i(TAG, "langonBindViewHolder: "+from);
                if (data.languageCode.equals(SharedPref.getString(SharedPref.CHAT_LANGUAGE, SharedPref.DEFAULT_CHAT_LANGUAGE))) {
                    myHolder.binding.tickRadio.setVisibility(View.VISIBLE);
                } else {
                    myHolder.binding.tickRadio.setVisibility(View.GONE);
                }
            } else {
                Log.i(TAG, "langonBindViewHolders: "+from);
                if (data.languageCode.equals(LocaleManager.getLanguageCode(context))) {
                    myHolder.binding.tickRadio.setVisibility(View.VISIBLE);
                } else {
                    myHolder.binding.tickRadio.setVisibility(View.GONE);
                }
            }


            myHolder.binding.mainLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (from != null && from.equals(Constants.TAG_CHAT)) {
                        Log.i(TAG, "langonBindViewHoldersma: "+from);
                        setTranslateLanguage(data.languageCode);
                    } else {
                        Log.i(TAG, "langonBindViewHoldersmai: "+from);
                        setNewLocale(context,data.languageCode,data.language);
                    }


                }
            });

        }

        private void setTranslateLanguage(String languageCode) {
            languageAdapter.notifyDataSetChanged();
            SharedPref.putString(SharedPref.CHAT_LANGUAGE,languageCode);
            finish();
        }

        private void setNewLocale(Context context, String languageCode,String languageName) {
            languageAdapter.notifyDataSetChanged();
            SharedPref.putString(Constants.TAG_LANGUAGE_CODE, languageCode);
            LocaleManager.setNewLocale(context, languageCode,languageName);

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        @Override
        public int getItemCount() {
            return languageList.size();
        }

        @SuppressLint("NonConstantResourceId")
        public class MyViewHolder extends RecyclerView.ViewHolder {
/*
            @BindView(R.id.tick_radio)
            MaterialRadioButton tick;
            @BindView(R.id.mainLay)
            RelativeLayout mainLay;
            @BindView(R.id.helpTitle)
            TextView langTitle;


            public MyViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);

            }*/


            // ADD THIS BINDING VARIABLE
            private final ItemLanguageBinding binding;

            // MODIFY THE CONSTRUCTOR
            public MyViewHolder(ItemLanguageBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResultlanguag: "+requestCode);
        if (requestCode == Constants.LANGUAGE_REQUEST_CODE){
            Log.i(TAG, "onActivityResultlangua: "+requestCode);
        }
    }
}
