package com.example.remotecheckin.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.remotecheckin.MainActivity;
import com.example.remotecheckin.R;
import com.example.remotecheckin.hook.DeviceInfoSpoof;
import com.example.remotecheckin.hook.LocationHook;
import com.example.remotecheckin.manager.AppWhitelistManager;
import com.example.remotecheckin.model.AppWhitelist;
import com.example.remotecheckin.model.LocationPoint;
import com.example.remotecheckin.storage.MMKVStorage;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 高级位置模拟服务
 * 集成了Hook、设备信息伪装、应用白名单等功能
 */
public class AdvancedMockLocationService extends Service {
    private static final String TAG = "AdvancedMockLoc";
    private static final String CHANNEL_ID = "AdvancedMockLocChannel";
    private static final int NOTIFICATION_ID = 2001;

    private LocationManager locationManager;
    private Handler handler;
    private boolean isRunning = false;

    // 单例实例
    private static AdvancedMockLocationService instance;

    // 位置数据
    private LocationPoint currentLocation;
    private Map<String, Location> appLocations; // 每个应用的独立位置
    private List<LatLng> routePath;
    private int currentRouteIndex = 0;
    private long updateInterval = 1000;

    // Hook和管理器
    private LocationHook locationHook;
    private DeviceInfoSpoof deviceInfoSpoof;
    private AppWhitelistManager whitelistManager;
    private MMKVStorage storage;

