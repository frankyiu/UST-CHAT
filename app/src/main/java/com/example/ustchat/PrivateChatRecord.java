package com.example.ustchat;

public class PrivateChatRecord {
    private String text;
    private String image;
    private String time;
    private String quotedID;
    private boolean isUser;

    public PrivateChatRecord() {}

    public PrivateChatRecord(String text, String image, String time, String quotedID, boolean isUser) {
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
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
}
