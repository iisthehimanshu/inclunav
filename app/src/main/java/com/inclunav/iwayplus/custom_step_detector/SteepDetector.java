package com.inclunav.iwayplus.custom_step_detector;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.inclunav.iwayplus.activities.BuildingAdapter;

public class SteepDetector {

    Context context;

    private static final int ACCEL_RING_SIZE = 50;
    private static final int VEL_RING_SIZE = 10;

    private static final String manufacturer = getDeviceManufacture();

    // change this threshold according to your sensitivity preferences
    private static float STEP_THRESHOLD ;  // 12f for samsung //7f for oneplus // 0.6f for tablet

    private static final int STEP_DELAY_NS = 300000000;

    private int accelRingCounter = 0;
    private float[] accelRingX = new float[ACCEL_RING_SIZE];
    private float[] accelRingY = new float[ACCEL_RING_SIZE];
    private float[] accelRingZ = new float[ACCEL_RING_SIZE];
    private int velRingCounter = 0;
    private float[] velRing = new float[VEL_RING_SIZE];
    private long lastStepTimeNs = 0;
    private float oldVelocityEstimate = 0;

    private StepListener listener;

    public void registerListener(StepListener listener) {
        Log.w("SteepDetector","Registered");
        this.listener = listener;
    }

    public SteepDetector(Context context) {
        this.context = context;
    }

    public void updateAccel(long timeNs, float x, float y, float z) {
        float[] currentAccel = new float[3];
        currentAccel[0] = x;
        currentAccel[1] = y;
        currentAccel[2] = z;

        // First step is to update our guess of where the global z vector is.
        accelRingCounter++;
        accelRingX[accelRingCounter % ACCEL_RING_SIZE] = currentAccel[0];
        accelRingY[accelRingCounter % ACCEL_RING_SIZE] = currentAccel[1];
        accelRingZ[accelRingCounter % ACCEL_RING_SIZE] = currentAccel[2];

        float[] worldZ = new float[3];
        worldZ[0] = SensorFilter.sum(accelRingX) / Math.min(accelRingCounter, ACCEL_RING_SIZE);
        worldZ[1] = SensorFilter.sum(accelRingY) / Math.min(accelRingCounter, ACCEL_RING_SIZE);
        worldZ[2] = SensorFilter.sum(accelRingZ) / Math.min(accelRingCounter, ACCEL_RING_SIZE);

        float normalization_factor = SensorFilter.norm(worldZ);

        worldZ[0] = worldZ[0] / normalization_factor;
        worldZ[1] = worldZ[1] / normalization_factor;
        worldZ[2] = worldZ[2] / normalization_factor;

        float currentZ = SensorFilter.dot(worldZ, currentAccel) - normalization_factor;
        velRingCounter++;
        velRing[velRingCounter % VEL_RING_SIZE] = currentZ;

        float velocityEstimate = SensorFilter.sum(velRing);

        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        if (windowManager != null) {
            windowManager.getDefaultDisplay().getMetrics(metrics);
            int screenWidth = metrics.widthPixels;
            if (screenWidth >= 1500) {
                //Log.d("devicetype", "updateAccel: tablet");
                STEP_THRESHOLD =  0.6f;
            } else {
                if(manufacturer.contains("samsung")){
                    Log.d("devicetype", "updateAccel: samsung");
                    STEP_THRESHOLD =  12f;
                } else if (manufacturer.contains("oneplus")) {
                    Log.d("devicetype", "updateAccel: oneplus");
                    STEP_THRESHOLD = 7f;
                }else if (manufacturer.contains("realme")) {
                    Log.d("devicetype", "updateAccel: oneplus");
                    STEP_THRESHOLD = 7f;
                } else if (manufacturer.contains("redmi")) {
                    Log.d("devicetype", "updateAccel: redmi");
                    STEP_THRESHOLD =12f;
                }
            }
        }

        if (velocityEstimate > STEP_THRESHOLD && oldVelocityEstimate <= STEP_THRESHOLD
                && (timeNs - lastStepTimeNs > STEP_DELAY_NS)) {
            listener.step(timeNs);
            lastStepTimeNs = timeNs;
        }
        oldVelocityEstimate = velocityEstimate;
    }

    public static String getDeviceManufacture(){
        return Build.MANUFACTURER.toLowerCase();
    }

}