package com.app.alhawanajd.Video;


import static androidx.databinding.DataBindingUtil.setContentView;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.app.alhawanajd.R;
import com.app.utils.Constants;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.radiobutton.MaterialRadioButton;


public class PrivacyBottomSheet extends BottomSheetDialogFragment {


    private final String currentSelection;
    private final String title;
    private final OnPrivacySelectedListener listener;


    public interface OnPrivacySelectedListener {
        void onSelected(String value);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);


        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;


            View bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                // Set transparent background so your custom drawable shows
                bottomSheet.setBackground(null);
            }
        });


        return dialog;
    }


    public PrivacyBottomSheet(String title, String currentSelection, OnPrivacySelectedListener listener) {
        this.title = title;
        this.currentSelection = currentSelection;
        this.listener = listener;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottomsheet_privacy_options, container, false);


        // Title
        TextView titleView = view.findViewById(R.id.bottomSheetTitle);
        titleView.setText(title);


        // Text options
        View optionEveryoneLay = view.findViewById(R.id.optionEveryoneLay);
        View optionFansLay = view.findViewById(R.id.optionFansLay);
        View optionOffLay = view.findViewById(R.id.optionOffLay);


        // Radio buttons
        MaterialRadioButton rbEveryone = view.findViewById(R.id.optionEveryoneradio);
        MaterialRadioButton rbFans = view.findViewById(R.id.optionFansradio);
        MaterialRadioButton rbOff = view.findViewById(R.id.optionOffradio);


        // Set last selected option
        rbEveryone.setChecked(Constants.TAG_EVERYONE.equals(currentSelection));
        rbFans.setChecked(Constants.TAG_FANS.equals(currentSelection));
        rbOff.setChecked(Constants.TAG_OFF.equals(currentSelection));


        // Common click handler
        View.OnClickListener clickListener = v -> {
            String selected = "";
            if (v.getId() == R.id.optionEveryoneLay || v.getId() == R.id.optionEveryoneradio) {
                selected = Constants.TAG_EVERYONE;
            } else if (v.getId() == R.id.optionFansLay || v.getId() == R.id.optionFansradio) {
                selected = Constants.TAG_FANS;
            } else if (v.getId() == R.id.optionOffLay || v.getId() == R.id.optionOffradio) {
                selected = Constants.TAG_OFF;
            }


            // Update radios manually
            rbEveryone.setChecked(Constants.TAG_EVERYONE.equals(selected));
            rbFans.setChecked(Constants.TAG_FANS.equals(selected));
            rbOff.setChecked(Constants.TAG_OFF.equals(selected));


            if (listener != null) listener.onSelected(selected);
            dismiss();
        };


        // Set listeners
        optionEveryoneLay.setOnClickListener(clickListener);
        optionFansLay.setOnClickListener(clickListener);
        optionOffLay.setOnClickListener(clickListener);
        rbEveryone.setOnClickListener(clickListener);
        rbFans.setOnClickListener(clickListener);
        rbOff.setOnClickListener(clickListener);


        return view;
    }
}


