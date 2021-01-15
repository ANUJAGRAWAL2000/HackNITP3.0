package com.example.chatapp.FriendRequest;

public class FriendRequest{

    private String UserName;
    private String PhotoName;
    private String UserId;
    private boolean requestSent;

    public FriendRequest(String userName, String photoName, String userId, boolean requestSent) {
        UserName = userName;
        PhotoName = photoName;
        UserId = userId;
        this.requestSent = requestSent;
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

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public boolean isRequestSent() {
        return requestSent;
    }

    public void setRequestSent(boolean requestSent) {
        this.requestSent = requestSent;
    }
}
