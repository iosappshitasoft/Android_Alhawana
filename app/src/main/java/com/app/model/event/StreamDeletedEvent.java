package com.app.model.event;

public class StreamDeletedEvent {
    private int selectedPosition;

    public StreamDeletedEvent(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    public int getDeletedPosition() {
        return selectedPosition;
    }

    public void setDeletedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }
}
