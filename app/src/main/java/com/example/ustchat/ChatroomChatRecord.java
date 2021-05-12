package com.example.ustchat;

public class ChatroomChatRecord {
    private String name;
    private String content;
    private String time;
    private String quotedContent;
    private boolean isUser;

    public ChatroomChatRecord() {}

    public ChatroomChatRecord(String name, String content, String time, String quotedContent, boolean isUser) {
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getQuotedContent() {
        return quotedContent;
    }

    public void setQuotedContent(String quotedContent) {
        this.quotedContent = quotedContent;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }
}
