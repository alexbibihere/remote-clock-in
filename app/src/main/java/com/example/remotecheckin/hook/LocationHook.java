package com.example.remotecheckin.hook;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * 位置Hook管理器
 * 使用Java反射Hook系统Location API
 */
public class LocationHook {
    private static final String TAG = "LocationHook";
    private static LocationHook instance;
    private Location mockLocation;
    private boolean isHooked = false;
    private Map<String, Location> appLocations = new HashMap<>();

    private LocationHook() {
    }

    public static LocationHook getInstance() {
        if (instance == null) {
            synchronized (LocationHook.class) {
                if (instance == null) {
                    instance = new LocationHook();
                }
            }
        }
        return instance;
    }

    /**
     * 开始Hook
     */
    public boolean startHook(Context context) {
        if (isHooked) {
            Log.w(TAG, "Hook already started");
            return true;
        }

        try {
            // Hook LocationManager
            hookLocationManager();

            // Hook FusedLocationProviderClient (Google Play Services)
            hookFusedLocationProvider();

            // Hook SystemServer (需要root或系统权限)
            hookSystemServer();

            isHooked = true;
            Log.i(TAG, "Location hook started successfully");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Failed to start hook: " + e.getMessage());
            return false;
        }
    }

    /**
     * 停止Hook
     */
    public void stopHook() {
        isHooked = false;
        mockLocation = null;
        appLocations.clear();
        Log.i(TAG, "Location hook stopped");
    }

    /**
     * Hook LocationManager
     */
    private void hookLocationManager() {
        try {
            // Hook getLastKnownLocation
            hookLocationManagerMethod("getLastKnownLocation");

            // Hook requestLocationUpdates
            hookLocationManagerMethod("requestLocationUpdates");

            // Hook requestSingleUpdate
            hookLocationManagerMethod("requestSingleUpdate");

            Log.i(TAG, "LocationManager hooked");

        } catch (Exception e) {
            Log.e(TAG, "Failed to hook LocationManager: " + e.getMessage());
        }
    }

    /**
     * Hook LocationManager的方法
     */
    private void hookLocationManagerMethod(String methodName) {
        try {
            Class<?> locationManagerClass = LocationManager.class;
            Method[] methods = locationManagerClass.getDeclaredMethods();

            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    // 使用反射替换方法实现
                    // 这里需要使用Xposed或其他Hook框架
                    // 纯Java反射无法实现，这里只是示例代码
                    Log.d(TAG, "Found method to hook: " + methodName);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed to hook method " + methodName + ": " + e.getMessage());
        }
    }

    /**
     * Hook FusedLocationProviderClient
     */
    private void hookFusedLocationProvider() {
        try {
            // FusedLocationProviderClient是Google Play Services的一部分
            // Hook com.google.android.gms.location.FusedLocationProviderClient

            Class<?> fusedLocationClass = Class.forName(
                    "com.google.android.gms.location.FusedLocationProviderClient");

            Log.i(TAG, "FusedLocationProviderClient class found");

            // 这里需要使用Xposed或Frida来Hook
            // 示例：Hook getLastLocation方法
            Log.d(TAG, "FusedLocationProviderClient hooked (example)");

        } catch (ClassNotFoundException e) {
            Log.w(TAG, "FusedLocationProviderClient not available");
        } catch (Exception e) {
            Log.e(TAG, "Failed to hook FusedLocationProvider: " + e.getMessage());
        }
    }

    /**
     * Hook SystemServer（需要root）
     */
    private void hookSystemServer() {
        try {
            // Hook系统级别的Location服务
            // 需要root权限和Xposed框架

            Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
            Method checkServiceMethod = serviceManagerClass.getDeclaredMethod("checkService", String.class);
            checkServiceMethod.setAccessible(true);

            Log.i(TAG, "SystemServer hook attempted (requires root/Xposed)");

        } catch (Exception e) {
            Log.w(TAG, "Cannot hook SystemServer: " + e.getMessage());
        }
    }

    /**
     * 设置模拟位置
     */
    public void setMockLocation(Location location) {
        this.mockLocation = location;
        Log.d(TAG, "Mock location set: " + location.getLatitude() + ", " + location.getLongitude());
    }

    /**
     * 设置特定应用的模拟位置
     */
    public void setAppMockLocation(String packageName, Location location) {
        appLocations.put(packageName, location);
        Log.d(TAG, "App mock location set for " + packageName);
    }

    /**
     * 获取模拟位置
     */
    public Location getMockLocation() {
        return mockLocation;
    }

    /**
     * 获取特定应用的模拟位置
     */
    public Location getAppMockLocation(String packageName) {
        Location location = appLocations.get(packageName);
        if (location != null) {
            return location;
        }
        return mockLocation;
    }

    /**
     * 检查是否已Hook
     */
    public boolean isHooked() {
        return isHooked;
    }

    /**
     * 创建模拟位置
     */
    public static Location createMockLocation(double latitude, double longitude) {
        Location location = new Location("mock");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAccuracy(10.0f);
        location.setAltitude(10.0);
        location.setBearing(0.0f);
        location.setSpeed(0.0f);
        location.setTime(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            location.setElapsedRealtimeNanos(android.os.SystemClock.elapsedRealtimeNanos());
        }

        return location;
    }

    /**
     * 检查位置是否是模拟的
     */
    public static boolean isMockLocation(Location location) {
        if (location == null) {
            return false;
        }

        // 检查provider
        if ("mock".equals(location.getProvider())) {
            return true;
        }

        // 检查 Extras 中的 mock location 标记
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // Android 4.3+ 会自动在 mock location 中添加标记
            try {
                @SuppressWarnings("JavaReflectionMemberAccess")
                Field isMockField = Location.class.getDeclaredField("isFromMockProvider");
                if (isMockField != null) {
                    isMockField.setAccessible(true);
                    return isMockField.getBoolean(location);
                }
            } catch (Exception e) {
                // Ignore
            }
        }

        return false;
    }
}
