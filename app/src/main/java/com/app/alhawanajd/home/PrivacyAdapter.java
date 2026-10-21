package com.app.alhawanajd.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.app.alhawanajd.R;
import com.app.alhawanajd.home.PrivacyBottomSheetDialog;
import com.app.model.VideoPrivacyModel;
import com.google.android.material.radiobutton.MaterialRadioButton;

import java.util.List;

public class PrivacyAdapter extends RecyclerView.Adapter<PrivacyAdapter.ViewHolder> {

    private final List<VideoPrivacyModel> list;
    private final Context context;
    private final PrivacyBottomSheetDialog.PrivacySelectedListener listener;

    public PrivacyAdapter(Context context, List<VideoPrivacyModel> list,
                          PrivacyBottomSheetDialog.PrivacySelectedListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        MaterialRadioButton checkIcon;

        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.tvPrivacyText);
            checkIcon = view.findViewById(R.id.ivCheck);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_privacy_option, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        VideoPrivacyModel model = list.get(position);
        holder.textView.setText(model.getPrivacy());

        // Show the radio button always
        holder.checkIcon.setVisibility(View.VISIBLE);

        // Mark checked state properly
        holder.checkIcon.setChecked(model.getState());

        holder.itemView.setOnClickListener(v -> {
            for (VideoPrivacyModel item : list) {
                item.setState(false);
            }
            model.setState(true);
            notifyDataSetChanged();
            listener.onSelected(model);
        });
    }



    @Override
    public int getItemCount() {
        return list.size();
    }
}
