package com.fortunequizking.model;

import com.google.gson.annotations.SerializedName;

public class RankingItem {
    @SerializedName("rank")
    private int rank;
    
    @SerializedName("user_id")
    private int userId;
    
    @SerializedName("username")
    private String username;
    
    @SerializedName("avatar")
    private String avatar;
    
    @SerializedName("score")
    private int score;
    
    @SerializedName("correct_count")
    private int correctCount;
    
    @SerializedName("total_count")
    private int totalCount;
    
    public RankingItem() {}
    
    public RankingItem(int rank, int userId, String username, String avatar, int score, 
                      int correctCount, int totalCount) {
        this.rank = rank;
        this.userId = userId;
        this.username = username;
        this.avatar = avatar;
        this.score = score;
        this.correctCount = correctCount;
        this.totalCount = totalCount;
    }
    
    // Getters
    public int getRank() { return rank; }
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getAvatar() { return avatar; }
    public int getScore() { return score; }
    public int getCorrectCount() { return correctCount; }
    public int getTotalCount() { return totalCount; }
    
    // Setters
    public void setRank(int rank) { this.rank = rank; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public void setScore(int score) { this.score = score; }
    public void setCorrectCount(int correctCount) { this.correctCount = correctCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
}