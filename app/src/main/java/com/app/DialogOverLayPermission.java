package com.app;

import android.annotation.SuppressLint;
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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.app.alhawanajd.R;
import com.app.alhawanajd.databinding.DialogOverlayPermissionBinding;
import com.app.helper.callback.OnOkClickListener;
import com.app.utils.SharedPref;

@SuppressLint("NonConstantResourceId")
public class DialogOverLayPermission extends DialogFragment {


   /* @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.dimenImage)
    ImageView dimenImage;
    @BindView(R.id.txt_description)
    TextView txtDescription;
    @BindView(R.id.btnAllow)
    Button btnAllow;
    @BindView(R.id.btnDeny)
    Button btnDeny;
    @BindView(R.id.btnDontAsk)
    CheckBox btnDontAsk;*/
    private Context context;
    private OnOkClickListener callBack;
    private DialogOverlayPermissionBinding binding; // ADD THIS LINE

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

    @SuppressLint("UseCompatLoadingForDrawables")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        /*View itemView = inflater.inflate(R.layout.dialog_overlay_permission, container, false);
        ButterKnife.bind(this, itemView);
*/
        binding = DialogOverlayPermissionBinding.inflate(inflater, container, false);
        setupClickListeners(); // We will create this method
        return binding.getRoot();


    }

    private void setupClickListeners() {
        binding.btnDontAsk.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                binding.btnAllow.setBackground(context.getDrawable(R.drawable.btn_bg_white));
                binding.btnAllow.setTextColor(context.getResources().getColor(R.color.textPrimary));
                binding.btnAllow.setEnabled(false);

            } else {
                binding.btnAllow.setBackground(context.getDrawable(R.drawable.btn_bg_primary));
                binding.btnAllow.setTextColor(context.getResources().getColor(R.color.colorWhite));
                binding.btnAllow.setEnabled(true);
            }
        });

        binding.btnDeny.setOnClickListener(view -> {
            SharedPref.putBoolean(SharedPref.POP_UP_WINDOW_PERMISSION, !binding.btnDontAsk.isChecked());
            callBack.onOkClicked(false, "");
        });

        binding.btnAllow.setOnClickListener(view -> {
            callBack.onOkClicked(true, "");
        });
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setCallBack(OnOkClickListener callBack) {
        this.callBack = callBack;
    }
}
