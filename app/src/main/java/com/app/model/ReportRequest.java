package com.app.model;

import com.google.gson.annotations.SerializedName;

public class ReportRequest {

    @SerializedName("user_id")
    private String userId;
    @SerializedName("report_by")
    private String reportBy;
    @SerializedName("report")
    private String report;
    @SerializedName("report_type")
    private String reportType;
    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("arabic_name")
    private String arabic_name;
    @SerializedName("report_id")
    private String report_id;

    public String getReport_id() {
        return report_id;
    }

    public void setReport_id(String report_id) {
        this.report_id = report_id;
    }

    public String getArabic_name() {
        return arabic_name;
    }

    public void setArabic_name(String arabic_name) {
        this.arabic_name = arabic_name;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReportBy() {
        return reportBy;
    }

    public void setReportBy(String reportBy) {
        this.reportBy = reportBy;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }
}

