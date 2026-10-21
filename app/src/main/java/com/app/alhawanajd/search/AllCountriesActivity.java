package com.app.alhawanajd.search;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.alhawanajd.R;
import com.app.alhawanajd.BaseFragmentActivity;
import com.app.alhawanajd.databinding.ActivityAllCountriesBinding;
import com.app.helper.LocaleManager;
import com.app.model.CountryResponse;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.app.utils.AppUtils;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.ArrayList;
import java.util.List;

public class AllCountriesActivity extends BaseFragmentActivity {

    private static final String TAG = AllCountriesActivity.class.getSimpleName();
  /*  @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.countryView)
    RecyclerView countryView;*/
    private ApiInterface apiInterface;
    private List<GradientDrawable> countryBgList = new ArrayList<>();
    private List<CountryResponse.Result> countryList = new ArrayList<>();

    private GridLayoutManager mLayoutManager;
    private AppUtils appUtils;
    private int bgListIndex = 0;
    private ActivityAllCountriesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_all_countries);
       // ButterKnife.bind(this);
        // 3. Inflate the layout using View Binding
        binding = ActivityAllCountriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        appUtils = new AppUtils(this);
        countryBgList = appUtils.getCountryBGList();
        initView();
    }

    private void initView() {
        binding.includeHeader.btnBack.setScaleX(LocaleManager.isRTL() ? -1 : 1);
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);
        binding.includeHeader.txtTitle.setText(getString(R.string.popular_countries));
        binding.includeHeader.txtTitle.setVisibility(View.VISIBLE);

        binding.includeHeader.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }



}
