package com.example.remotecheckin.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * 位置点数据模型
 */
public class LocationPoint {
    private long id;
    private String name;
    private double latitude;
    private double longitude;
    private float accuracy;
    private long timestamp;

    public LocationPoint() {
    }

    public LocationPoint(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = 10.0f; // 默认精度10米
        this.timestamp = System.currentTimeMillis();
    }

    public LocationPoint(String name, LatLng latLng) {
        this(name, latLng.latitude, latLng.longitude);
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public LatLng toLatLng() {
        return new LatLng(latitude, longitude);
    }

    @Override
    public String toString() {
        return "LocationPoint{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", accuracy=" + accuracy +
                ", timestamp=" + timestamp +
                '}';
    }
}
