package com.app.alhawanajd.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.alhawanajd.R;
import com.app.model.VideoPrivacyModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

public class PrivacyBottomSheetDialog {

    private final BottomSheetDialog dialog;
    private final List<VideoPrivacyModel> dataList;
    private final Context context;

    public interface PrivacySelectedListener {
        void onSelected(VideoPrivacyModel selectedModel);
    }

    public PrivacyBottomSheetDialog(Context context, List<VideoPrivacyModel> list, PrivacySelectedListener listener) {
        this.context = context;
        this.dataList = list;

        View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_privacy, null);
        dialog = new BottomSheetDialog(context, R.style.SecondaryBottomSheet);
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(true);


        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            View bottomSheetInternal = bottomSheetDialog.findViewById(
                    com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheetInternal != null) {
                bottomSheetInternal.setBackground(null);
            }
        });

        RecyclerView recycler = view.findViewById(R.id.recyclerPrivacy);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        tvTitle.setText(context.getString(R.string.who_can_view));

        recycler.setLayoutManager(new LinearLayoutManager(context));
        PrivacyAdapter adapter = new PrivacyAdapter(context, list, selectedModel -> {
            listener.onSelected(selectedModel);
            dialog.dismiss();
        });
        recycler.setAdapter(adapter);
    }


    public void show() {
        dialog.show();
    }
}

