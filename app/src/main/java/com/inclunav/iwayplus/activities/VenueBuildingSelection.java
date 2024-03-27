package com.inclunav.iwayplus.activities;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.inclunav.iwayplus.layout_utilities.InstantAutoComplete;
import com.inclunav.iwayplus.R;
import com.inclunav.iwayplus.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

public class VenueBuildingSelection extends AppCompatActivity {

    Navigation objA = new Navigation();

    private InstantAutoComplete venueEditText;
    private ArrayAdapter venueListAdapter;

    private InstantAutoComplete buildingEditText;
    private ArrayAdapter buildingListAdapter;

    private ArrayList<String> venueList = new ArrayList<>();
    private ArrayList<Double> venueData = new ArrayList<>();
    private ArrayList<Double> buildingData = new ArrayList<>();
    private ArrayList<String> buildingList = new ArrayList<>();

    ImageView EraseVenueTextBox, EraseBuildingTextBox;

    public static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;
    private ImageView micButtonVenue, micButtonBuilding;
    String micCount = "0";
    private String base_url;
    private FrameLayout loadingView;
    private double latitude;
    private double longitude;
    private RequestQueue MyRequestQueue;
    private String currentVenue;
    private String currentBuilding;
    private InputMethodManager imm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        base_url = getApplicationContext().getResources().getString(R.string.server_base_url);
        MyRequestQueue = Volley.newRequestQueue(this);

        loadingView = findViewById(R.id.loadingView);
        loadingView.setOnTouchListener((view, motionEvent) -> true);

