package com.inclunav.iwayplus.activities;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.inclunav.iwayplus.R;


public class LandingPage extends AppCompatActivity {

    Button startButton;
    FrameLayout loadingView;
    Button englishLangButton;
    Button hindiLangButton;
    LinearLayout showLanguageOption, showStartBtn;
    SharedPreferences prefs;
    private Vibrator vibe;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        NewDashboard.NewBuildingData.clear();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);
        startButton = findViewById(R.id.startButton);
        loadingView = findViewById(R.id.landingPageView);
        showLanguageOption = findViewById(R.id.showLanguageOption);
        showStartBtn = findViewById(R.id.showStartBtn);
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            Uri deepLinkUri = intent.getData();
            String deepLink = deepLinkUri.toString();

            // Handle the deep link as needed
            if (deepLink.equals("iwayplus://inclunav.com")) {
                // Handle this specific deep link
                // You can navigate to a specific fragment or perform some action here
            }
        }
        prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
        String language = prefs.getString("language", "");

        if(language.length() > 0 ) {
            showStartBtn.setVisibility(View.VISIBLE);
            showLanguageOption.setVisibility(View.GONE);
        } else {
            showStartBtn.setVisibility(View.GONE);
            showLanguageOption.setVisibility(View.VISIBLE);
        }

        loadingView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        englishLangButton = findViewById(R.id.englishLang);
        hindiLangButton = findViewById(R.id.hindiLang);

        englishLangButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibe.vibrate(200);
                SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("language","en");
                editor.commit();
                startActivity(new Intent(LandingPage.this,PermissionsRequestActivity.class));
            }
        });

        hindiLangButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibe.vibrate(200);
                SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("language","hi");
                editor.commit();
                startActivity(new Intent(LandingPage.this,PermissionsRequestActivity.class));
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
                Log.i("LandingPage", String.valueOf(prefs.getBoolean("loggedIn", false)));
                if (prefs.getBoolean("loggedIn", false)){
                    startActivity(new Intent(LandingPage.this,PermissionsRequestActivity.class));
                }
                else {
                    startActivity(new Intent(LandingPage.this,PermissionsRequestActivity.class));
                }
            }
        });
    }

    public void gotologinPage(View view) {
        vibe.vibrate(200);
        startActivity(new Intent(this, PermissionsRequestActivity.class));
    }
}
