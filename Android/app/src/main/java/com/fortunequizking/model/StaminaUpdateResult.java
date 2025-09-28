package com.fortunequizking.model;

import com.google.gson.annotations.SerializedName;

public class StaminaUpdateResult {
    
    @SerializedName("stamina")
    private int stamina;
    
    @SerializedName("change")
    private String change; // 注意：后端返回的是字符串类型
    
    public int getStamina() {
        return stamina;
    }
    
    public void setStamina(int stamina) {
        this.stamina = stamina;
    }
    
    public String getChange() {
        return change;
    }
    
    public void setChange(String change) {
        this.change = change;
    }
}