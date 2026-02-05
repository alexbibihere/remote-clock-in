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
import android.R;
import com.example.remotecheckin.model.LocationPoint;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * 位置模拟服务
 * 用于模拟GPS位置和移动轨迹
 */
public class MockLocationService extends Service {
    private static final String TAG = "MockLocationService";
    private static final String CHANNEL_ID = "MockLocationChannel";
    private static final int NOTIFICATION_ID = 1001;

    private LocationManager locationManager;
    private Handler handler;
    private boolean isRunning = false;
    private LocationPoint currentLocation;
    private List<LatLng> routePath;
    private int currentRouteIndex = 0;
    private long updateInterval = 1000; // 1秒更新一次

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "MockLocationService created");

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        handler = new Handler(Looper.getMainLooper());
        routePath = new ArrayList<>();

        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();

            if ("START_MOCK".equals(action)) {
                LocationPoint location = (LocationPoint) intent.getSerializableExtra("location");
                startMockLocation(location);
            } else if ("START_ROUTE".equals(action)) {
                ArrayList<LatLng> path = intent.getParcelableArrayListExtra("path");
                long interval = intent.getLongExtra("interval", 1000);
                startRouteSimulation(path, interval);
            } else if ("STOP".equals(action)) {
                stopMockLocation();
            }
        }

        return START_STICKY;
    }

    /**
     * 开始模拟固定位置
     */
    private void startMockLocation(LocationPoint location) {
        Log.d(TAG, "Starting mock location: " + location);
        this.currentLocation = location;
        this.isRunning = true;

        // 添加mock位置提供者
        try {
            locationManager.addTestProvider(
                    LocationManager.GPS_PROVIDER,
                    false,
                    false,
                    false,
                    false,
                    true,
                    true,
                    true,
                    android.location.Criteria.POWER_LOW,
                    android.location.Criteria.ACCURACY_FINE
            );
            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);

            // 开始持续更新位置
            startLocationUpdates();
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

        try {
            locationManager.addTestProvider(
                    LocationManager.GPS_PROVIDER,
                    false,
                    false,
                    false,
                    false,
                    true,
                    true,
                    true,
                    android.location.Criteria.POWER_LOW,
                    android.location.Criteria.ACCURACY_FINE
            );
            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);

            startRouteUpdates();
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
                        currentRouteIndex++;

                        if (currentRouteIndex >= routePath.size()) {
                            // 轨迹完成，循环或停止
                            currentRouteIndex = 0; // 循环
                        }

                        handler.postDelayed(this, updateInterval);
                    } else {
                        stopMockLocation();
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

            Log.d(TAG, String.format("Location updated: %.6f, %.6f", latitude, longitude));
        } catch (Exception e) {
            Log.e(TAG, "Error updating location: " + e.getMessage());
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

        try {
            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            Log.e(TAG, "Error removing test provider: " + e.getMessage());
        }

        stopForeground(true);
        stopSelf();
    }

    /**
     * 创建通知渠道
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "位置模拟服务",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("正在模拟GPS位置");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    /**
     * 创建通知
     */
    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("位置模拟运行中")
                .setContentText("正在模拟GPS位置")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
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
    }
}
