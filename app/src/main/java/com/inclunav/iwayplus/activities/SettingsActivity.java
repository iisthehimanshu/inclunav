package com.inclunav.iwayplus.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.inclunav.iwayplus.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class SettingsActivity extends AppCompatActivity {
    Button logout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        logout = findViewById(R.id.logout);
    }

    public void logout(View view) {

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut();

        SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("loggedIn",false);
        editor.putBoolean("userExist",false);
        editor.putString("mob_no","");
        editor.putString("email","");
        editor.apply();
        Intent i = new Intent(getApplicationContext(), PermissionsRequestActivity.class);
        startActivity(i);
        Toast.makeText(getApplicationContext(),"logged Out!",Toast.LENGTH_SHORT).show();
        finishAffinity();
//        TaskStackBuilder.create(getApplicationContext()).addNextIntentWithParentStack(i).startActivities();
    }

    public void goProfileSetting(View view) {
        startActivity(new Intent(SettingsActivity.this, ProfileSetting.class));
    }

    public void goNavigationSetting(View view) {
        startActivity(new Intent(SettingsActivity.this, NavigationSetting.class));
    }

    public void goBack(View view) {
        onBackPressed();
    }


}
