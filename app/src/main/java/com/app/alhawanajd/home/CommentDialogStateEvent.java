package com.app.alhawanajd.home;

public final class CommentDialogStateEvent {
    private final String commentCount; // or int

    public CommentDialogStateEvent(String commentCount) {
        this.commentCount = commentCount;
    }
    public String getCommentCount() {
        return commentCount;
    }
    @Override public String toString() {
        return "CommentDialogStateEvent{commentCount=" + commentCount + "}";
    }
}