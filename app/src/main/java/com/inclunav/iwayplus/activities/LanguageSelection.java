package com.inclunav.iwayplus.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.inclunav.iwayplus.R;

public class LanguageSelection extends AppCompatActivity {
    Button englishLangButton;
    Button hindiLangButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selection);
        englishLangButton = findViewById(R.id.englishLang);
        hindiLangButton = findViewById(R.id.hindiLang);

        englishLangButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("language","en");
                editor.commit();
                startActivity(new Intent(LanguageSelection.this,PermissionsRequestActivity.class));
            }
        });

        hindiLangButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("language","hi");
                editor.commit();
                startActivity(new Intent(LanguageSelection.this,PermissionsRequestActivity.class));
            }
        });
    }
}
