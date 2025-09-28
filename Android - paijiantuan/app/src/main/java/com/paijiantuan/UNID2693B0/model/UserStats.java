package com.paijiantuan.UNID2693B0.model;

import com.google.gson.annotations.SerializedName;

public class UserStats {
    @SerializedName("user_info")
    private UserInfo userInfo;
    
    @SerializedName("stats")
    private Stats stats;
    
    public static class Stats {
        @SerializedName("correct_count")
        private int correctCount;
        
        @SerializedName("total_count")
        private int totalCount;
        
        @SerializedName("total_score")
        private int totalScore;
        
        @SerializedName("correct_rate")
        private double correctRate;
        
        public Stats() {}
        
        public Stats(int correctCount, int totalCount, int totalScore, double correctRate) {
            this.correctCount = correctCount;
            this.totalCount = totalCount;
            this.totalScore = totalScore;
            this.correctRate = correctRate;
        }
        
        // Getters
        public int getCorrectCount() {
            return correctCount;
        }
        
        public int getTotalCount() {
            return totalCount;
        }
        
        public int getTotalScore() {
            return totalScore;
        }
        
        public double getCorrectRate() {
            return correctRate;
        }
        
        // Setters
        public void setCorrectCount(int correctCount) {
            this.correctCount = correctCount;
        }
        
        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }
        
        public void setTotalScore(int totalScore) {
            this.totalScore = totalScore;
        }
        
        public void setCorrectRate(double correctRate) {
            this.correctRate = correctRate;
        }
    }
    
    public UserStats() {}
    
    public UserStats(UserInfo userInfo, Stats stats) {
        this.userInfo = userInfo;
        this.stats = stats;
    }
    
    // Getters
    public UserInfo getUserInfo() {
        return userInfo;
    }
    
    public Stats getStats() {
        return stats;
    }
    
    // Setters
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
    
    public void setStats(Stats stats) {
        this.stats = stats;
    }
}
