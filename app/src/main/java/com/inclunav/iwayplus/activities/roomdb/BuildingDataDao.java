package com.inclunav.iwayplus.activities.roomdb;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface BuildingDataDao {

    // Insert a single BuildingDataEntity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBuildingData(BuildingDataEntity buildingDataEntity);

    // Retrieve BuildingDataEntity by building name
    @Query("SELECT * FROM building_data WHERE buildingName = :buildingName")
    BuildingDataEntity getBuildingDataByBuildingName(String buildingName);

    // You can define other database operations as needed, such as getting all data or deleting data.
}
