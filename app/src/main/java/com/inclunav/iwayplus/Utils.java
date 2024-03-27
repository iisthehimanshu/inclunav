package com.inclunav.iwayplus;

import android.graphics.Point;

import com.inclunav.iwayplus.pdr.Sentences;
import com.inclunav.iwayplus.path_search.PathSearcher;

import java.util.ArrayList;
import java.util.Map;

// Java program to check if two given line segments intersect
//source: https://www.geeksforgeeks.org/check-if-two-given-line-segments-intersect/
public class Utils {

    // Given three colinear points p, q, r, the function checks if
    // point q lies on line segment 'pr'
    public static String[] hindiNums_zero_to_hundred = {"शून्य","एक","दो","तीन","चार","पांच","छह","सात","आठ","नौ","दस","ग्यारह","बारह","तेरह","चौदह","पंद्रह","सोलह","सत्रह","अठारह","उन्नीस","बीस","इक्कीस","बाईस","तेईस","चौबीस","पच्चीस","छब्बीस","सत्ताईस","अट्ठाईस","उनतीस","तीस","इकतीस","बत्तीस","तैंतीस","चौंतीस","पैंतीस","छत्तीस","सैंतीस","अड़तीस","उनतालीस","चालीस","इकतालीस","बयालीस","तैंतालीस","चौंतालीस","पैंतालीस","छियालीस","सैंतालीस","अड़तालीस","उनचास","पचास","इक्याबन","बावन","तिरेपन","चौबन","पचपन","छप्पन","सत्तावन","अट्ठावन","उनसठ","साठ","इकसठ","बासठ","तिरसठ","चौंसठ","पैंसठ","छियासठ","सड़सठ","अड़सठ","उनहत्तर","सत्तर","इकहत्तर","बहत्तर","तिहत्तर","चौहत्तर","पचहत्तर","छिहत्तर","सतहत्तर","अठहत्तर","उनासी","अस्सी","इक्यासी","बयासी","तिरासी","चौरासी","पचासी","छियासी","सतासी","अठासी","नवासी","नब्बे","इक्यानबे","बानवे","तिरानवे","चौरानवे","पचानवे","छियानवे","सत्तानवे","अट्ठानवे","निन्यानवे","सौ"};
//    public static Long cache_time_millis = 60*60*1000L;
    public static Long cache_time_millis = 1L;

    private static boolean onSegment(Point p, Point q, Point r)
    {
        if (q.x <= Math.max(p.x, r.x) && q.x >= Math.min(p.x, r.x) &&
                q.y <= Math.max(p.y, r.y) && q.y >= Math.min(p.y, r.y))
            return true;

        return false;
    }

    private static int orientation(Point p, Point q, Point r)
    {
        int val = (q.y - p.y) * (r.x - q.x) -
                (q.x - p.x) * (r.y - q.y);

        if (val == 0) return 0; // colinear

        return (val > 0)? 1: 2; // clock or counterclock wise
    }

    public static boolean doesIntersect(Point p1, Point q1, Point p2, Point q2)
    {
        int o1 = orientation(p1, q1, p2);
        int o2 = orientation(p1, q1, q2);
        int o3 = orientation(p2, q2, p1);
        int o4 = orientation(p2, q2, q1);

        // General case
        if (o1 != o2 && o3 != o4)
            return true;

        // Special Cases
        // p1, q1 and p2 are colinear and p2 lies on segment p1q1
        if (o1 == 0 && onSegment(p1, p2, q1)) return true;

        // p1, q1 and q2 are colinear and q2 lies on segment p1q1
        if (o2 == 0 && onSegment(p1, q2, q1)) return true;

        // p2, q2 and p1 are colinear and p1 lies on segment p2q2
        if (o3 == 0 && onSegment(p2, p1, q2)) return true;

        // p2, q2 and q1 are colinear and q1 lies on segment p2q2
        if (o4 == 0 && onSegment(p2, q1, q2)) return true;

        return false; // Doesn't fall in any of the above cases
    }


    private static ArrayList<Point> interpolatePoints(Point p1, Point p2, double step_size, double gpx, double gpy){
        //returns a list of interpolated points between p1 and p2 with 1 step_size apart points from p1 to p2
        //useful for plotting the dotted path where each dot is seperated by a step
        double d = Math.sqrt(Math.pow(p1.x-p2.x,2)+Math.pow(p1.y-p2.y,2));
        ArrayList<Point> result = new ArrayList<>();
        result.add(new Point((int)(p1.x*gpx),(int)(p1.y*gpy)));
        double counter = step_size;
        while(counter < d){
            double x = p1.x + (counter/d)*(p2.x-p1.x);
            double y = p1.y + (counter/d)*(p2.y-p1.y);
            result.add(new Point((int)(x*gpx),(int)(y*gpy)));
            counter+=1*step_size;
        }
        return result;
    }

