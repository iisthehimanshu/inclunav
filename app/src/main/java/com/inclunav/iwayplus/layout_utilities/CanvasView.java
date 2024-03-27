package com.inclunav.iwayplus.layout_utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import com.inclunav.iwayplus.R;

import java.util.ArrayList;

public class CanvasView extends View {
    public int width;
    public int height;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    Context context;
    private Paint mPaint;

    private ArrayList<Point> searchedPath = new ArrayList<>();
    private ArrayList<Point> unmodifiedPath = new ArrayList<>();
    private int locInPath=-1; //the index of location where user is in path, start to this index we mark as completed

    //*****arrow related****
    private boolean arrowEnabled=false;
    private float arrowlineX;
    private float arrowlineY;
    private float arrowDir; //in degrees
    private RectF precisionOval;
    Path arrow_path;
    Matrix arrow_matrix;
    Point destPoint;
    Point sourcePoint;
    float pathDotSize=15;
    float sourceDotSize =25;
    float original_pathDotSize=15;
    float original_sourceDotSize =25;
    String endPointName = null;
    Handler handler=new Handler();
    Runnable animator;

    Bitmap orig = BitmapFactory.decodeResource(getResources(), R.drawable.arrow);

    Bitmap arrowBitmap = Bitmap.createScaledBitmap(orig,50,50,true);
    Bitmap destOrig = BitmapFactory.decodeResource(getResources(), R.drawable.loc_icon);
    Bitmap destBitmap = Bitmap.createScaledBitmap(destOrig,50,50,true);

    //************************

    public CanvasView(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;

        // we set a new Path
        mPath = new Path();
        arrow_path = new Path();
        arrow_matrix = new Matrix();
//        DPE = new DashPathEffect(new float[] {1,20}, 50);

        // and we set a new Paint with the desired attributes
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(1f);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
//        mPaint.setPathEffect(DPE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        // your Canvas will draw onto the defined Bitmap
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        //building borders lines
//        mPath.moveTo(0,0);
//        mPath.lineTo(width,0);
//        mPath.moveTo(0,0);
//        mPath.lineTo(0,height);
//        mPath.moveTo(width,0);
//        mPath.lineTo(width,height);
//        mPath.moveTo(0,height);
//        mPath.lineTo(width,height);
//        canvas.drawPath(mPath, mPaint);

        //drawing the searched path
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(false);
        mPaint.setStrokeWidth(pathDotSize);
        mPaint.setColor(Color.parseColor("#68FF00")); //gray color for dotted points


        for(Point obj: searchedPath){
            canvas.drawPoint(obj.x,obj.y,mPaint);
        }

        //for drawing destination point as red and source point as blue
        if(unmodifiedPath.size()>0){
            destPoint = unmodifiedPath.get(unmodifiedPath.size()-1);
            sourcePoint = unmodifiedPath.get(0);
            canvas.drawBitmap(destBitmap,destPoint.x-destBitmap.getWidth()/2,destPoint.y-destBitmap.getHeight(),mPaint);
            mPaint.setColor(Color.BLUE);
            mPaint.setStrokeWidth(sourceDotSize);
            canvas.drawPoint(sourcePoint.x, sourcePoint.y, mPaint);
        }


        if(endPointName!=null){
            mPaint.setStrokeWidth(2f);
            mPaint.setTextSize(25);
            mPaint.setColor(Color.RED);
            canvas.drawText(endPointName, destPoint.x, destPoint.y-destBitmap.getHeight()-10, mPaint);
        }


        //start to this locInPath we mark as completed with green color
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(pathDotSize);
        for(int j=0; j<locInPath;j++){
            canvas.drawPoint(unmodifiedPath.get(j).x,unmodifiedPath.get(j).y,mPaint);
        }

        //draw the precision oval
        mPaint.setStrokeWidth(2f);
        mPaint.setAntiAlias(true);
        mPaint.setColor(getResources().getColor(R.color.transparentOvalColor));
        mPaint.setStyle(Paint.Style.FILL);
        if(precisionOval!=null) canvas.drawOval(precisionOval, mPaint);





        if(arrowEnabled) drawArrowLine(canvas);



    }


    public void clearCanvas() {
        locInPath=-1;
        mPath.reset();
        disableArrow();
        searchedPath.clear();
        invalidate();
    }


    private void drawArrowLine(Canvas canvas) {
        arrow_matrix.setRotate(arrowDir,arrowBitmap.getWidth()/2,arrowBitmap.getHeight()/2);
        arrow_matrix.postTranslate(arrowlineX-arrowBitmap.getWidth()/2,arrowlineY-arrowBitmap.getWidth()/2);
        canvas.drawBitmap(arrowBitmap, arrow_matrix,null);


    }

    public void updateArrowLine(float x, float y, float dir){
        //update the constants for arrow line
        arrowEnabled = true;
        arrowlineX = x;
        arrowlineY = y;
        arrowDir = dir;
        invalidate();
    }

    public void disableArrow(){
        this.arrowEnabled = false;
        invalidate();
    }

    public void drawPath(ArrayList<Point> coords){
        locInPath=-1;
        unmodifiedPath.clear();
        searchedPath.clear();
        searchedPath.addAll(coords);
        unmodifiedPath.addAll(coords);
        invalidate();
    }

    public void updateLocationInPath(int currIndexInPath) {
        locInPath = currIndexInPath;
        invalidate();
    }

    //this will be called in steps
    public void updateScale(float scale) {
        pathDotSize = Math.max(7f,Math.min(original_pathDotSize,original_pathDotSize/scale));
        sourceDotSize = Math.max(12f,Math.min(original_sourceDotSize, original_sourceDotSize /scale));

        int newArrowdim = Math.max(20,Math.min(50,(int)(50.0/scale)));

        arrowBitmap = Bitmap.createScaledBitmap(orig,newArrowdim,newArrowdim,true);
        invalidate();

    }

    public void updateDotSizes(int floorWidth, int floorLength){
        //sets dots sizes on the basis of map size
        // if it is large dimension map then dot sizes should be small and vice versa
        float diagonalLength = (float) (Math.sqrt(floorLength*floorLength+floorWidth*floorWidth));;
        pathDotSize = Math.min(25f, Math.max(7f,(15f*223f)/diagonalLength));
        sourceDotSize = Math.min(30f, Math.max(12f,(25f*223f)/diagonalLength));
        original_sourceDotSize = sourceDotSize;
        original_pathDotSize = pathDotSize;
        invalidate();
    }

    public void updateEndPointName(String name){
        endPointName = name;
        invalidate();
    }

    public void animatePath(){
        if(animator!=null){
            handler.removeCallbacks(animator);
        }
        searchedPath.clear();
        final int[] index ={locInPath+1};
        animator = new Runnable() {
            @Override
            public void run() {
                if(index[0]!=unmodifiedPath.size()){
                    searchedPath.add(unmodifiedPath.get(index[0]));
                    invalidate();
                    index[0]++;
                    handler.postDelayed(this,100);
                }
            }
        };
        handler.post(animator);
    }

    public void updatePrecisionOval(RectF precisionOval) {
        this.precisionOval = precisionOval;
        invalidate();
    }
}


