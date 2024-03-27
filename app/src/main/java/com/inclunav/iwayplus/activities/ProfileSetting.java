package com.inclunav.iwayplus.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.inclunav.iwayplus.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class ProfileSetting extends AppCompatActivity {
    Button startButton;
    TextView userDetailName;
    TextView userDetailEmail;
    TextView userDetailPassword;
    TextView userDetailMobile;

    Button deleteacc;

//    ImageView onEditName;
//    ImageView onEditPassword;
//    ImageView onEditMobile;
//    ImageView onEditEmail;

    //Edit Name
    LinearLayout editName;
    EditText userPasswordEditName;
    EditText userDetailNewName;
    Button onConfirmNewName;
    Button onCancelEditName;

    //Edit Password
    LinearLayout editPassword;
    EditText userPasswordEditPassword;
    EditText userDetailNewPassword;
    EditText userDetailConfirmNewPassword;
    Button sendOTPEditPassword;
    EditText userOTPEditPassword;
    Button onConfirmNewPassword;
    Button onCancelEditPassword;

    //Edit Phone No
    LinearLayout editMobile;
    EditText userPasswordEditMobile;
    EditText userDetailNewNumber;
    Button sendOTPEditMobile;
    EditText userOTPEditMobile;
    Button onConfirmNewMobile;
    Button onCancelEditMobile;

    //Edit Email
    LinearLayout editEmail;
    EditText userPasswordEditEmail;
    EditText userDetailNewEmail;
    Button sendOTPEditEmail;
    EditText userOTPEditEmail;
    Button onConfirmNewEmail;
    Button onCancelEditEmail;

    FrameLayout loadingView;
    private RequestQueue MyRequestQueue;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);
        deleteacc = findViewById(R.id.delete_account);
        userDetailName = findViewById(R.id.userDetailName);
        userDetailPassword = findViewById(R.id.userDetailPassword);
        userDetailMobile = findViewById(R.id.userDetailMobile);
        userDetailEmail = findViewById(R.id.userDetailEmail);

        //Edit Name
        editName = findViewById(R.id.editName);
        userPasswordEditName = findViewById(R.id.userPasswordEditName);
        userDetailNewName = findViewById(R.id.userDetailNewName);
        onConfirmNewName = findViewById(R.id.onConfirmNewName);
        onCancelEditName = findViewById(R.id.onCancelEditName);

        onConfirmNewName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                onConfirmNewNameMethod();
            }
        });

        deleteacc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://maps.iwayplus.in/#/account/delete"; // Replace with the URL you want to open
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);

            }
        });

        onCancelEditName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                onCancelEditNameMethod();
            }
        });

        //Edit Password
        editPassword = findViewById(R.id.editPassword);
        userPasswordEditPassword = findViewById(R.id.userPasswordEditPassword);
        userDetailNewPassword = findViewById(R.id.userDetailNewPassword);
        userDetailConfirmNewPassword = findViewById(R.id.userDetailConfirmNewPassword);
        sendOTPEditPassword = findViewById(R.id.sendOTPEditPassword);
        userOTPEditPassword = findViewById(R.id.userOTPEditPassword);
        onConfirmNewPassword = findViewById(R.id.onConfirmNewPassword);
        onCancelEditPassword = findViewById(R.id.onCancelEditPassword);

        //Edit Mobile no.
        editMobile = findViewById(R.id.editMobile);
        userPasswordEditMobile = findViewById(R.id.userPasswordEditMobile);
        userDetailNewNumber = findViewById(R.id.userDetailNewNumber);
        sendOTPEditMobile = findViewById(R.id.sendOTPEditMobile);
        userOTPEditMobile = findViewById(R.id.userOTPEditMobile);
        onConfirmNewMobile = findViewById(R.id.onConfirmNewMobile);
        onCancelEditMobile = findViewById(R.id.onCancelEditMobile);

        //Edit email
        editEmail = findViewById(R.id.editEmail);
        userPasswordEditEmail = findViewById(R.id.userPasswordEditEmail);
        userDetailNewEmail = findViewById(R.id.userDetailNewEmail);
        sendOTPEditEmail = findViewById(R.id.sendOTPEditEmail);
        userOTPEditEmail = findViewById(R.id.userOTPEditEmail);
        onConfirmNewEmail = findViewById(R.id.onConfirmNewEmail);
        onCancelEditEmail = findViewById(R.id.onCancelEditEmail);

        MyRequestQueue = Volley.newRequestQueue(this);
        getUserDetails();

    }

    private void onConfirmNewNameMethod() {
        String password_str = userPasswordEditName.getText().toString();
        String new_name_str = userDetailNewName.getText().toString();
        SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
        String password = prefs.getString("password",null);
        String mobile_str = prefs.getString("mobile",null);
        String id_str = prefs.getString("id",null);
        String token = prefs.getString("token","");
        if(password_str.length() < 0 && new_name_str.length() < 0){
            errorPopup("Please enter the fields properly");
            return;
        } else if(!password_str.matches(password)){
            errorPopup("Password is not correct");
            return;
        } else if(!(mobile_str.length() < 0 && id_str.length() < 0 && token.length() < 0)){
            String VerifyOTPURL = getApplicationContext().getResources().getString(R.string.verify_mobile_otp);
            HashMap<String, String> params = new HashMap<>();
            params.put("mobileNumber", mobile_str);
            params.put("newName", new_name_str);
            params.put("id", id_str);
            JsonObjectRequest req = new JsonObjectRequest(VerifyOTPURL, new JSONObject(params), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.e("res", "res:  "+ response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("error is ", "" + error);
                }
            }) {
                //This is for Headers If You Needed
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/json");
                    params.put("Authorization", token);
                    return params;
                }
            };
            req.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            req.setShouldCache(false);
            req.setTag(this);
            MyRequestQueue.getCache().clear();
            MyRequestQueue.add(req);
        } else {
            errorPopup("Data is not valid");
            return;
        }
    }

    private void onCancelEditNameMethod() {
    }

    private void getUserDetails() {
        SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
        String token = prefs.getString("token","");
        String id = prefs.getString("id","");
        Log.e("response", "user details  "+token + "  " + id);
        if(token.length() > 0 && id.length() > 0) {
            String loginURL = getApplicationContext().getResources().getString(R.string.user_detail_url);
            HashMap<String, String> params = new HashMap<>();
            params.put("token", token);
            params.put("id", id);
            JsonObjectRequest req = new JsonObjectRequest(loginURL, new JSONObject(params) , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.e("res", "res:  "+ response);
                    try {
                        SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        userDetailName.setText(response.getString("name"));
                        userDetailMobile.setText(response.getString("mobileNumber"));
                        userDetailEmail.setText(response.getString("email"));
                        editor.putString("name",response.getString("name"));
                        editor.putString("email",response.getString("email"));
                        editor.putString("mobile",response.getString("mobileNumber"));
                        editor.putString("id",response.getString("id"));
                        editor.commit();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("error is ", "" + error);
                }
            }) {
                //This is for Headers If You Needed
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/json");
                    params.put("Authorization", token);
                    return params;
                }
            };
            req.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            req.setShouldCache(false);
            req.setTag(this);
            MyRequestQueue.getCache().clear();
            MyRequestQueue.add(req);
        } else {
            return;
        }

    }

    public void onEditName(View view) {
        if (editName.getVisibility() == View.VISIBLE)
        {
            editName.setVisibility(View.GONE);
        }
        else
        {
            editName.setVisibility(View.VISIBLE);
        }
    }

    public void onEditPassword(View view) {
        if (editPassword.getVisibility() == View.VISIBLE)
        {
            editPassword.setVisibility(View.GONE);
        }
        else
        {
            editPassword.setVisibility(View.VISIBLE);
        }
    }

    public void onEditMobile(View view) {
        if (editMobile.getVisibility() == View.VISIBLE)
        {
            editMobile.setVisibility(View.GONE);
        }
        else
        {
            editMobile.setVisibility(View.VISIBLE);
        }
    }

    public void onEditEmail(View view) {
        if (editEmail.getVisibility() == View.VISIBLE)
        {
            editEmail.setVisibility(View.GONE);
        }
        else
        {
            editEmail.setVisibility(View.VISIBLE);
        }
    }

    public void goBack(View view) {
        onBackPressed();
    }

    private void responsePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Success");
        builder.setMessage("Your Mobile no. is successfully registered.\nClick Proceed button to fill Registration detail Page");
        builder.setPositiveButton("PROCEED", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                startActivity(new Intent(ProfileSetting.this,PermissionsRequestActivity.class));
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

    private void errorPopup(String errorMessage){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage(errorMessage);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //do nothing
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
