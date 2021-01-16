package com.example.chatapp.Chat;

public class ChatListModel {

    private String userId;
    private String userName;
    private String photoName;
    private String lastMessage;
    private String lastMessageTime;
    private String unReadCount;

    public ChatListModel(String userId, String userName, String photoName, String lastMessage, String lastMessageTime, String unReadCount) {
        this.userId = userId;
        this.userName = userName;
        this.photoName = photoName;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.unReadCount = unReadCount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhotoName() {
        return photoName;
    }

    public void setPhotoName(String photoName) {
        this.photoName = photoName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public String getUnReadCount() {
        return unReadCount;
    }

    public void setUnReadCount(String unReadCount) {
        this.unReadCount = unReadCount;
    }
}
