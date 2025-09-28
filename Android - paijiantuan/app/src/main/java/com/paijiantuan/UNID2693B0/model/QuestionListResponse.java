package com.paijiantuan.UNID2693B0.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * 用于处理题目列表API返回的数据结构
 * API返回的数据格式: {"code":1,"msg":"获取题目列表成功","time":"1756088403","data":{"questions":[],"total":0,"page":"1","limit":"20"}}
 */
public class QuestionListResponse {
    
    @SerializedName("questions")
    private List<Question> questions;
    
    @SerializedName("total")
    private int total;
    
    @SerializedName("page")
    private String page;
    
    @SerializedName("limit")
    private String limit;
    
    // Getter and Setter methods
    public List<Question> getQuestions() {
        return questions;
    }
    
    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
    
    public int getTotal() {
        return total;
    }
    
    public void setTotal(int total) {
        this.total = total;
    }
    
    public String getPage() {
        return page;
    }
    
    public void setPage(String page) {
        this.page = page;
    }
    
    public String getLimit() {
        return limit;
    }
    
    public void setLimit(String limit) {
        this.limit = limit;
    }
}