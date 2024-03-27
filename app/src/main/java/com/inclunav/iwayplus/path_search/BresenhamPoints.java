package com.inclunav.iwayplus.path_search;

import android.graphics.Point;

import java.util.ArrayList;

//enhanced BersenHamPoints

//given two points this class returns the integer points between these two points

public class BresenhamPoints {
    static ArrayList<Point> bresenham(Point p1, Point p2) {
        ArrayList<Point> result = new ArrayList<>();
        int i; // loop counter
        int ystep, xstep; // the step on y and x axis
        int error; // the error accumulated during the increment
        int errorprev; // *vision the previous value of the error variable
        int y = p1.y, x = p1.x; // the line points
        int ddy, ddx; // compulsory variables: the double values of dy and dx
        int dx = p2.x - p1.x;
        int dy = p2.y - p1.y;
        result.add(p1);
        // NB the last point can't be here, because of its previous point (which has to be verified)
        if (dy < 0) {
            ystep = -1;
            dy = -dy;
        } else
            ystep = 1;
        if (dx < 0) {
            xstep = -1;
            dx = -dx;
        } else
            xstep = 1;
        ddy = 2 * dy; // work with double values for full precision
        ddx = 2 * dx;
        if (ddx >= ddy) { // first octant (0 <= slope <= 1)
            // compulsory initialization (even for errorprev, needed when dx==dy)
            errorprev = error = dx; // start in the middle of the square
            for (i = 0; i < dx; i++) { // do not use the first point (already done)
                x += xstep;
                error += ddy;
                if (error > ddx) { // increment y if AFTER the middle ( > )
                    y += ystep;
                    error -= ddx;
                    // three cases (octant == right->right-top for directions below):
                    if (error + errorprev < ddx){// bottom square also
                        result.add(new Point(x,y-ystep));
                    }
                    else if (error + errorprev > ddx){ // left square also
                        result.add(new Point(x-xstep,y));
                    }
                    else { // corner: bottom and left squares also
                        result.add(new Point(x,y-ystep));
                        result.add(new Point(x-xstep,y));
                    }
                }
                result.add(new Point(x,y));
                errorprev = error;
            }
        } else { // the same as above
            errorprev = error = dy;
            for (i = 0; i < dy; i++) {
                y += ystep;
                error += ddx;
                if (error > ddy) {
                    x += xstep;
                    error -= ddy;
                    if (error + errorprev < ddy){
                        result.add(new Point(x-xstep,y));
                    }
                    else if (error + errorprev > ddy){
                        result.add(new Point(x,y-ystep));
                    }
                    else {
                        result.add(new Point(x-xstep,y));
                        result.add(new Point(x,y-ystep));
                    }
                }
                result.add(new Point(x,y));
                errorprev = error;
            }
        }

        return result;
    }
}
