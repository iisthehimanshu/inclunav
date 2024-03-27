package com.inclunav.iwayplus.layout_utilities;

public class SliderItem {
    private int width;
    private int height;
    private String imageURL;
    private String floor;

    public SliderItem(int w, int h, String imgurl,String floorName){
        this.width = w;
        this.height = h;
        this.imageURL = imgurl;
        this.floor = floorName;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getFloor() {
        return floor;
    }
}
