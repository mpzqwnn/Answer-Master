package com.paijiantuan.UNID2693B0.model;

public class User {
    private String userId;
    private String nickname;
    private String avatarUrl;
    private int todayQuizCount;
    private int historyQuizCount;
    private String registerTime;
    
    // 构造方法、getter和setter方法
    public User(String userId, String nickname, String registerTime) {
        this.userId = userId;
        this.nickname = nickname;
        this.registerTime = registerTime;
        this.todayQuizCount = 0;
        this.historyQuizCount = 0;
    }
    
    // getter方法
    public String getUserId() {
        return userId;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public int getTodayQuizCount() {
        return todayQuizCount;
    }
    
    public int getHistoryQuizCount() {
        return historyQuizCount;
    }
    
    public String getRegisterTime() {
        return registerTime;
    }
    
    // setter方法
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public void incrementTodayQuizCount() {
        this.todayQuizCount++;
    }
    
    public void incrementHistoryQuizCount() {
        this.historyQuizCount++;
    }
}