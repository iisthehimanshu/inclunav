package com.inclunav.iwayplus.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.inclunav.iwayplus.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.views.MapView;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.speech.tts.TextToSpeech.QUEUE_ADD;

public class DashBoard  extends AppCompatActivity {

    private GoogleMap mMap;

    private static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;
    private RequestQueue MyRequestQueue;
    private Vibrator vibe;

    private static TextToSpeech textToSpeech;
    private boolean ttsInitialized = false;
    private String audio_language;

    String Address;
    Double latitude, longitude;
    private FrameLayout loadingView2;

    private MapView map;
    private IMapController mapController;

    private static final String TAG = "OsmActivity";


    private static final int PERMISSION_REQUEST_CODE = 1;
    Button navigate, explore;
    TextView currentlocation, header_title, header_icon;
    private FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        navigate = findViewById(R.id.navigate);
        explore = findViewById(R.id.explore);
        header_title = findViewById(R.id.header_title);
        header_icon = findViewById(R.id.header_icon);
        currentlocation = findViewById(R.id.currentlocation);
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);

        SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
        audio_language = prefs.getString("language", "en");
        MyRequestQueue = Volley.newRequestQueue(this);
        navigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibe.vibrate(200);
                Toast.makeText(getApplicationContext(), "Please wait", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(DashBoard.this, MainActivity.class);
                startActivity(i);
            }
        });

        explore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibe.vibrate(200);
                String url = "https://conference.iwayplus.in/";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });

        header_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibe.vibrate(200);
                boolean isUsrExist = prefs.getBoolean("userExist", false);
                Intent i;
                if (isUsrExist) {
                    i = new Intent(DashBoard.this, ProfileSetting.class);
                }
                else {
                    i = new Intent(DashBoard.this, ProfileSetting.class);
                }
                startActivity(i);
            }
        });
        getUserDetails();
    }

//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//        initializeTTS();
//    }

    private void initializeTTS() {
        textToSpeech = new TextToSpeech(getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int ttsLang;
                if(audio_language.equals("en")){
                    ttsLang = textToSpeech.setLanguage(Locale.US);
                }
                else{
                    ttsLang = textToSpeech.setLanguage(new Locale("hi"));
                }

                if (ttsLang == TextToSpeech.LANG_MISSING_DATA
                        || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "The Language is not supported!");
                } else {
                    Log.i("TTS", "Language Supported.");
                }
                Log.i("TTS", "Initialization success. ");
                ttsInitialized = true;
                getLocation();
            } else {
                Log.e("TTS", "TTS initialization failed");
            }
        });
    }
    public void getLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        double lat = location.getLatitude();
                        double longi = location.getLongitude();

                        LatLng sydney = new LatLng(lat, longi);
                        mMap.addMarker(new MarkerOptions()
                                .position(sydney)
                                .title("Marker in Sydney"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo( 15.0f));
                        getAddress(lat, longi);
                    }
                }
            });
    };



    public static void speakTTS(String toSpeak){
        Log.e("speakTTS", ": "+ toSpeak);
        int speechStatus;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            speechStatus = textToSpeech.speak(toSpeak, QUEUE_ADD,null,null);
        }
        else{
            speechStatus = textToSpeech.speak(toSpeak, QUEUE_ADD,null);
        }

        if (speechStatus == TextToSpeech.ERROR) {
            Log.e("TTS", "Error in converting Text to Speech!");
        }
    }

    @Override
    public void onDestroy(){
        if(ttsInitialized) textToSpeech.stop();
        MyRequestQueue.cancelAll(this);
        super.onDestroy();
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
                        String name = String.valueOf(response.getString("name").trim().charAt(0));
                        header_icon.setText(name.toUpperCase());
                        header_title.setText("Welcome " + String.valueOf(response.getString("name").toUpperCase()));
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

    public void getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(DashBoard.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            Address = obj.getAddressLine(0);
            Log.e("IGA", "Address : " + Address+ " + " +ttsInitialized);
            if(Address.length() > 0) {
                currentlocation.setTextColor(Color.WHITE);
                currentlocation.setText(Address);
//                if(ttsInitialized) speakTTS("You are at " +Address);
            }else {
                currentlocation.setText("Location not found. Please reload your location services");
                currentlocation.setTextColor(Color.RED);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void share(View view) {
        if(Address.length() <= 0){
            Toast.makeText(DashBoard.this,"unable to share your location",Toast.LENGTH_SHORT).show();
            return;
        } else {
            String shareBodyText = null;
            if(Address != null && Address.length() > 0){
                shareBodyText = "Hi, My location is "+Address;
            }
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareSubText = "share your location";
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubText);
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareBodyText);
            startActivity(Intent.createChooser(shareIntent, "Share With"));
        }
    }

    public void openSettings(View view) {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    public void goBack(View view) {
        onBackPressed();
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
