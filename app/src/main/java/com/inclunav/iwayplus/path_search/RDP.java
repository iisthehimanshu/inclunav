package com.inclunav.iwayplus.path_search;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

public class RDP {
    private static double distance(Point a, Point b){
        return Math.sqrt(Math.pow(a.y-b.x, 2) + Math.pow(a.y-b.x,2));
    }

    private static double point_line_distance(Point point,
                                       Point start,Point end){
        if( start.equals(end)){
            return distance(point, start);
        }
        else{
            double n = Math.abs((end.x-start.x)*(start.y-point.y) - (start.x-point.x)*(end.y-start.y));
            double d = Math.sqrt(Math.pow(end.x-start.x,2) + Math.pow(end.y-start.y,2));
            return n/d;
        }
    }

    private static ArrayList<Point>  rdp(List<Point> points, double epsilon){
        double dmax = 0.0;
        int index = 0;
        for(int i=0; i<points.size()-1; i++){
            double d = point_line_distance(points.get(i),points.get(0),points.get(points.size()-1));
            if(d>dmax){
                index = i;
                dmax = d;
            }
        }

        ArrayList<Point> results = new ArrayList<>();
        if(points.size()<3){
            return new ArrayList<>(points);
        }

        if(dmax >= epsilon){
            List<Point> temp1 = rdp(points.subList(0,index+1),epsilon);
            temp1 = temp1.subList(0,temp1.size()-1);
            List<Point> temp2 = rdp(points.subList(index,points.size()),epsilon);
            results.addAll(temp1);
            results.addAll(temp2);
        }
        else{
            results.add(points.get(0));
            results.add(points.get(points.size()-1));
        }
        return results;
    }

    public static ArrayList<Point> getSimplifiedPath(ArrayList<Point> originalPath, double tolerance){
        //tolerance is in feet in our case
        return rdp(originalPath,tolerance);
    }
}
