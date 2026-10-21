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
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.app.alhawanajd.R;
import com.app.alhawanajd.databinding.DialogFundsAlertBinding;
import com.app.helper.callback.OnOkCancelClickListener;
import com.makeramen.roundedimageview.RoundedImageView;


public class DialogBuyGems extends DialogFragment {

    private static final String TAG = DialogBuyGems.class.getSimpleName();
    /*@BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.dimenImage)
    RoundedImageView dimenImage;
    @BindView(R.id.txt_description)
    TextView txtDescription;
    @BindView(R.id.btnOkay)
    Button btnOkay;
    @BindView(R.id.btnNo)
    Button btnNo;*/
    private Context context;
    private OnOkCancelClickListener callBack;
    private DialogFundsAlertBinding binding; // ADD THIS LINE

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
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
            // wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            window.setAttributes(wlp);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
       // View itemView = inflater.inflate(R.layout.dialog_funds_alert, container, false);
       // ButterKnife.bind(this, itemView);
       // initView();
        // return itemView;

        binding = DialogFundsAlertBinding.inflate(inflater, container, false);
        initView();
        setupClickListeners(); // We will create this method next
        return binding.getRoot();
    }

    private void initView() {
        binding.btnNo.setText(context.getString(R.string.not_now));
        binding.btnOkay.setText(context.getString(R.string.buy_gems));
        binding.txtDescription.setVisibility(View.GONE);
        binding.txtTitle.setTextSize(24);
        binding.txtTitle.setText(R.string.insufficient_funds);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setCallBack(OnOkCancelClickListener callBack) {
        this.callBack = callBack;
    }

    // ADD THIS NEW METHOD
    private void setupClickListeners() {
        binding.btnOkay.setOnClickListener(view -> {
            if (callBack != null) {
                callBack.onOkClicked(null);dismiss(); // It's good practice to dismiss the dialog after a click
            }
        });

        binding.btnNo.setOnClickListener(view -> {
            if (callBack != null) {
                callBack.onCancelClicked(null);
                dismiss(); // Dismiss the dialog here as well
            }
        });
    }

    /*@OnClick({R.id.btnOkay, R.id.btnNo})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnOkay:
                callBack.onOkClicked(null);
                break;
            case R.id.btnNo:
                callBack.onCancelClicked(null);
                break;
        }
    }*/
}