    // 设备信息
    private DeviceInfoSpoof.DeviceTemplate deviceTemplate;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "AdvancedMockLocationService created");

        instance = this;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        handler = new Handler(Looper.getMainLooper());
        appLocations = new ConcurrentHashMap<>();

        // 初始化Hook和管理器
        locationHook = LocationHook.getInstance();
        deviceInfoSpoof = DeviceInfoSpoof.getInstance();
        whitelistManager = AppWhitelistManager.getInstance(this);
        storage = MMKVStorage.getInstance(this);

        // 创建通知渠道
        createNotificationChannel();

        // 加载设置
        loadSettings();

        Log.d(TAG, "Service initialized successfully");
    }

    /**
     * 加载设置
     */
    private void loadSettings() {
        // 加载设备模板
        String templateName = storage.loadSetting("device_template", "XIAOMI_MI10");
        try {
            deviceTemplate = DeviceInfoSpoof.DeviceTemplate.valueOf(templateName);
            deviceInfoSpoof.setDeviceTemplate(deviceTemplate);
        } catch (IllegalArgumentException e) {
            deviceTemplate = DeviceInfoSpoof.DeviceTemplate.XIAOMI_MI10;
        }

        // 应用设备信息伪装
        deviceInfoSpoof.modifyBuildClass();

        Log.d(TAG, "Settings loaded, device template: " + deviceTemplate.name());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();

            switch (action) {
                case "START_MOCK":
                    LocationPoint location = (LocationPoint) intent.getSerializableExtra("location");
                    startMockLocation(location);
                    break;

                case "START_ROUTE":
                    ArrayList<LatLng> path = intent.getParcelableArrayListExtra("path");
                    long interval = intent.getLongExtra("interval", 1000);
                    startRouteSimulation(path, interval);
                    break;

                case "STOP":
                    stopMockLocation();
                    break;

                case "SET_APP_LOCATION":
                    String packageName = intent.getStringExtra("packageName");
                    LocationPoint appLocation = (LocationPoint) intent.getSerializableExtra("location");
                    setAppLocation(packageName, appLocation);
                    break;

                case "SET_DEVICE_TEMPLATE":
                    String templateName = intent.getStringExtra("template");
                    setDeviceTemplate(templateName);
                    break;

                case "START_HOOK":
                    startHook();
                    break;
            }
        }

        return START_STICKY;
    }

    /**
     * 开始Hook
     */
    private void startHook() {
        boolean success = locationHook.startHook(this);
        if (success) {
            Log.i(TAG, "Hook started successfully");
            updateNotification("Hook已启动");
        } else {
            Log.e(TAG, "Failed to start hook");
        }
    }

    /**
     * 开始模拟位置
     */
    private void startMockLocation(LocationPoint location) {
        Log.d(TAG, "Starting mock location: " + location);
        this.currentLocation = location;
        this.isRunning = true;

        // 设置Hook的模拟位置
        Location mockLoc = LocationHook.createMockLocation(
                location.getLatitude(),
                location.getLongitude()
        );
        locationHook.setMockLocation(mockLoc);

        // 启动Hook
        locationHook.startHook(this);

        try {
            // 添加mock位置提供者
            locationManager.addTestProvider(
                    LocationManager.GPS_PROVIDER,
                    false, false, false, false,
                    true, true, true,
                    android.location.Criteria.POWER_LOW,
                    android.location.Criteria.ACCURACY_FINE
            );
            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);

            // 为白名单中的应用设置位置
            List<AppWhitelist> enabledApps = whitelistManager.getEnabledApps();
            for (AppWhitelist app : enabledApps) {
                appLocations.put(app.getPackageName(), mockLoc);
            }

            // 开始持续更新位置
            startLocationUpdates();

            // 启动前台服务
            startForeground(NOTIFICATION_ID, createNotification("模拟位置: " + location.getName()));

            Log.i(TAG, "Mock location started successfully");

        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException: " + e.getMessage());
            stopSelf();
        } catch (Exception e) {
            Log.e(TAG, "Error starting mock location: " + e.getMessage());
            stopSelf();
        }
    }

    /**
     * 开始轨迹模拟
     */
    private void startRouteSimulation(ArrayList<LatLng> path, long interval) {
        if (path == null || path.isEmpty()) {
            Log.e(TAG, "Route path is empty");
            return;
        }

        Log.d(TAG, "Starting route simulation with " + path.size() + " points");
        this.routePath = path;
        this.currentRouteIndex = 0;
        this.updateInterval = interval;
        this.isRunning = true;

        // 启动Hook
        locationHook.startHook(this);

        try {
            locationManager.addTestProvider(
                    LocationManager.GPS_PROVIDER,
                    false, false, false, false,
                    true, true, true,
                    android.location.Criteria.POWER_LOW,
                    android.location.Criteria.ACCURACY_FINE
            );
            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);

            startRouteUpdates();

            startForeground(NOTIFICATION_ID, createNotification("轨迹模拟中..."));

            Log.i(TAG, "Route simulation started successfully");

        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException: " + e.getMessage());
            stopSelf();
        } catch (Exception e) {
            Log.e(TAG, "Error starting route simulation: " + e.getMessage());
            stopSelf();
        }
    }

    /**
     * 开始位置更新
     */
    private void startLocationUpdates() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isRunning && currentLocation != null) {
                    updateLocation(
                            currentLocation.getLatitude(),
                            currentLocation.getLongitude(),
                            currentLocation.getAccuracy()
                    );
                    handler.postDelayed(this, updateInterval);
                }
            }
        });
    }

    /**
     * 开始轨迹更新
     */
    private void startRouteUpdates() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isRunning && !routePath.isEmpty()) {
                    if (currentRouteIndex < routePath.size()) {
                        LatLng point = routePath.get(currentRouteIndex);
                        updateLocation(point.latitude, point.longitude, 10.0f);

                        // 更新Hook位置
                        Location mockLoc = LocationHook.createMockLocation(
                                point.latitude, point.longitude
                        );
                        locationHook.setMockLocation(mockLoc);

                        currentRouteIndex++;

                        if (currentRouteIndex >= routePath.size()) {
                            // 轨迹完成，循环
                            currentRouteIndex = 0;
                        }

                        handler.postDelayed(this, updateInterval);
                    }
                }
            }
        });
    }

    /**
     * 更新模拟位置
     */
    private void updateLocation(double latitude, double longitude, float accuracy) {
        try {
            Location mockLocation = new Location(LocationManager.GPS_PROVIDER);
            mockLocation.setLatitude(latitude);
            mockLocation.setLongitude(longitude);
            mockLocation.setAccuracy(accuracy);
            mockLocation.setAltitude(10.0);
            mockLocation.setBearing(0.0f);
            mockLocation.setSpeed(0.0f);
            mockLocation.setTime(System.currentTimeMillis());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            }

            locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mockLocation);

            // 更新白名单应用的位置
            for (AppWhitelist app : whitelistManager.getEnabledApps()) {
                appLocations.put(app.getPackageName(), mockLocation);
            }

            Log.d(TAG, String.format("Location updated: %.6f, %.6f", latitude, longitude));

        } catch (Exception e) {
            Log.e(TAG, "Error updating location: " + e.getMessage());
        }
    }

    /**
     * 设置特定应用的位置
     */
    private void setAppLocation(String packageName, LocationPoint location) {
        Location mockLoc = LocationHook.createMockLocation(
                location.getLatitude(),
                location.getLongitude()
        );

        appLocations.put(packageName, mockLoc);
        locationHook.setAppMockLocation(packageName, mockLoc);

        Log.d(TAG, "App location set: " + packageName + " -> " +
                location.getLatitude() + ", " + location.getLongitude());
    }

    /**
     * 设置设备模板
     */
    private void setDeviceTemplate(String templateName) {
        try {
            DeviceInfoSpoof.DeviceTemplate template =
                    DeviceInfoSpoof.DeviceTemplate.valueOf(templateName);

            deviceInfoSpoof.setDeviceTemplate(template);
            deviceInfoSpoof.modifyBuildClass();

            // 保存设置
            storage.saveSetting("device_template", templateName);

            Log.i(TAG, "Device template set: " + templateName);
            updateNotification("设备模板: " + templateName);

        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid device template: " + templateName);
        }
    }

    /**
     * 停止模拟位置
     */
    private void stopMockLocation() {
        Log.d(TAG, "Stopping mock location");

        isRunning = false;

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        locationHook.stopHook();

        try {
            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            Log.e(TAG, "Error removing test provider: " + e.getMessage());
        }

        stopForeground(true);
        stopSelf();

        Log.i(TAG, "Mock location stopped");
    }

    /**
     * 创建通知渠道
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "高级位置模拟",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("集成Hook和设备伪装的位置模拟");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    /**
     * 创建通知
     */
    private Notification createNotification(String contentText) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("高级位置模拟")
                .setContentText(contentText)
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    /**
     * 更新通知
     */
    private void updateNotification(String contentText) {
        Notification notification = createNotification(contentText);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(NOTIFICATION_ID, notification);
    }

    /**
     * 获取服务实例
     */
    public static AdvancedMockLocationService getInstance() {
        return instance;
    }

    /**
     * 检查是否正在运行
     */
    public static boolean isServiceRunning() {
        return instance != null && instance.isRunning;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopMockLocation();
        instance = null;
    }

    /**
     * 获取当前模拟位置
     */
    public LocationPoint getCurrentLocation() {
        return currentLocation;
    }

    /**
     * 获取应用特定位置
     */
    public Location getAppLocation(String packageName) {
        return appLocations.get(packageName);
    }

    /**
     * 获取白名单管理器
     */
    public AppWhitelistManager getWhitelistManager() {
        return whitelistManager;
    }

    /**
     * 获取设备信息伪装器
     */
    public DeviceInfoSpoof getDeviceInfoSpoof() {
        return deviceInfoSpoof;
    }

    /**
     * 获取位置Hook
     */
    public LocationHook getLocationHook() {
        return locationHook;
    }
}
