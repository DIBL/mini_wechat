package com.Elessar.app.server;

/**
 * Created by Hans on 1/27/19.
 */
public class Message {
    public static final String FROM_USER = "fromUser", TO_USER = "toUser", TEXT = "text", TIMESTAMP = "timestamp", ISREAD = "isRead";
    private final String fromUser, toUser, text;
    private final Long timestamp;
    private final Boolean isRead;
    public Message (String fromUser, String toUser, String text, Long timestamp, Boolean isRead) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.text = text;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    public String getFromUser() {
        return fromUser;
    }

    public String getToUser() {
        return toUser;
    }

    public String getText() {
        return text;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Boolean getIsRead() {
        return isRead;
    }

}
