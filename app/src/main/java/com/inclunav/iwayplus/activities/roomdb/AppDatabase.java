package com.inclunav.iwayplus.activities.roomdb;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.inclunav.iwayplus.activities.allbuildingdb.AllBuildingDataDao;
import com.inclunav.iwayplus.activities.allbuildingdb.AllBuildingDataEntity;
@Database(entities = {BuildingDataEntity.class, AllBuildingDataEntity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;


    public abstract AllBuildingDataDao allBuildingDataDao();

    public abstract BuildingDataDao buildingDataDao();


    // Singleton pattern to ensure only one instance of the database is created
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "app-database")
                    .fallbackToDestructiveMigration() // Use this only during development, it wipes out and recreates the database if the schema changes.
                    .build();
        }
        return instance;
    }
}

