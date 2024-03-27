package com.inclunav.iwayplus.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.inclunav.iwayplus.R;

public class IsometricFloorView extends AppCompatActivity {
    private String[] colors = {"#FF5733","#3390FF", "#33FF46"};
    private int CARD_SIZE;
    private String currFloor;
    private String destFloor;
    private String[] allFloors;
    private FrameLayout isometricContainer;
    private View currColor;
    private View destColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_isometric_floor_view);
        currColor = findViewById(R.id.currColor);
        destColor = findViewById(R.id.destColor);

        Bundle extras = getIntent().getExtras();
        currFloor = extras.getString("currFloor");
        destFloor = extras.getString("destFloor");
        allFloors = extras.getStringArray("floors");
        CARD_SIZE =dpTopx(200);


        isometricContainer = findViewById(R.id.isometricContainer);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screen_width = displayMetrics.widthPixels;

        ScrollView.LayoutParams params2 = new ScrollView.LayoutParams(
                (int)(1.5*CARD_SIZE),
                ScrollView.LayoutParams.WRAP_CONTENT
        );
        params2.setMargins((screen_width/2)-((int)(1.5*CARD_SIZE)/2),0,0,0);
        isometricContainer.setLayoutParams(params2);



    }

    @Override
    protected void onStart() {
        if(currFloor.equals(destFloor)){
            destColor.setBackgroundColor(Color.parseColor("#3EFF00")); //green
        }
        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
        );
        textViewParams.gravity = Gravity.BOTTOM|Gravity.END;
        for(int i=1;i<=allFloors.length;i++){

            String fname = allFloors[i-1];

            LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            FrameLayout.LayoutParams cardParams =new FrameLayout.LayoutParams(
                    CARD_SIZE,CARD_SIZE
            );

            cardParams.setMargins(0, (allFloors.length-i)*dpTopx(50), 0, 0);
            cardParams.gravity = Gravity.CENTER | Gravity.TOP;

            View v = vi.inflate(R.layout.isometric_floor_card,null);
            TextView tv = v.findViewById(R.id.floor_name);
            tv.setText(fname + " floor");
            tv.setLayoutParams(textViewParams);
            v.setLayoutParams(cardParams);
            v.setBackgroundColor(Color.parseColor(getFloorColor(fname)));
            isometricContainer.addView(v);
            ObjectAnimator anim2 = ObjectAnimator.ofFloat(v,"rotationX",0f,80f);
            anim2.setDuration(100);
            anim2.start();
            v.animate().rotation(45).start();

        }
        super.onStart();
    }

    private String getFloorColor(String fname){
        //if currFloor then green , if destFloor then red, else blue
        if(fname.equals(currFloor)){
            return "#3EFF00"; //green
        }
        else if (fname.equals(destFloor)){
            return "#FF0049"; //red
        }
        else{
            //greyish
            return "#E6E6E6";
        }
    }


    private String getNextColor(int i){
        return colors[i%(colors.length)];
    }

    private int dpTopx(int dpval){
        Resources r = getApplicationContext().getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dpval,
                r.getDisplayMetrics()
        );
    }

}
