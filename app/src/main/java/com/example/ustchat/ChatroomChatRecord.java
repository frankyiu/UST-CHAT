package com.example.ustchat;

import com.google.firebase.database.Exclude;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatroomChatRecord {
    private String name;
    private String content;
    private Object time;
    private String quotedContent;
    private boolean isUser;

    public ChatroomChatRecord() {}

    public ChatroomChatRecord(String name, String content, Object time, String quotedContent, boolean isUser) {
        this.name = name;
        this.content = content;
        this.time = time;
        this.quotedContent = quotedContent;
        this.isUser = isUser;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Object getTimeStamp(){
        return time;
    }
    @Exclude
    public String getTime() {
        Date date = new Date((Long) time);
        SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return sfd.format(date);
    }

    public void setTime(Object time) {
        this.time = time;
    }

    public String getQuotedContent() {
        return quotedContent;
    }

    public void setQuotedContent(String quotedContent) {
        this.quotedContent = quotedContent;
    }

    @Exclude
    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }
}
