package com.fortunequizking.model;

import com.google.gson.annotations.SerializedName;

public class Category {
    @SerializedName("id")
    private int id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("intro")
    private String intro;
    
    @SerializedName("image")
    private String image;
    
    @SerializedName("questions")
    private int questions;
    
    public Category() {}
    
    public Category(int id, String name, String intro, String image, int questions) {
        this.id = id;
        this.name = name;
        this.intro = intro;
        this.image = image;
        this.questions = questions;
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getIntro() {
        return intro;
    }
    
    public String getImage() {
        return image;
    }
    
    public int getQuestions() {
        return questions;
    }
    
    // Setters
    public void setId(int id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setIntro(String intro) {
        this.intro = intro;
    }
    
    public void setImage(String image) {
        this.image = image;
    }
    
    public void setQuestions(int questions) {
        this.questions = questions;
    }
}
