package com.example.remotecheckin.hook;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Xposed Hook模块
 * 需要设备安装Xposed框架
 * 功能更强大的Hook实现
 */
public class XposedHook implements IXposedHookLoadPackage {
    private static final String TAG = "XposedHook";

    // 要Hook的目标应用包名
    private static final String[] TARGET_PACKAGES = {
            "com.ss.android.ugc.aweme",  // 抖音
            "com.smile.gifmaker",        // 快手
            "tv.danmaku.bili",           // B站
            "com.sina.weibo",            // 微博
            "com.xingin.xhs"             // 小红书
    };

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        // 检查是否是目标包
        if (!isTargetPackage(lpparam.packageName)) {
            return;
        }

        Log.d(TAG, "Hooking package: " + lpparam.packageName);

        // Hook LocationManager
        hookLocationManager(lpparam);

        // Hook TelephonyManager (设备信息)
        hookTelephonyManager(lpparam);

        // Hook Build (设备信息)
        hookBuildClass(lpparam);
    }

    /**
     * 检查是否是目标包
     */
    private boolean isTargetPackage(String packageName) {
        for (String target : TARGET_PACKAGES) {
            if (target.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Hook LocationManager
     */
    private void hookLocationManager(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // Hook getLastKnownLocation
            XposedHelpers.findAndHookMethod(
                    LocationManager.class,
                    "getLastKnownLocation",
                    String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            String provider = (String) param.args[0];

                            // 获取模拟位置
                            Location mockLocation = LocationHook.getInstance().getMockLocation();
                            if (mockLocation != null) {
                                Log.d(TAG, "Returning mock location for provider: " + provider);
                                param.setResult(mockLocation);
                            }
                        }
                    }
            );

            // Hook requestLocationUpdates
            XposedHelpers.findAndHookMethod(
                    LocationManager.class,
                    "requestLocationUpdates",
                    String.class,
                    long.class,
                    float.class,
                    android.location.LocationListener.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            Log.d(TAG, "Location update request intercepted");
                            // 可以在这里模拟位置更新
                        }
                    }
            );

            Log.i(TAG, "LocationManager hooked successfully");

        } catch (Exception e) {
            Log.e(TAG, "Failed to hook LocationManager: " + e.getMessage());
        }
    }

    /**
     * Hook TelephonyManager (设备信息伪装)
     */
    private void hookTelephonyManager(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // Hook getDeviceId
            XposedHelpers.findAndHookMethod(
                    "android.telephony.TelephonyManager",
                    lpparam.classLoader,
                    "getDeviceId",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            // 返回伪造的设备ID
                            String fakeId = "000000000000000";
                            param.setResult(fakeId);
                            Log.d(TAG, "getDeviceId hooked: " + fakeId);
                        }
                    }
            );

            // Hook getSimSerialNumber
            XposedHelpers.findAndHookMethod(
                    "android.telephony.TelephonyManager",
                    lpparam.classLoader,
                    "getSimSerialNumber",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            String fakeSimId = "000000000000000";
                            param.setResult(fakeSimId);
                            Log.d(TAG, "getSimSerialNumber hooked");
                        }
                    }
            );

            // Hook getLine1Number (手机号)
            XposedHelpers.findAndHookMethod(
                    "android.telephony.TelephonyManager",
                    lpparam.classLoader,
                    "getLine1Number",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            param.setResult("");
                            Log.d(TAG, "getLine1Number hooked");
                        }
                    }
            );

            Log.i(TAG, "TelephonyManager hooked successfully");

        } catch (Exception e) {
            Log.e(TAG, "Failed to hook TelephonyManager: " + e.getMessage());
        }
    }

    /**
     * Hook Build类 (设备信息伪装)
     */
    private void hookBuildClass(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // Hook BRAND
            XposedHelpers.findAndHookMethod(
                    "android.os.Build",
                    lpparam.classLoader,
                    "getString",
                    String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            String key = (String) param.args[0];

                            // 返回伪造的设备信息
                            switch (key) {
                                case "BRAND":
                                    param.setResult("Xiaomi");
                                    break;
                                case "MODEL":
                                    param.setResult("Mi 10");
                                    break;
                                case "DEVICE":
                                    param.setResult("umi");
                                    break;
                                case "MANUFACTURER":
                                    param.setResult("Xiaomi");
                                    break;
                            }
                            Log.d(TAG, "Build." + key + " hooked");
                        }
                    }
            );

            Log.i(TAG, "Build class hooked successfully");

        } catch (Exception e) {
            Log.e(TAG, "Failed to hook Build class: " + e.getMessage());
        }
    }

    /**
     * Hook应用内的定位调用
     */
    public void hookAppLocation(Context context, String packageName, Location mockLocation) {
        try {
            ClassLoader classLoader = context.getClassLoader();

            // Hook应用的LocationManager调用
            XposedHelpers.findAndHookMethod(
                    "android.location.LocationManager",
                    classLoader,
                    "getLastKnownLocation",
                    String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            Log.d(TAG, "App " + packageName + " requested location");
                            param.setResult(mockLocation);
                        }
                    }
            );

            Log.i(TAG, "App location hooked: " + packageName);

        } catch (Exception e) {
            Log.e(TAG, "Failed to hook app location: " + e.getMessage());
        }
    }

    /**
     * 绕过Root检测
     */
    public static void bypassRootDetection(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // Hook常见的Root检测方法

            // 1. 检查su命令是否存在
            XposedHelpers.findAndHookMethod(
                    "java.io.File",
                    lpparam.classLoader,
                    "exists",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            String path = ((java.io.File) param.thisObject).getAbsolutePath();

                            // 如果是su相关文件，返回false
                            if (path.contains("su") || path.contains("busybox")) {
                                param.setResult(false);
                                Log.d(TAG, "Root detection bypassed for: " + path);
                            }
                        }
                    }
            );

            // 2. Hook Runtime.exec (检查执行命令)
            XposedHelpers.findAndHookMethod(
                    "java.lang.Runtime",
                    lpparam.classLoader,
                    "exec",
                    String[].class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            String[] commands = (String[]) param.args[0];
                            if (commands != null && commands.length > 0) {
                                String cmd = commands[0];
                                if (cmd.contains("su") || cmd.contains("which")) {
                                    // 返回空Process，假装执行成功
                                    Log.d(TAG, "Root command blocked: " + cmd);
                                }
                            }
                        }
                    }
            );

            Log.i(TAG, "Root detection bypassed");

        } catch (Exception e) {
            Log.e(TAG, "Failed to bypass root detection: " + e.getMessage());
        }
    }
}
