
package com.app.model;

import com.google.gson.annotations.SerializedName;

public class Report {

    @SerializedName("title")
    private String title;

    @SerializedName("arabic_title")
    private String arabic_title;

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    public String getArabic_name() {
        return arabic_name;
    }

    public void setArabic_name(String arabic_name) {
        this.arabic_name = arabic_name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getArabic_title() {
        return arabic_title;
    }

    public void setArabic_title(String arabic_title) {
        this.arabic_title = arabic_title;
    }

    @SerializedName("arabic_name")
    private String arabic_name;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


}
