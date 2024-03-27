package com.inclunav.iwayplus.pdr;

import android.graphics.Point;
import android.util.Log;
import android.widget.ImageView;

import com.inclunav.iwayplus.layout_utilities.CanvasView;
import com.inclunav.iwayplus.Utils;

import java.util.ArrayList;

public class FloorObj {
    private double gpx; //grid to pixels in x of current floor
    private double gpy; //grid to pixels in y of current floor
    private String floor_name; //name of current floor (as from the data)
    private int breadth; //height
    private int length; //width
    private String endPointName; //name of last node point in the path of this floor
    private int viewWidth; //width of view containing this FloorObj
    private int viewHeight; //height of view containing this FloorObh
//    private int displayX; //X location in pixels
//    private int displayY; //Y location in pixels
//    private double xf; //X location in pixels but float
//    private double yf; //Y location in pixels but float
//    private CanvasView canvas; //Canvas view associated with this floor
//    private View image_canvas; //image view associated with this floor
//    private int locInGridX;
//    private int locInGridY;
    private float floorRotation;
    private ArrayList<Point> path;
    private double pathWeight;
//    private PathSearcher PS; //pathSearcher of this floor
    private ArrayList<ArrayList<Point>> simplePolys = new ArrayList<>(); //stores polygons clicked points list
    private Point q11, q12, q21, q22, q31, q32, q41, q42;
    private double X, Y, s, c, xnew, ynew, amountPerRotation, totalRotation;
    private CanvasView canvasView; //canvas corresponding to this fobj
    private ImageView imageView; // imageview corresponding to this fobj

    //
//    public FloorObj(){
//        this.path = new ArrayList<>();
//    }
//
    public void setGpx(double val){
        this.gpx = val;
    }

    public void setGpy(double val){
        this.gpy = val;
    }
//
    public void setFloorName(String val){
        this.floor_name = val;
    }

    public void setLengthAndBreadth(int[] lb){
        this.length = lb[0];
        this.breadth = lb[1];
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public void setCanvasView(CanvasView canvasView) {
        this.canvasView = canvasView;
    }

    public void setViewHeight(int viewHeight) {
        this.viewHeight = viewHeight;
    }

    public void setViewWidth(int viewWidth) {
        this.viewWidth = viewWidth;
    }

    public void setPathWeight(double pathWeight) {
        this.pathWeight = pathWeight;
    }

    public double getPathWeight() {
        return pathWeight;
    }

    public int getViewHeight() {
        return viewHeight;
    }

    public int getViewWidth() {
        return viewWidth;
    }

    public int getLength() {
        return length;
    }

    public int getBreadth() {
        return breadth;
    }
    //
//    public void setDisplayXY(int val1, int val2){
//        this.displayX = val1;
//        this.displayY = val2;
//    }
//
//    public void set_xf_yf(double val1, double val2){
//        this.xf = val1;
//        this.yf = val2;
//    }
//    public void setCanvas(CanvasView c){
//        this.canvas = c;
//    }
//
//    public void setCurrLocInGrid(int val1, int val2) {
//        this.locInGridX = val1;
//        this.locInGridY = val2;
//    }
//

    public void setEndPointName(String endPointName) {
        this.endPointName = endPointName;
    }

    public void setFloorRotation(float val){
        this.floorRotation = val;
    }
    public void setPath(ArrayList<Point> path){
        this.path = path;
    }
//    public void setPS(PathSearcher p){
//        this.PS = p;
//    }
//    public void setImage_canvas(View v){
//        this.image_canvas = v;
//    }
//
//    public void updateDisplayXY(){
//        //updates displayX and displayY by using xf and yf
//        this.displayX = (int)this.xf;
//        this.displayY = (int)this.yf;
//    }
//
//
    public double getGpx(){
        return this.gpx;
    }
//
    public double getGpy(){
        return this.gpy;
    }
//
    public String getFloorName(){
        return this.floor_name;
    }

    public String getEndPointName() {
        return endPointName;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public CanvasView getCanvasView() {
        return canvasView;
    }

    //
//    public int getDisplayX(){
//        return this.displayX;
//    }
//
//    public int getDisplayY(){
//        return this.displayY;
//    }
//
//    public double getXf(){
//        return this.xf;
//    }
//    public double getYf(){
//        return this.yf;
//    }
////    public CanvasView getCanvas(){
////        return this.canvas;
////    }
//    public int getLocInGridX() {
//        return locInGridX;
//    }
//    public int getLocInGridY() {
//        return locInGridY;
//    }
//
    public float getFloorRotation() {
        return floorRotation;
    }
    public ArrayList<Point> getPath(){
        Log.e("path", " : " + path);
        return this.path;
    }
////    public View getImage_canvas(){
////        return this.image_canvas;
////    }
//    public PathSearcher getPS(){
//        return this.PS;
//    }
//
//    public boolean equals(FloorObj obj2){
//        return this.name.equals(obj2.getFloorName());
//    }

    //this is the clicked points of the polygon, useful for us in detecting intersection
    //of lines. When checking step transition we check whether step line intersects with any side
    // of this polygon or not
    public void addNWSimplePoly(String s){
        ArrayList<Point> thisPoly = new ArrayList<>();
        for(String linearCoord: s.split(",")){
            int lCoord = Integer.parseInt(linearCoord);
            //keep in mind canvas coordinate system
            int X = lCoord%length;
            int Y = (lCoord-X)/length;
            thisPoly.add(new Point((int)(X*gpx),(int)(Y*gpy)));
        }
        simplePolys.add(thisPoly);
    }
    private boolean isValidTransition(Point p1, Point p2){
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
    public Point getValidTransitionPoint(Point p1, Point p2){
        //this function rotates the destination point p2 around source point p1 (of an step) till
        //source to destination becomes a valid transition
        //it does so by rotating point by amountPerRotation degrees till +-90 degrees for finding the valid point

        amountPerRotation = 5;
        totalRotation = 1;
        Point resultP = new Point(p2.x,p2.y);
        while (Math.abs(totalRotation)<180 && !isValidTransition(p1,resultP)){
            rotate_point(p1,p2,resultP, totalRotation);
            totalRotation*=-1; //switch amountPerRotation left and right
            if(totalRotation>0){
                totalRotation+=amountPerRotation;
            }
        }
        return resultP;
    }
    private void rotate_point(Point p1, Point p2, Point resultP, double angle) {
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

}
