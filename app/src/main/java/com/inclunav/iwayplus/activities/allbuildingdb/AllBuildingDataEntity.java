package com.inclunav.iwayplus.activities.allbuildingdb;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;

@Entity(tableName = "AllBuilding_data")
public class AllBuildingDataEntity {
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "_id")
    private String _id;

    @ColumnInfo(name = "initialBuildingName")
    private String initialBuildingName;

    @ColumnInfo(name = "initialVenueName")
    private String initialVenueName;

    @ColumnInfo(name = "buildingName")
    private String buildingName;

    @ColumnInfo(name = "buildingCode")
    private String buildingCode;

    @ColumnInfo(name = "venueName")
    private String venueName;

    @ColumnInfo(name = "category")
    private String category;

    @TypeConverters(CoordinatesConverter.class)
    @ColumnInfo(name = "coordinates")
    private List<Double> coordinates;

    @ColumnInfo(name = "address")
    private String address;

    @ColumnInfo(name = "liveStatus")
    private boolean liveStatus;

    @ColumnInfo(name = "photo")
    private String photo;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getInitialBuildingName() {
        return initialBuildingName;
    }

    public void setInitialBuildingName(String initialBuildingName) {
        this.initialBuildingName = initialBuildingName;
    }

    public String getInitialVenueName() {
        return initialVenueName;
    }

    public void setInitialVenueName(String initialVenueName) {
        this.initialVenueName = initialVenueName;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getBuildingCode() {
        return buildingCode;
    }

    public void setBuildingCode(String buildingCode) {
        this.buildingCode = buildingCode;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<Double> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isLiveStatus() {
        return liveStatus;
    }

    public void setLiveStatus(boolean liveStatus) {
        this.liveStatus = liveStatus;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
