package com.example.remotecheckin.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * 位置工具类
 * 提供位置相关的辅助方法
 */
public class LocationUtils {

    /**
     * 检查是否有位置权限
     */
    public static boolean hasLocationPermissions(Context context) {
        int fineLocation = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocation = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        return fineLocation == PackageManager.PERMISSION_GRANTED &&
                coarseLocation == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 检查是否开启了模拟位置
     */
    public static boolean isMockLocationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.Secure.getString(
                    context.getContentResolver(),
                    "mock_location"
            ).equals("1");
        } else {
            return Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ALLOW_MOCK_LOCATION
            ).equals("1");
        }
    }

    /**
     * 检查GPS是否开启
     */
    public static boolean isGPSEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null &&
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * 获取最后已知位置
     */
    public static Location getLastKnownLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            return null;
        }

        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;

        for (String provider : providers) {
            try {
                Location location = locationManager.getLastKnownLocation(provider);
                if (location != null) {
                    if (bestLocation == null || location.getAccuracy() < bestLocation.getAccuracy()) {
                        bestLocation = location;
                    }
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        return bestLocation;
    }

    /**
     * 将LatLng转换为Location
     */
    public static Location latLngToLocation(LatLng latLng, String provider) {
        Location location = new Location(provider);
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        location.setAccuracy(10.0f);
        location.setTime(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            location.setElapsedRealtimeNanos(android.os.SystemClock.elapsedRealtimeNanos());
        }

        return location;
    }

    /**
     * 计算两个LatLng之间的距离（米）
     */
    public static float distanceBetween(LatLng start, LatLng end) {
        float[] results = new float[1];
        Location.distanceBetween(
                start.latitude, start.longitude,
                end.latitude, end.longitude,
                results
        );
        return results[0];
    }

    /**
     * 检查位置是否在指定范围内
     */
    public static boolean isWithinRadius(LatLng center, LatLng point, double radiusInMeters) {
        return distanceBetween(center, point) <= radiusInMeters;
    }

    /**
     * 格式化位置信息
     */
    public static String formatLocationInfo(LatLng latLng) {
        return String.format("纬度: %.6f, 经度: %.6f", latLng.latitude, latLng.longitude);
    }

    /**
     * 格式化坐标为度分秒格式
     */
    public static String formatCoordinateToDMS(double coordinate, boolean isLatitude) {
        String direction = "";
        if (isLatitude) {
            direction = coordinate >= 0 ? "N" : "S";
        } else {
            direction = coordinate >= 0 ? "E" : "W";
        }

        coordinate = Math.abs(coordinate);
        int degrees = (int) coordinate;
        int minutes = (int) ((coordinate - degrees) * 60);
        double seconds = ((coordinate - degrees) * 60 - minutes) * 60;

        return String.format("%d°%d'%.2f\"%s", degrees, minutes, seconds, direction);
    }

    /**
     * 生成位置分享文本
     */
    public static String generateLocationShareText(LatLng latLng, String locationName) {
        StringBuilder sb = new StringBuilder();
        sb.append("位置名称: ").append(locationName).append("\n");
        sb.append("坐标: ").append(formatLocationInfo(latLng)).append("\n");
        sb.append("谷歌地图: https://maps.google.com/?q=")
                .append(latLng.latitude).append(",").append(latLng.longitude).append("\n");
        sb.append("高德地图: https://uri.amap.com/marker?position=")
                .append(latLng.longitude).append(",").append(latLng.latitude);

        return sb.toString();
    }
}
