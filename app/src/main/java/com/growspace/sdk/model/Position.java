package com.growspace.sdk.model;

public class Position {
    private float azimuth;
    private float distance;

    public Position(float f, float f2) {
        this.distance = f;
        this.azimuth = f2;
    }

    public float getDistance() {
        return this.distance;
    }

    public float getAzimuth() {
        return this.azimuth;
    }

}
