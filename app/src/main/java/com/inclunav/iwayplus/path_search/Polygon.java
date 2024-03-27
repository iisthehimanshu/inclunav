package com.inclunav.iwayplus.path_search;

import java.util.ArrayList;

public class Polygon
{
    // Polygon coodinates.
    private ArrayList<int[]> poly;

    // Number of sides in the polygon.
    private int polySides;

    /**
     * Default constructor.
     * @param points corner points of the Polygon.
     */
    public Polygon(ArrayList<int[]> points )
    {
        poly = new ArrayList<>();
        poly.addAll(points);
        polySides = points.size();
    }

    /**
     * Checks if the Polygon contains a point.
     * @see "http://alienryderflex.com/polygon/"
     * @param x Point horizontal pos.
     * @param y Point vertical pos.
     * @return Point is in Poly flag.
     */
//    public boolean contains( int x, int y )
//    {
//        boolean oddTransitions = false;
//        for( int i = 0, j = polySides -1; i < polySides; j = i++ )
//        {
//            if( ( polyY[ i ] < y && polyY[ j ] >= y ) || ( polyY[ j ] < y && polyY[ i ] >= y ) )
//            {
//                if( polyX[ i ] + ( y - polyY[ i ] ) / ( polyY[ j ] - polyY[ i ] ) * ( polyX[ j ] - polyX[ i ] ) < x )
//                {
//                    oddTransitions = !oddTransitions;
//                }
//            }
//        }
//        return oddTransitions;
//    }

    public boolean contains( int x, int y )
    {
        boolean oddTransitions = false;
        for( int i = 0, j = polySides -1; i < polySides; j = i++ )
        {
            if( ( poly.get(i)[1] < y && poly.get(j)[1] >= y ) || ( poly.get(j)[1] < y && poly.get(i)[1] >= y ) )
            {
                if( poly.get(i)[0] + ( y - poly.get(i)[1] ) / ( poly.get(j)[1] - poly.get(i)[1] ) * ( poly.get(j)[0] - poly.get(i)[0] ) < x )
                {
                    oddTransitions = !oddTransitions;
                }
            }
        }
        return oddTransitions;
    }
}