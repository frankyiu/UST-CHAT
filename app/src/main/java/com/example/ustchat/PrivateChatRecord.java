package com.example.ustchat;

import com.google.firebase.database.Exclude;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PrivateChatRecord {
    private String id;
    private String name;
    private String text;
    private String image;
    private Object time;
    private String quotedID;
    private boolean isUser;

    public PrivateChatRecord() {
    }

    public PrivateChatRecord(String name, String text, String image, Object time, String quotedID, boolean isUser) {
        this.name = name;
        this.text = text;
        this.image = image;
        this.time = time;
        this.quotedID = quotedID;
        this.isUser = isUser;
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

    public Object getTimeStamp() {return time;}

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

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
