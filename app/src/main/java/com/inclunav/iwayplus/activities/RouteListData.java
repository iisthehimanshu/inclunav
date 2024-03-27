package com.inclunav.iwayplus.activities;

public class RouteListData {
    private String description;
    private String time;
    private int imgId;
    public RouteListData(String description, String time, int imgId) {
        this.description = description;
        this.time = time;
        this.imgId = imgId;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public int getImgId() {
        return imgId;
    }
    public void setImgId(int imgId) {
        this.imgId = imgId;
    }
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}