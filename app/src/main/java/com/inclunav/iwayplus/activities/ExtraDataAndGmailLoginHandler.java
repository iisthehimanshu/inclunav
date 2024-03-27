package com.inclunav.iwayplus.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.inclunav.iwayplus.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ExtraDataAndGmailLoginHandler extends AppCompatActivity {
    EditText feets;
    EditText inches;
    private FrameLayout loadingView;
    private RequestQueue MyRequestQueue;
    EditText age;
    CheckBox isVisuallyImpaired;
    private InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra_data_and_gmail_login_handler);
        age = findViewById(R.id.age_extradata);
        isVisuallyImpaired = findViewById(R.id.visuallyImpaired_extradata);
        feets = findViewById(R.id.feets);
        inches = findViewById(R.id.inches);
        loadingView = findViewById(R.id.loadingView_extradata);
        imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        MyRequestQueue = Volley.newRequestQueue(this);
        //call login api to check whether user exists or not
        //if it does then save to sharedPreds and lead to PermissionsRequestActivity
        //if it doesn't then get extra input from user in this activity
        loadingView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        loginCheckHandler();

    }

    private void loginCheckHandler() {
        String loginURL = getApplicationContext().getResources().getString(R.string.login_user_url);
        Bundle extras = getIntent().getExtras();
        HashMap<String, String> params = new HashMap<>();
        params.put("email", extras.getString("email"));
        params.put("password", extras.getString("password"));
        params.put("mode", extras.getString("mode"));


        JsonObjectRequest req = new JsonObjectRequest(loginURL, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.w("loginWithGMAIL", response.toString());
                        try {
                            if(response.getBoolean("success")){
                                //save to sharedPrefs and lead to PermissionRequestActivity

                                SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("name",response.getString("name"));
                                editor.putString("email",response.getString("email"));
                                editor.putString("token",response.getString("token"));
                                editor.putString("age",response.getString("age"));
                                editor.putString("height_in_cms",response.getString("height"));
                                double heightInCms = Double.valueOf(response.getString("height"));
                                editor.putFloat("step_size", (float)(heightInCms*0.01));
                                editor.putBoolean("isVisuallyImpaired", response.getBoolean("isVisuallyImpaired"));
                                editor.putBoolean("loggedIn",true);
                                editor.commit();
                                startActivity(new Intent(ExtraDataAndGmailLoginHandler.this,PermissionsRequestActivity.class));
                                finishAffinity();
                            }
                            else{
                                //ask for extra fields to register by removing the loading view
                                loadingView.setVisibility(View.GONE);
                            }
                        } catch (JSONException e) {
                            loginGmailRetryPopup("Some error occured!");
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingView.setVisibility(View.GONE);
                if (error instanceof NetworkError) {
                    loginGmailRetryPopup("Cannot connect to Internet. Please check your connection!");
                } else if (error instanceof ServerError) {
                    loginGmailRetryPopup("Server error!");
                } else if (error instanceof AuthFailureError) {
                    loginGmailRetryPopup("Server error!");
                } else if (error instanceof ParseError) {
                    loginGmailRetryPopup("Some error occured!");
                } else if (error instanceof NoConnectionError) {
                    loginGmailRetryPopup("Cannot connect to Internet...Please check your connection!");
                } else if (error instanceof TimeoutError) {
                    loginGmailRetryPopup("Connection TimeOut! Please check your internet connection.");
                }
                else{
                    loginGmailRetryPopup("Server error occured!");
                }
            }
        });
        req.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        req.setShouldCache(false);
        req.setTag(this);
        MyRequestQueue.getCache().clear();
        MyRequestQueue.add(req);
    }

    public void register(View view){
        register();
    }

    private void register() {
        loadingView.setVisibility(View.VISIBLE);
        String registerURL = getApplicationContext().getResources().getString(R.string.register_user_url);
        if(!isNumeric(age.getText().toString())){
            someErrorPopup("invalid age");
            return;
        }
        Bundle extras = getIntent().getExtras();
        HashMap<String, String> params = new HashMap<>();
        params.put("name", extras.getString("name"));
        params.put("email", extras.getString("email"));
        params.put("password", extras.getString("password"));
        params.put("age",age.getText().toString());
        if(isVisuallyImpaired.isChecked()){
            params.put("isVisuallyImpaired", "true");
        }
        else{
            params.put("isVisuallyImpaired", "false");
        }
        params.put("mode", extras.getString("mode"));
        String hfeet = feets.getText().toString();
        String hinch = inches.getText().toString();
        if(hfeet.equals("") || hinch.equals("")){
            Toast.makeText(this, "please input all fields", Toast.LENGTH_LONG).show();
            return;
        }
        double heightInCms = Integer.parseInt(hfeet)*30.48 + Integer.parseInt(hinch)*2.54;
        final double stepSize = heightInCms*0.01; //step size in feets
        String heightcmsRounded = String.valueOf((int) Math.round(heightInCms));
        params.put("height",heightcmsRounded);

        JsonObjectRequest req = new JsonObjectRequest(registerURL, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        loadingView.setVisibility(View.GONE);
                        try {
                            if(response.getBoolean("success")){
                                SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                JSONObject data = response.getJSONObject("data");
                                editor.putString("name",data.getString("name"));
                                editor.putString("email",data.getString("email"));
                                editor.putString("token",data.getString("token"));
                                editor.putString("age",data.getString("age"));
                                editor.putString("height_in_cms",data.getString("height"));
                                editor.putBoolean("isVisuallyImpaired", data.getBoolean("isVisuallyImpaired"));
                                editor.putFloat("step_size", (float)stepSize);
                                editor.putBoolean("loggedIn",true);
                                editor.commit();
                                responsePopup(response);
                            }
                            else{
                                someErrorPopup(response.getString("error"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingView.setVisibility(View.GONE);
                if (error instanceof NetworkError) {
                    registerRetryPopup("Cannot connect to Internet. Please check your connection!");
                } else if (error instanceof ServerError) {
                    registerRetryPopup("Server error!");
                } else if (error instanceof AuthFailureError) {
                    registerRetryPopup("Server error!");
                } else if (error instanceof ParseError) {
                    registerRetryPopup("Some error occured!");
                } else if (error instanceof NoConnectionError) {
                    registerRetryPopup("Cannot connect to Internet...Please check your connection!");
                } else if (error instanceof TimeoutError) {
                    registerRetryPopup("Connection TimeOut! Please check your internet connection.");
                }
                else{
                    registerRetryPopup("Server error occured!");
                }
            }
        });

        req.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        req.setShouldCache(false);
        req.setTag(this);
        MyRequestQueue.getCache().clear();
        MyRequestQueue.add(req);


    }

    private void loginGmailRetryPopup(String errorMessage){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage(errorMessage);
        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                loginCheckHandler(); //resend the login with gmail request
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void registerRetryPopup(String errorMessage){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage(errorMessage);
        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                register(); //resend the register request
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void someErrorPopup(String errorMessage){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage(errorMessage);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //registration activity exists on the stack so show it
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void responsePopup(JSONObject response) throws JSONException {
        if(response.getBoolean("success")){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Success");
            builder.setMessage("Your registration has successfully completed.\nClick Proceed button to proceed to the dashboard");
            builder.setPositiveButton("PROCEED", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                    startActivity(new Intent(getApplicationContext(),PermissionsRequestActivity.class));
                    finishAffinity();
                }
            });

            final AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnShowListener( new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(Color.parseColor("#448200"));
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#FFFFFF"));
                }
            });
            dialog.show();
        }
        else{
            registerRetryPopup(response.getString("error"));
        }
    }

    private boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            long d = Long.parseLong(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private void hideKeyBoard(){
        //Hide:
        if(imm.isAcceptingText()){
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        }

    }
}
