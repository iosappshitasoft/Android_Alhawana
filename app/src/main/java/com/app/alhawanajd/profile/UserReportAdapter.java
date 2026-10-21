package com.app.alhawanajd.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.alhawanajd.R;
import com.app.alhawanajd.App;
import com.app.helper.LocaleManager;
import com.app.model.Report;
import com.app.utils.api.ApiCallback;
import com.app.utils.api.CommonApi;

import java.util.ArrayList;
import java.util.List;

public class UserReportAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    private List<Report> reportList = new ArrayList<>();
    private RecyclerView.ViewHolder viewHolder;
    private ApiCallback apiCallback;
    private String profileId;

    public UserReportAdapter(Context context, List<Report> reportList, String profileId, ApiCallback apiCallback) {
        this.context = context;
        this.reportList = reportList;
        this.profileId = profileId;
        this.apiCallback = apiCallback;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_user_report, parent, false);
        viewHolder = new UserReportAdapter.MyViewHolder(itemView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int pos) {
        int position = holder.getAbsoluteAdapterPosition();
        String lang = LocaleManager.getLanguageCode(context);
        if(lang.equalsIgnoreCase("en")){
            ((MyViewHolder) holder).txtReport.setText(reportList.get(position).getName());
        }else {
            ((MyViewHolder) holder).txtReport.setText(reportList.get(position).getArabic_name());
        }
       // ((MyViewHolder) holder).txtReport.setText(reportList.get(position).getTitle());
        ((MyViewHolder) holder).itemReportLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.preventMultipleClick(view);
                CommonApi.reportUser(profileId, reportList.get(position).getTitle(), reportList.get(position).getId(), new ApiCallback() {
                    @Override
                    public void onResponse(Object response) {
                        apiCallback.onResponse(response);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        apiCallback.onFailure(t);
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtReport;
        LinearLayout itemReportLay;

        public MyViewHolder(View view) {
            super(view);
            txtReport = view.findViewById(R.id.txtReport);
            itemReportLay = view.findViewById(R.id.itemReportLay);
        }
    }
}