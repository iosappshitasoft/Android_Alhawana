package com.app.data.model;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class Tagged implements Serializable {

    private ArrayList<String> taggedIdsList;
    private ArrayList<String> taggedNameList;

    public Tagged() {
        this.taggedIdsList = new ArrayList<>();
        this.taggedNameList = new ArrayList<>();
    }

    public void addTaggedEntry(@NonNull String id,
                               @NonNull String nameForId) {
            this.taggedIdsList.add(id);
            this.taggedNameList.add(nameForId);
    }

    public ArrayList<String> getTaggedIdsList() {
        return taggedIdsList;
    }

    public void setTaggedIdsList(ArrayList<String> taggedIdsList) {
        this.taggedIdsList = taggedIdsList;
    }

    public ArrayList<String> getTaggedNameList() {
        return taggedNameList;
    }

    public String getCommaSeparatedTaggedNameList() {
        final Iterator<?> it = taggedNameList.iterator();
        if (!it.hasNext()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(it.next());
        while (it.hasNext()) {
            sb.append(",");
            sb.append(it.next());
        }
        return sb.toString();
    }

    public String getCommaSeparatedTaggedIdList() {
        final Iterator<?> it = taggedIdsList.iterator();
        if (!it.hasNext()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(it.next());
        while (it.hasNext()) {
            sb.append(",");
            sb.append(it.next());
        }
        return sb.toString();
    }

    public void setTaggedNameList(ArrayList<String> taggedNameList) {
        this.taggedNameList = taggedNameList;
    }
}