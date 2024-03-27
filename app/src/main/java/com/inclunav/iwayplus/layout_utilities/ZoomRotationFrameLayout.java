package com.inclunav.iwayplus.layout_utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.inclunav.iwayplus.R;

public class ZoomRotationFrameLayout extends FrameLayout{
    private boolean rotEnabled = false;
    private float lastSuccessfulScale=1.0f;
    private int activity_width,activity_height;
    private Float boundingCenterX,boundingCenterY,boundingScale;

    private enum Mode {
        NONE,
        DRAG,
        ZOOM
    }


    private static final float MIN_ZOOM = 0.5f;
    private static final float MAX_ZOOM = 4.0f;

    private Mode mode = Mode.NONE;
    private float scale = 1.0f;
    private float lastScaleFactor = 1.0f;

    private boolean firstTouch = false;
    CanvasView canvasChild; //will fetch this on the first touch if there is any
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    private PointF start = new PointF();
    private PointF mid = new PointF();
    float oldDist = 1f;
    float[] lastEvent = null;
    float oldRot = 0f;
    float newRot = 0f;
    float rotOffset = 0;
    float rotEnableThreshold = 10;
    float oldAutoRot=0f;
    boolean autoRotDisabled = false;

    public ZoomRotationFrameLayout(Context context) {
        super(context);
        init();
    }

