package com.example.chatapp.Chat;

public class MessageModel {

    private String message;
    private String message_from;
    private String message_id;
    private String message_type;
    private long timestamp;


    public MessageModel() {
    }

    public MessageModel(String message, String message_from, String message_id, String message_type, long timestamp) {
        this.message = message;
        this.message_from = message_from;
        this.message_id = message_id;
        this.message_type = message_type;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage_from() {
        return message_from;
    }

    public void setMessage_from(String message_from) {
        this.message_from = message_from;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getMessage_type() {
        return message_type;
    }

    public void setMessage_type(String message_type) {
        this.message_type = message_type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
