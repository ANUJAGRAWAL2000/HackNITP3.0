package com.example.chatapp.RequestReceived;

public class RequestReceived {

    private String UserId;
    private String UserName;
    private String PhotoName;

    public RequestReceived(String userId, String userName, String photoName) {
        UserId = userId;
        UserName = userName;
        PhotoName = photoName;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getPhotoName() {
        return PhotoName;
    }

    public void setPhotoName(String photoName) {
        PhotoName = photoName;
    }
}
