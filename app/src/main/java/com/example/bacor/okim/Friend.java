package com.example.bacor.okim;

/**
 * 好友列表
 */

public class Friend {
    private static String userId;
    private static String friend;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        Friend.userId = userId;
    }

    public String getFriend() {
        return friend;
    }

    public void setFriend(String friend) {
        Friend.friend = friend;
    }
}
