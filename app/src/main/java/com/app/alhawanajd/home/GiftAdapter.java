package com.app.alhawanajd.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.app.alhawanajd.databinding.ItemGiftsBinding; // 1. Import ItemGiftsBinding
import com.app.alhawanajd.databinding.ItemLoadingBinding; // 1. Import ItemLoadingBinding
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.app.alhawanajd.R;
import com.app.db.DBHelper;
import com.app.alhawanajd.App;
import com.app.alhawanajd.LoginActivity;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.model.GetSet;
import com.app.model.Gift;
import com.app.utils.Constants;
import com.app.utils.DevicePref;
import com.app.utils.SharedPref;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import timber.log.Timber;

// ButterKnife imports are removed
public class GiftAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_FOOTER = 1;
    private final Context context;
    private final List<Gift> giftList;
    private boolean showLoading = false;
    private AdapterItemClickListener clickListener;
    private int itemWidth;

    public GiftAdapter(Context context, List<Gift> giftList, AdapterItemClickListener clickListener, int itemWidth) {
        Timber.d("GiftAdapter: instance %s", giftList);
        this.context = context;
        this.giftList = giftList;
        this.clickListener = clickListener;
        this.itemWidth = itemWidth;
    }

    @Override
    public @NotNull RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_TYPE_ITEM) {
            // 3. Inflate using View Binding for the item
            ItemGiftsBinding binding = ItemGiftsBinding.inflate(inflater, parent, false);
            return new MyViewHolder(binding);
        } else { // It must be VIEW_TYPE_FOOTER
            // 3. Inflate using View Binding for the footer
            ItemLoadingBinding loadingBinding = ItemLoadingBinding.inflate(inflater, parent, false);
            return new FooterViewHolder(loadingBinding);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.@NotNull ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {
            MyViewHolder myViewHolder = (MyViewHolder) holder;
            // The get(position) can cause a crash if the list is empty during footer display.
            // Check position against list size.
            if (position >= giftList.size()) return;

            final Gift gift = giftList.get(position);

            // Dynamically set the item width
            ViewGroup.LayoutParams layoutParams = myViewHolder.itemView.getLayoutParams();
            layoutParams.width = itemWidth; // Set calculated width
            myViewHolder.itemView.setLayoutParams(layoutParams);

            myViewHolder.binding.progressBar.setVisibility(View.VISIBLE);
            myViewHolder.binding.giftImage.setVisibility(View.INVISIBLE);

            if (gift.getGiftIcon() != null) {
                Glide.with(context)
                        .load(gift.getGiftIcon())
                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                myViewHolder.binding.progressBar.setVisibility(View.GONE);
                                myViewHolder.binding.giftImage.setImageDrawable(context.getDrawable(R.drawable.gift_primary));
                                myViewHolder.binding.giftImage.setVisibility(View.VISIBLE);
                                myViewHolder.binding.selectionBackgroundLay.setVisibility(View.GONE); // Make sure to handle selection visibility
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                myViewHolder.binding.progressBar.setVisibility(View.GONE);
                                myViewHolder.binding.giftImage.setImageDrawable(resource);
                                myViewHolder.binding.giftImage.setVisibility(View.VISIBLE);
                                myViewHolder.binding.selectionBackgroundLay.setVisibility(View.GONE); // Make sure to handle selection visibility
                                return false;
                            }
                        }).apply(new RequestOptions().error(R.drawable.gift_primary))
                        .into(myViewHolder.binding.giftImage);
            }
            myViewHolder.binding.txtGiftName.setText(gift.getGiftTitle());
            myViewHolder.binding.txtGiftPrice.setText((GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) ? "" + gift.getGiftGemsPrime() : "" + gift.getGiftGems());

        } else if (holder instanceof FooterViewHolder) {
            FooterViewHolder footerHolder = (FooterViewHolder) holder;
            footerHolder.binding.progressBar.setIndeterminate(true);
            footerHolder.binding.progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (showLoading && isPositionFooter(position))
            return VIEW_TYPE_FOOTER;
        return VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        int itemCount = giftList.size();
        if (showLoading)
            itemCount++;
        return itemCount;
    }

    public boolean isPositionFooter(int position) {
        return position == getItemCount() - 1 && showLoading;
    }

    public void showLoading(boolean value) {
        showLoading = value;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        // Hold the binding object
        private final ItemGiftsBinding binding;

        public MyViewHolder(ItemGiftsBinding binding) {
            super(binding.getRoot());
            this.binding = binding; // Store the binding

            ViewGroup.LayoutParams params = binding.itemLay.getLayoutParams();
            params.width = DevicePref.getInstance(context).getInt(DevicePref.SCREEN_WIDTH, 0) / 4;
            binding.itemLay.setLayoutParams(params);

            // Set the click listener directly on the view from the binding object
            binding.itemLay.setOnClickListener(v -> onViewClicked());
        }

        // The @OnClick annotation is removed, but the logic is preserved here
        public void onViewClicked() {
            int position = getAbsoluteAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;

            if (Constants.Login_method != "skip") {
                if ((GetSet.getPremiumMember().equals(Constants.TAG_TRUE) &&
                        GetSet.getGems() >= giftList.get(position).getGiftGemsPrime()) ||
                        (!GetSet.getPremiumMember().equals(Constants.TAG_TRUE) &&
                                GetSet.getGems() >= giftList.get(position).getGiftGems())) {
                    clickListener.onItemClicked(giftList.get(position), position, Constants.TAG_GIFT);
                } else {
                    App.makeToast(context.getString(R.string.not_enough_gems));
                }
            } else {
                GetSet.reset();
                // ** THIS IS THE CORRECTED LINE - PRESERVED FROM YOUR ORIGINAL CODE **
                SharedPref.clearAll();
                DBHelper.getInstance(context).clearDB();
                Intent logout = new Intent(context, LoginActivity.class);
                logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(logout);
            }
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        // Hold the binding object
        private final ItemLoadingBinding binding;

        public FooterViewHolder(ItemLoadingBinding binding) {
            super(binding.getRoot());
            this.binding = binding; // Store the binding
        }
    }
}
