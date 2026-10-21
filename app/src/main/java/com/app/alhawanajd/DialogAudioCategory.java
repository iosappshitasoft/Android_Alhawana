package com.app.alhawanajd;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.app.alhawanajd.R;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.utils.AdminData;
import com.app.utils.Constants;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;

import java.util.ArrayList;
import java.util.List;

public class DialogAudioCategory extends BaseActivity{

    private static final String TAG = DialogAudioCategory.class.getSimpleName();
    private FragmentActivity mActivity;
    private BottomSheetDialog audioDialog;
    private RecyclerView rvAudioCategory;
    private Button btnSelect;
    private TextView txtTitle;

    private AdapterItemClickListener adapterListener;
    private ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
    private String selectedAudio = null;
    private AudioCategoryAdapter adapter;
  //  private List<String> categoryList = new ArrayList<>();
   private List<AudioCategory> categoryList = new ArrayList<>();
    private int selectedPosition = -1;// no selection by default

    public DialogAudioCategory(FragmentActivity mActivity, int pos) {
        this.mActivity = mActivity;
        this.selectedPosition = pos;
    }

    public void openDialog(String selectedAudio, AdapterItemClickListener listener) {
        this.selectedAudio = selectedAudio;
        adapterListener = listener;
        View sheetView = mActivity.getLayoutInflater().inflate(R.layout.bottom_sheet_audio_category, null);
        audioDialog = new BottomSheetDialog(mActivity, R.style.SecondaryBottomSheet); // Style here
        audioDialog.setContentView(sheetView);
        audioDialog.setCanceledOnTouchOutside(true);

        rvAudioCategory = sheetView.findViewById(R.id.rvAudioCategory);
        btnSelect = sheetView.findViewById(R.id.btnSelect);
        txtTitle = sheetView.findViewById(R.id.txtTitle);
        txtTitle.setText(mActivity.getString(R.string.audio_category));
        //   btnBack.setVisibility(View.GONE);
        initCategories();

        /*audioDialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
            @SuppressLint("CutPasteId") View bottomSheet1 = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet1).setState(BottomSheetBehavior.STATE_EXPANDED);
            BottomSheetBehavior.from(bottomSheet1).setSkipCollapsed(true);
            BottomSheetBehavior.from(bottomSheet1).setHideable(true);
        });*/
        audioDialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

            if (bottomSheet != null) {
                bottomSheet.setBackground(null); // Remove white background
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
            }
        });

        audioDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet12 = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet12).setState(BottomSheetBehavior.STATE_COLLAPSED);
                BottomSheetBehavior.from(bottomSheet12).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet12).setHideable(true);
            }
        });

        audioDialog.show();
    }

    private void initCategories() {
        categoryList = new ArrayList<>(AdminData.audioCategory);
        Log.e(TAG,"categoryList : " + categoryList.size());
        if (categoryList.size() > 0 && categoryList.contains(Constants.TAG_MUTED)) {
            categoryList.remove(Constants.TAG_MUTED);
        }
        adapter = new AudioCategoryAdapter(mActivity.getApplicationContext(), selectedAudio, categoryList, selectedPosition, new AdapterItemClickListener() {
            @Override
            public void onItemClicked(Object o, int position, String type) {
                adapterListener.onItemClicked(o, position, type);
                selectedPosition = position;
                if (audioDialog.isShowing())
                    audioDialog.dismiss();
            }
        });
        rvAudioCategory.setLayoutManager(new LinearLayoutManager(mActivity.getApplicationContext()));
        rvAudioCategory.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }
}
