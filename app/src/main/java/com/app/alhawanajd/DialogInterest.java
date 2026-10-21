package com.app.alhawanajd;

import static android.view.View.VISIBLE;
import static com.app.alhawanajd.App.dpToPx;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;
import com.app.alhawanajd.R;
import com.app.db.DBHelper;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.helper.callback.OnOkClickListener;
import com.app.model.GetSet;
import com.app.model.InterestResponse;
import com.app.model.Request.InterestRequest;
import com.app.utils.Constants;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;
import java.util.List;

public class DialogInterest extends DialogFragment implements View.OnClickListener {

    private static final String TAG = DialogInterest.class.getSimpleName();

    private Button fab_select;
    private MaterialTextView tv_skip, tvSelectAll;
    private AppCompatImageView btnBack;
    private RecyclerView interest_rec;
    private ProgressBar progressBar;
    private OnOkClickListener callBack;
    private Context context;
    private String from;
    private String lanCode;

    private Point mDisplaySize = new Point();

    CategoryAdapter categoryAdapter;
    ArrayList<InterestResponse.Interest> categoryResponse = new ArrayList<>();
    ArrayList<InterestResponse.Interest> selectedInterestList = new ArrayList<>();
    ArrayList<String> profileInterestList = new ArrayList<>();
    ArrayList<String> profileInterestIdList = new ArrayList<>();

    private ApiInterface apiInterface;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    // Your code here
                    dismiss();
                    return true; // Capture onKey
                }
                return false; // Don't capture
            }
        });
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorWhite)));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            Window window = dialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            window.setAttributes(wlp);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.dialog_interest, container, false);
        return itemView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            window.getWindowManager().getDefaultDisplay().getSize(mDisplaySize);
        }
        findViews(view);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        StaggeredGridLayoutManager sglm = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        sglm.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        interest_rec.setLayoutManager(sglm);
        int spacingInPixels = dpToPx(context,8);
        interest_rec.addItemDecoration(new StaggeredGridItemDecoration(spacingInPixels));

