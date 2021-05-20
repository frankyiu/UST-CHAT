package com.example.ustchat;

public class PrivateMessageRecord {
    // To-DO : ChatroomRecord
    private String title;
    private String username;
    private String targetName;
    private String latestName;
    private String latestReply;
    private String latestReplyTime;
    private int unreadCount;

    public PrivateMessageRecord() {
    }

    public PrivateMessageRecord(String title, String username, String targetName, String latestName, String latestReply, String latestReplyTime, int unreadCount) {
        this.title = title;
        this.username = username;
        this.targetName = targetName;
        this.latestName = latestName;
        this.latestReply = latestReply;
        this.latestReplyTime = latestReplyTime;
        this.unreadCount = unreadCount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getLatestName() {
        return latestName;
    }

    public void setLatestName(String latestName) {
        this.latestName = latestName;
    }

    public String getLatestReply() {
        return latestReply;
    }

    public void setLatestReply(String latestReply) {
        this.latestReply = latestReply;
    }

    public String getLatestReplyTime() {
        return latestReplyTime;
    }

    public void setLatestReplyTime(String latestReplyTime) {
        this.latestReplyTime = latestReplyTime;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }


}
