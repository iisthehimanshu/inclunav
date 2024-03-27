package com.inclunav.iwayplus.enums;

public enum ServiceTypes {
    BEACONS("Beacons"),
    REST_ROOMS("Rest Rooms"),
    DRINKING_WATER("Drinking Water | Non Drinking"),
    AR("AR"),
    KIOSK("Kiosk"),
    INFORMATION_CENTRE("Information Centre"),
    HELP_DESK("Help Desk"),
    BREAK_ROOM("Break Room"),
    BABY_CARE("Baby Care"),
    CLOAK_ROOM("Cloak Room"),
    FOOD_AND_DRINKS("Food And Drinks"),
    SECURITY_ROOM("Security Room"),
    MEDICAL_ROOM("Medical Room"),
    DUSTBIN("Dustbin"),
    CHANGING_ROOM("Changing Room"),
    TRANSPORTATION("Transportation"),
    TEMPORARY_SERVICE("Temporary Service"),
    ;
    private final String serviceType;
    ServiceTypes(String serviceType) {
        this.serviceType = serviceType;
    }
    public String value(){
        return serviceType;
    }
}
