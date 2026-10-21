
package com.app.addon.livestream.external.model;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class StartStreamResponse implements Serializable {

    @Expose
    private String message;
    @Expose
    private String status;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
