package com.app.alhawanajd;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.app.alhawanajd.databinding.DialogFundsAlertBinding; // 1. Import the binding class
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.app.helper.callback.OnOkCancelClickListener;

// 2. Remove ButterKnife imports
// import butterknife.BindView;
// import butterknife.ButterKnife;
// import butterknife.OnClick;

public class DialogCreditGems extends DialogFragment {

    // 3. Declare the binding object
    private DialogFundsAlertBinding binding;

    private Context context;
    private OnOkCancelClickListener callBack;
    private String message = "";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorTransparent)));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            Window window = dialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            window.setAttributes(wlp);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        // 4. Inflate the layout using View Binding
        binding = DialogFundsAlertBinding.inflate(inflater, container, false);
        // ButterKnife.bind(this, itemView) is no longer needed
        initView();
        setupClickListeners(); // 5. Set up click listeners
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Null out the binding object to avoid memory leaks in fragments
        binding = null;
    }

    private void initView() {
        // 6. Access views via the binding object
        binding.txtTitle.setVisibility(View.GONE);
        binding.btnNo.setVisibility(View.GONE);
        binding.txtDescription.setVisibility(View.VISIBLE);

        if (message.equals(getString(R.string.filter_error_message))) {
            binding.txtDescription.setTextSize(18);
            Glide.with(context)
                    .load(R.drawable.alhawana)
                    .apply(new RequestOptions().transform(new CircleCrop()))
                    .into(binding.dimenImage);
            binding.dimenImage.setVisibility(View.VISIBLE);
        } else if (message.equals(getString(R.string.no_user_message))) {
            Glide.with(context)
                    .load(R.drawable.alhawana)
                    .apply(new RequestOptions().transform(new CircleCrop()))
                    .into(binding.dimenImage);
            binding.txtDescription.setVisibility(View.GONE);
            binding.txtTitle.setVisibility(View.VISIBLE);
            binding.txtTitle.setText(message);
            binding.dimenImage.setVisibility(View.VISIBLE);
        } else if (message.equals(getString(R.string.you_dont_have_any_gifts))) {
            binding.txtDescription.setVisibility(View.GONE);
            binding.txtTitle.setVisibility(View.VISIBLE);
            binding.txtTitle.setText(message);
            binding.dimenImage.setImageDrawable(context.getDrawable(R.drawable.gift_primary));
            binding.dimenImage.setVisibility(View.VISIBLE);
        } else if (message.equals(getString(R.string.gems_purchase_response))) {
            binding.txtDescription.setVisibility(View.GONE);
            binding.txtTitle.setVisibility(View.VISIBLE);
            binding.txtTitle.setText(message);
            binding.dimenImage.setVisibility(View.VISIBLE);
            binding.dimenImage.setImageDrawable(context.getDrawable(R.drawable.coinprimarycolor));
        } else if (message.equals(getString(R.string.request_sent_successfully))) {
            binding.txtDescription.setVisibility(View.GONE);
            binding.txtTitle.setVisibility(View.VISIBLE);
            binding.txtTitle.setText(message);
            binding.dimenImage.setVisibility(View.VISIBLE);
            binding.dimenImage.setImageResource(R.drawable.blue_gift);
        } else {
            binding.txtDescription.setTextSize(24);
        }

        binding.txtDescription.setText(message);
        binding.txtDescription.setTextColor(getResources().getColor(R.color.colorBlack));
    }

    private void setupClickListeners() {
        binding.btnOkay.setOnClickListener(v -> {
            if (callBack != null) {
                callBack.onOkClicked(null);
            }
            dismiss(); // Dismiss the dialog on click
        });

        binding.btnNo.setOnClickListener(v -> {
            if (callBack != null) {
                callBack.onCancelClicked(null);
            }
            dismiss(); // Dismiss the dialog on click
        });
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setCallBack(OnOkCancelClickListener callBack) {
        this.callBack = callBack;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    // 7. The @OnClick method is removed. Its logic is now in setupClickListeners().
}
