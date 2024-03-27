package com.inclunav.iwayplus.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

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


public class NavigationSetting extends AppCompatActivity {
    Button englishLang, hindiLang;
    Button sighted, blind;
    Button lessThen5, btw5_6, moreThen6;
    Button adolescent, adult, seniorCitizen;
    ImageButton walk, wheelChair;
    private RequestQueue MyRequestQueue;
    SharedPreferences prefs;
    private Vibrator vibe;

    String language, vision, height, age, navigationMode;

    @SuppressLint({"ClickableViewAccessibility", "ResourceAsColor"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_settings);
        englishLang = findViewById(R.id.englishLang);
        hindiLang = findViewById(R.id.hindiLang);

        sighted = findViewById(R.id.sighted);
        blind = findViewById(R.id.blind);

        lessThen5 = findViewById(R.id.lessThen5);
        btw5_6 = findViewById(R.id.btw5_6);
        moreThen6 = findViewById(R.id.moreThen6);

        adolescent = findViewById(R.id.adolescent);
        adult = findViewById(R.id.adult);
        seniorCitizen = findViewById(R.id.seniorCitizen);

        walk = findViewById(R.id.walk);
        wheelChair = findViewById(R.id.wheelChair);
        MyRequestQueue = Volley.newRequestQueue(this);

        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        getUserDetails();

        englishLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibe.vibrate(200);
                englishLang.setBackgroundColor(Color.rgb(47, 200, 173));
                hindiLang.setBackgroundColor(Color.rgb(22, 23, 28));

                englishLang.setTextColor(Color.rgb(0, 0, 0));
                hindiLang.setTextColor(Color.rgb(255, 255, 255));
                updateLanguage("en");
            }
        });
        hindiLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibe.vibrate(200);
                englishLang.setBackgroundColor(Color.rgb(22, 23, 28));
                hindiLang.setBackgroundColor(Color.rgb(47, 200, 173));

                hindiLang.setTextColor(Color.rgb(0, 0, 0));
                englishLang.setTextColor(Color.rgb(255, 255, 255));
                updateLanguage("hi");
            }
        });

        sighted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibe.vibrate(200);
                sighted.setBackgroundColor(Color.rgb(47, 200, 173));
                blind.setBackgroundColor(Color.rgb(22, 23, 28));

                sighted.setTextColor(Color.rgb(0, 0, 0));
                blind.setTextColor(Color.rgb(255, 255, 255));
                updateVisionType("sighted");
            }
        });
        blind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibe.vibrate(200);
                sighted.setBackgroundColor(Color.rgb(22, 23, 28));
                blind.setBackgroundColor(Color.rgb(47, 200, 173));

                sighted.setTextColor(Color.rgb(255, 255, 255));
                blind.setTextColor(Color.rgb(0, 0, 0));
                updateVisionType("blind");
            }
        });

        lessThen5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibe.vibrate(200);
                lessThen5.setBackgroundColor(Color.rgb(47, 200, 173));
                btw5_6.setBackgroundColor(Color.rgb(22, 23, 28));
                moreThen6.setBackgroundColor(Color.rgb(22, 23, 28));

                lessThen5.setTextColor(Color.rgb(0, 0, 0));
                btw5_6.setTextColor(Color.rgb(255, 255, 255));
                moreThen6.setTextColor(Color.rgb(255, 255, 255));
                updateHeight("<5");
            }
        });
        btw5_6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibe.vibrate(200);
                lessThen5.setBackgroundColor(Color.rgb(22, 23, 28));
                btw5_6.setBackgroundColor(Color.rgb(47, 200, 173));
                blind.setBackgroundColor(Color.rgb(22, 23, 28));

                lessThen5.setTextColor(Color.rgb(255, 255, 255));
                btw5_6.setTextColor(Color.rgb(0, 0, 0));
                moreThen6.setTextColor(Color.rgb(255, 255, 255));
                updateHeight("5FT-6FT");
            }
        });
        moreThen6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibe.vibrate(200);
                lessThen5.setBackgroundColor(Color.rgb(22, 23, 28));
                btw5_6.setBackgroundColor(Color.rgb(22, 23, 28));
                moreThen6.setBackgroundColor(Color.rgb(47, 200, 173));

                lessThen5.setTextColor(Color.rgb(255, 255, 255));
                btw5_6.setTextColor(Color.rgb(255, 255, 255));
                moreThen6.setTextColor(Color.rgb(0, 0, 0));
                updateHeight(">6");
            }
        });

        adolescent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibe.vibrate(200);
                adolescent.setBackgroundColor(Color.rgb(47, 200, 173));
                adult.setBackgroundColor(Color.rgb(22, 23, 28));
                seniorCitizen.setBackgroundColor(Color.rgb(22, 23, 28));

                adolescent.setTextColor(Color.rgb(0, 0, 0));
                adult.setTextColor(Color.rgb(255, 255, 255));
                seniorCitizen.setTextColor(Color.rgb(255, 255, 255));
                upadteAgeGroup("adolescent");
            }
        });
        adult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibe.vibrate(200);
                adolescent.setBackgroundColor(Color.rgb(22, 23, 28));
                adult.setBackgroundColor(Color.rgb(47, 200, 173));
                seniorCitizen.setBackgroundColor(Color.rgb(22, 23, 28));

                adolescent.setTextColor(Color.rgb(255, 255, 255));
                adult.setTextColor(Color.rgb(0, 0, 0));
                seniorCitizen.setTextColor(Color.rgb(255, 255, 255));
                upadteAgeGroup("adult");
            }
        });
        seniorCitizen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibe.vibrate(200);
                adolescent.setBackgroundColor(Color.rgb(22, 23, 28));
                adult.setBackgroundColor(Color.rgb(22, 23, 28));
                seniorCitizen.setBackgroundColor(Color.rgb(47, 200, 173));

                adolescent.setTextColor(Color.rgb(255, 255, 255));
                adult.setTextColor(Color.rgb(255, 255, 255));
                seniorCitizen.setTextColor(Color.rgb(0, 0, 0));
                upadteAgeGroup("senior-citizen");
            }
        });

        walk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibe.vibrate(200);
                walk.setBackgroundColor(Color.rgb(47, 200, 173));
                wheelChair.setBackgroundColor(Color.rgb(22, 23, 28));
                upadteNavigationMode("walking");
            }
        });
        wheelChair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibe.vibrate(200);
                walk.setBackgroundColor(Color.rgb(22, 23, 28));
                wheelChair.setBackgroundColor(Color.rgb(47, 200, 173));
                upadteNavigationMode("wheelchair");
            }
        });
    }

    private void getUserDetails() {
        SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
        String token = prefs.getString("token","");
        String id = prefs.getString("id","");
        Log.e("response", "user details  "+token + "  " + id);
        if(token.length() > 0 && id.length() > 0) {
            String loginURL = getApplicationContext().getResources().getString(R.string.user_portfolio_url);
            HashMap<String, String> params = new HashMap<>();
            params.put("token", token);
            params.put("id", id);
            JsonObjectRequest req = new JsonObjectRequest(loginURL, new JSONObject(params) , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.e("res", "res:  "+ response);
                    try {
                        JSONObject data = response.getJSONObject("properties");
                        SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("language",data.getString("language"));
                        editor.putString("visionType",data.getString("visionType"));
                        editor.putString("ageGroup",data.getString("ageGroup"));
                        editor.putString("navigationMode",data.getString("navigationMode"));
                        editor.putString("height",data.getString("height"));
                        editor.commit();
                        updateButtonValue();
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
        }
    }

    private void updateButtonValue() {
        prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
        language = prefs.getString("language", "");
        vision = prefs.getString("visionType", "");
        height = prefs.getString("height", "");
        age = prefs.getString("ageGroup", "");
        navigationMode = prefs.getString("navigationMode", "");

        Log.e("prefs", String.valueOf(prefs));
        if(language.equals("en")) {
            englishLang.setBackgroundColor(Color.rgb(47, 200, 173));
            hindiLang.setBackgroundColor(Color.rgb(22, 23, 28));
            englishLang.setTextColor(Color.rgb(0, 0, 0));
            hindiLang.setTextColor(Color.rgb(255, 255, 255));
        } else if(language.equals("hi")) {
            englishLang.setBackgroundColor(Color.rgb(22, 23, 28));
            hindiLang.setBackgroundColor(Color.rgb(47, 200, 173));
            hindiLang.setTextColor(Color.rgb(0, 0, 0));
            englishLang.setTextColor(Color.rgb(255, 255, 255));
        } else {
            englishLang.setBackgroundColor(Color.rgb(47, 200, 173));
            hindiLang.setBackgroundColor(Color.rgb(22, 23, 28));
            englishLang.setTextColor(Color.rgb(0, 0, 0));
            hindiLang.setTextColor(Color.rgb(255, 255, 255));
        }

        if(vision.equals("sighted")) {
            sighted.setBackgroundColor(Color.rgb(47, 200, 173));
            blind.setBackgroundColor(Color.rgb(22, 23, 28));
            sighted.setTextColor(Color.rgb(0, 0, 0));
            blind.setTextColor(Color.rgb(255, 255, 255));
        } else if(vision.equals("blind")) {
            sighted.setBackgroundColor(Color.rgb(22, 23, 28));
            blind.setBackgroundColor(Color.rgb(47, 200, 173));
            sighted.setTextColor(Color.rgb(255, 255, 255));
            blind.setTextColor(Color.rgb(0, 0, 0));
        } else {
            sighted.setBackgroundColor(Color.rgb(47, 200, 173));
            blind.setBackgroundColor(Color.rgb(22, 23, 28));
            sighted.setTextColor(Color.rgb(0, 0, 0));
            blind.setTextColor(Color.rgb(255, 255, 255));
        }

        if(height.equals("<5")) {
            lessThen5.setBackgroundColor(Color.rgb(47, 200, 173));
            btw5_6.setBackgroundColor(Color.rgb(22, 23, 28));
            moreThen6.setBackgroundColor(Color.rgb(22, 23, 28));
            lessThen5.setTextColor(Color.rgb(0, 0, 0));
            btw5_6.setTextColor(Color.rgb(255, 255, 255));
            moreThen6.setTextColor(Color.rgb(255, 255, 255));
        } else if(height.equals("5FT-6FT")) {
            lessThen5.setBackgroundColor(Color.rgb(22, 23, 28));
            btw5_6.setBackgroundColor(Color.rgb(47, 200, 173));
            moreThen6.setBackgroundColor(Color.rgb(22, 23, 28));
            lessThen5.setTextColor(Color.rgb(255, 255, 255));
            btw5_6.setTextColor(Color.rgb(0, 0, 0));
            moreThen6.setTextColor(Color.rgb(255, 255, 255));
        } else if(height.equals(">6")) {
            lessThen5.setBackgroundColor(Color.rgb(22, 23, 28));
            btw5_6.setBackgroundColor(Color.rgb(22, 23, 28));
            moreThen6.setBackgroundColor(Color.rgb(47, 200, 173));
            lessThen5.setTextColor(Color.rgb(255, 255, 255));
            btw5_6.setTextColor(Color.rgb(255, 255, 255));
            moreThen6.setTextColor(Color.rgb(0, 0, 0));
        } else {
            lessThen5.setBackgroundColor(Color.rgb(47, 200, 173));
            btw5_6.setBackgroundColor(Color.rgb(22, 23, 28));
            moreThen6.setBackgroundColor(Color.rgb(22, 23, 28));
            lessThen5.setTextColor(Color.rgb(0, 0, 0));
            btw5_6.setTextColor(Color.rgb(255, 255, 255));
            moreThen6.setTextColor(Color.rgb(255, 255, 255));
        }

        if(age.equals("adolescent")) {
            adolescent.setBackgroundColor(Color.rgb(47, 200, 173));
            adult.setBackgroundColor(Color.rgb(22, 23, 28));
            seniorCitizen.setBackgroundColor(Color.rgb(22, 23, 28));
            adolescent.setTextColor(Color.rgb(0, 0, 0));
            adult.setTextColor(Color.rgb(255, 255, 255));
            seniorCitizen.setTextColor(Color.rgb(255, 255, 255));
        } else if(age.equals("adult")) {
            adolescent.setBackgroundColor(Color.rgb(22, 23, 28));
            adult.setBackgroundColor(Color.rgb(47, 200, 173));
            seniorCitizen.setBackgroundColor(Color.rgb(22, 23, 28));
            adolescent.setTextColor(Color.rgb(255, 255, 255));
            adult.setTextColor(Color.rgb(0, 0, 0));
            seniorCitizen.setTextColor(Color.rgb(255, 255, 255));
        } else if(age.equals("senior-citizen")) {
            adolescent.setBackgroundColor(Color.rgb(22, 23, 28));
            adult.setBackgroundColor(Color.rgb(22, 23, 28));
            seniorCitizen.setBackgroundColor(Color.rgb(47, 200, 173));
            adolescent.setTextColor(Color.rgb(255, 255, 255));
            adult.setTextColor(Color.rgb(255, 255, 255));
            seniorCitizen.setTextColor(Color.rgb(0, 0, 0));
        } else {
            adolescent.setBackgroundColor(Color.rgb(47, 200, 173));
            adult.setBackgroundColor(Color.rgb(22, 23, 28));
            seniorCitizen.setBackgroundColor(Color.rgb(22, 23, 28));
            adolescent.setTextColor(Color.rgb(0, 0, 0));
            adult.setTextColor(Color.rgb(255, 255, 255));
            seniorCitizen.setTextColor(Color.rgb(255, 255, 255));
        }

        if(navigationMode.equals("walking")) {
            walk.setBackgroundColor(Color.rgb(47, 200, 173));
            wheelChair.setBackgroundColor(Color.rgb(22, 23, 28));

        } else if(navigationMode.equals("wheelchair")) {
            walk.setBackgroundColor(Color.rgb(22, 23, 28));
            wheelChair.setBackgroundColor(Color.rgb(47, 200, 173));
        } else {
            walk.setBackgroundColor(Color.rgb(47, 200, 173));
            wheelChair.setBackgroundColor(Color.rgb(22, 23, 28));
        }
    }

    private void updateLanguage(String text) {
        SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
        String token = prefs.getString("token","");
        String id = prefs.getString("id","");
        if(text.length() > 0) {
            String loginURL = getApplicationContext().getResources().getString(R.string.update_language_url);
            HashMap<String, String> params = new HashMap<>();
            params.put("language", text);
            params.put("id", id);
            JsonObjectRequest req = new JsonObjectRequest(loginURL, new JSONObject(params) , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.e("res", "res:  "+ response);
                    Toast.makeText(NavigationSetting.this, "Language Updated Successfully", Toast.LENGTH_SHORT).show();
                    try {
                        SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("language", text);
                        editor.commit();
                        updateButtonValue();
                    } catch (Exception e) {
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
            errorPopup("Please select the language");
            return;
        }

    }

    private void updateVisionType(String text) {
        SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
        String token = prefs.getString("token","");
        String id = prefs.getString("id","");
        if(token.length() > 0 && id.length() > 0) {
            String loginURL = getApplicationContext().getResources().getString(R.string.update_vision_url);
            HashMap<String, String> params = new HashMap<>();
            params.put("visionType", text);
            params.put("id", id);
            JsonObjectRequest req = new JsonObjectRequest(loginURL, new JSONObject(params) , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("visionType",text);
                        editor.commit();
                        updateButtonValue();
                    } catch (Exception e) {
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

    private void updateHeight(String text) {
        SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
        String token = prefs.getString("token","");
        String id = prefs.getString("id","");
        if(token.length() > 0 && id.length() > 0) {
            String loginURL = getApplicationContext().getResources().getString(R.string.update_height_url);
            HashMap<String, String> params = new HashMap<>();
            params.put("height", text);
            params.put("id", id);
            JsonObjectRequest req = new JsonObjectRequest(loginURL, new JSONObject(params) , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Toast.makeText(NavigationSetting.this, "Height Updated Successfully", Toast.LENGTH_SHORT).show();
                    try {
                        SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("height",text);
                        editor.commit();
                        updateButtonValue();
                    } catch (Exception e) {
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

    private void upadteAgeGroup(String text) {
        SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
        String token = prefs.getString("token","");
        String id = prefs.getString("id","");
        if(token.length() > 0 && id.length() > 0) {
            String loginURL = getApplicationContext().getResources().getString(R.string.update_age_url);
            HashMap<String, String> params = new HashMap<>();
            params.put("ageGroup", text);
            params.put("id", id);
            JsonObjectRequest req = new JsonObjectRequest(loginURL, new JSONObject(params) , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Toast.makeText(NavigationSetting.this, "Age group Updated Successfully", Toast.LENGTH_SHORT).show();
                    try {
                        SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("ageGroup", text);
                        editor.commit();
                        updateButtonValue();
                    } catch (Exception e) {
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

    private void upadteNavigationMode(String text) {
        SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
        String token = prefs.getString("token","");
        String id = prefs.getString("id","");
        Log.e("response", "user details  "+token + "  " + id);
        if(token.length() > 0 && id.length() > 0) {
            String loginURL = getApplicationContext().getResources().getString(R.string.update_navigation_mode_url);
            HashMap<String, String> params = new HashMap<>();
            params.put("navigationMode", text);
            params.put("id", id);
            JsonObjectRequest req = new JsonObjectRequest(loginURL, new JSONObject(params) , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Toast.makeText(NavigationSetting.this, "Navigation mode Updated Successfully", Toast.LENGTH_SHORT).show();
                    try {
                        SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("navigationMode", text);
                        editor.commit();
                        updateButtonValue();
                    } catch (Exception e) {
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

    public void chooseEnglishLang(View view) {

    }

    public void chooseHindiLang(View view) {

    }
}
