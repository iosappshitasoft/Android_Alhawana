package com.app.alhawanajd;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.alhawanajd.R;
import com.app.helper.LocaleManager;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.utils.Constants;

import java.util.List;

public class AudioCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    private final List<AudioCategory> categoryList;
  //  private final List<String> categoryList;
    private RecyclerView.ViewHolder viewHolder;
    private String selectedAudio = null;
    private AdapterItemClickListener adapterListener;

    private int selectedPosition = -1;// no selection by default

    public AudioCategoryAdapter(Context context, String selectedAudio, List<AudioCategory> categoryList, /*List<String> categoryList ,*/ int selectPos, AdapterItemClickListener adapterListener) {
        this.context = context;
        this.categoryList = categoryList;
        this.adapterListener = adapterListener;
        this.selectedAudio = selectedAudio;
        this.selectedPosition = selectPos;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_audio_category, parent, false);
        viewHolder = new MyViewHolder(itemView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int pos) {
        int position = viewHolder.getAbsoluteAdapterPosition();
        if (viewHolder instanceof MyViewHolder) {
            MyViewHolder holder = (MyViewHolder) viewHolder;
         //   holder.txtCategory.setText(categoryList.get(position));
          //  holder.txtCategory.setText(categoryList.get(position).getName());
            String name = categoryList.get(position).getName();
            String arabicName = categoryList.get(position).getArabicName();
            String lang = LocaleManager.getLanguageCode(context);
            Log.e("AudioCategoryAdapter", lang +"test");

            if (lang.equals("en")){
                holder.txtCategory.setText(name);
            }else{
                holder.txtCategory.setText(arabicName);
            }


            holder.btnSelect.setChecked(selectedAudio != null && selectedAudio.equals(holder.txtCategory.getText().toString()));
            holder.btnSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.itemLay.performClick();
                }
            });
            holder.itemLay.setOnClickListener(v -> {
                selectedAudio = holder.txtCategory.getText().toString();
                selectedPosition = position;
                notifyDataSetChanged();
                adapterListener.onItemClicked(categoryList.get(position), position, Constants.TAG_AUDIO);
//                adapterListener.onItemClicked(categoryList.get(position).getId(), position, Constants.TAG_AUDIO);
            });

        }
    }

    public String getSelectedCategory() {
        return selectedAudio;
    }

    public void setSelectedCategory(String selectedCategory) {
        this.selectedAudio = selectedCategory;
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView txtCategory;
        private RelativeLayout itemLay;
        private RadioButton btnSelect;

        public MyViewHolder(View view) {
            super(view);
            txtCategory = view.findViewById(R.id.txtCategory);
            itemLay = view.findViewById(R.id.itemLay);
            btnSelect = view.findViewById(R.id.btnSelect);
        }

    }
}