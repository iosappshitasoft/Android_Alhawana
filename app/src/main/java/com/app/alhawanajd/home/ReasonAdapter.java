package com.app.alhawanajd.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.alhawanajd.App;
import com.app.alhawanajd.R;
import com.app.model.Report;
import com.app.utils.api.ApiCallback;
import com.app.utils.api.CommonApi;
import com.app.utils.api.ReportApiCallback;

import java.util.List;

public class ReasonAdapter extends RecyclerView.Adapter {
    private final Context context;
    private final List<Report> reportList;
    private RecyclerView.ViewHolder viewHolder;
    private String videoId = null;
    private ReportApiCallback apiCallback;
    private int adapterPosition;

    public ReasonAdapter(Context context, List<Report> reportList, String videoId, int adapterPosition, ReportApiCallback apiCallback) {
        this.context = context;
        this.reportList = reportList;
        this.videoId = videoId;
        this.apiCallback = apiCallback;
        this.adapterPosition = adapterPosition;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_report_reason, parent, false);
        viewHolder = new MyViewHolder(itemView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int pos) {
        int position = holder.getAbsoluteAdapterPosition();
        ((MyViewHolder) holder).txtReport.setText(reportList.get(position).getTitle());
        ((MyViewHolder) holder).itemReportLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.preventMultipleClick(view);
                CommonApi.reportVideo(videoId, reportList.get(position).getTitle(), true, new ApiCallback() {
                    @Override
                    public void onResponse(Object response) {
                        apiCallback.onResponse(response, adapterPosition);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        apiCallback.onFailure(t, adapterPosition);
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    @SuppressLint("NonConstantResourceId")
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtReport;
        LinearLayout itemReportLay;

        public MyViewHolder(View view) {
            super(view);
            txtReport = view.findViewById(R.id.txtReport);
            itemReportLay = view.findViewById(R.id.itemReportLay);
        }

    }
}