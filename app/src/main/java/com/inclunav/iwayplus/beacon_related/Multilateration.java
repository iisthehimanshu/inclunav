package com.inclunav.iwayplus.beacon_related;
import com.lemmingapex.trilateration.NonLinearLeastSquaresSolver;
import com.lemmingapex.trilateration.TrilaterationFunction;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;

public class Multilateration {
    private static LeastSquaresOptimizer.Optimum findOptimum(double[][] positions, double[] distances) {
        TrilaterationFunction trilaterationFunction = new TrilaterationFunction(positions, distances);
        LeastSquaresOptimizer leastSquaresOptimizer = new LevenbergMarquardtOptimizer();
        NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(trilaterationFunction, leastSquaresOptimizer);
        return solver.solve();
    }

    private static double[] predictedLocation(LeastSquaresOptimizer.Optimum optimum){
        //the return value is centroid
        return optimum.getPoint().toArray();
    }

    //positions are x,y,z(optional) coordinates of beacons
    //distances are distances corresponding to those beacons
    //This function returns the best location of the point
    public static double[] getLocation(double[][] positions, double[] distances) {
        return predictedLocation(findOptimum(positions, distances));
    }
}
