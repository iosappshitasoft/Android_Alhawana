package com.app.Zego.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.app.addon.livestream.external.utils.Constants;
import com.app.alhawanajd.App;
import com.app.alhawanajd.PrimeActivity;
import com.app.alhawanajd.PrimeActivityOld;
import com.app.alhawanajd.R;
import com.app.alhawanajd.databinding.ItemGemsStoreBinding;
import com.app.alhawanajd.databinding.LayoutPrimeBannerBinding;
import com.app.alhawanajd.databinding.LayoutRenewalBannerBinding;
import com.app.model.GemsStoreResponse;
import com.app.model.GetSet;
import com.app.utils.AppUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class GemsAdapter extends RecyclerView.Adapter {

    private final Context context;
    private List<GemsStoreResponse.GemsList> gemsList = new ArrayList<>();
    private boolean showLoading = false;
    private RecyclerView.ViewHolder viewHolder;
    Typeface typefaceLight = null, typefaceRegular;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_FOOTER = 1;
    private final int VIEW_TYPE_BANNER = 2;
    private final int VIEW_TYPE_PLACEHOLDER = 3;
    public interface OnGemClickListener {
        void onGemClicked(GemsStoreResponse.GemsList gem);
    }
    private OnGemClickListener listener;


    public GemsAdapter(Context context, List<GemsStoreResponse.GemsList> gemsList, OnGemClickListener listener) {
        this.context = context;
        this.gemsList = gemsList;
        this.listener = listener;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typefaceLight = context.getResources().getFont(R.font.font_light);
            typefaceRegular = context.getResources().getFont(R.font.font_regular);
        } else {
            typefaceLight = ResourcesCompat.getFont(context, R.font.font_light);
            typefaceRegular = ResourcesCompat.getFont(context, R.font.font_regular);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.item_gems_new, parent, false);
            viewHolder = new MyViewHolder(itemView, ItemGemsStoreBinding.bind(itemView));
        } else if (viewType == VIEW_TYPE_PLACEHOLDER) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.placeholder_gems_store, parent, false);
            viewHolder = new PlaceHolder(itemView);
        } else if (viewType == VIEW_TYPE_BANNER) {
            View v;
            if (GetSet.getPremiumMember() != null && GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                v = LayoutInflater.from(context).inflate(R.layout.layout_prime_banner, parent, false);
                viewHolder = new PrimeViewHolder(v, LayoutPrimeBannerBinding.bind(v));
            } else if (GetSet.isOncePurchased()) {
                v = LayoutInflater.from(context).inflate(R.layout.layout_renewal_banner, parent, false);
                viewHolder = new RenewalViewHolder(v, LayoutRenewalBannerBinding.bind(v));
            } else {
                v = LayoutInflater.from(context).inflate(R.layout.layout_prime_banner, parent, false);
                viewHolder = new PrimeViewHolder(v, LayoutPrimeBannerBinding.bind(v));
            }
        } else if (viewType == VIEW_TYPE_FOOTER) {
            View v = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
            viewHolder = new FooterViewHolder(v);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {
            final GemsStoreResponse.GemsList gems = gemsList.get(position);
            ((MyViewHolder) holder).txtGemsCount.setText("" + Math.round(gems.getGemCount()));
            ((MyViewHolder) holder).txtPrice.setText(gems.getGemPrice());
           /* Glide.with(context)
                    .load(gems.getGemIcon())
                    .apply(new RequestOptions().placeholder(R.drawable.diamond).error(R.drawable.diamond).dontAnimate().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(((MyViewHolder) holder).giftImage);*/
        } else if (holder instanceof PlaceHolder) {

        } else if (holder instanceof PrimeViewHolder) {
            if (GetSet.getPremiumMember() != null && GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                ((PrimeViewHolder) holder).btnSubscribe.setVisibility(View.GONE);
                ((PrimeViewHolder) holder).premiumLay.setVisibility(View.GONE);
                ((PrimeViewHolder) holder).subscribeLay.setVisibility(View.GONE);
            } else {
                ((PrimeViewHolder) holder).premiumLay.setVisibility(View.GONE);
                ((PrimeViewHolder) holder).btnSubscribe.setVisibility(View.GONE);
                ((PrimeViewHolder) holder).subscribeLay.setVisibility(View.GONE);
            }
            ((PrimeViewHolder) holder).txtPrimeTitle.setText(AppUtils.getPrimeTitle(context));
            ((PrimeViewHolder) holder).txtPrice.setText(AppUtils.getPrimeContent(context));
        } else if (holder instanceof RenewalViewHolder) {
            RenewalViewHolder renewalViewHolder = (RenewalViewHolder) holder;
            ((RenewalViewHolder) holder).renewalLay.setVisibility(View.GONE);

        } else if (holder instanceof FooterViewHolder) {
            FooterViewHolder footerHolder = (FooterViewHolder) holder;
            footerHolder.progressBar.setIndeterminate(true);
            footerHolder.progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (showLoading && isPositionFooter(position))
            return VIEW_TYPE_FOOTER;
        else if (gemsList == null || gemsList.size() == 0 ||
                position > gemsList.size() || gemsList.get(position) == null)
            return VIEW_TYPE_PLACEHOLDER;
        else if (gemsList.get(position).getViewType() == VIEW_TYPE_BANNER) {
            return VIEW_TYPE_BANNER;
        } else
            return VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        int itemCount = gemsList.size();
        if (showLoading)
            itemCount++;
        return itemCount;
    }

    public boolean isPositionFooter(int position) {
        return position == getItemCount() - 1 && showLoading;
    }

    public void showLoading(boolean value) {
        showLoading = value;
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView giftImage;
        TextView txtGemsCount;
        TextView txtPrice;
        CardView itemLay;

        public MyViewHolder(View view, @NonNull ItemGemsStoreBinding bind) {
            super(view);
            giftImage = bind.giftImage;
            txtGemsCount = bind.txtGemsCount;
            txtPrice = bind.txtPrice;
            itemLay = bind.itemLay;
            txtGemsCount.setTypeface(typefaceRegular);
            txtPrice.setTypeface(typefaceRegular);

            bind.itemLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onViewClicked();
                }
            });
        }


        public void onViewClicked() {
            //isGoneForRecharge = true;
            App.preventMultipleClick(itemView);
            /*purchaseSku = gemsList.get(getAdapterPosition()).getGemTitle();
            initBilling(true);*/
            if (listener != null) {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onGemClicked(gemsList.get(pos));
                }
            }
        }
    }


    public class PlaceHolder extends RecyclerView.ViewHolder {

        ShimmerFrameLayout shimmerLayout;

        public PlaceHolder(View view) {
            super(view);
            shimmerLayout = view.findViewById(R.id.shimmerLayout);
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        ProgressBar progressBar;
        public FooterViewHolder(View parent) {
            super(parent);
            progressBar = parent.findViewById(R.id.progressBar);
        }
    }

    public class PrimeViewHolder extends RecyclerView.ViewHolder {

        TextView txtPrice;
        Button btnSubscribe;
        MaterialCardView subscribeLay;
        TextView txtPrimeTitle;
        RelativeLayout premiumLay;
        RelativeLayout primeBgLay;

        public PrimeViewHolder(View parent, @NonNull LayoutPrimeBannerBinding bind) {
            super(parent);
            txtPrice = bind.txtPrice;
            btnSubscribe = bind.btnSubscribe;
            subscribeLay = bind.subscribeLay;
            premiumLay = bind.premiumLay;
            primeBgLay = bind.primeBgLay;
            txtPrimeTitle = bind.txtPrimeTitle;
            txtPrimeTitle.setTypeface(typefaceLight);
            txtPrice.setTypeface(typefaceLight);
            btnSubscribe.setTypeface(typefaceLight);

            bind.subscribeLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (GetSet.getPremiumMember() != null && !GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                        App.preventMultipleClick(btnSubscribe);
                        App.preventMultipleClick(subscribeLay);
                        context.startActivity(new Intent(context, PrimeActivityOld.class));
                    }
                }
            });
        }
    }

    public class RenewalViewHolder extends RecyclerView.ViewHolder {
        Button btnRenewal;
        RelativeLayout renewalBgLay;
        MaterialCardView renewalLay;
        TextView txtRenewalTitle;
        TextView txtRenewalDes;

        public RenewalViewHolder(View parent, @NonNull LayoutRenewalBannerBinding bind) {
            super(parent);
            btnRenewal = bind.btnRenewal;
            renewalBgLay = bind.renewalBgLay;
            renewalLay = bind.renewalLay;
            txtRenewalTitle = bind.txtRenewalTitle;
            txtRenewalDes = bind.txtRenewalDes;
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(5, 10, 5, 30);
            renewalLay.setLayoutParams(params);
            txtRenewalTitle.setTypeface(typefaceLight);
            txtRenewalDes.setTypeface(typefaceLight);
            btnRenewal.setTypeface(typefaceLight);
            btnRenewal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (GetSet.getPremiumMember() != null && !GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                        App.preventMultipleClick(btnRenewal);
                        App.preventMultipleClick(renewalLay);
                        context.startActivity(new Intent(context, PrimeActivityOld.class));
                    }
                }
            });
        }
    }
}
