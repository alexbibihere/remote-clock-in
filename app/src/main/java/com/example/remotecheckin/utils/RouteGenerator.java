package com.example.remotecheckin.utils;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * 轨迹生成工具类
 * 用于生成平滑的移动轨迹
 */
public class RouteGenerator {

    /**
     * 生成平滑轨迹
     * @param waypoints 关键点列表
     * @param pointsPerSegment 每段插入的点数
     * @return 平滑后的轨迹点列表
     */
    public static List<LatLng> generateSmoothRoute(List<LatLng> waypoints, int pointsPerSegment) {
        List<LatLng> smoothRoute = new ArrayList<>();

        if (waypoints == null || waypoints.isEmpty()) {
            return smoothRoute;
        }

        if (waypoints.size() == 1) {
            smoothRoute.add(waypoints.get(0));
            return smoothRoute;
        }

        // 对每两个相邻点之间进行插值
        for (int i = 0; i < waypoints.size() - 1; i++) {
            LatLng start = waypoints.get(i);
            LatLng end = waypoints.get(i + 1);

            // 添加起始点
            smoothRoute.add(start);

            // 在两点之间插入中间点
            for (int j = 1; j <= pointsPerSegment; j++) {
                double ratio = (double) j / (pointsPerSegment + 1);
                LatLng interpolated = interpolate(start, end, ratio);
                smoothRoute.add(interpolated);
            }
        }

        // 添加最后一个点
        smoothRoute.add(waypoints.get(waypoints.size() - 1));

        return smoothRoute;
    }

    /**
     * 在两点之间进行线性插值
     * @param start 起始点
     * @param end 终点
     * @param ratio 插值比例 (0-1)
     * @return 插值点
     */
    private static LatLng interpolate(LatLng start, LatLng end, double ratio) {
        double lat = start.latitude + (end.latitude - start.latitude) * ratio;
        double lng = start.longitude + (end.longitude - start.longitude) * ratio;
        return new LatLng(lat, lng);
    }

    /**
     * 生成圆形轨迹
     * @param center 圆心
     * @param radiusInMeters 半径（米）
     * @param numberOfPoints 点的数量
     * @return 圆形轨迹点列表
     */
    public static List<LatLng> generateCircularRoute(LatLng center, double radiusInMeters, int numberOfPoints) {
        List<LatLng> circleRoute = new ArrayList<>();

        double radiusInDegrees = radiusInMeters / 111320.0; // 大约：1度纬度 = 111.32 km

        for (int i = 0; i < numberOfPoints; i++) {
            double angle = 2 * Math.PI * i / numberOfPoints;

            double lat = center.latitude + radiusInDegrees * Math.cos(angle);
            double lng = center.longitude + radiusInDegrees * Math.sin(angle) / Math.cos(Math.toRadians(center.latitude));

            circleRoute.add(new LatLng(lat, lng));
        }

        return circleRoute;
    }

    /**
     * 生成矩形轨迹
     * @param center 中心点
     * @param widthInMeters 宽度（米）
     * @param heightInMeters 高度（米）
     * @param pointsPerSide 每边的点数
     * @return 矩形轨迹点列表
     */
    public static List<LatLng> generateRectangularRoute(LatLng center, double widthInMeters, double heightInMeters, int pointsPerSide) {
        List<LatLng> rectangleRoute = new ArrayList<>();

        double halfWidthInDegrees = (widthInMeters / 2) / 111320.0;
        double halfHeightInDegrees = (heightInMeters / 2) / 111320.0;

        double north = center.latitude + halfHeightInDegrees;
        double south = center.latitude - halfHeightInDegrees;
        double east = center.longitude + halfWidthInDegrees / Math.cos(Math.toRadians(center.latitude));
        double west = center.longitude - halfWidthInDegrees / Math.cos(Math.toRadians(center.latitude));

        // 上边（从西到东）
        for (int i = 0; i < pointsPerSide; i++) {
            double ratio = (double) i / (pointsPerSide - 1);
            double lng = west + (east - west) * ratio;
            rectangleRoute.add(new LatLng(north, lng));
        }

        // 右边（从北到南）
        for (int i = 1; i < pointsPerSide; i++) {
            double ratio = (double) i / (pointsPerSide - 1);
            double lat = north + (south - north) * ratio;
            rectangleRoute.add(new LatLng(lat, east));
        }

        // 下边（从东到西）
        for (int i = 1; i < pointsPerSide; i++) {
            double ratio = (double) i / (pointsPerSide - 1);
            double lng = east - (east - west) * ratio;
            rectangleRoute.add(new LatLng(south, lng));
        }

        // 左边（从南到北）
        for (int i = 1; i < pointsPerSide; i++) {
            double ratio = (double) i / (pointsPerSide - 1);
            double lat = south - (south - north) * ratio;
            rectangleRoute.add(new LatLng(lat, west));
        }

        return rectangleRoute;
    }

    /**
     * 计算两点之间的距离（米）
     * @param start 起始点
     * @param end 终点
     * @return 距离（米）
     */
    public static double calculateDistance(LatLng start, LatLng end) {
        double lat1 = Math.toRadians(start.latitude);
        double lat2 = Math.toRadians(end.latitude);
        double deltaLat = Math.toRadians(end.latitude - start.latitude);
        double deltaLng = Math.toRadians(end.longitude - start.longitude);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return 6371000 * c; // 地球半径 6371 km
    }

    /**
     * 计算轨迹总长度
     * @param route 轨迹点列表
     * @return 总长度（米）
     */
    public static double calculateRouteDistance(List<LatLng> route) {
        double totalDistance = 0;

        for (int i = 0; i < route.size() - 1; i++) {
            totalDistance += calculateDistance(route.get(i), route.get(i + 1));
        }

        return totalDistance;
    }
}
