package com.inclunav.iwayplus.activities;

import java.util.ArrayList;

public class BuildingListRecycleView {

    private String buildingName;

    public BuildingListRecycleView(String name) {
        buildingName = name;
    }

    public String getName() {
        return buildingName;
    }

    public static ArrayList<BuildingListRecycleView> createContactsList(int numContacts) {
        ArrayList<BuildingListRecycleView> contacts = new ArrayList<BuildingListRecycleView>();
        return contacts;
    }
}
