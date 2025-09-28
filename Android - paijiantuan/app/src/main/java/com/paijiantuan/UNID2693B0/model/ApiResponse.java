package com.paijiantuan.UNID2693B0.model;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    
    @SerializedName("code")
    private int code;
    
    @SerializedName("msg")
    private String message;
    
    @SerializedName("time")
    private long timestamp;
    
    @SerializedName("data")
    private T data;
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    /**
     * 判断请求是否成功
     */
    public boolean isSuccess() {
        return code == 1;
    }
}