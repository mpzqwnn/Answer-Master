package com.fortunequizking.model;

import com.google.gson.annotations.SerializedName;

public class AnswerHistory {
    @SerializedName("id")
    private int id;
    
    @SerializedName("question_id")
    private int questionId;
    
    @SerializedName("question_content")
    private String questionContent;
    
    @SerializedName("selected_option")
    private String selectedOption;
    
    @SerializedName("is_correct")
    private boolean isCorrect;
    
    @SerializedName("time_spent")
    private int timeSpent;
    
    @SerializedName("category_id")
    private int categoryId;
    
    @SerializedName("category_name")
    private String categoryName;
    
    @SerializedName("createtime")
    private String createdAt;
    
    public AnswerHistory() {}
    
    public AnswerHistory(int id, int questionId, String questionContent, String selectedOption, 
                        boolean isCorrect, int timeSpent, int categoryId, String categoryName, 
                        String createdAt) {
        this.id = id;
        this.questionId = questionId;
        this.questionContent = questionContent;
        this.selectedOption = selectedOption;
        this.isCorrect = isCorrect;
        this.timeSpent = timeSpent;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.createdAt = createdAt;
    }
    
    // Getters
    public int getId() { return id; }
    public int getQuestionId() { return questionId; }
    public String getQuestionContent() { return questionContent; }
    public String getSelectedOption() { return selectedOption; }
    public boolean isCorrect() { return isCorrect; }
    public int getTimeSpent() { return timeSpent; }
    public int getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public String getCreatedAt() { return createdAt; }
    
    // Setters
    public void setId(int id) { this.id = id; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }
    public void setQuestionContent(String questionContent) { this.questionContent = questionContent; }
    public void setSelectedOption(String selectedOption) { this.selectedOption = selectedOption; }
    public void setCorrect(boolean correct) { isCorrect = correct; }
    public void setTimeSpent(int timeSpent) { this.timeSpent = timeSpent; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}