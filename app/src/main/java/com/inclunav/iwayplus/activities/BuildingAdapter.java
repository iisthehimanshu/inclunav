package com.inclunav.iwayplus.activities;

import static android.graphics.Color.rgb;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Network;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;


import com.inclunav.iwayplus.R;
import com.inclunav.iwayplus.activities.roomdb.AppDatabase;
import com.inclunav.iwayplus.activities.apifetcher;
import com.inclunav.iwayplus.activities.roomdb.BuildingDataEntity;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class BuildingAdapter extends RecyclerView.Adapter<BuildingAdapter.ViewHolder> {

    static AppDatabase appDatabase;

    private static Vibrator vibe;

    static String buildingNamenew=null;
    static String initialBuildingName=null;
    static String venueNamenew=null;
    static String initialVenueName=null;
    static double latitudenew;
    static double longitudenew;


    private static Context context;
    private static List<Map<String, String>> buildingDataList = null;

    public BuildingAdapter(Context context, List<Map<String, String>> buildingDataList) {
        this.context = context;
        this.buildingDataList = buildingDataList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        appDatabase = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "app-database").build();
        View view = LayoutInflater.from(context).inflate(R.layout.item_building, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, String> buildingData = buildingDataList.get(position);
        String initialBuildingName = buildingData.get("initialBuildingName");
        String initialVenueName = buildingData.get("initialVenueName");
        new AsyncTask<Void, Void, BuildingDataEntity>() {
            @Override
            protected BuildingDataEntity doInBackground(Void... voids) {
                // Retrieve data from the Room Database here
                return appDatabase.buildingDataDao().getBuildingDataByBuildingName(initialBuildingName);
            }

            @Override
            protected void onPostExecute(BuildingDataEntity cachedData) {
                if (cachedData != null) {
                    holder.isdownloaded.setColorFilter(rgb(0,255,0));
                    holder.isdownloaded.setContentDescription("you can use this building offline");
                    holder.download.setVisibility(View.GONE);
                    holder.downloadicon.setVisibility(View.GONE);


                    holder.tint.setVisibility(View.GONE);
                    holder.updatebuilding.setVisibility(View.VISIBLE);
                    holder.updatebuilding.setColorFilter(rgb(128, 170, 255));
                }
            }
        }.execute();

        holder.buildingNameTextView.setText(buildingData.get("buildingName"));
        holder.venueNameTextView.setText(buildingData.get("venueName"));
        holder.buildingNameTextView.setContentDescription(buildingData.get("buildingName"));
        holder.venueNameTextView.setContentDescription(buildingData.get("venueName"));
        String distance = buildingData.get("distance") + " m";
        holder.distance.setText(distance);
        holder.distance.setContentDescription(distance + "meters");

        //Picasso.get().load("https://dev.iwayplus.in/uploads/"+buildingData.get("photo")).into(holder.buildingImageView);
        holder.downloadicon.setVisibility(View.VISIBLE);
        holder.updatebuilding.setContentDescription("update data for " + buildingData.get("buildingName") );
        holder.download.setContentDescription("download data for " + buildingData.get("buildingName") );
        holder.isdownloaded.setContentDescription("You can access this building online only");
        if(buildingData.get("buildingName").equals("EMPOWER 2023")){
            holder.isdownloaded.setVisibility(View.GONE);
            holder.updatebuilding.setVisibility(View.GONE);
            holder.download.setVisibility(View.GONE);
            holder.downloadicon.setVisibility(View.GONE);
            holder.tint.setVisibility(View.GONE);
            holder.cal.setVisibility(View.VISIBLE);
            holder.loc.setVisibility(View.VISIBLE);
            holder.distance.setText("IIT Madras");

        }
        if(buildingData.get("buildingName").equals("IITM Research Park")){
            holder.cal.setVisibility(View.GONE);
            holder.loc.setVisibility(View.GONE);
            Random random = new Random();
            int randomDistance = random.nextInt(4) + 15;
            holder.distance.setText(randomDistance + " m");

        }
        holder.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("entered", "downloading building data");
                Toast.makeText(v.getContext(), "Downloading data for " + buildingData.get("buildingName"), Toast.LENGTH_SHORT).show();
                Log.d("roomdbb", "onClick: ");
                String apiUrl =
                        //"https://inclunav.apps.iitd.ac.in/node/wayfinding/v1/app/android-navigation/ResearchPark/ResearchParkMain/null";
                        "https://annotation.iwayplus.in/wayfinding/v1/app/android-navigation/"+initialVenueName+"/"+initialBuildingName+"/"+"null";
                apifetcher obj = new apifetcher(apiUrl,initialBuildingName, holder, v, context);
                obj.start();

            }
        });
        holder.updatebuilding.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("entered", "updating building data");
                Toast.makeText(v.getContext(), "Updating data for " + buildingData.get("buildingName"), Toast.LENGTH_SHORT).show();
                Log.d("roomdbb", "onClick: ");
                String apiUrl =
                        //"https://inclunav.apps.iitd.ac.in/node/wayfinding/v1/app/android-navigation/ResearchPark/ResearchParkMain/null";
                        "https://annotation.iwayplus.in/wayfinding/v1/app/android-navigation/"+initialVenueName+"/"+initialBuildingName+"/"+"null";
                apifetcher obj = new apifetcher(apiUrl,initialBuildingName, holder, v, context);
                obj.start();

            }
        });

    }


    @Override
    public int getItemCount() {
        return buildingDataList.size();
    }

    public void setData(List<Map<String, String>> filteredBuildingDataList) {
        buildingDataList = filteredBuildingDataList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageButton updatebuilding;
        ImageView imgView;
        TextView buildingNameTextView;
        TextView venueNameTextView;
        TextView distance;
        ImageView buildingImageView;
        ImageButton download;
        ImageView downloadicon;
        View tint;
        ImageView isdownloaded;
        ImageView loc;
        ImageView cal;

        public ViewHolder(View itemView) {
            super(itemView);
            updatebuilding = itemView.findViewById(R.id.updatebuilding);
            updatebuilding.setVisibility(View.GONE);
            imgView = itemView.findViewById(R.id.imageView3);
            imgView.setVisibility(View.GONE);
            buildingNameTextView = itemView.findViewById(R.id.building_name);
            venueNameTextView = itemView.findViewById(R.id.venue_name);
            buildingImageView = itemView.findViewById(R.id.building_image);
            tint = itemView.findViewById(R.id.tint);
            distance = itemView.findViewById(R.id.distance);
            download = itemView.findViewById(R.id.downloadbuilding);
            downloadicon = itemView.findViewById(R.id.downloadicon);
            download.setVisibility(View.VISIBLE);
            downloadicon.setVisibility(View.VISIBLE);
            isdownloaded = itemView.findViewById(R.id.isdownloaded);
            loc = itemView.findViewById(R.id.loc);
            cal = itemView.findViewById(R.id.cal);
            loc.setVisibility(View.GONE);
            cal.setVisibility(View.GONE);

            // Add an OnClickListener to the root view (the card)
            itemView.setOnClickListener(this);

            for(int i = 0; i<buildingDataList.size();i++){
                Map<String, String> clickedBuildingData = buildingDataList.get(i);
                String initialBuildingName = clickedBuildingData.get("initialBuildingName");

            }

        }



        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                // Get the clicked item's data
                Map<String, String> clickedBuildingData = buildingDataList.get(position);

                if(clickedBuildingData.get("buildingName").equals("EMPOWER 2023")){
                    String url = "https://conference.iwayplus.in/";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    view.getContext().startActivity(intent);
                }else {
                    // Access the buildingName for the clicked card
                    buildingNamenew = clickedBuildingData.get("buildingName");
                    venueNamenew = clickedBuildingData.get("venueName");
                    initialBuildingName = clickedBuildingData.get("initialBuildingName");
                    initialVenueName = clickedBuildingData.get("initialVenueName");
                    latitudenew=Double.parseDouble(clickedBuildingData.get("latitude"));
                    longitudenew=Double.parseDouble(clickedBuildingData.get("longitude"));






//                vibe.vibrate(200);
                    Intent intent = new Intent(view.getContext(), Navigation.class);
                    view.getContext().startActivity(intent);

                    // Now you have the buildingName, and you can use it as needed
                    // For example, you can display it in a Toast message
                    Toast.makeText(view.getContext(), "Clicked Building: " + buildingNamenew, Toast.LENGTH_SHORT).show();

                    // Or you can pass it to another activity or perform any desired action
                }

            }
        }

    }




}


