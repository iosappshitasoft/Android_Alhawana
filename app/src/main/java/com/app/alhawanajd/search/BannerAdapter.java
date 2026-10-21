package com.app.alhawanajd.search;

import android.content.Context;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.app.alhawanajd.R;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.model.SearchPageResponse;
import com.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class BannerAdapter extends PagerAdapter {
    private static final String TAG = BannerAdapter.class.getSimpleName();
    private Context mContext;
    private RelativeLayout itemLay, imageLay;
    private ImageView ivBanner;
    private List<SearchPageResponse.Result.Banner> bannerList = new ArrayList<>();
    private AdapterItemClickListener clickListener;
    private LayoutInflater inflater;
    public final static float BIG_SCALE = 1.0f;
    public final static float SMALL_SCALE = 0.7f;
    public final static float DIFF_SCALE = BIG_SCALE - SMALL_SCALE;

    public BannerAdapter(Context mContext, List<SearchPageResponse.Result.Banner> bannerList,
                         AdapterItemClickListener clickListener) {
        this.mContext = mContext;
        this.bannerList = bannerList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.item_banner_image, container, false);
        itemLay = itemView.findViewById(R.id.itemLay);
        imageLay = itemView.findViewById(R.id.imageLay);
        ivBanner = itemView.findViewById(R.id.ivBanner);
        if (position != -1) {
            SearchPageResponse.Result.Banner bannerItem = bannerList.get(position);
            Timber.tag(TAG).d("instantiateItem: %s", new Gson().toJson(bannerItem));

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageLay.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
            imageLay.setLayoutParams(params);

            Glide.with(mContext)
                    /*.load(stream.getPublisherImage())*/
                    .load(bannerItem.getBannerImage())
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_banner)
                    .error(R.drawable.placeholder_banner)
                    .into(ivBanner);

            itemLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (bannerItem.getType().equals(Constants.TAG_USER)) {
                        clickListener.onItemClicked(bannerItem, position, bannerItem.getType());
                    } else {
                        clickListener.onItemClicked(bannerItem, position, Constants.TAG_HASHTAGS);
                    }
                }
            });
        }

        ((ViewPager) container).addView(itemView, 0);
        return itemView;
    }

    @Override
    public int getCount() {
        return bannerList.size();
    }

    @Override
    public void destroyItem(ViewGroup arg0, int arg1, Object arg2) {
        ((ViewPager) arg0).removeView((View) arg2);
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == ((View) arg1);

    }

    @Override
    public Parcelable saveState() {
        return null;
    }
}
