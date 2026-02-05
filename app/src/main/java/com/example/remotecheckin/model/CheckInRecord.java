package com.example.remotecheckin.model;

/**
 * 打卡记录数据模型
 */
public class CheckInRecord {
    private long id;
    private long locationPointId;
    private String locationName;
    private double latitude;
    private double longitude;
    private long scheduledTime;
    private long actualTime;
    private boolean success;
    private String errorMessage;

    public CheckInRecord() {
    }

    public CheckInRecord(long locationPointId, String locationName,
                         double latitude, double longitude, long scheduledTime) {
        this.locationPointId = locationPointId;
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.scheduledTime = scheduledTime;
        this.actualTime = 0;
        this.success = false;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getLocationPointId() {
        return locationPointId;
    }

    public void setLocationPointId(long locationPointId) {
        this.locationPointId = locationPointId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
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

    public long getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(long scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public long getActualTime() {
        return actualTime;
    }

    public void setActualTime(long actualTime) {
        this.actualTime = actualTime;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "CheckInRecord{" +
                "id=" + id +
                ", locationPointId=" + locationPointId +
                ", locationName='" + locationName + '\'' +
                ", scheduledTime=" + scheduledTime +
                ", actualTime=" + actualTime +
                ", success=" + success +
                '}';
    }
}
