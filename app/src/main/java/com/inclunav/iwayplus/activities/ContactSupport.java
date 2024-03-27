package com.inclunav.iwayplus.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.inclunav.iwayplus.R;

import org.json.JSONObject;

import java.util.HashMap;

public class ContactSupport extends AppCompatActivity {

    EditText name, userMobile, description;
    Button submitContactSupport;
    private RequestQueue MyRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_support);
        name = findViewById(R.id.name);
        userMobile = findViewById(R.id.userMobile);
        description = findViewById(R.id.description);
        submitContactSupport = findViewById(R.id.submitContactSupport);

        submitContactSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                onSubmit();
            }
        });
        MyRequestQueue = Volley.newRequestQueue(this);
    }

    public void onSubmit() {
        //get userDetail and password_field for custom login

        String name_str = name.getText().toString();
        String userMobile_str = userMobile.getText().toString();
        String description_str = description.getText().toString();

        if(!(name_str.length()>0)){
            errorPopup("Please enter Name");
            return;
        }


        if(userMobile_str.length() < 0){
            errorPopup("Please enter Mobile no.");
            return;
        }

        if(description_str.length() < 8){
            errorPopup("Please enter description");
            return;
        }

        String loginURL = getApplicationContext().getResources().getString(R.string.login_user_url);
        HashMap<String, String> params = new HashMap<>();
        params.put("mobileNumber", name_str);
        params.put("password", userMobile_str);
        params.put("mode", description_str);
        JsonObjectRequest req = new JsonObjectRequest(loginURL, new JSONObject(params),
            response -> {
                try {
                    if(response.getBoolean("success")){
                        //save to sharedPrefs and lead to PermissionRequestActivity
                    }
                    else{
                        errorPopup(response.getString("error"));
                    }
                } catch (Exception e) {
                    errorPopup("Some error occured!");
                    e.printStackTrace();
                }
            }, error -> {
            if (error instanceof NetworkError) {
                errorPopup("Cannot connect to Internet. Please check your connection!");
            } else if (error instanceof ServerError) {
                errorPopup("Server error!");
            } else if (error instanceof AuthFailureError) {
                errorPopup("Server error!");
            } else if (error instanceof ParseError) {
                errorPopup("Some error occured!");
            } else if (error instanceof NoConnectionError) {
                errorPopup("Cannot connect to Internet...Please check your connection!");
            } else if (error instanceof TimeoutError) {
                errorPopup("Connection TimeOut! Please check your internet connection.");
            }
            else{
                errorPopup("Server error occured!");
            }
        });
        req.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        req.setShouldCache(false);
        req.setTag(this);
        MyRequestQueue.getCache().clear();
        MyRequestQueue.add(req);
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

    public void goBack(View view) {
        onBackPressed();
    }

}