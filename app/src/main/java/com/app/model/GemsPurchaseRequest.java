package com.app.model;

import com.google.gson.annotations.SerializedName;

public class GemsPurchaseRequest {
    @SerializedName("type_of_purchase")
    private String purchase_type;

    @SerializedName("sdk_token")
    private String sdk_token;

    @SerializedName("time_zone")
    private String time_zone;
    @SerializedName("purchaseToken")
    private String purchaseToken;
    @SerializedName("user_id")
    private String userId;
    @SerializedName("gem_id")
    private String gemId;
    @SerializedName("paid_amount")
    private String paidAmount;
    @SerializedName("transaction_id")
    private String transactionId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGemId() {
        return gemId;
    }

    public void setGemId(String gemId) {
        this.gemId = gemId;
    }

    public String getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(String paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTime_zone() {
        return time_zone;
    }

    public void setTime_zone(String time_zone) {
        this.time_zone = time_zone;
    }

    public String getSdk_token() {
        return sdk_token;
    }

    public void setSdk_token(String sdk_token) {
        this.sdk_token = sdk_token;
    }

    public String getPurchaseToken() {
        return purchaseToken;
    }

    public void setPurchaseToken(String purchaseToken) {
        this.purchaseToken = purchaseToken;
    }
    public String getPurchase_type() {
        return purchase_type;
    }

    public void setPurchase_type(String purchase_type) {
        this.purchase_type = purchase_type;
    }

}
