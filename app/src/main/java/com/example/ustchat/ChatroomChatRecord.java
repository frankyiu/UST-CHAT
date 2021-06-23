package com.example.ustchat;

import com.google.firebase.database.Exclude;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatroomChatRecord {
    private String id;
    private String name;
    private String text;
    private String image;
    private Object time;
    private String quotedID;
    private boolean isUser;

    public ChatroomChatRecord() {
    }

    public ChatroomChatRecord(String name, String text, String image, Object time, String quotedID, boolean isUser) {
        this.name = name;
        this.text = text;
        this.image = image;
        this.time = time;
        this.quotedID = quotedID;
        this.isUser = isUser;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public String getQuotedID() {
        return quotedID;
    }

    public void setQuotedID(String quotedID) {
        this.quotedID = quotedID;
    }

    @Exclude
    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Exclude
    public String getId() {
        return id;
    }
}
