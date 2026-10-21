package com.app.alhawanajd;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import com.app.alhawanajd.databinding.DialogFingerPrintBinding; // 1. Import the binding class
import com.app.helper.FingerprintUiHelper;
import com.app.helper.callback.FingerPrintCallBack;

// 2. Remove ButterKnife imports (if any, they are gone from the class)

@RequiresApi(api = Build.VERSION_CODES.M)
public class DialogFingerPrint extends DialogFragment implements FingerprintUiHelper.Callback {

    private static final String TAG = DialogFingerPrint.class.getSimpleName();

    // 3. Declare the binding object and remove all @BindView annotations
    private DialogFingerPrintBinding binding;

    private FingerPrintCallBack fingerPrintCallBack;
    private FingerprintUiHelper mFingerprintUiHelper;
    private InputMethodManager mInputMethodManager;
    private Stage mStage = Stage.FINGERPRINT;
    private FingerprintManager.CryptoObject mCryptoObject;
    private Context context;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mInputMethodManager = context.getSystemService(InputMethodManager.class);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Do not create a new Fragment when the Activity is re-created such as orientation changes.
        setRetainInstance(true);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // request a window without the title
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
            // The original color was primary, but a transparent background is usually desired for dialogs
            // to show custom corners. If your layout has a solid background, this works fine.
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            Window window = dialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            window.setAttributes(wlp);
        }
    }

    @SuppressLint("WrongConstant")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        // 4. Inflate the layout using View Binding
        binding = DialogFingerPrintBinding.inflate(inflater, container, false);

        // 5. Access views via the binding object. The ButterKnife.bind() call is removed.
        binding.txtAppName.setText(getString(R.string.app_name) + " " + getString(R.string.app_locked));
        binding.iconLock.playAnimation();
        binding.iconLock.setRepeatCount(com.airbnb.lottie.utils.LottieValueAnimator.INFINITE);
        binding.iconLock.setRepeatMode(com.airbnb.lottie.utils.LottieValueAnimator.RESTART);

        initView();

        // 6. Return the root view from the binding object
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 7. Set binding to null to avoid memory leaks
        binding = null;
    }

    private void initView() {
        // 8. Pass the views from the binding object to the helper class
        mFingerprintUiHelper = new FingerprintUiHelper(
                context.getSystemService(FingerprintManager.class),
                binding.iconFingerPrint,
                binding.fingerprintStatus,
                binding.iconLock,
                this,
                context);

        if (!mFingerprintUiHelper.isFingerprintAuthAvailable()) {
            mFingerprintUiHelper.stopListening();
        }
    }

    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Sets the crypto object to be passed in when authenticating with fingerprint.
     */
    public void setCryptoObject(FingerprintManager.CryptoObject cryptoObject) {
        mCryptoObject = cryptoObject;
    }


    /**
     * Proceed the purchase operation
     *
     * @param withFingerprint {@code true} if the purchase was made by using a fingerprint
     * @param mCryptoObject   the Crypto object
     */
    @Override
    public void onAuthenticated() {
        // Callback from FingerprintUiHelper. Let the activity know that authentication was
        // successful.
        fingerPrintCallBack.onPurchased(true /* withFingerprint */, mCryptoObject);
        dismiss();
    }



    @Override
    public void onError(String errString) {
        fingerPrintCallBack.onError(errString);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mStage == Stage.FINGERPRINT) {
            mFingerprintUiHelper.startListening(mCryptoObject);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mFingerprintUiHelper.stopListening();
    }

    public void setCallBack(FingerPrintCallBack fingerPrintCallBack) {
        this.fingerPrintCallBack = fingerPrintCallBack;
    }

    /**
     * Enumeration to indicate which authentication method the user is trying to authenticate with.
     */
    public enum Stage {
        FINGERPRINT,
        NEW_FINGERPRINT_ENROLLED,
        PASSWORD
    }
}
