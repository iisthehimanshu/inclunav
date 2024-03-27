package com.inclunav.iwayplus.path_search;

import android.util.Log;

import com.inclunav.iwayplus.pdr.FloorObj;

import java.util.ArrayList;

//for every connector point of source floor we provide path option to the user
// i.e. we search path from all the connector vertexes to destination
// then join the source vertex to these connector vertexes
// each pathOption will have distance of path and if user clicks on that pathOption
// he will be shown that path


public class PathOption {
    private String passingFrom;
    private ArrayList<FloorObj> path;
    private double pathDistance;

    public PathOption(String passingFrom, ArrayList<FloorObj> path){
        this.passingFrom = passingFrom;
        this.path = path;
    }

    public void setPathDistance(double pathDistance) {
        this.pathDistance = pathDistance;
    }

    public double getPathDistance() {
        Log.d("getpd", ""+pathDistance);
        return pathDistance;
    }

    public ArrayList<FloorObj> getPath() {
        return path;
    }

    public String getPassingFrom() {
        return passingFrom;
    }
}