    //converts simple path to interpolated path with coordiantes according to canvas gpx and gpy
    public static ArrayList<Point> interpolatePath(ArrayList<Point> path, double step_size, double gridMultX, double gridMultY){
        //interpolates points in the path, since simplified path is only turning points so need to interpolate it

        ArrayList<Point> resultpath = new ArrayList<>();
        if(path.size()==1){
            resultpath.add(new Point((int)(path.get(0).x*gridMultX),(int)(path.get(0).y*gridMultY)));
            return resultpath;
        }
        for(int i=0; i<path.size()-1;i++){
            resultpath.addAll(interpolatePoints(path.get(i),path.get(i+1), step_size, gridMultX, gridMultY));
        }
        return resultpath;
    }

    //generates a pathSearcher object using the floorname, its dimension from floorDim and its non walkables from floorToNonWalkables
    public static PathSearcher generatePathSearcher(String floor_name, Map<String, int[]> floorDim, Map<String, ArrayList<String>> floorToNonWalkables) {
        int[] floor_dimension = floorDim.get(floor_name);
        PathSearcher PS = new PathSearcher(floor_dimension[0],floor_dimension[1]);
        //add non walkables in this path searcher
        for(String s: floorToNonWalkables.get(floor_name)){ PS.addNonWalkablePoints(s); }
        return PS;
    }

    //returns turn angle amount and direction in clock language
    public static String angleToClocks(Integer angle,String language){
        int clockAmount;
        if (angle<0){
            angle = angle+360;
        }
        if (angle >= 337.5 && angle <= 22.5) {
            return Sentences.turn_right.getSentence(language,String.valueOf("Straight"));
        } else if (angle > 22.5 && angle <= 67.5) {
            return Sentences.turn_right.getSentence(language,String.valueOf("slight right"));
        } else if (angle > 67.5 && angle <= 112.5) {
            return Sentences.turn_right.getSentence(language,String.valueOf("right"));
        } else if (angle > 112.5 && angle <= 157.5) {
            return Sentences.turn_right.getSentence(language,String.valueOf("sharp right"));
        } else if (angle > 157.5 && angle <= 202.5) {
            return Sentences.turn_right.getSentence(language,String.valueOf("U Turn"));
        } else if (angle >= 202.5 && angle <= 247.5) {
            return Sentences.turn_right.getSentence(language,String.valueOf("sharp left"));
        } else if (angle > 247.5 && angle <= 292.5) {
            return Sentences.turn_right.getSentence(language,String.valueOf("left"));
        } else if (angle > 292.5 && angle <= 337.5) {
            return Sentences.turn_right.getSentence(language,String.valueOf("slight left"));
        } else{
                return "";
            }

        }

//    public static String angleToClocks(Integer angle,String language){
//        int clockAmount;
//        if(angle<0){
//            clockAmount = (int)Math.round((360+angle)/30.0);
//            //left direction
//            if(clockAmount==0 || clockAmount==12){
//                return "";
////                clockAmount = 11;
//            }
//            return Sentences.turn_left.getSentence(language,String.valueOf(clockAmount));
//        }
//        else{
//            //right direction
//            clockAmount = (int)Math.round(angle/30.0);
//            if(clockAmount==0 || clockAmount==12){
//                return "";
////                clockAmount = 1;
//            }
//            return Sentences.turn_right.getSentence(language,String.valueOf(clockAmount));
//        }
//    }

    //given rssi and txPower, it returns the distance in metres
    public static double getDistance(double rssi, int txPower){
        if (rssi == 0.0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }

        double ratio = rssi/txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            return (0.89976)*Math.pow(ratio,7.7095) + 0.111;
        }
    }

    public static Point[] boundingBoxCorners(ArrayList<Point> arr){
        //calculate minimum and maximum x,y among the points
        Point minP = new Point(999999,999999);
        Point maxP = new Point(-10,-10);
        for(Point p: arr){
            minP.x = Math.min(minP.x,p.x);
            minP.y = Math.min(minP.y,p.y);
            maxP.x = Math.max(maxP.x,p.x);
            maxP.y = Math.max(maxP.y,p.y);
        }

        //return the 4 corners of bounding box
        //topleft:(xmin, ymin), bottomleft:(xmin, ymax), bottomright:(xmax, ymax), topright:(xmax, ymin)
        Point c1 = new Point(minP.x,minP.y);
        Point c2 = new Point(minP.x,maxP.y);
        Point c3 = new Point(maxP.x,maxP.y);
        Point c4 = new Point(maxP.x,minP.y);

        return new Point[]{c1,c2,c3,c4};
    }

    public static String[] splitCamelCaseString(String s){
        return s.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
    }

}