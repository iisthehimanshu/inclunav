package com.inclunav.iwayplus.path_search;

import android.graphics.Point;
import android.util.Log;


import com.inclunav.iwayplus.Utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class PathSearcher {

    private int M; //floor breadth i.e. rows
    private int N; //floor length i.e. cols
    private int[][] mat; //matrix of 0s and 1, where 0 indicates walkable and 1 indicates non walkable
    private double lowestDist;
    private ArrayList<ArrayList<Point>> simplePolys = new ArrayList<>(); //stores polygons clicked points list

    // Below arrays details all 8 possible movements from a cell
    private final int[] row = { -1, 0, 0, 1 };
    private final int[] col = { 0, -1, 1, 0}; //left,right,up,down
    private final int[] row2 = { -1, -1, 1, 1 };
    private final int[] col2 = { -1, 1, -1, 1 };// diagonals

    private Map<Integer, Integer> coordWithBoundaryDist = new HashMap<>();


    //variables used by functions in this class
    private Point q11, q12, q21, q22, q31, q32, q41, q42;
    private double X, Y, s, c, xnew, ynew, amountPerRotation, totalRotation;

    class Qelem
    {
        // (x, y) represents matrix cell coordinates
        // dist represent its minimum distance from the source
        int x, y;
        double dist;
        ArrayList<Point> PATH;

        Qelem(int x, int y, double dist) {
            this.x = x;
            this.y = y;
            this.dist = dist;
            PATH = new ArrayList<>();
        }
    }


    public PathSearcher(int floorLength, int floorBreadth){

        this.M = floorBreadth;
        this.N = floorLength;
        this.mat = new int[this.M][this.N];
    }

    private boolean isValid(int[][] mat, int[][] visited, int row, int col)
    {
        return (row >= 0) && (row < M) && (col >= 0) && (col < N)
                && mat[row][col] == 0 && visited[row][col]==0;
    }
    public ArrayList<Point> getSimplePath(Point source, Point dest){

//        Log.w("searchingPath",source.toString()+" to "+dest.toString());
        int i = source.y; //because x,y of canvas and matrix are inverse
        int j = source.x;
        int x = dest.y;
        int y = dest.x;
        //takes {sourceX, sourceY}, {destX, destY} and returns shortest path

        int[][] visited = new int[M][N];

        // create an empty queue
        Queue<Qelem> q = new ArrayDeque<>();

        // mark source cell as visited and enqueue the source Qelem
        visited[i][j] = 1;
        q.add(new Qelem(i, j, 0));

        // stores length of longest path from source to destination
        double min_dist = Integer.MAX_VALUE;
        ArrayList<Point> minPath = new ArrayList<>();
        while (!q.isEmpty())
        {
            // pop front Qelem from queue and process it
            Qelem Qelem = q.poll();

            // (i, j) represents current cell and dist stores its
            // minimum distance from the source
            i = Qelem.x;
            j = Qelem.y;
            ArrayList<Point> pathArr = Qelem.PATH;
            double dist = Qelem.dist;

            // if destination is found, update min_dist and stop
//            if (i == x && j == y)
//            {
//                min_dist = dist;
//                lowestDist = dist;
//                minPath = pathArr;
//                break;
//            }
            boolean tobreak=false;

            // check for all 4 possible movements from current cell
            // and enqueue each valid movement
            // neighbors with distance 1 get added first
            for (int k = 0; k < 4; k++)
            {
                // check if it is possible to go to position
                // (i + row[k], j + col[k]) from current position
                if (isValid(mat, visited, i + row[k], j + col[k]))
                {
                    // mark next cell as visited and enqueue it
                    visited[i + row[k]][j + col[k]] = 1;
                    Qelem n = new Qelem(i + row[k], j + col[k], dist + 1);
                    n.PATH.addAll(pathArr);
                    n.PATH.add(new Point(j,i));
                    if(x==n.x && y==n.y){
                        n.PATH.add(new Point(y,x));
                        minPath = n.PATH;
                        min_dist = n.dist;
                        tobreak = true;
                        break;
                    }
                    q.add(n);
                }
            }
            if(tobreak){
                break;
            }

            // neighbors with distance 1.414 get added second
            for (int k = 0; k < 4; k++)
            {
                // check if it is possible to go to position
                // (i + row[k], j + col[k]) from current position
                if (isValid(mat, visited, i + row2[k], j + col2[k]))
                {
                    // mark next cell as visited and enqueue it
                    visited[i + row2[k]][j + col2[k]] = 1;
                    Qelem n = new Qelem(i + row2[k], j + col2[k], dist + 1.414);
                    n.PATH.addAll(pathArr);
                    n.PATH.add(new Point(j,i));
                    if(x==n.x && y==n.y){
                        n.PATH.add(new Point(y,x));
                        minPath = n.PATH;
                        min_dist = n.dist;
                        tobreak = true;
                        break;
                    }
                    q.add(n);
                }
            }
            if(tobreak){
                break;
            }
        }

        try{
            if (min_dist != Integer.MAX_VALUE) {
//            Log.d("findingPath",source.x+" "+source.y+"|"+dest.x+" "+dest.y+"/"+min_dist);
                lowestDist = min_dist;
                return RDP.getSimplifiedPath(minPath,2);
            }
            else{
                lowestDist = Integer.MAX_VALUE;
                Log.e("PathSearcherErr", "err in searching path");
            }

        }catch(Exception e){
            Log.e("PathSearcherErr", "err from catch block "+ e);
        }




        return RDP.getSimplifiedPath(minPath,2);
    }

    private ArrayList<Point> optimizePath(ArrayList<Point> minPath, int maxSearchLength) {
        //this function will optimize points of path such that the points are
        //at a feasible distance from the non walkable boundary
        //maxSearchLength is the maximum value till which bfs will run for a point
        //such that no non-walkable is under that distance

        if(minPath.size()<=2){
            return minPath;
        }

        ArrayList<Point> resultPath = new ArrayList<>();
        resultPath.add(minPath.get(0));
        for(int i =1; i<minPath.size()-1;i++){//we skip source and destination optimization
            Point P = minPath.get(i);
            Point optimized = optimizePoint(P,maxSearchLength);
            resultPath.add(optimized);

        }
        resultPath.add(minPath.get(minPath.size()-1));
        return resultPath;
    }

    private Point optimizePoint(Point p, int maxSearchLength) {

        int myBoundaryDist = getBoundaryDist(p.x,p.y,maxSearchLength);
        Log.e("mbdist",p.x+","+p.y+": "+myBoundaryDist+"");
        if(myBoundaryDist==maxSearchLength-1){
            return p; //means this point is already at least maxSearchLength away from wall so no need to shift it
        }

        int leftPointBDist = getBoundaryDist(p.x-1,p.y,maxSearchLength);
        int rightPointBDist = getBoundaryDist(p.x+1,p.y,maxSearchLength);
        int upPointBDist = getBoundaryDist(p.x,p.y-1,maxSearchLength);
        int downPointBDist = getBoundaryDist(p.x,p.y+1,maxSearchLength);
        Log.w("lrud",leftPointBDist+","+rightPointBDist+","+upPointBDist+","+downPointBDist);

        int maxDist = Math.max(Math.max(leftPointBDist,rightPointBDist), Math.max(upPointBDist,downPointBDist));

        if(maxDist==myBoundaryDist){
            return p; //no point in going to neighbors
        }
        else if (maxDist==leftPointBDist){
            return optimizePoint(new Point(p.x-1,p.y), maxSearchLength);
        }
        else if (maxDist==rightPointBDist){
            return optimizePoint(new Point(p.x+1,p.y), maxSearchLength);
        }
        else if (maxDist==upPointBDist){
            return optimizePoint(new Point(p.x,p.y-1), maxSearchLength);
        }
        else{
            return optimizePoint(new Point(p.x,p.y+1), maxSearchLength);
        }
    }

    private boolean isNonWalkable(int row, int col){
        return (row >= 0) && (row < M) && (col >= 0) && (col < N) && mat[row][col] == 1;
    }

    private Integer getBoundaryDist(int X, int Y, int maxSearchLength) {
        if(coordWithBoundaryDist.containsKey(N*X+Y)){
            return coordWithBoundaryDist.get(N*X+Y);
        }

        if((Y < 0) || (Y >= M) || (X < 0) || (X >= N)){
            return -1; //invalid point
        }

        Set<Integer> visited = new HashSet<>();
        int dist = 0;
        ArrayList<int[]> Q = new ArrayList<>();
        Q.add(new int[]{X,Y});
        while(dist<maxSearchLength && Q.size()>0){

            ArrayList<int[]> newQ = new ArrayList<>();

            for(int[] obj: Q){
                if(visited.contains(N*obj[0]+obj[1])){
                    continue;
                }
                if(isNonWalkable(obj[1],obj[0])){ //y and x are opposite in path
                    coordWithBoundaryDist.put(N*X+Y,dist);
                    return dist;
                }
                visited.add(N*obj[0]+obj[1]); //linear combination
                for (int k = 0; k < 4; k++){
                    int x = obj[0]+row[k];
                    int y = obj[1]+col[k];
                    if(!visited.contains(N*x+y)){
                        newQ.add(new int[]{x,y});
                    }
                }

            }

            Q = newQ;
            dist++;
        }

        coordWithBoundaryDist.put(N*X+Y,dist);
        return dist;
    }

    public double getDist(){
        return lowestDist;
    }

    public void addNWSimplePoly(String s, double gpx, double gpy){
        //this is the clicked points of the polygon, useful for us in detecting intersection
        //of lines
        ArrayList<Point> thisPoly = new ArrayList<>();
        for(String linearCoord: s.split(",")){
            int lCoord = Integer.parseInt(linearCoord);
            //keep in mind canvas coordinate system
            int X = lCoord%N;
            int Y = (lCoord-X)/N;
            thisPoly.add(new Point((int)(X*gpx),(int)(Y*gpy)));
        }
        simplePolys.add(thisPoly);
    }

    public void addNonWalkablePoints(String s){
        //s is string like "1233, 4233, 4454, 4532"

        for(String linearCoord: s.split(",")){
            int lCoord = Integer.parseInt(linearCoord);
            //keep in mind canvas coordinate system
            int X = lCoord%N;
            int Y = (lCoord-X)/N;
            this.mat[Y][X] = 1;
        }

        //first of all simplify the polygon so that we
        //only have corner points and no points along edge
        //this will reduce the time of Polygon contains class

//        ArrayList<int[]> simplifiedPoints = RDP.getSimplifiedPath(originalPoints,0.001);
//        Polygon p = new Polygon(simplifiedPoints);
//        for(int[] tmp: simplifiedPoints){
//            System.out.print(tmp[0]+","+tmp[1]+"\n");
//        }
//        Log.d("PathSearcher","added poly of size"+simplifiedPoints.size());
//        nonWalkablePolygons.add(p);
    }

    //    public boolean validLocation(int X, int Y ){
    //        //returns true if the point is not inside any non walkable polygon
    //        for(Polygon p: nonWalkablePolygons){
    //            if(p.contains(X,Y)){
    //                return false;
    //            }
    //        }
    //        return true;
    //    }

    private boolean validTransition2(Point p1, Point p2, double gpx, double gpy){
        //(p1,p2) is step line and checks if that step line intersects with any non walkable boundary

        //poly has points coordinates in pixels
        for(ArrayList<Point> poly: simplePolys){
            for(int i=0;i<poly.size()-1;i++){
                //we also have to consider the grid size to be 1 feet so there will be 4 lines instead of 1 line (q1,q2)

                q11 = new Point(poly.get(i).x, poly.get(i).y);
                q12 = new Point(poly.get(i+1).x, poly.get(i+1).y);

                q21 = new Point((int)(q11.x+gpx), q11.y);
                q22 = new Point((int)(q12.x+gpx), q12.y);

                q31 = new Point(q11.x, (int)(q11.y+gpy));
                q32 = new Point(q12.x, (int)(q12.y+gpy));

                q41 = new Point((int)(q11.x+gpx), (int)(q11.y+gpy));
                q42 = new Point((int)(q12.x+gpx), (int)(q12.y+gpy));

                if(Utils.doesIntersect(p1,p2,q11,q12) || Utils.doesIntersect(p1,p2,q21,q22)
                || Utils.doesIntersect(p1,p2,q31,q32) || Utils.doesIntersect(p1,p2,q41,q42)
                ){
                    return false;
                }
            }
        }
        return true;
    }

    public boolean validTransition(Point p1, Point p2, double gpx, double gpy){
        // get all the points between start=(x1,y1) and end=(x2,y2) and check if the transition
        // from start to end doesn't have any non walkable boundary
        // because each step can have multiple points jump
        // 1 (False, non walkable) and 0 (walkable)
        ArrayList<Point> transitionPoints = BresenhamPoints.bresenham(p1,p2);
        for(Point P: transitionPoints){
            if(this.mat[Math.min((int)(P.y/gpy), M-1)][Math.min((int)(P.x/gpx),N-1)]==1){
                return false;
            }
        }
        return true;
    }

//    private ArrayList<Point[]> getIntersectingSides(Point p1, Point p2, double gpx, double gpy) {
//        //returns the list of intersecting sides (Point[] is a side) over all the polygons that intersect with p1->p2 line
//        ArrayList<Point[]> result = new ArrayList<>();
//        for(ArrayList<Point> poly: simplePolys){
//            for(int i=0;i<poly.size()-1;i++){
//                Point q1 = new Point((int)(poly.get(i).x*gpx), (int)(poly.get(i).y*gpy));
//                Point q2 = new Point((int)(poly.get(i+1).x*gpx), (int)(poly.get(i+1).y*gpy));
//                if(Utils.doesIntersect(p1,p2,q1,q2)){
//                    Point[] arr = new Point[]{q1,q2};
//                    result.add(arr);
//                }
//            }
//        }
//        return result;
//    }


    public Point getValidTransitionPoint(Point p1, Point p2, double gpx, double gpy){
        //this function rotates the destination point p2 around source point p1 (of an step) till
        //source to destination becomes a valid transition
        //it does so by rotating point by amountPerRotation degrees till +-90 degrees for finding the valid point

        amountPerRotation = 5;
        totalRotation = 1;
        Point resultP = new Point(p2.x,p2.y);
        while (Math.abs(totalRotation)<180 && !validTransition2(p1,resultP, gpx, gpy)){
            rotate_point(p1,p2,resultP, totalRotation);
            totalRotation*=-1; //switch amountPerRotation left and right
            if(totalRotation>0){
                totalRotation+=amountPerRotation;
            }
        }
        return resultP;
    }


    private void rotate_point(Point p1, Point p2, Point resultP, double angle)
    {
        //rotates point p2 taking p1 as pivot and gives the resultant point in resultP
        X = p2.x - p1.x;
        Y = p2.y - p1.y;
        s = Math.sin(Math.toRadians(angle));
        c = Math.cos(Math.toRadians(angle));

        // translate point back to origin:


        // rotate point
        xnew = X*c - Y*s;
        ynew = X*s + Y*c;

        // translate point back:
        resultP.x = (int)(xnew + p1.x);
        resultP.y = (int)(ynew + p1.y);
    }



    public ArrayList<Point> optimizePath2(ArrayList<Point> path){
        ArrayList<Point> result = new ArrayList<>();
        result.add(path.get(0));

        for(int i=1; i<path.size();i++){
            Point curr = path.get(i);
            Point prev = path.get(i-1);
            double theta = Math.atan2((curr.y-prev.y),(curr.x-prev.x));
            double leftd = 0;
            double rightd = 0;
            double carryX = curr.x;
            double carryY = curr.y;

            while(rightd<5){
                carryX += (rightd)*Math.cos((Math.PI/2) - theta);
                carryY += (rightd)*Math.sin((Math.PI/2) - theta);
                if(isNonWalkable((int)carryY, (int)carryX)){
                    break;
                }
                rightd++;
            }

            carryX = curr.x;
            carryY = curr.y;
            while(leftd<5){
                carryX += (leftd)*Math.cos((Math.PI/2) + theta);
                carryY += (leftd)*Math.sin((Math.PI/2) + theta);
                if(isNonWalkable((int)(carryY), (int)(carryX))){
                    break;
                }
                leftd++;
            }

            if(leftd<rightd){
                double dToMove = (rightd-leftd)/2;
                int newX = (int)(curr.x + (dToMove)*Math.cos((Math.PI/2) - theta));
                int newY = (int)(curr.y + (dToMove)*Math.sin((Math.PI/2) - theta));
                result.add(new Point(newX,newY));
            }
            else{
                double dToMove = (leftd-rightd)/2;
                int newX = (int)(curr.x + (dToMove)*Math.cos((Math.PI/2) + theta));
                int newY = (int)(curr.y + (dToMove)*Math.sin((Math.PI/2) + theta));
                result.add(new Point(newX,newY));

            }

        }

        return result;
    }

}