    public ZoomRotationFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ZoomRotationFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        this.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        autoRotDisabled = true;
                        ((FrameLayout)view.getParent()).findViewById(R.id.rotationEnabledInfoView).setVisibility(View.VISIBLE);
                        if(!firstTouch){
                            firstTouch = true;

                            //get any canvas Child if it exists
                            for(int i=0;i<getChildCount();i++){
                                if(getChildAt(i).getId() == R.id.mapCanvas){
                                    canvasChild = (CanvasView) getChildAt(i);
                                    break;
                                }

                            }

                        }
                        savedMatrix.set(matrix);
                        start.set(motionEvent.getX(), motionEvent.getY());
                        mode = Mode.DRAG;
                        lastEvent = null;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mode == Mode.DRAG) {
                            matrix.set(savedMatrix);
                            matrix.postTranslate(motionEvent.getX() - start.x, motionEvent.getY() - start.y);
                        } else if (mode == Mode.ZOOM && motionEvent.getPointerCount() == 2) {
                            float newDist = spacing(motionEvent);
                            matrix.set(savedMatrix);
                            if (newDist > 10f) {
                                float newScale = newDist / oldDist;
                                scale = lastScaleFactor*newScale;

                                //for updating the dot sizes on canvasChild if there is any canvasChild
                                if(lastSuccessfulScale!=(float)(Math.round(scale*2.0)/2.0)){
                                    lastSuccessfulScale = (float)(Math.round(scale*2.0)/2.0);
                                    if(canvasChild!=null) canvasChild.updateScale(scale);
                                }

                                matrix.postScale(newScale, newScale, mid.x, mid.y);
                            }
                            if (lastEvent != null) {
                                newRot = rotation(motionEvent);
                                float rotAngle = newRot - oldRot;
                                if(!rotEnabled && Math.abs(rotAngle)>rotEnableThreshold){
                                    rotEnabled = true;
                                    rotOffset = -1*Math.signum(rotAngle)*rotEnableThreshold;
                                }
                                if(rotEnabled){
                                    matrix.postRotate(rotAngle+ rotOffset, mid.x, mid.y);
                                }

                            }
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        oldDist = spacing(motionEvent);
                        updateMidPoint(mid, motionEvent);
                        if (oldDist > 10f) {
                            savedMatrix.set(matrix);
                            mode = Mode.ZOOM;
                        }
                        lastEvent = new float[4];
                        lastEvent[0] = motionEvent.getX(0);
                        lastEvent[1] = motionEvent.getX(1);
                        lastEvent[2] = motionEvent.getY(0);
                        lastEvent[3] = motionEvent.getY(1);
                        oldRot = rotation(motionEvent);
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        lastScaleFactor = scale;
                        rotEnabled = false;
                        mode = Mode.NONE;
                        lastEvent = null;
                        break;
                    case MotionEvent.ACTION_UP:
                        mode = Mode.NONE;
                        break;
                }


                getParent().requestDisallowInterceptTouchEvent(true);
                applyMatrix();


                return true;
            }
        });
    }


    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);

        return (float) Math.toDegrees(radians);
    }
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    private void updateMidPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }


    @SuppressLint("NewApi")
    public void resetLayoutToDefault(){
        mode = Mode.NONE;
        scale = 1.0f;
        lastScaleFactor = 1.0f;
        firstTouch = false;
        matrix = new Matrix();
        savedMatrix = new Matrix();
        start = new PointF();
        mid = new PointF();
        oldDist = 1f;
        lastEvent = null;
        oldRot = 0f;
        newRot = 0f;

        if(boundingCenterX!=null){
            zoomToBoundingBox();
        }
        else{
            applyMatrix();
            if(canvasChild!=null) canvasChild.updateScale(1.0f);
        }


    }

    @SuppressLint("NewApi")
    private void applyMatrix(){
        for(int i=0;i<getChildCount();i++){
            getChildAt(i).setAnimationMatrix(matrix);
        }
    }


    public void updateBoundingBoxConstants(Point[] boundingBoxCorners,int activity_width, int activity_height){
        this.activity_width = activity_width;
        this.activity_height = activity_height;
        Point c1 = boundingBoxCorners[0];
        Point c2 = boundingBoxCorners[1];
        Point c3 = boundingBoxCorners[2];
        Point c4 = boundingBoxCorners[3];

        boundingCenterX = (float)((c1.x+c3.x)/2.0);
        boundingCenterY = (float)((c1.y+c3.y)/2.0);
        //calculate scale and apply it
        float boundingScaleX = (c4.x == c1.x)? 1.0f : (float) (activity_width * 1.0 / (Math.abs(c4.x - c1.x)));
        float boundingScaleY = (c2.y == c1.y)? 1.0f : (float) (activity_height * 1.0 / (Math.abs(c2.y - c1.y)));
        boundingScale = Math.min(Math.min(boundingScaleX,boundingScaleY)*0.7f, 5f);

    }


    //zooms to center of bounding box with the specified scale
    public void zoomToBoundingBox() {
        //set the mid point as mid point of bounding box

        //bring the bounding box center to screen center
        matrix.postTranslate((activity_width >> 1) -boundingCenterX, (activity_height >> 1) -boundingCenterY);
        //scale taking the center as pivot
        matrix.postScale(boundingScale, boundingScale, activity_width >> 1, activity_height >> 1);
        applyMatrix();

        lastScaleFactor = scale = boundingScale;

        //get any canvas Child if it exists
        for(int i=0;i<getChildCount();i++){
            if(getChildAt(i).getId() == R.id.mapCanvas){
                canvasChild = (CanvasView) getChildAt(i);
                break;
            }

        }
        if(canvasChild!=null) canvasChild.updateScale(scale);

    }

    public void rotateLayout(Float angle, Float pivotX, Float pivotY){
        if(autoRotDisabled) return;
        matrix.setTranslate((activity_width >> 1) -pivotX, (activity_height >> 1) -pivotY);
        matrix.postRotate(angle,(activity_width >> 1),(activity_height >> 1));
        matrix.postScale(lastScaleFactor, lastScaleFactor, activity_width >> 1, activity_height >> 1);
        for(int i=0;i<getChildCount();i++){
            if(getChildAt(i).getId() == R.id.mapCanvas){
                canvasChild = (CanvasView) getChildAt(i);
                break;
            }

        }
        if(canvasChild!=null) canvasChild.updateScale(scale);
        applyMatrix();
    }

    public void resumeAutoRotation(){
        autoRotDisabled = false;
        ((FrameLayout)this.getParent()).findViewById(R.id.rotationEnabledInfoView).setVisibility(View.INVISIBLE);
    }


}