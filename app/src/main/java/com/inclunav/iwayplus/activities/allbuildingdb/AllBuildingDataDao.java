package com.inclunav.iwayplus.activities.allbuildingdb;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AllBuildingDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllBuildingData(AllBuildingDataEntity allBuildingDataEntity);

    @Query("SELECT * FROM AllBuilding_data")
    List<AllBuildingDataEntity> getAllBuildingData();
}
