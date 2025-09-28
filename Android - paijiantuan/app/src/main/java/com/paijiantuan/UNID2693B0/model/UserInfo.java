package com.paijiantuan.UNID2693B0.model;

import com.google.gson.annotations.SerializedName;

public class UserInfo {
    
    @SerializedName("id")
    private int id;
    
    @SerializedName("username")
    private String username;
    
    @SerializedName("nickname")
    private String nickname;
    
    @SerializedName("avatar")
    private String avatarUrl;
    
    @SerializedName("mobile")
    private String mobile;
    
    @SerializedName("token")
    private String token;
    
    @SerializedName("score")
    private int score;
    
    @SerializedName("level")
    private int level;
    
    @SerializedName("status")
    private String status; // 用户状态，如"normal", "banned"等
    
    @SerializedName("ban_reason")
    private String banReason; // 封禁原因
    
    @SerializedName("ban_expire_date")
    private String banExpireDate; // 封禁截止日期（格式：yyyy-MM-dd HH:mm:ss）
    
    @SerializedName("createtime")
    private long createTime; // 注册时间戳（秒）
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public String getMobile() {
        return mobile;
    }
    
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    // 添加score和level的getter和setter
    public int getScore() {
        return score;
    }
    
    public void setScore(int score) {
        this.score = score;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getBanReason() {
        return banReason;
    }
    
    public void setBanReason(String banReason) {
        this.banReason = banReason;
    }
    
    public String getBanExpireDate() {
        return banExpireDate;
    }
    
    public void setBanExpireDate(String banExpireDate) {
        this.banExpireDate = banExpireDate;
    }
    
    public long getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}