package com.inclunav.iwayplus.enums;

public enum GeometryTypes {
    NODE("NODE"),
    FLOOR("FLOOR");

    private final String type;
    GeometryTypes(String type) {
        this.type = type;
    }

    public String value() {
        return type;
    }
}
