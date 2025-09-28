package com.paijiantuan.UNID2693B0.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class Question {
    @SerializedName("id")
    private int id;
    
    @SerializedName("content")
    private String content;
    
    @SerializedName("type")
    private String type;
    
    @SerializedName("difficulty")
    private String difficulty;
    
    @SerializedName("category_id")
    private int categoryId;
    
    @SerializedName("category_name")
    private String categoryName;
    
    @SerializedName("options")
    private Map<String, String> options;
    
    @SerializedName("correct_answer")
    private String correctAnswer;
    
    @SerializedName("explanation")
    private String explanation;
    
    @SerializedName("createtime")
    private String createdAt;
    
    @SerializedName("updated_at")
    private String updatedAt;
    
    // 兼容旧版本的字段
    private int level;
    private String questionText;
    private String option1;
    private String option2;
    
    public Question() {}
    
    // 新版本构造函数
    public Question(int id, String content, String type, String difficulty, int categoryId, 
                   String categoryName, Map<String, String> options, String correctAnswer, 
                   String explanation, String createdAt, String updatedAt) {
        this.id = id;
        this.content = content;
        this.type = type;
        this.difficulty = difficulty;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // 旧版本构造函数（保持兼容性）
    public Question(int level, String questionText, String option1, String option2, String correctAnswer) {
        this.level = level;
        this.questionText = questionText;
        this.option1 = option1;
        this.option2 = option2;
        this.correctAnswer = correctAnswer;
        this.content = questionText; // 兼容新版本
    }
    
    // 新版本getter方法
    public int getId() {
        return id;
    }
    
    public String getContent() {
        return content;
    }
    
    public String getType() {
        return type;
    }
    
    public String getDifficulty() {
        return difficulty;
    }
    
    public int getCategoryId() {
        return categoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public Map<String, String> getOptions() {
        return options;
    }
    
    public String getCorrectAnswer() {
        return correctAnswer;
    }
    
    public String getExplanation() {
        return explanation;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    // 旧版本getter方法（保持兼容性）
    public int getLevel() {
        return level;
    }
    
    public String getQuestionText() {
        return questionText != null ? questionText : content;
    }
    
    public String getOption1() {
        return option1;
    }
    
    public String getOption2() {
        return option2;
    }
    
    // Setter方法
    public void setId(int id) {
        this.id = id;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
    
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public void setOptions(Map<String, String> options) {
        this.options = options;
    }
    
    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
    
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }
    
    public void setOption1(String option1) {
        this.option1 = option1;
    }
    
    public void setOption2(String option2) {
        this.option2 = option2;
    }
}