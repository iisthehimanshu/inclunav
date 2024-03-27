package com.inclunav.iwayplus.enums;

public enum RoomTypes {
    ROOM_DOOR("Room Door"),
    MAIN_ENTRY("Main Entry"),
    EMERGENCY_EXIT("Emergency Exit");

    private final String roomType;
    RoomTypes(String roomType) {
        this.roomType = roomType;

    }

    public String value() {
        return roomType;
    }
}
