package com.inclunav.iwayplus.pdr;

import android.graphics.Point;

import java.util.LinkedList;
import java.util.Queue;

/*
  class which maintains the running average of step location (in pixels) in the canvas.
  It takes average of previous windowSize steps. latestLocation is the updated average location
  NOT USED RIGHT NOW BUT FOR FUTURE PURPOSE
 */

public class StepsAverage {
    private Queue<Point> stepsQueue;
    private int windowSize;
    private Point latestLocation;

    public StepsAverage(int windowSize){
        this.windowSize = windowSize;
        stepsQueue = new LinkedList<>();
    }
    public void add(Point newPoint){
        if(latestLocation==null){
            latestLocation = new Point(newPoint.x, newPoint.y);
            stepsQueue.add(newPoint);
            return;
        }

        if(this.stepsQueue.size() >= windowSize){
            Point evicted = stepsQueue.poll();
            latestLocation.x = latestLocation.x + (newPoint.x - evicted.x)/windowSize;
            latestLocation.y = latestLocation.y + (newPoint.y - evicted.y)/windowSize;
            stepsQueue.add(newPoint);
            return;
        }

        latestLocation.x = (latestLocation.x*stepsQueue.size() + newPoint.x)/(stepsQueue.size()+1);
        latestLocation.y = (latestLocation.y*stepsQueue.size() + newPoint.y)/(stepsQueue.size()+1);
        stepsQueue.add(newPoint);
    }

    public Point getLocation(){
        return latestLocation;
    }

    public void clear(){
        stepsQueue.clear();
        latestLocation = null;
    }
}
