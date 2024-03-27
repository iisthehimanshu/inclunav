package com.inclunav.iwayplus.activities.allbuildingdb;

import androidx.room.TypeConverter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CoordinatesConverter {

    @TypeConverter
    public static List<Double> fromString(String value) {
        if (value == null) {
            return null;
        }
        String[] coordinatesArray = value.split(",");
        List<Double> coordinatesList = new ArrayList<>();
        for (String coordinateStr : coordinatesArray) {
            coordinatesList.add(Double.parseDouble(coordinateStr));
        }
        return coordinatesList;
    }

    @TypeConverter
    public static String toString(List<Double> coordinates) {
        if (coordinates == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < coordinates.size(); i++) {
            builder.append(coordinates.get(i));
            if (i < coordinates.size() - 1) {
                builder.append(",");
            }
        }
        return builder.toString();
    }
}
