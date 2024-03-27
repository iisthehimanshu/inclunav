package com.inclunav.iwayplus.activities;

import static android.graphics.Color.rgb;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.inclunav.iwayplus.activities.roomdb.AppDatabase;
import com.inclunav.iwayplus.activities.roomdb.BuildingDataEntity;
import com.inclunav.iwayplus.enums.GeometryTypes;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class apifetcher extends Thread {
    static AppDatabase appDatabase;
    String apiUrl;
    String ib;
    BuildingAdapter.ViewHolder holder;
    View v;
    Context context;

    apifetcher(String apiUrl, String ib, BuildingAdapter.ViewHolder holder, View v, Context context) {
        this.apiUrl = apiUrl;
        this.ib = ib;
        this.holder = holder;
        this.v = v;
        this.context = context;
    }


    public void run() {
        appDatabase = BuildingAdapter.appDatabase;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(apiUrl)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responsedata = response.body().string();
                BuildingDataEntity entity = new BuildingDataEntity();
                entity.setBuildingName(ib);
                entity.setResponseData(responsedata);
                appDatabase.buildingDataDao().insertBuildingData(entity);
                ((MainActivity) holder.itemView.getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        holder.isdownloaded.setColorFilter(rgb(0, 255, 0));
                        holder.download.setVisibility(View.GONE);
                        holder.downloadicon.setVisibility(View.GONE);
                        holder.isdownloaded.setContentDescription("you can use this building offline");
                        holder.tint.setVisibility(View.GONE);
                        holder.updatebuilding.setVisibility(View.VISIBLE);
                        holder.updatebuilding.setColorFilter(rgb(128, 170, 255));
                    }
                });
            } else {
                ((MainActivity) holder.itemView.getContext()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(v.getContext(), "There was a server error, please try again later", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        } catch (IOException e) {
            ((MainActivity) holder.itemView.getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(v.getContext(), "You are not connected to the internet, please try again later "+e, Toast.LENGTH_SHORT).show();
                }
            });
            e.printStackTrace();
            // Handle the exception here
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}