        venueEditText = findViewById(R.id.venueList);
        venueListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, venueList);
        venueEditText.setAdapter(venueListAdapter);
        venueEditText.setThreshold(1);

        buildingEditText = findViewById(R.id.buildingList);
        buildingListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, buildingList);
        buildingEditText.setAdapter(buildingListAdapter);
        buildingEditText.setThreshold(1);

        EraseVenueTextBox = findViewById(R.id.EraseVenueTextBox);
        EraseBuildingTextBox = findViewById(R.id.EraseBuildingTextBox);

        EraseBuildingTextBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildingEditText.setText("");
            }
        });

        EraseVenueTextBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                venueEditText.setText("");
            }
        });

        micButtonVenue = findViewById(R.id.mic_button_venue);
        micButtonBuilding = findViewById(R.id.mic_button_building);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                Log.e("onReadyForSpeech", " : ");

            }

            @Override
            public void onBeginningOfSpeech() {
                Log.e("onBeginningOfSpeech", " : ");
                if(micCount.equals("1")) {
                    venueEditText.setText("");
                    venueEditText.setHint("Listening...");
                } else if(micCount.equals("2")) {
                    buildingEditText.setText("");
                    buildingEditText.setHint("Listening...");
                } else if(micCount.equals("0")) {
                    Log.e("at 0", " :  nothing to do");
                }
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                Log.e("onResults", " : ");
                if(micCount.equals("1")) {
                    micButtonVenue.setImageResource(R.drawable.ic_baseline_mic_off_24);
                    ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    venueEditText.setText(data.get(0));
                } else if(micCount.equals("2")) {
                    micButtonBuilding.setImageResource(R.drawable.ic_baseline_mic_off_24);
                    ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    buildingEditText.setText(data.get(0));
                } else if(micCount.equals("0")) {
                    Log.e("at 0", " : ");
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        micButtonVenue.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.e("mtion", " " + motionEvent.getAction() +  MotionEvent.ACTION_UP);
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    micCount="0";
                    Log.e("onCLick", " : 1 : ACTION_UP");
                    speechRecognizer.stopListening();
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    micCount="1";
                    micButtonVenue.setImageResource(R.drawable.ic_baseline_mic_24);
                    speechRecognizer.startListening(speechRecognizerIntent);
                    Log.e("onCLick", " : 1 : ACTION_DOWN");
                }
                return false;
            }
        });

        micButtonBuilding.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    speechRecognizer.stopListening();
                    micCount="0";
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    micCount="2";
                    micButtonBuilding.setImageResource(R.drawable.ic_baseline_mic_24);
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
                return false;
            }
        });
        imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        getVenueList();
    }

    private void getVenueList() {
        String vURL = base_url+"v1/app/venue-list";
        loadingView.setVisibility(View.VISIBLE);
        StringRequest req = new StringRequest(Request.Method.POST, vURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response)
                    {
                        loadingView.setVisibility(View.GONE);
                        try {
                            JSONObject jsonObject= new JSONObject(response.toString());
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            for(int i=0;i<jsonArray.length();i++){
                                JSONObject node = jsonArray.getJSONObject(i);
                                String venueName = node.getString("venueName");
                                JSONArray coordinatesJSON = node.getJSONArray("coordinates");
                                double lat = (double) coordinatesJSON.get(0);
                                double lng = (double) coordinatesJSON.get(1);
                                double d =  distance(latitude, longitude, lat, lng);
                                StringBuilder venueNameNew = new StringBuilder();
                                for (String s : Utils.splitCamelCaseString(venueName)) {
                                    venueNameNew.append(s).append(" ");
                                }
                                Log.e("distance", d + ", " + venueNameNew.toString() );
                                venueData.add(d);
                                venueList.add(venueNameNew.toString().trim());
                            }
                            if(venueList.size() > 0 ) {
                                getNearestVenue();
                            }
                            venueListAdapter = new ArrayAdapter<String>(VenueBuildingSelection.this,android.R.layout.simple_dropdown_item_1line, venueList);
                            venueEditText.setAdapter(venueListAdapter);
                            venueListAdapter.notifyDataSetChanged();
                            loadingView.setVisibility(View.GONE);
                            initVenueListClicks();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingView.setVisibility(View.GONE);
                        if (error instanceof NetworkError) {
                            retryPopupVenueList("Cannot connect to Internet. Please check your connection!");
                        } else if (error instanceof ServerError) {
                            retryPopupVenueList("Server error!");
                        } else if (error instanceof AuthFailureError) {
                            retryPopupVenueList("Server error!");
                        } else if (error instanceof ParseError) {
                            retryPopupVenueList("Some error occured!");
                        } else if (error instanceof NoConnectionError) {
                            retryPopupVenueList("Cannot connect to Internet...Please check your connection!");
                        } else if (error instanceof TimeoutError) {
                            retryPopupVenueList("Connection TimeOut! Please check your internet connection.");
                        }
                        else{
                            retryPopupVenueList("Server error occured!");
                        }
                    }
                });

        req.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        req.setShouldCache(false);
        req.setTag(this);
        MyRequestQueue.getCache().clear();
        MyRequestQueue.add(req);
    }

    private double distance (double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private void getNearestVenue() {
        Log.e("getNearestVenue", "venueList ; " + venueList + ", " + currentVenue);
        double min = (double) venueData.get(0);
        int index = 0;
        for(int i=0;i<venueData.size();i++) {
            if(venueData.get(i) < min) {
                min = venueData.get(i);
                index = i;
            }
        }
        if(min < 1.0) {


            currentVenue = venueList.get(index).trim();
            venueEditText.setHint("Change Venue");
            currentBuilding = null;
            buildingList.clear();
            hideKeyBoard();
            String selectedVenue = venueList.get(index);
            StringBuilder actualVenueName = new StringBuilder();
            for (String s : selectedVenue.split(" ")) {
                actualVenueName.append(s);
            }
            getBuildingList(actualVenueName.toString(), "NV");
        }
    }

    private void initVenueListClicks() {
        venueEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String sourceName = adapterView.getItemAtPosition(i).toString();
                Log.e("Venue Souce", sourceName);
                currentVenue = sourceName.trim();
                buildingEditText.setText("");
                venueEditText.setHint("Change Venue");
                currentBuilding = null;
                buildingList.clear();
                hideKeyBoard();
                getBuildingList(currentVenue, "CV");
            }
        });
        //        if the input is not in the list then show error icon on the right
        venueEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                String currInput = venueEditText.getText().toString();
                if( !currInput.equals("") && currInput.length()>0 && !venueList.contains(currInput)){
                    venueEditText.setError("Please select from the dropdown");
                }
            }
        });
    }

    private void  getBuildingList(String venue, String venueType) {
        String building_list_url = base_url+"v1/app/building-list";
        HashMap<String, String> params = new HashMap<>();
        params.put("venueName", venue);
        loadingView.setVisibility(View.VISIBLE);
        JsonObjectRequest req = new JsonObjectRequest(building_list_url, new JSONObject(params),
                response -> {
                    loadingView.setVisibility(View.GONE);
                    Log.e("respomse", "res : " + response + ", venueType : " + venueType);
                    try {
                        if(response.getBoolean("success")){
                            //save to sharedPrefs and lead to PermissionRequestActivity
                            JSONArray data = response.getJSONArray("data");
                            for(int j=0;j<data.length();j++){
                                String nodeName = data.getJSONObject(j).getString("buildingName");
                                buildingList.add(nodeName);
                                double lat = data.getJSONObject(j).getDouble("lat");
                                double lng = data.getJSONObject(j).getDouble("lng");
                                double d =  distance(latitude, longitude, lat, lng);
                                buildingData.add(d);
                            }
                            if(venueType == "NV" && buildingList.size() > 0) {
                                getNearestBuilding();
                            }
                            Collections.sort(buildingList); //search is easier when list is sorted
                            buildingListAdapter = new ArrayAdapter<String>(VenueBuildingSelection.this,android.R.layout.simple_dropdown_item_1line, buildingList);
                            buildingEditText.setAdapter(buildingListAdapter);
                            buildingListAdapter.notifyDataSetChanged();
                            initBuildingListClicks();
                        }
                        else{
                            errorPopup(response.getString("error"));
                        }
                    } catch (JSONException e) {
                        errorPopup("Some error occurred!");
                        e.printStackTrace();
                    }
                }, error -> {
            loadingView.setVisibility(View.GONE);
            if (error instanceof NetworkError) {
                errorPopup("Cannot connect to Internet. Please check your connection!");
            } else if (error instanceof ServerError) {
                errorPopup("Server error!");
            } else if (error instanceof AuthFailureError) {
                errorPopup("Server error!");
            } else if (error instanceof ParseError) {
                errorPopup("Some error occurred!");
            } else if (error instanceof NoConnectionError) {
                errorPopup("Cannot connect to Internet...Please check your connection!");
            } else if (error instanceof TimeoutError) {
                errorPopup("Connection TimeOut! Please check your internet connection.");
            }
            else{
                errorPopup("Server error occurred!");
            }
        });
        req.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        req.setShouldCache(false);
        req.setTag(this);
        MyRequestQueue.getCache().clear();
        MyRequestQueue.add(req);
    }

    private void getNearestBuilding() {
        double min = buildingData.get(0);
        int index = 0;
        for(int i=0;i<buildingData.size();i++) {
            if(buildingData.get(i) < min) {
                min = buildingData.get(i);
                index = i;
            }
        }
        if(min < 1.0) {
            currentBuilding = buildingList.get(index).trim();
//            topHeaderText.setText(currentBuilding +  ", "+ currentVenue);
            buildingEditText.setHint("Change Building");
            hideKeyBoard();
//            getBuildingInfo();
        }
    }

    private void initBuildingListClicks() {
        buildingEditText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String sourceName = adapterView.getItemAtPosition(i).toString();
                currentBuilding = sourceName;
                buildingEditText.setHint("Change Building");
                hideKeyBoard();
            }
        });
        //        if the input is not in the list then show error icon on the right
        buildingEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                String currInput = buildingEditText.getText().toString();
                if( !currInput.equals("") && currInput.length()>0 && !buildingList.contains(currInput)){
                    buildingEditText.setError("Please select from the dropdown");
                }
            }
        });
    }

    private void retryPopupVenueList(String errorMessage){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage(errorMessage);
        builder.setPositiveButton("Retry", (dialog, id) -> {
            // User clicked OK button
            getVenueList(); //resend the venue request
        });
        builder.setNegativeButton("Go Back", (dialog,id)-> finish());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void hideKeyBoard(){
        //Hide:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            try {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }catch (Exception ignore){};
        }
        if(imm.isAcceptingText()){
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
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
    }
}