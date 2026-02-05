package com.example.remotecheckin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.remotecheckin.model.LocationPoint;
import com.example.remotecheckin.utils.RouteGenerator;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * 地图选点Activity
 * 用于在地图上选择位置和规划轨迹
 */
public class MapPickerActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Marker selectedMarker;
    private List<LatLng> routePoints = new ArrayList<>();
    private Polyline routePolyline;
    private boolean isRouteMode = false;

    private EditText nameEditText;
    private Button confirmButton;
    private Button routeButton;
    private Button clearRouteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        // 初始化视图
        nameEditText = findViewById(R.id.location_name_edit);
        confirmButton = findViewById(R.id.confirm_button);
        routeButton = findViewById(R.id.route_button);
        clearRouteButton = findViewById(R.id.clear_route_button);

        // 获取地图片段
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // 设置按钮点击事件
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmSelection();
            }
        });

        routeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRouteMode) {
                    finishRoute();
                } else {
                    startRouteMode();
                }
            }
        });

        clearRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearRoute();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // 设置地图类型
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // 启用我的位置按钮
        try {
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        // 设置地图点击事件
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (isRouteMode) {
                    addRoutePoint(latLng);
                } else {
                    selectLocation(latLng);
                }
            }
        });

        // 设置标记点击事件
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                selectedMarker = marker;
                return false;
            }
        });

        // 移动到默认位置（北京）
        LatLng beijing = new LatLng(39.9042, 116.4074);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(beijing, 12f));
    }

    /**
     * 选择位置
     */
    private void selectLocation(LatLng latLng) {
        // 移除之前的标记
        if (selectedMarker != null) {
            selectedMarker.remove();
        }

        // 添加新标记
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title("选中位置")
                .draggable(true);
        selectedMarker = mMap.addMarker(markerOptions);

        // 移动相机到选定位置
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    /**
     * 开始轨迹模式
     */
    private void startRouteMode() {
        isRouteMode = true;
        routeButton.setText("完成轨迹");
        clearRouteButton.setVisibility(View.VISIBLE);

        // 清空之前的轨迹
        clearRoute();

        Toast.makeText(this, "点击地图添加轨迹点", Toast.LENGTH_SHORT).show();
    }

    /**
     * 添加轨迹点
     */
    private void addRoutePoint(LatLng latLng) {
        routePoints.add(latLng);

        // 添加标记
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("点 " + routePoints.size())
                .draggable(true));

        // 更新轨迹线
        updateRoutePolyline();

        Toast.makeText(this, "已添加 " + routePoints.size() + " 个点", Toast.LENGTH_SHORT).show();
    }

    /**
     * 更新轨迹线
     */
    private void updateRoutePolyline() {
        if (routePolyline != null) {
            routePolyline.remove();
        }

        if (routePoints.size() >= 2) {
            PolylineOptions options = new PolylineOptions()
                    .addAll(routePoints)
                    .color(0xFF2196F3)
                    .width(5)
                    .geodesic(true);

            routePolyline = mMap.addPolyline(options);
        }
    }

    /**
     * 完成轨迹规划
     */
    private void finishRoute() {
        if (routePoints.size() < 2) {
            Toast.makeText(this, "至少需要2个点才能创建轨迹", Toast.LENGTH_SHORT).show();
            return;
        }

        // 生成平滑轨迹
        List<LatLng> smoothRoute = RouteGenerator.generateSmoothRoute(routePoints, 10);

        // 返回结果
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra("route", new ArrayList<>(smoothRoute));
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * 清空轨迹
     */
    private void clearRoute() {
        routePoints.clear();

        if (routePolyline != null) {
            routePolyline.remove();
            routePolyline = null;
        }

        mMap.clear();

        Toast.makeText(this, "轨迹已清空", Toast.LENGTH_SHORT).show();
    }

    /**
     * 确认选择
     */
    private void confirmSelection() {
        if (selectedMarker == null) {
            Toast.makeText(this, "请先在地图上选择位置", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = nameEditText.getText().toString().trim();
        if (name.isEmpty()) {
            name = "位置 " + System.currentTimeMillis();
        }

        LatLng latLng = selectedMarker.getPosition();

        // 创建位置对象
        LocationPoint location = new LocationPoint(name, latLng);

        // 返回结果
        Intent intent = new Intent();
        intent.putExtra("location", location);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (isRouteMode) {
            // 退出轨迹模式
            isRouteMode = false;
            routeButton.setText("规划轨迹");
            clearRouteButton.setVisibility(View.GONE);
            clearRoute();
        } else {
            super.onBackPressed();
        }
    }
}
