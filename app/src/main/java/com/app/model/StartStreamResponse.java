
package com.app.model;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class StartStreamResponse implements Serializable {

    @Expose
    private String message;
    @Expose
    private String status;

    public String getTaskId() {
        return TaskId;
    }

    public void setTaskId(String taskId) {
        TaskId = taskId;
    }

    @Expose
    private  String TaskId;

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
