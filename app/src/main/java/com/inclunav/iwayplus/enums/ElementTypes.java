package com.inclunav.iwayplus.enums;

public enum ElementTypes {
    ROOMS("Rooms"),
    SERVICES("Services"),
    FLOOR_CONNECTION("FloorConnection");

    private final String elementType;
    ElementTypes(String elementType) {
        this.elementType = elementType;
    }

    public String value() {
        return elementType;
    }
}
