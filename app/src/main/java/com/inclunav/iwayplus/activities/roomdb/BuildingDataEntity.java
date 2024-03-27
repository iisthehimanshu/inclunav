package com.inclunav.iwayplus.activities.roomdb;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "building_data")
public class BuildingDataEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String buildingName;
    private String responseData;

    // Getter and Setter methods for your fields (id, buildingName, responseData)

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }
}