//        GridLayoutManager layoutManager = new GridLayoutManager(context, 3); // 3 items per row
//        interest_rec.setLayoutManager(layoutManager);

        lanCode = LocaleManager.getLanguageCode(context);
        Log.d(TAG, "onViewCreated: language Code "+lanCode);
        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }
        if (from != null && from.equals(Constants.TAG_SIGNUP)) {
            btnBack.setVisibility(View.GONE);
        } else {
            btnBack.setVisibility(VISIBLE);
        }
        fab_select.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        tv_skip.setOnClickListener(this);
        tvSelectAll.setOnClickListener(this);

        String interestList = SharedPref.getString(SharedPref.INTEREST_LIST, "");
        String interestIdList = SharedPref.getString(SharedPref.INTEREST_IDLIST, "");
        profileInterestList = new ArrayList<String>(Arrays.asList(interestList.split(",")));
        profileInterestIdList = new ArrayList<String>(Arrays.asList(interestIdList.split(",")));
        categoryAdapter = new CategoryAdapter(context, categoryResponse, mDisplaySize);
        interest_rec.setAdapter(categoryAdapter);


        categoryAdapter.notifyDataSetChanged();
        getInterestFromApi();
    }

    private void findViews(View itemView) {
        btnBack = itemView.findViewById(R.id.btnBack);
        tv_skip = itemView.findViewById(R.id.tv_skip);
        tvSelectAll = itemView.findViewById(R.id.tv_select_all);
        fab_select = itemView.findViewById(R.id.fab_select);
        interest_rec = itemView.findViewById(R.id.interest_rec);
        progressBar = itemView.findViewById(R.id.progressBar);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_select_all:
                if (isAllSelected()) {
                    // Deselect everything
                    categoryAdapter.clearSelections();
                    tvSelectAll.setText(R.string.select_all);
                } else {
                    // Select everything
                    categoryAdapter.selectAll();
                    tvSelectAll.setText(R.string.deselect_all);
                }
                break;
            case R.id.btnBack:
                dismissAllowingStateLoss();
                break;
            case R.id.tv_skip:
                selectedInterestList.clear();
//                categoryAdapter.notifyDataSetChanged();
                selectedInterestList.addAll(categoryResponse);
                if (categoryAdapter != null) {
                    categoryAdapter.notifyDataSetChanged();
                    if (selectedInterestList.size() > 0)
                        saveInterestToApi(selectedInterestList);
                }
                break;
            case R.id.fab_select:
                Timber.d("onViewClicked: %s", App.getGsonPrettyInstance().toJson(selectedInterestList));
                if (selectedInterestList.size() > 0) {
                    if (selectedInterestList.size() == 1 && selectedInterestList.get(0).getName().equals(Constants.TAG_MUTED)) {
                        Toasty.warning(context, R.string.interest_warning, Toasty.LENGTH_SHORT).show();
                    } else {
                        ArrayList<InterestResponse.Interest> tempList = new ArrayList<>(selectedInterestList);
                        for (InterestResponse.Interest interest : selectedInterestList) {
                            if (interest.getName().equals(Constants.TAG_MUTED)) {
                                tempList.remove(interest);
                            }
                        }
                        saveInterestToApi(tempList);
                    }
                } else {
                    Toasty.warning(context, R.string.interest_warning, Toasty.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void setContext(FragmentActivity activity) {
        this.context = activity;
    }

    public void setCallBack(OnOkClickListener onOkClickListener) {
        this.callBack = onOkClickListener;
    }

    private void getInterestFromApi() {
        if (NetworkReceiver.isConnected()) {
            fab_select.setEnabled(false);
            tv_skip.setEnabled(false);
            progressBar.setVisibility(VISIBLE);
            Call<InterestResponse> call = apiInterface.getInterestList();
            call.enqueue(new Callback<InterestResponse>() {
            /*    @Override
                public void onResponse(@NotNull Call<InterestResponse> call, @NotNull Response<InterestResponse> response) {
                    if (response.isSuccessful() && response.body().getStatus().equals("true")) {
//                        Timber.d("getInterestFromApi: %s", App.getGsonPrettyInstance().toJson(response.body().getInterests()));
                        categoryResponse.addAll(response.body().getInterests());
                        for (InterestResponse.Interest interest : categoryResponse) {
                            for (String interestName : profileInterestList) {
                                if (interest.getName().equals(interestName)) {
                                    selectedInterestList.add(interest);
                                }
                            }
                        }
                    }else if(response.body().getStatus().equals(Constants.TAG_ACCOUNT_BLOCKED)) {
                        App.makeToast(getString(R.string.Account_block));
                        GetSet.reset();
                        SharedPref.clearAll();
                        DBHelper.getInstance(context).clearDB();
                        Intent logout = new Intent(context, LoginActivity.class);
                        logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(logout);
                    }
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            enableButtons(true);
                            progressBar.setVisibility(View.GONE);
                            categoryAdapter.notifyDataSetChanged();
                        }
                    });
                }*/

                @Override
                public void onResponse(@NotNull Call<InterestResponse> call, @NotNull Response<InterestResponse> response) {
                    if (response.isSuccessful() && response.body().getStatus().equals("true")) {
                        List<InterestResponse.Interest> interests = response.body().getInterests();
                        Log.d("DialogInterest", "Saved Profile Interests (SharedPref): " + profileInterestList);
                        Log.d("DialogInterest", "Saved Profile Interests (SharedPref): " + profileInterestIdList);
                        // Remove "Muted" from the list
                        for (InterestResponse.Interest interest : interests) {
                            if (!"Muted".equalsIgnoreCase(interest.getName())) {
                                categoryResponse.add(interest);

                                for (String interestId : profileInterestIdList) {
                                    if (interest.getId().equals(interestId)) {
                                        selectedInterestList.add(interest);
                                        Log.d("DialogInterest", "Matched Interest → ID: " + interest.getId() + " | Name: " + interest.getName());
                                        break;
                                    }
                                }

                                // refresh adapter AFTER updating list
                              //  categoryAdapter.notifyDataSetChanged();

                                // update select/deselect all label
                                tvSelectAll.setVisibility(VISIBLE);
                                if (isAllSelected()) {
                                    tvSelectAll.setText(R.string.deselect_all);
                                } else {
                                    tvSelectAll.setText(R.string.select_all);
                                }
//
                            }
                        }
                    } else if (response.body().getStatus().equals(Constants.TAG_ACCOUNT_BLOCKED)) {
                        App.makeToast(getString(R.string.Account_block));
                        GetSet.reset();
                        SharedPref.clearAll();
                        DBHelper.getInstance(context).clearDB();
                        Intent logout = new Intent(context, LoginActivity.class);
                        logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(logout);
                    }

                    new Handler(Looper.getMainLooper()).post(() -> {
                        enableButtons(true);
                        progressBar.setVisibility(View.GONE);
                        categoryAdapter.notifyDataSetChanged();
                    });
                }


                @Override
                public void onFailure(Call<InterestResponse> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    Timber.e("getInterestFromApi: %s", t.getMessage());
                    enableButtons(true);
                }
            });
        }
    }


    private void saveInterestToApi(ArrayList<InterestResponse.Interest> isSelectedInterest) {
        if (NetworkReceiver.isConnected()) {
            progressBar.setVisibility(VISIBLE);
            enableButtons(false);
            InterestRequest interestRequest = new InterestRequest();
            interestRequest.setUserId(GetSet.getUserId());
            interestRequest.setToken(GetSet.getAuthToken());
            JSONArray jsonArray = new JSONArray();
            for (InterestResponse.Interest interest : isSelectedInterest) {
                jsonArray.put(interest.getId());
            }
            interestRequest.setInterest(String.valueOf(jsonArray));

//        Timber.d("SaveInterest: %s ", App.getGsonPrettyInstance().toJson(interestRequest));

            Call<Map<String, String>> call = apiInterface.saveInterest(interestRequest);

            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                    if (response.isSuccessful() && response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                        StringBuilder sb = new StringBuilder();
                        for (InterestResponse.Interest interest : isSelectedInterest) {
                            sb.append(interest.getId()).append(",");
                        }
                        if (isSelectedInterest.size() > 0) {
                            String interest = sb.deleteCharAt(sb.length() - 1).toString();
                            SharedPref.putString(SharedPref.INTEREST_LIST, interest);
                        }
                    } else {
                        enableButtons(true);
                    }
                    if (from != null && from.equals(Constants.TAG_PROFILE)) {
                        progressBar.setVisibility(View.GONE);
                        callBack.onOkClicked("", from);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        callBack.onOkClicked("", "");
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    Timber.e("saveInterestToApi: %s", t.getMessage());
                    enableButtons(true);
                    progressBar.setVisibility(View.GONE);
                }
            });
        } else {
            enableButtons(true);
        }
    }

    private void enableButtons(boolean enabled) {
        fab_select.setEnabled(enabled);
        tv_skip.setEnabled(enabled);
        btnBack.setEnabled(enabled);
    }

    public void setFrom(String from) {
        this.from = from;
    }


    public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

        Context mContext;
        ArrayList<InterestResponse.Interest> interestResponse;

        Point display;
        private int spanCount = 2;
        private int normalHeightPx;
        private int doubleHeightPx;

        public CategoryAdapter(Context mContext, ArrayList<InterestResponse.Interest> InterestResponse,
                               final @NonNull Point display) {
            this.mContext = mContext;
            this.interestResponse = InterestResponse;
            this.display = display;
            // Compute column width based on screen width so heights scale with device.
            DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
            int screenWidth = dm.widthPixels;

            // Adjust this based on your RecyclerView padding and item spacing.
            // If you use 16dp padding + 8dp spacing, change values here accordingly.
            int totalHorizontalPadding = dpToPx(mContext,16) * 2; // left+right padding
            int totalSpacingBetweenColumns = dpToPx(mContext,8) * (spanCount - 1);

            int columnWidth = (screenWidth - totalHorizontalPadding - totalSpacingBetweenColumns) / spanCount;

            // normal = square of column width; double = twice the height
            normalHeightPx = columnWidth;            // e.g. square
            doubleHeightPx = normalHeightPx * 2 + dpToPx(mContext,8); // small extra to prevent overlap
        }

        @NonNull
        @Override
        public CategoryAdapter.CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_choose, parent, false);

            // Set proper initial layout params
            StaggeredGridLayoutManager.LayoutParams layoutParams =
                    new StaggeredGridLayoutManager.LayoutParams(
                            normalHeightPx,
                            StaggeredGridLayoutManager.LayoutParams.WRAP_CONTENT);
            itemView.setLayoutParams(layoutParams);
            CategoryAdapter.CategoryViewHolder h =  new CategoryViewHolder(itemView);

            return h;
        }

        @Override
        public void onBindViewHolder(@NonNull CategoryAdapter.CategoryViewHolder holder, int pos) {
            int position = holder.getAbsoluteAdapterPosition();
//            Timber.d("onBindViewHolder111111: %s", interestResponse.get(position).getName());

            holder.itemView.setVisibility(VISIBLE);

            holder.parentLay.setVisibility(VISIBLE);

            Glide.with(holder.mImageView)
                    .load(interestResponse.get(position).getIntIcon())
                    .error(R.drawable.alhawana)
                    .into(holder.mImageView);

            if (lanCode.equals("en")){
                holder.mTvTitle.setText(interestResponse.get(position).getName());
            }else{
                holder.mTvTitle.setText(interestResponse.get(position).getArabicName());
            }

            int normalHeight = dpToPx(holder.itemView.getContext(), 135);
            int tallHeight = dpToPx(holder.itemView.getContext(), 270);

            // every 3rd item (1-based) should be double height
            boolean isDouble = (position % 6 == 0 || position % 6 == 3);

//            int targetHeight = isDouble ? doubleHeightPx : normalHeightPx;
            int targetHeight = isDouble ? tallHeight : normalHeight;

            // Get or create LayoutParams and set height
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp == null) {
                lp = new StaggeredGridLayoutManager.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, targetHeight);
            } else {
                lp.height = targetHeight;
            }

            // Ensure LayoutParams is the StaggeredGridLayoutManager one
            if (!(lp instanceof StaggeredGridLayoutManager.LayoutParams)) {
                lp = new StaggeredGridLayoutManager.LayoutParams(lp.width, lp.height);
            }
            // avoid making any item full-span
            ((StaggeredGridLayoutManager.LayoutParams) lp).setFullSpan(false);

            holder.itemView.setLayoutParams(lp);
            if (!selectedInterestList.contains(categoryResponse.get(position))) {
                holder.parentLay.setBackgroundResource(R.drawable.bg_interest_tag);
                holder.mTvTitle.setTextColor(getResources().getColor(R.color.colorGrey5));
                holder.mImageView.setColorFilter(getResources().getColor(R.color.colorGrey5));
                holder.tick.setVisibility(View.GONE);
            } else {
                holder.parentLay.setBackgroundResource(R.drawable.bg_interest_tag_selected);
                holder.mTvTitle.setTextColor(getResources().getColor(R.color.colorWhite));
                holder.mImageView.setColorFilter(getResources().getColor(R.color.colorWhite));
                holder.tick.setVisibility(VISIBLE);
            }

            holder.parentLay.setOnClickListener(v -> {
                if (!selectedInterestList.contains(categoryResponse.get(position))) {
                    selectedInterestList.add(categoryResponse.get(position));
//                        holder.mCardView.setStrokeColor(getResources().getColor(R.color.colorPrimary));
                    holder.parentLay.setBackgroundResource(R.drawable.bg_interest_tag_selected);
                    holder.mTvTitle.setTextColor(getResources().getColor(R.color.colorWhite));
                    holder.mImageView.setColorFilter(getResources().getColor(R.color.colorWhite));
                    holder.tick.setVisibility(VISIBLE);
                } else {
                    selectedInterestList.remove(categoryResponse.get(position));
//                        holder.mCardView.setStrokeColor(getResources().getColor(R.color.colorWhite));
                    holder.parentLay.setBackgroundResource(R.drawable.bg_interest_tag);
                    holder.mTvTitle.setTextColor(getResources().getColor(R.color.colorGrey5));
                    holder.mImageView.setColorFilter(getResources().getColor(R.color.colorGrey5));
                    holder.tick.setVisibility(View.GONE);
                }
            });
//            }
        }

        public void selectAll() {
            selectedInterestList.clear();
            selectedInterestList.addAll(categoryResponse);
            notifyDataSetChanged();
        }

        public void clearSelections() {
            selectedInterestList.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return interestResponse.size();
        }


        private int imageHeight(@NonNull Point display, int position) {

            int height1 = display.y / 100 * 30;
            int height2 = display.y / 100 * 25;
            if (position % 2 == 0) return height1;
            else return height2;
        }

        public class CategoryViewHolder extends RecyclerView.ViewHolder {

            TextView mTvTitle;
            ImageView mImageView,tick;
            View parentLay;

            public CategoryViewHolder(@NonNull View itemView) {
                super(itemView);

                mTvTitle = itemView.findViewById(R.id.mTvTitle);
                mImageView = itemView.findViewById(R.id.mImageView);
                parentLay = itemView.findViewById(R.id.parentLay);
                tick = itemView.findViewById(R.id.tick);
            }
        }

    }

    private boolean isAllSelected() {
        return !categoryResponse.isEmpty() &&
                selectedInterestList.size() == categoryResponse.size();
    }

}
