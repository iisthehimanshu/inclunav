package com.inclunav.iwayplus.activities;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.inclunav.iwayplus.activities.allbuildingdb.AllBuildingDataEntity;
import com.inclunav.iwayplus.activities.roomdb.AppDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class NewDashboard  {
    static List<Map<String, String>> NewBuildingData = new ArrayList<>();
    static double userlatt;
    static double userlongg;



    public static class MyHttpPostTask extends AsyncTask<Void, Void, List<Map<String, String>>> {

        static AppDatabase appDatabase;
        private Context context;

        private List<Map<String, String>> buildingDataList;
        private BuildingAdapter adapter;

        public static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
            // Radius of the Earth in meters
            double earthRadius = 6371000; // meters

            // Convert latitude and longitude from degrees to radians
            lat1 = Math.toRadians(lat1);
            lon1 = Math.toRadians(lon1);
            lat2 = Math.toRadians(lat2);
            lon2 = Math.toRadians(lon2);

            // Haversine formula
            double dlat = lat2 - lat1;
            double dlon = lon2 - lon1;
            double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            // Calculate the distance
            double distan = earthRadius * c;

            return distan;
        }

        public MyHttpPostTask(List<Map<String, String>> buildingDataList, BuildingAdapter adapter, Context context) {
            this.buildingDataList = buildingDataList;
            this.adapter = adapter;
            this.context = context;
            appDatabase = AppDatabase.getInstance(context);
        }



        @Override
        protected List<Map<String, String>> doInBackground(Void... params) {

            List<AllBuildingDataEntity> existingData = appDatabase.allBuildingDataDao().getAllBuildingData();
            Log.d("rooooomdbb", "doInBackground: present data " + existingData);
            if(!existingData.isEmpty()){
                for(AllBuildingDataEntity entity : existingData){
                    Log.d("rooooomdbb", "doInBackground: present data " + entity);
                    Map<String, String> buildingData = new HashMap<>();

                    buildingData.put("buildingName", entity.getBuildingName());
                    buildingData.put("venueName", entity.getVenueName());
                    buildingData.put("photo", entity.getPhoto());
                    buildingData.put("initialBuildingName", entity.getInitialBuildingName());
                    buildingData.put("initialVenueName", entity.getInitialVenueName());

                    List<Double> coordinates = entity.getCoordinates();
                    if (coordinates != null && coordinates.size() == 2) {
                        buildingData.put("latitude", String.valueOf(coordinates.get(0)));
                        buildingData.put("longitude", String.valueOf(coordinates.get(1)));
//                            double d = haversineDistance(userlatt,userlongg,coordinates.get(0),coordinates.get(1));
//                            buildingData.put("distance", String.valueOf(d));

                    }
                    NewBuildingData.add(buildingData);
                }
            }else{
                Log.d("rooooomdbb", "doInBackground: fetching data");
                String apiUrl = "https://dev.iwayplus.in/secured/building/all";

                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json"); // Set content type if needed
                    conn.setRequestProperty("x-access-token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI2NjE2MzgzZjU5MGIyNmRlNzFiYWIwMmUiLCJyb2xlcyI6WyJ1c2VyIl0sImlhdCI6MTcxMjczMjIyMywiZXhwIjoxNzEyNzMzMDYzfQ.ic7K2nKyKhT2qvx3YZSyYpPiiBNR7DxnKWlp1AjKSOY");

                    int responseCode = conn.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String inputLine;

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }

                        in.close();

                        // Parse JSON response using Gson
                        Gson gson = new Gson();
                        List<Map<String, Object>> data = gson.fromJson(response.toString(), List.class);
                        AllBuildingDataEntity entity = new AllBuildingDataEntity();

                        // Iterate through the JSON data and extract the fields you need
                        for (Map<String, Object> map : data) {
                            Map<String, String> buildingData = new HashMap<>();
                            entity.set_id((String)map.get("_id"));
                            buildingData.put("buildingName", (String) map.get("buildingName"));
                            entity.setBuildingName((String) map.get("buildingName"));
                            buildingData.put("venueName", (String) map.get("venueName"));
                            entity.setVenueName((String) map.get("venueName"));
                            buildingData.put("photo", (String) map.get("photo"));
                            entity.setPhoto((String) map.get("photo"));
                            buildingData.put("initialBuildingName", (String) map.get("initialBuildingName"));
                            entity.setInitialBuildingName((String) map.get("initialBuildingName"));
                            buildingData.put("initialVenueName", (String) map.get("initialVenueName"));
                            entity.setInitialVenueName((String) map.get("initialVenueName"));
                            List<Double> coordinates = (List<Double>) map.get("coordinates");
                            entity.setCoordinates((List<Double>) map.get("coordinates"));
                            Log.d("buildingdataaaa",buildingData.toString());
                            if (coordinates != null && coordinates.size() == 2) {
                                buildingData.put("latitude", String.valueOf(coordinates.get(0)));
                                buildingData.put("longitude", String.valueOf(coordinates.get(1)));
//                            double d = haversineDistance(userlatt,userlongg,coordinates.get(0),coordinates.get(1));
//                            buildingData.put("distance", String.valueOf(d));

                            }
                            if((Boolean)map.get("liveStatus")){
                                appDatabase.allBuildingDataDao().insertAllBuildingData(entity);
                                NewBuildingData.add(buildingData);
                            }
                        }

                        return NewBuildingData;
                    } else {
                        // Handle the error, e.g., by returning an error message
                        return null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    // Handle exceptions here
                    return null;
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Map<String, String>> result) {
            // This method will be called when the HTTP POST request is complete
            // You can process the 'result' list of maps here
            if (result != null) {
                for (Map<String, String> buildingData : result) {
                    // Clear the buildingDataList and add the sorted data
                    buildingDataList.clear();
                    buildingDataList.addAll(NewBuildingData);
                    adapter.notifyDataSetChanged();
                    // Do something with the extracted data
                }
            } else {
                // Handle the case where the request failed
            }
        }
    }
}