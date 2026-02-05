package com.example.remotecheckin;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.remotecheckin.database.DatabaseHelper;
import com.example.remotecheckin.model.CheckInRecord;
import com.example.remotecheckin.model.LocationPoint;
import com.example.remotecheckin.service.CheckInService;
import com.example.remotecheckin.service.MockLocationService;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 主Activity
 * 应用的主界面
 */
public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int MAP_PICKER_REQUEST_CODE = 1002;
    private static final int ROUTE_PICKER_REQUEST_CODE = 1003;

    private DatabaseHelper databaseHelper;
    private ListView locationListView;
    private ListView recordListView;
    private TextView currentLocationText;
    private Button addLocationButton;
    private Button mockLocationButton;
    private Button scheduleCheckInButton;
    private Button viewRecordsButton;
    private Button routeSimulationButton;

    private LocationPoint selectedLocation;
    private List<LocationPoint> locationList;
    private List<CheckInRecord> recordList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);

        initViews();
        checkPermissions();
        loadData();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        locationListView = findViewById(R.id.location_list);
        recordListView = findViewById(R.id.record_list);
        currentLocationText = findViewById(R.id.current_location_text);
        addLocationButton = findViewById(R.id.add_location_button);
        mockLocationButton = findViewById(R.id.mock_location_button);
        scheduleCheckInButton = findViewById(R.id.schedule_checkin_button);
        viewRecordsButton = findViewById(R.id.view_records_button);
        routeSimulationButton = findViewById(R.id.route_simulation_button);

        // 添加位置按钮
        addLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMapPicker();
            }
        });

        // 模拟位置按钮
        mockLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedLocation != null) {
                    startMockLocation(selectedLocation);
                } else {
                    Toast.makeText(MainActivity.this, "请先选择一个位置", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 定时打卡按钮
        scheduleCheckInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedLocation != null) {
                    showTimePickerDialog();
                } else {
                    Toast.makeText(MainActivity.this, "请先选择一个位置", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 查看记录按钮
        viewRecordsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleRecordsView();
            }
        });

        // 轨迹模拟按钮
        routeSimulationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRoutePicker();
            }
        });

        // 位置列表点击事件
        locationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedLocation = locationList.get(position);
                currentLocationText.setText(
                        "当前选择: " + selectedLocation.getName() + "\n" +
                        "坐标: " + String.format("%.6f, %.6f",
                                selectedLocation.getLatitude(),
                                selectedLocation.getLongitude())
                );
                mockLocationButton.setEnabled(true);
                scheduleCheckInButton.setEnabled(true);
            }
        });

        // 位置列表长按删除事件
        locationListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                LocationPoint location = locationList.get(position);
                deleteLocation(location);
                return true;
            }
        });

        mockLocationButton.setEnabled(false);
        scheduleCheckInButton.setEnabled(false);
    }

    /**
     * 检查权限
     */
    private void checkPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        // 位置权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        // Android 12+ 后台位置权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_BACKGROUND_LOCATION")
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add("android.permission.ACCESS_BACKGROUND_LOCATION");
            }
        }

        // Android 13+ 通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS")
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add("android.permission.POST_NOTIFICATIONS");
            }
        }

        // 存储权限（Android 10及以下）
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsNeeded.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE
            );
        }
    }

    /**
     * 加载数据
     */
    private void loadData() {
        locationList = databaseHelper.getAllLocationPoints();
        updateLocationList();

        recordList = databaseHelper.getAllCheckInRecords();
        updateRecordList();
    }

    /**
     * 更新位置列表
     */
    private void updateLocationList() {
        List<Map<String, String>> data = new ArrayList<>();

        for (LocationPoint location : locationList) {
            Map<String, String> item = new HashMap<>();
            item.put("name", location.getName());
            item.put("coordinate", String.format("%.6f, %.6f",
                    location.getLatitude(), location.getLongitude()));
            data.add(item);
        }

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                data,
                android.R.layout.simple_list_item_2,
                new String[]{"name", "coordinate"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );

        locationListView.setAdapter(adapter);
    }

    /**
     * 更新记录列表
     */
    private void updateRecordList() {
        List<Map<String, String>> data = new ArrayList<>();

        for (CheckInRecord record : recordList) {
            Map<String, String> item = new HashMap<>();
            item.put("location", record.getLocationName());

            String timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(record.getScheduledTime());
            String status = record.isSuccess() ? "成功" : "失败";

            item.put("status", timeStr + " - " + status);
            data.add(item);
        }

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                data,
                android.R.layout.simple_list_item_2,
                new String[]{"location", "status"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );

        recordListView.setAdapter(adapter);
    }

    /**
     * 打开地图选点
     */
    private void openMapPicker() {
        Intent intent = new Intent(this, MapPickerActivity.class);
        startActivityForResult(intent, MAP_PICKER_REQUEST_CODE);
    }

    /**
     * 打开轨迹规划
     */
    private void openRoutePicker() {
        Intent intent = new Intent(this, MapPickerActivity.class);
        startActivityForResult(intent, ROUTE_PICKER_REQUEST_CODE);
    }

    /**
     * 开始模拟位置
     */
    private void startMockLocation(LocationPoint location) {
        Intent intent = new Intent(this, MockLocationService.class);
        intent.setAction("START_MOCK");
        intent.putExtra("location", location);
        startService(intent);

        Toast.makeText(this, "开始模拟位置: " + location.getName(), Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示时间选择对话框
     */
    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minuteOfHour) -> {
                    // 设置打卡时间
                    Calendar scheduledTime = Calendar.getInstance();
                    scheduledTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    scheduledTime.set(Calendar.MINUTE, minuteOfHour);
                    scheduledTime.set(Calendar.SECOND, 0);

                    // 如果时间已过，设置为明天
                    if (scheduledTime.getTimeInMillis() < System.currentTimeMillis()) {
                        scheduledTime.add(Calendar.DAY_OF_MONTH, 1);
                    }

                    scheduleCheckIn(selectedLocation, scheduledTime.getTimeInMillis());
                },
                hour,
                minute,
                true
        );

        timePickerDialog.show();
    }

    /**
     * 安排打卡
     */
    private void scheduleCheckIn(LocationPoint location, long scheduledTime) {
        Intent intent = new Intent(this, CheckInService.class);
        intent.setAction("SCHEDULE_CHECKIN");
        intent.putExtra("locationPointId", location.getId());
        intent.putExtra("scheduledTime", scheduledTime);
        startService(intent);

        String timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(scheduledTime);

        Toast.makeText(this, "已安排打卡: " + location.getName() + " at " + timeStr,
                Toast.LENGTH_LONG).show();
    }

    /**
     * 切换记录视图
     */
    private void toggleRecordsView() {
        if (recordListView.getVisibility() == View.VISIBLE) {
            recordListView.setVisibility(View.GONE);
            locationListView.setVisibility(View.VISIBLE);
            viewRecordsButton.setText("查看打卡记录");
        } else {
            recordListView.setVisibility(View.VISIBLE);
            locationListView.setVisibility(View.GONE);
            viewRecordsButton.setText("返回位置列表");
        }
    }

    /**
     * 删除位置
     */
    private void deleteLocation(LocationPoint location) {
        databaseHelper.deleteLocationPoint(location.getId());
        loadData();
        Toast.makeText(this, "已删除: " + location.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == MAP_PICKER_REQUEST_CODE) {
                // 地图选点返回
                LocationPoint location = (LocationPoint) data.getSerializableExtra("location");
                if (location != null) {
                    long id = databaseHelper.addLocationPoint(location);
                    location.setId(id);
                    loadData();
                    Toast.makeText(this, "已添加位置: " + location.getName(), Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == ROUTE_PICKER_REQUEST_CODE) {
                // 轨迹规划返回
                ArrayList<LatLng> route = data.getParcelableArrayListExtra("route");
                if (route != null && !route.isEmpty()) {
                    Intent intent = new Intent(this, MockLocationService.class);
                    intent.setAction("START_ROUTE");
                    intent.putParcelableArrayListExtra("path", route);
                    intent.putExtra("interval", 1000L);
                    startService(intent);

                    Toast.makeText(this, "开始轨迹模拟，共 " + route.size() + " 个点",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "部分权限未授予，功能可能受限", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
