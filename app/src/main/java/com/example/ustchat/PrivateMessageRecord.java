package com.example.ustchat;

public class PrivateMessageRecord {
    private String title;
    private String latestName;
    private String latestReply;
    private String latestReplyTime;
    private int unreadCount;

    public PrivateMessageRecord() {}

    public PrivateMessageRecord(String title, String latestName, String latestReply, String latestReplyTime, int unreadCount) {
        this.title = title;
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
