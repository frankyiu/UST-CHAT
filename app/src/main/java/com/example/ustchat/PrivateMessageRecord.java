package com.example.ustchat;

import com.google.firebase.database.Exclude;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PrivateMessageRecord {
    // To-DO : ChatroomRecord
    private String id;
    private String title;
    private String username;
    private String targetName;
    private String latestName;
    private String latestReply;
    private Object latestReplyTime;
    private int unreadCount;

    public PrivateMessageRecord() {}

    public PrivateMessageRecord(String title, String username, String targetName, String latestName, String latestReply, Object latestReplyTime, int unreadCount) {
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

    public String getUsername() { return username; }

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

    public Object getTimeStamp() {
        return latestReplyTime;
    }

    @Exclude
    public String getLatestReplyTime() {
        Date date = new Date((Long) latestReplyTime);
        SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return sfd.format(date);
    }
    public void setLatestReplyTime(Object latestReplyTime) { this.latestReplyTime = latestReplyTime; }
    @Exclude
    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public void setId(String id) {
        this.id = id;
    }
    @Exclude
    public String getId() {
        return id;
    }
}
