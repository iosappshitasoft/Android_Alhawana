package com.app.Zego.Model;


import com.app.addon.livestream.external.model.StreamDetails;

public class PKUserStreamModel {

    StreamDetails model;
    int sort ;

    public PKUserStreamModel(StreamDetails model, int sort) {
        this.model = model;
        this.sort = sort;
    }


    public StreamDetails getModel() {
        return model;
    }

    public void setModel(StreamDetails model) {
        this.model = model;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }
}
