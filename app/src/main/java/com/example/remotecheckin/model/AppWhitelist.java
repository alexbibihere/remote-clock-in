package com.example.remotecheckin.model;

import android.graphics.drawable.Drawable;

/**
 * 应用白名单模型
 */
public class AppWhitelist {
    private String packageName;
    private String appName;
    private boolean enabled;
    private Drawable icon;
    private long addedTime;

    public AppWhitelist() {
        this.enabled = true;
        this.addedTime = System.currentTimeMillis();
    }

    public AppWhitelist(String packageName, String appName) {
        this.packageName = packageName;
        this.appName = appName;
        this.enabled = true;
        this.addedTime = System.currentTimeMillis();
    }

    public AppWhitelist(String packageName, String appName, boolean enabled) {
        this.packageName = packageName;
        this.appName = appName;
        this.enabled = enabled;
        this.addedTime = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public long getAddedTime() {
        return addedTime;
    }

    public void setAddedTime(long addedTime) {
        this.addedTime = addedTime;
    }

    @Override
    public String toString() {
        return "AppWhitelist{" +
                "packageName='" + packageName + '\'' +
                ", appName='" + appName + '\'' +
                ", enabled=" + enabled +
                ", addedTime=" + addedTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppWhitelist that = (AppWhitelist) o;
        return packageName != null ? packageName.equals(that.packageName) : that.packageName == null;
    }

    @Override
    public int hashCode() {
        return packageName != null ? packageName.hashCode() : 0;
    }
}
