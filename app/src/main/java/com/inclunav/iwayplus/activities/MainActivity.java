package com.inclunav.iwayplus.activities;



import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.inclunav.iwayplus.R;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements LocationHelper.LocationListener {
    public static String desna;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    public Context context = this;
    private List<Map<String, String>> filteredBuildingDataList;
    private RecyclerView recyclerView;
    private BuildingAdapter adapter;
    private List<Map<String, String>> buildingDataList;
    private LocationHelper locationHelper;
    private Vibrator vibe;

    ImageButton header_icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
        header_icon = findViewById(R.id.header_icon);

        recyclerView = findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this, calculateSpanCount());
        recyclerView.setLayoutManager(layoutManager);
        // Initialize the adapter with an empty list
        buildingDataList = new ArrayList<>();
        adapter = new BuildingAdapter(this, buildingDataList);
        recyclerView.setAdapter(adapter);

        // Initialize the location helper
        locationHelper = new LocationHelper(this, this);

        new NewDashboard.MyHttpPostTask(buildingDataList, adapter, MainActivity.this).execute();

        // Call the AsyncTask to initiate the HTTP requestc
        // Check and request location permission
        if (checkLocationPermission()) {
            locationHelper.startLocationUpdates();
        } else {
            requestLocationPermission();
        }

        header_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isUsrExist = prefs.getBoolean("userExist", false);
                Intent i;
                if (isUsrExist) {
                    i = new Intent(MainActivity.this, ProfileSetting.class);
                }
                else {
                    i = new Intent(MainActivity.this, ProfileSetting.class);
                }
                startActivity(i);
            }
        });

        filteredBuildingDataList = new ArrayList<>(buildingDataList);

        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            Uri deepLinkUri = intent.getData();

            // Extract parameters from the deep link
            String sc = Uri.decode(deepLinkUri.getQueryParameter("sc"));
            String fl = Uri.decode(deepLinkUri.getQueryParameter("fl"));
            Log.d("testuhh", "onCreate: " + sc + fl);
            // Now, you have the values of param1 and param2
            if (sc != null ) {

                desna = sc +" (L"+fl+")";
                Log.d("testuhh", "onCreate: " + desna);
                BuildingAdapter.initialVenueName = "IITMRP";
                BuildingAdapter.initialBuildingName = "ResearchParkNew";
                BuildingAdapter.buildingNamenew = "IITM Research Park";
                BuildingAdapter.venueNamenew = "IITMRP";
                Toast.makeText(context, "Clicked Building: IITM Research Park", Toast.LENGTH_SHORT).show();
                Intent intenttonav = new Intent(this, Navigation.class);
                context.startActivity(intenttonav);

            }
        }
    }

    private int calculateSpanCount() {
        // Get the screen width
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float screenWidth = outMetrics.widthPixels;

        // Calculate the desired item width (you can adjust this value)
        float itemWidth = getResources().getDimension(R.dimen.item_building);

        // Calculate the span count based on screen width and item width
        int spanCount = (int) (screenWidth / itemWidth);

        // Ensure at least 1 column
        return Math.max(spanCount, 1);
    }

    private void filter(String text) {
        // Clear the filtered list
        filteredBuildingDataList.clear();

        // If the text is empty, show all items
        if (TextUtils.isEmpty(text)) {
            filteredBuildingDataList.addAll(buildingDataList);
        } else {
            // Filter the data based on the input text
            text = text.toLowerCase().trim();
            for (Map<String, String> buildingData : buildingDataList) {
                String buildingName = buildingData.get("buildingName").toLowerCase();
                String venueName = buildingData.get("venueName").toLowerCase();
                if (buildingName.contains(text) || venueName.contains(text)) {
                    filteredBuildingDataList.add(buildingData);
                }
            }
        }

        // Update the RecyclerView adapter with the filtered data
        adapter.setData(filteredBuildingDataList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLocationReceived(Location location) {
        // Handle the received location here
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        if(NewDashboard.MyHttpPostTask.haversineDistance(NewDashboard.userlatt,NewDashboard.userlongg,latitude,longitude) > 500){
            NewDashboard.userlatt = latitude;
            NewDashboard.userlongg = longitude;
            updateDistancesAndData();
            buildingDataList.clear();
            buildingDataList.addAll(NewDashboard.NewBuildingData);
            try {
                buildingDataList.sort(Comparator.comparing(map -> Double.parseDouble(map.get("distance"))));
            } catch (ClassCastException e) {
                Log.d("errorr", "Error: Unable to cast 'distance' values to String.");
                e.printStackTrace();
            } catch (Exception e) {
                Log.d("errorr", "An unexpected error occurred while sorting.");
                e.printStackTrace();
            }
            adapter.notifyDataSetChanged();
            Log.d("dahim", "onLocationReceived: "+longitude);
        }
    }

    private void updateDistancesAndData() {
        for (Map<String, String> buildingData : buildingDataList) {
            double beaconLatitude = Double.parseDouble(buildingData.get("latitude"));
            double beaconLongitude = Double.parseDouble(buildingData.get("longitude"));
            double distance = NewDashboard.MyHttpPostTask.haversineDistance(NewDashboard.userlatt, NewDashboard.userlongg, beaconLatitude, beaconLongitude);
            distance = (int) distance;
            buildingData.put("distance", String.format("%.2f", distance) );
        }
    }

    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationHelper.startLocationUpdates();
            } else {
                // Handle the case when the user denies location permission
            }
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        locationHelper.stopLocationUpdates();
    }

    public void openSettings(View view) {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }


}
