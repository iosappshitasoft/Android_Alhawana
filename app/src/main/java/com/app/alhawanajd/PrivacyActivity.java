package com.app.alhawanajd;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.app.alhawanajd.R;
import com.app.alhawanajd.databinding.ActivityTermsBinding;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.model.TermsResponse;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("NonConstantResourceId")
public class PrivacyActivity extends BaseFragmentActivity {

    private static final String TAG = "AccountPrivacy";

 ApiInterface apiInterface;
 private ActivityTermsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    //    setContentView(R.layout.activity_terms);
     //   ButterKnife.bind(this);
        binding = ActivityTermsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        binding.includeHeader.btnBack.setVisibility(View.VISIBLE);
        binding.progressBar.setVisibility(View.VISIBLE);



        if (LocaleManager.isRTL()) {
            binding.includeHeader.btnBack.setRotation(180);
        } else {
            binding.includeHeader.btnBack.setRotation(0);
        }

        initWebView();
        if (getIntent().hasExtra(Constants.TAG_TITLE)) {
            setTitle(getIntent().getStringExtra(Constants.TAG_TITLE));
            loadLink(getIntent().getStringExtra(Constants.TAG_DESCRIPTION));
        } else {
            getTerms();
        }

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);

       binding.includeHeader.btnBack.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               finish();
           }
       });
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), NetworkReceiver.isConnected());
        registerNetworkReceiver();
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
        super.onDestroy();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        WebSettings webSetting = binding.webView.getSettings();
        webSetting.setBuiltInZoomControls(true);
        binding.webView.setWebViewClient(new WebViewClient());
        webSetting.setJavaScriptEnabled(true);
        binding.webView.clearCache(true);
    }

    private void getTerms() {
        Call<TermsResponse> call = apiInterface.getTerms();
        call.enqueue(new Callback<TermsResponse>() {
            @Override
            public void onResponse(@NotNull Call<TermsResponse> call, @NotNull Response<TermsResponse> response) {
                TermsResponse termsResponse = response.body();
                if (termsResponse.getStatus().equals(Constants.TAG_TRUE) && termsResponse.getTerms() != null) {
                    setTitle(termsResponse.getTerms().getHelpTitle());
                    loadLink(termsResponse.getTerms().getHelpDescrip());
                }
            }

            @Override
            public void onFailure(@NotNull Call<TermsResponse> call, @NotNull Throwable t) {

            }
        });
    }

    public void setTitle(String title) {
        binding.includeHeader.txtTitle.setVisibility(View.VISIBLE);
        binding.includeHeader.txtTitle.setText(title);
        binding.includeHeader.txtTitle.setTextColor(getResources().getColor(R.color.colorWhite));
    }

    public void loadLink(String helpDescrip) {
        binding.webView.loadData(helpDescrip, "text/html; charset=utf-8", "UTF-8");
    }

    public class WebViewClient extends android.webkit.WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            // TODO Auto-generated method stub
            proceedUrl(view, url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {

            // TODO Auto-generated method stub

            super.onPageFinished(view, url);
            binding.progressBar.setVisibility(View.GONE);
        }

    }

    private void proceedUrl(WebView view, String url) {
        if (url.startsWith("mailto:")) {
            startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse(url)));
        } else if (url.startsWith("tel:")) {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(url)));
        } else {
            view.loadUrl(url);
        }
    }

}
