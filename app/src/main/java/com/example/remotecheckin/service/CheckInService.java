package com.example.remotecheckin.service;

import android.app.AlarmManager;
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
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.remotecheckin.MainActivity;
import android.R;
import com.example.remotecheckin.model.CheckInRecord;
import com.example.remotecheckin.model.LocationPoint;
import com.example.remotecheckin.database.DatabaseHelper;

import java.util.List;

/**
 * 自动打卡服务
 * 用于定时执行打卡操作
 */
public class CheckInService extends Service {
    private static final String TAG = "CheckInService";
    private static final String CHANNEL_ID = "CheckInChannel";
    private static final int NOTIFICATION_ID = 1002;

    private DatabaseHelper databaseHelper;
    private LocationManager locationManager;
    private Handler handler;
    private boolean isRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "CheckInService created");

        databaseHelper = new DatabaseHelper(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        handler = new Handler(Looper.getMainLooper());

        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();

            if ("SCHEDULE_CHECKIN".equals(action)) {
                long locationPointId = intent.getLongExtra("locationPointId", -1);
                long scheduledTime = intent.getLongExtra("scheduledTime", 0);
                scheduleCheckIn(locationPointId, scheduledTime);
            } else if ("EXECUTE_CHECKIN".equals(action)) {
                long locationPointId = intent.getLongExtra("locationPointId", -1);
                executeCheckIn(locationPointId);
            } else if ("STOP".equals(action)) {
                stopCheckInService();
            }
        }

        return START_STICKY;
    }

    /**
     * 安排打卡任务
     */
    private void scheduleCheckIn(long locationPointId, long scheduledTime) {
        Log.d(TAG, "Scheduling check-in for location: " + locationPointId + " at time: " + scheduledTime);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent checkInIntent = new Intent(this, CheckInService.class);
        checkInIntent.setAction("EXECUTE_CHECKIN");
        checkInIntent.putExtra("locationPointId", locationPointId);

        PendingIntent pendingIntent = PendingIntent.getService(
                this,
                (int) locationPointId,
                checkInIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // 使用精确闹钟
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        scheduledTime,
                        pendingIntent
                );
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    scheduledTime,
                    pendingIntent
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    scheduledTime,
                    pendingIntent
            );
        }

        // 启动前台服务
        startForeground(NOTIFICATION_ID, createNotification("打卡任务已安排"));
    }

    /**
     * 执行打卡操作
     */
    private void executeCheckIn(long locationPointId) {
        Log.d(TAG, "Executing check-in for location: " + locationPointId);

        // 从数据库获取位置信息
        LocationPoint location = databaseHelper.getLocationPoint(locationPointId);
        if (location == null) {
            Log.e(TAG, "Location not found: " + locationPointId);
            return;
        }

        // 创建打卡记录
        CheckInRecord record = new CheckInRecord(
                locationPointId,
                location.getName(),
                location.getLatitude(),
                location.getLongitude(),
                System.currentTimeMillis()
        );

        // 更新通知
        startForeground(NOTIFICATION_ID, createNotification("正在打卡: " + location.getName()));

        // 模拟打卡过程
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                performCheckIn(location, record);
            }
        }, 2000); // 延迟2秒执行
    }

    /**
     * 执行实际的打卡操作
     */
    private void performCheckIn(LocationPoint location, CheckInRecord record) {
        try {
            Log.d(TAG, "Performing check-in at: " + location.getName());

            // 1. 启动位置模拟
            Intent mockIntent = new Intent(this, MockLocationService.class);
            mockIntent.setAction("START_MOCK");
            mockIntent.putExtra("location", location);
            startService(mockIntent);

            // 2. 等待位置更新
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 3. 这里可以添加实际的打卡逻辑
                    // 例如：打开打卡应用、模拟点击等

                    // 4. 记录打卡结果
                    record.setActualTime(System.currentTimeMillis());
                    record.setSuccess(true);

                    // 5. 保存到数据库
                    databaseHelper.addCheckInRecord(record);

                    Log.d(TAG, "Check-in completed successfully");

                    // 6. 更新通知
                    startForeground(NOTIFICATION_ID, createNotification("打卡成功: " + location.getName()));

                    // 7. 停止位置模拟
                    Intent stopMockIntent = new Intent(CheckInService.this, MockLocationService.class);
                    stopMockIntent.setAction("STOP");
                    startService(stopMockIntent);

                    // 8. 延迟后停止服务
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            stopForeground(true);
                            stopSelf();
                        }
                    }, 5000);
                }
            }, 3000); // 等待3秒让位置生效

        } catch (Exception e) {
            Log.e(TAG, "Error during check-in: " + e.getMessage());

            record.setActualTime(System.currentTimeMillis());
            record.setSuccess(false);
            record.setErrorMessage(e.getMessage());
            databaseHelper.addCheckInRecord(record);
        }
    }

    /**
     * 获取所有待执行的打卡任务
     */
    private List<CheckInRecord> getScheduledCheckIns() {
        // 从数据库获取未来的打卡任务
        return databaseHelper.getUpcomingCheckIns();
    }

    /**
     * 停止打卡服务
     */
    private void stopCheckInService() {
        Log.d(TAG, "Stopping check-in service");

        isRunning = false;

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        // 取消所有闹钟
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        List<CheckInRecord> scheduledCheckIns = getScheduledCheckIns();

        for (CheckInRecord record : scheduledCheckIns) {
            Intent intent = new Intent(this, CheckInService.class);
            intent.setAction("EXECUTE_CHECKIN");
            intent.putExtra("locationPointId", record.getLocationPointId());

            PendingIntent pendingIntent = PendingIntent.getService(
                    this,
                    (int) record.getLocationPointId(),
                    intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE
            );

            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
            }
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
                    "自动打卡服务",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("自动打卡任务通知");

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
                .setContentTitle("远程打卡")
                .setContentText(contentText)
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
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
        stopCheckInService();
    }
}
