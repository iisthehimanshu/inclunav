package com.inclunav.iwayplus.pdr;

public class Node {
    private int gridX;
    private int gridY;
    String name;
    private String floor;
    Node next;

    // Constructor
    public Node(int x, int y, String n, String floor_name)
    {
        gridX = x;
        gridY = y;
        name = n;
        floor = floor_name;
        next = null;
    }

    public int getGridX(){
        return gridX;
    }

    public int getGridY(){
        return gridY;
    }

    public String name(){
        return name;
    }

    public String getFloor(){
        return floor;
    }

    public void setGridX(int xval){
        this.gridX = xval;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public void setGridY(int yval){
        this.gridY = yval;
    }
}
