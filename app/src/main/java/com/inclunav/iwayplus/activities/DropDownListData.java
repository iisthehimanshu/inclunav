package com.inclunav.iwayplus.activities;

import java.util.ArrayList;

public class DropDownListData {
    private String text1, text2;
    private String time, content;
    private ArrayList<DropDownListData> customPojo =new ArrayList<>();

    public DropDownListData() {
    }

    public String getContent(){return content;}
    //setting content value
    public void setContent(String content){this.content=content;}


    public String getText1() {
        return text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    public String getText2() {
        return text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


}