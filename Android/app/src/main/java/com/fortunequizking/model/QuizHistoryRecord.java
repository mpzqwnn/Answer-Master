package com.fortunequizking.model;

import com.google.gson.annotations.SerializedName;

/**
 * 匹配服务器端getUserAnswerHistory接口返回的数据结构
 */
public class QuizHistoryRecord {
    @SerializedName("index")
    private int index;
    
    @SerializedName("time")
    private String time;
    
    @SerializedName("is_correct")
    private int isCorrect;
    
    public QuizHistoryRecord() {}
    
    public QuizHistoryRecord(int index, String time, int isCorrect) {
        this.index = index;
        this.time = time;
        this.isCorrect = isCorrect;
    }
    
    // Getters
    public int getIndex() { return index; }
    public String getTime() { return time; }
    public int getIsCorrect() { return isCorrect; }
    
    // Setters
    public void setIndex(int index) { this.index = index; }
    public void setTime(String time) { this.time = time; }
    public void setIsCorrect(int isCorrect) { this.isCorrect = isCorrect; }
}