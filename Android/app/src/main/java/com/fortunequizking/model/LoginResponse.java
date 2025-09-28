package com.fortunequizking.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    
    @SerializedName("userinfo")
    private UserInfo userInfo;
    
    // 如果需要，还可以添加其他可能在data中返回的字段
    
    public UserInfo getUserInfo() {
        return userInfo;
    }
    
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
}