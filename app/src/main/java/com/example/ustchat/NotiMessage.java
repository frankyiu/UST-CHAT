package com.example.ustchat;

class NotiMessage{
    private String from;
    private String content;
    private String title;

    public NotiMessage(){}
    public NotiMessage(String from, String content, String title) {
        this.from = from;
        this.content = content;
        this.title = title;
    }

    public String getFrom() {
        return from;
    }

    public String getContent() {
        return content;
    }

    public String getTitle() {
        return title;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
