package com.inclunav.iwayplus.pdr;

import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.inclunav.iwayplus.layout_utilities.CanvasView;
import com.inclunav.iwayplus.layout_utilities.ZoomRotationFrameLayout;

public class DeviceAttitudeHandler implements SensorEventListener {
    int zz = 1;

    private float previousOrientation = 0.0f;

    private static final float ALPHA = 1.5f; // Adjust this value for your desired smoothing level
    private float smoothedOrientation = 0.0f;


    private SensorManager mSensorManager;
    private Sensor mSensor;
    private float[] mRotationMatrixFromVector = new float[16] ;
    private float[] mRotationMatrix = new float[16];
    private float[] orientationVals = new float[3];
    private boolean drawOnCanvas=false;
    private CanvasView canvasToDraw;
    private ZoomRotationFrameLayout attachedZRFL;
    private float locX;
    private float locY;
    private float floorRotation;
    private RectF precisionOval; //for showing precision around location
    private static int CONFIDENCE_AT_1_METRE = 8;

    public float getSmoothedOrientationYaw(float newOrientation) {
        // Apply a low-pass filter to smooth the sensor data
        smoothedOrientation = smoothedOrientation * (1 - ALPHA) + newOrientation * ALPHA;

        // Correct for floor rotation as needed
        float val = smoothedOrientation - floorRotation;
        if (val < 0) {
            val = val + 360;
        }

        return val;
    }

    public DeviceAttitudeHandler(SensorManager sensorM){
        mSensorManager   = sensorM ;
        mSensor          = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    public void start(){
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void stop(){
        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
//        Date currentTime = Calendar.getInstance().getTime();
//        Log.e("MagEvent", ""+event.accuracy+", time:"+currentTime);

        // StackOverFlow
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            mSensorManager.getRotationMatrixFromVector(mRotationMatrixFromVector, event.values);

            mSensorManager.remapCoordinateSystem(mRotationMatrixFromVector,
                    SensorManager.AXIS_X, SensorManager.AXIS_Z,
                    mRotationMatrix);
            mSensorManager.getOrientation(mRotationMatrix, orientationVals);

            orientationVals[0] = (float) Math.toDegrees(orientationVals[0]); //azimuth
            orientationVals[1] = (float) Math.toDegrees(orientationVals[1]); //pitch
            orientationVals[2] = (float) Math.toDegrees(orientationVals[2]); //roll
        }
        if(drawOnCanvas){
            canvasToDraw.updateArrowLine(locX, locY, getOrientationYaw());
            canvasToDraw.updatePrecisionOval(precisionOval);
            attachedZRFL.rotateLayout(-getOrientationYaw(),locX,locY);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        switch(sensor.getType()){
            case Sensor.TYPE_MAGNETIC_FIELD :
                switch(accuracy) {
                    case SensorManager.SENSOR_STATUS_ACCURACY_LOW :
//                        doSomething();
                        break;
                    case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM :
//                        doSomethingElse();
                        break;
                    case SensorManager.SENSOR_STATUS_ACCURACY_HIGH :
//                        doNothing();
                        break;
                }
                break;
            default:
                break;
        }
    }

    // getter yaw
    public float getOrientationYaw(){
        float toreturn = orientationVals[0]+orientationVals[2];

        if (toreturn<0){
            toreturn+=360;
        }
        float val = toreturn-floorRotation;
        if(val<0){
            val=val+360;
        }

        float val2 = getSmoothedOrientationYaw(val);
        return val2;

    }

    public void enableDrawOnCanvas(CanvasView c, ZoomRotationFrameLayout zrfl){
        //enables drawing direction on canvas
        drawOnCanvas = true;
        Log.e("DAH", "draw on canvas enabled");
        canvasToDraw = c;
        attachedZRFL = zrfl;
    }

    public void disableDrawOnCanvas(){
        //enables drawing direction on canvas
        drawOnCanvas = false;
        Log.e("DAH", "draw on canvas DISABLED");
    }

    public void updateCanvasLocation(float x, float y){
        //change the point where we have to draw the arrow
        locX = x;
        locY = y;
    }

    public void updateCanvasPrecision(double confidence, double gpx, double gpy){
        //draws an oval around the locX, locY on the basis of confidence
        // oval side is anti proportional to confidence
        double approxDist = Math.max(CONFIDENCE_AT_1_METRE*3.24/confidence, 1.0); //3.24 is metre to feetn
        float distPX = (float)(approxDist*gpx);
        float distPY = (float)(approxDist*gpy);
        precisionOval = new RectF(locX-distPX, locY-distPY, locX+distPX, locY+distPY);
    }

    public void updateFloorRotation(float BR){
        floorRotation = BR;
    }

}
