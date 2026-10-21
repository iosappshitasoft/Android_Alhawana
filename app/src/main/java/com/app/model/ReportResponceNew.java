package com.app.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReportResponceNew {

    @SerializedName("categories")
    List<Categories> categories;


    public void setCategories(List<Categories> categories) {
        this.categories = categories;
    }
    public List<Categories> getCategories() {
        return categories;
    }


    public static class Subcategories {

        @SerializedName("id")
        String id;

        @SerializedName("title")
        String title;

        @SerializedName("reasons")
        String reasons;

        @SerializedName("subcategoryCount")
        int subcategoryCount;

        @SerializedName("textbox")
        String textbox;

        @SerializedName("personsearch")
        String personSearch;

        @SerializedName("its_link")
        String itsLink;

        @SerializedName("subcategories")
        List<Subcategories> subcategories2;

        public String getItsLink() {
            return itsLink;
        }

        public void setItsLink(String itsLink) {
            this.itsLink = itsLink;
        }

        public void setId(String id) {
            this.id = id;
        }
        public String getId() {
            return id;
        }

        public void setTitle(String title) {
            this.title = title;
        }
        public String getTitle() {
            return title;
        }

        public void setReasons(String reasons) {
            this.reasons = reasons;
        }
        public String getReasons() {
            return reasons;
        }

        public int getSubcategoryCount() {
            return subcategoryCount;
        }

        public void setSubcategoryCount(int subcategoryCount) {
            this.subcategoryCount = subcategoryCount;
        }

        public String getTextbox() {
            return textbox;
        }

        public void setTextbox(String textbox) {
            this.textbox = textbox;
        }

        public String getPersonSearch() {
            return personSearch;
        }

        public void setPersonSearch(String personSearch) {
            this.personSearch = personSearch;
        }

        public List<Subcategories> getSubcategories2() {
            return subcategories2;
        }

        public void setSubcategories2(List<Subcategories> subcategories2) {
            this.subcategories2 = subcategories2;
        }
    }


    public static class Categories {

        @SerializedName("id")
        String id;

        @SerializedName("title")
        String title;

        @SerializedName("reasons")
        String reasons;

        @SerializedName("subcategoryCount")
        int subcategoryCount;

        @SerializedName("textbox")
        String textbox;

        @SerializedName("personsearch")
        String personSearch;

        @SerializedName("subcategories")
        List<Subcategories> subcategories;





        public void setId(String id) {
            this.id = id;
        }
        public String getId() {
            return id;
        }

        public void setTitle(String title) {
            this.title = title;
        }
        public String getTitle() {
            return title;
        }

        public void setSubcategories(List<Subcategories> subcategories) {
            this.subcategories = subcategories;
        }
        public List<Subcategories> getSubcategories() {
            return subcategories;
        }

        public String getReasons() {
            return reasons;
        }

        public void setReasons(String reasons) {
            this.reasons = reasons;
        }

        public int getSubcategoryCount() {
            return subcategoryCount;
        }

        public void setSubcategoryCount(int subcategoryCount) {
            this.subcategoryCount = subcategoryCount;
        }

        public String getTextbox() {
            return textbox;
        }

        public void setTextbox(String textbox) {
            this.textbox = textbox;
        }

        public String getPersonSearch() {
            return personSearch;
        }

        public void setPersonSearch(String personSearch) {
            this.personSearch = personSearch;
        }
    }



    public static class Reasons {

        @SerializedName("id")
        int id;

        @SerializedName("description")
        String description;


        public void setId(int id) {
            this.id = id;
        }
        public int getId() {
            return id;
        }

        public void setDescription(String description) {
            this.description = description;
        }
        public String getDescription() {
            return description;
        }

    }

}



