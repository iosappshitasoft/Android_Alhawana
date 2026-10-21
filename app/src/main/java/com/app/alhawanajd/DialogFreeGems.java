package com.app.alhawanajd;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
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

import com.app.alhawanajd.databinding.DialogFreeGemsBinding; // 1. Import the binding class
import com.app.helper.callback.OnOkClickListener;
import com.app.utils.AdminData;

// 2. Remove ButterKnife imports
// import butterknife.BindView;
// import butterknife.ButterKnife;
// import butterknife.OnClick;

public class DialogFreeGems extends DialogFragment {

    // 3. Declare the binding object and remove old view fields
    private DialogFreeGemsBinding binding;

    private Context context;
    private OnOkClickListener callBack;

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
            window.setAttributes(wlp);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        // 4. Inflate the layout using View Binding
        binding = DialogFreeGemsBinding.inflate(inflater, container, false);

        // The old ButterKnife.bind() call is removed.

        // 5. Access views via the binding object
        String firstString = context.getString(R.string.congratulations_you_got) + " ";
        String noOfGems = "" + AdminData.freeGems;
        String lastString = " " + context.getString(R.string.gems_free_coins);
        SpannableStringBuilder str = new SpannableStringBuilder(firstString + noOfGems + lastString);
        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), firstString.length(), firstString.length() + noOfGems.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.txtDescription.setText(str);

        // 6. Set up the click listener manually
        binding.btnOkay.setOnClickListener(v -> {
            if (callBack != null) {
                callBack.onOkClicked("", "");
            }
            dismiss(); // It's good practice to also dismiss the dialog
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Null out the binding object to avoid memory leaks in fragments
        binding = null;
    }

    // The old @OnClick method is removed.
    /*
    @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btnOkay)
    public void onViewClicked() {
        callBack.onOkClicked("", "");
    }
    */

    public void setContext(Context context) {
        this.context = context;
    }

    public void setCallBack(OnOkClickListener callBack) {
        this.callBack = callBack;
    }
}
