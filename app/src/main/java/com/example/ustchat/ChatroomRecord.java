package com.example.ustchat;

import com.google.firebase.database.Exclude;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ChatroomRecord {
    private String id;
    private String cat;
    private String title;
    private String posterName;
    private Object createDate;
    private String latestName;
    private String latestReply;
    private List<String> tags;
    private int viewCount;
    private int chatCount;
    private boolean isBookmarked;

    public ChatroomRecord() {}

    public ChatroomRecord(String cat,String title, String posterName, Object createDate,
                          String latestName, String latestReply, List<String> tags,
                          int viewCount, int chatCount, boolean isBookmarked) {
        this.cat = cat;
        this.title = title;
        this.posterName = posterName;
        this.createDate = createDate;
        this.latestName = latestName;
        this.latestReply = latestReply;
        this.tags = tags;
        this.viewCount = viewCount;
        this.chatCount = chatCount;
        this.isBookmarked = isBookmarked;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterName() {
        return posterName;
    }

    public void setPosterName(String posterName) {
        this.posterName = posterName;
    }

    public Object getTimeStamp() {
        return createDate;
    }

    @Exclude
    public String getCreateDate() {
        Date date = new Date((Long) createDate);
        SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy");
        return sfd.format(date);
    }
    public void setCreateDate(Object createDate) {
        this.createDate = createDate;
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public int getChatCount() {
        return chatCount;
    }

    public void setChatCount(int chatCount) {
        this.chatCount = chatCount;
    }
    @Exclude
    public boolean isBookmarked() {  return isBookmarked; }

    public void setBookmarked(boolean bookmarked) { isBookmarked = bookmarked; }

    @Exclude
    public String getId() {return id;}

    public void setId(String id) {this.id = id;}

    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

}
