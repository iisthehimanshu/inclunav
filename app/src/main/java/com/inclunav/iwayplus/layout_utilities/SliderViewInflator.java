package com.inclunav.iwayplus.layout_utilities;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.inclunav.iwayplus.activities.MainActivity;
import com.inclunav.iwayplus.pdr.FloorObj;
import com.inclunav.iwayplus.Utils;
import com.inclunav.iwayplus.R;
import com.squareup.picasso.Picasso;

import org.osmdroid.config.Configuration;

import java.io.File;

public class SliderViewInflator extends FrameLayout {
    public SliderViewInflator(Context context){
        super(context);
    }

    public SliderViewInflator(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
    }

    public SliderViewInflator(Context context, AttributeSet attributeSet, int i){
        super(context, attributeSet,i);
    }

    public SliderViewInflator(Context context, AttributeSet attributeSet, SliderItem sItem, FloorObj fobj, int activity_width, int activity_height) {
        super(context, attributeSet);
        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(30f);
        circularProgressDrawable.setColorFilter(Color.parseColor("#000000"),android.graphics.PorterDuff.Mode.SRC_IN);
        circularProgressDrawable.start();
        final RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(circularProgressDrawable);

        LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflator.inflate(R.layout.slider_view_card,this);

        ImageView imgView = findViewById(R.id.imageCanvas);
//        MapView mapView = findViewById(R.id.mapView);
        CanvasView canvasView = findViewById(R.id.mapCanvas);
        LinearLayout rotationEnabledInfoView = findViewById(R.id.rotationEnabledInfoView);
//        fobj.setImageView(imgView);
        fobj.setCanvasView(canvasView);

        //since this item is inside a linearlayout which is inside a horizontalScrollView so
        //we only need to set width because height is auto filled
//        mapView.getLayoutParams().width = sItem.getWidth();
        imgView.getLayoutParams().width = sItem.getWidth();
        canvasView.getLayoutParams().width = sItem.getWidth();
//        mapView.getLayoutParams().height = sItem.getHeight();
        imgView.getLayoutParams().height = sItem.getHeight();
        canvasView.getLayoutParams().height = sItem.getHeight();
        rotationEnabledInfoView.getLayoutParams().width = activity_width;
        rotationEnabledInfoView.getLayoutParams().height = activity_height;


        String unique_sig_of_image = sItem.getImageURL()+ (System.currentTimeMillis()/ Utils.cache_time_millis);



        Glide.with(context)
                .load(sItem.getImageURL())
                .apply(requestOptions)
                .signature(new ObjectKey(unique_sig_of_image))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imgView);
    }



}