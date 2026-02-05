package com.example.remotecheckin.hook;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 设备信息伪装
 * 伪装设备信息以绕过应用检测
 */
public class DeviceInfoSpoof {
    private static final String TAG = "DeviceInfoSpoof";
    private static DeviceInfoSpoof instance;

    // 真实设备信息
    private String realBrand;
    private String realModel;
    private String realDevice;
    private String realManufacturer;
    private String realSerial;
    private String realAndroidId;

    // 伪装的设备信息
    private Map<String, String> spoofInfo = new HashMap<>();

    private DeviceInfoSpoof() {
        // 保存真实信息
        realBrand = Build.BRAND;
        realModel = Build.MODEL;
        realDevice = Build.DEVICE;
        realManufacturer = Build.MANUFACTURER;
        realSerial = getSerialNumber();
        realAndroidId = getAndroidId();

        // 设置默认伪装信息（小米设备）
        setDefaultSpoofInfo();
    }

    public static synchronized DeviceInfoSpoof getInstance() {
        if (instance == null) {
            instance = new DeviceInfoSpoof();
        }
        return instance;
    }

    /**
     * 设置默认伪装信息
     */
    private void setDefaultSpoofInfo() {
        // 小米 Mi 10
        spoofInfo.put("BRAND", "Xiaomi");
        spoofInfo.put("MODEL", "Mi 10");
        spoofInfo.put("DEVICE", "umi");
        spoofInfo.put("MANUFACTURER", "Xiaomi");
        spoofInfo.put("SERIAL", "unknown");
        spoofInfo.put("ANDROID_ID", "9774d56d682e549c");
        spoofInfo.put("BOARD", "kona");
        spoofInfo.put("HARDWARE", "qcom");
        spoofInfo.put("FINGERPRINT", "Xiaomi/umi/umi:11/RKQ1.200826.002/V12.5.7.0.RJBCNXM:user/release-keys");
    }

    /**
     * 设置自定义伪装信息
     */
    public void setSpoofInfo(String key, String value) {
        spoofInfo.put(key, value);
        Log.d(TAG, "Spoof info set: " + key + " = " + value);
    }

    /**
     * 获取伪装的设备信息
     */
    public String getSpoofInfo(String key, String defaultValue) {
        return spoofInfo.getOrDefault(key, defaultValue);
    }

    /**
     * 获取序列号
     */
    private String getSerialNumber() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Build.getSerial();
            } else {
                return Build.SERIAL;
            }
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 获取Android ID
     */
    private String getAndroidId() {
        // 需要Context，这里返回默认值
        return "9774d56d682e549c";
    }

    /**
     * 伪装Build.BRAND
     */
    public String spoofBrand() {
        return spoofInfo.get("BRAND");
    }

    /**
     * 伪装Build.MODEL
     */
    public String spoofModel() {
        return spoofInfo.get("MODEL");
    }

    /**
     * 伪装Build.DEVICE
     */
    public String spoofDevice() {
        return spoofInfo.get("DEVICE");
    }

    /**
     * 伪装Build.MANUFACTURER
     */
    public String spoofManufacturer() {
        return spoofInfo.get("MANUFACTURER");
    }

    /**
     * 伪装Build.FINGERPRINT
     */
    public String spoofFingerprint() {
        return spoofInfo.get("FINGERPRINT");
    }

    /**
     * 使用反射修改Build类的值
     */
    public boolean modifyBuildClass() {
        try {
            // 修改Build.BRAND
            setStaticField(Build.class, "BRAND", spoofInfo.get("BRAND"));

            // 修改Build.MODEL
            setStaticField(Build.class, "MODEL", spoofInfo.get("MODEL"));

            // 修改Build.MANUFACTURER
            setStaticField(Build.class, "MANUFACTURER", spoofInfo.get("MANUFACTURER"));

            // 修改Build.DEVICE
            setStaticField(Build.class, "DEVICE", spoofInfo.get("DEVICE"));

            Log.i(TAG, "Build class modified successfully");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Failed to modify Build class: " + e.getMessage());
            return false;
        }
    }

    /**
     * 使用反射设置静态字段
     */
    private void setStaticField(Class<?> clazz, String fieldName, String value) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~0x10); // 去除final

            field.set(null, value);

            Log.d(TAG, "Field modified: " + fieldName + " = " + value);

        } catch (Exception e) {
            Log.e(TAG, "Failed to set field " + fieldName + ": " + e.getMessage());
        }
    }

    /**
     * 获取TelephonyManager信息并伪装
     */
    public String spoofDeviceId(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            // 获取真实设备ID
            String realDeviceId = tm.getDeviceId();

            // 返回伪装的设备ID
            String spoofedDeviceId = "000000000000000"; // 15个0

            Log.d(TAG, "Device ID spoofed: " + realDeviceId + " -> " + spoofedDeviceId);
            return spoofedDeviceId;

        } catch (SecurityException e) {
            Log.e(TAG, "No permission to get device ID");
            return "000000000000000";
        }
    }

    /**
     * 伪装SIM序列号
     */
    public String spoofSimSerialNumber(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            String realSimSerial = tm.getSimSerialNumber();
            String spoofedSimSerial = "000000000000000";

            Log.d(TAG, "SIM serial spoofed: " + realSimSerial + " -> " + spoofedSimSerial);
            return spoofedSimSerial;

        } catch (SecurityException e) {
            Log.e(TAG, "No permission to get SIM serial");
            return "000000000000000";
        }
    }

    /**
     * 伪装手机号
     */
    public String spoofLineNumber(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            String realLine = tm.getLine1Number();
            String spoofedLine = "";

            Log.d(TAG, "Line number spoofed: " + realLine + " -> " + spoofedLine);
            return spoofedLine;

        } catch (SecurityException e) {
            Log.e(TAG, "No permission to get line number");
            return "";
        }
    }

    /**
     * 预设设备模板
     */
    public void setDeviceTemplate(DeviceTemplate template) {
        switch (template) {
            case XIAOMI_MI10:
                spoofInfo.put("BRAND", "Xiaomi");
                spoofInfo.put("MODEL", "Mi 10");
                spoofInfo.put("DEVICE", "umi");
                spoofInfo.put("MANUFACTURER", "Xiaomi");
                spoofInfo.put("BOARD", "kona");
                spoofInfo.put("FINGERPRINT", "Xiaomi/umi/umi:11/RKQ1.200826.002/V12.5.7.0.RJBCNXM:user/release-keys");
                break;

            case HUAWEI_P40:
                spoofInfo.put("BRAND", "HUAWEI");
                spoofInfo.put("MODEL", "ELS-AN00");
                spoofInfo.put("DEVICE", "HWELS");
                spoofInfo.put("MANUFACTURER", "HUAWEI");
                spoofInfo.put("BOARD", "elsa");
                spoofInfo.put("FINGERPRINT", "HUAWEI/ELS-AN00/HWELS:10/HUAWEIELS-AN00/10.1.0.160C00:user/release-keys");
                break;

            case SAMSUNG_S20:
                spoofInfo.put("BRAND", "samsung");
                spoofInfo.put("MODEL", "SM-G981B");
                spoofInfo.put("DEVICE", "x1q");
                spoofInfo.put("MANUFACTURER", "samsung");
                spoofInfo.put("BOARD", "exynos990");
                spoofInfo.put("FINGERPRINT", "samsung/x1qeea/x1q:11/RP1A.200720.012/G981BXXU1AUK5:user/release-keys");
                break;

            case OPPO_FIND_X3:
                spoofInfo.put("BRAND", "OPPO");
                spoofInfo.put("MODEL", "PEEM00");
                spoofInfo.put("DEVICE", "OP4F81");
                spoofInfo.put("MANUFACTURER", "OPPO");
                spoofInfo.put("BOARD", "msmnile");
                spoofInfo.put("FINGERPRINT", "OPPO/PEEM00/OP4F81:11/RKQ1.200905.002/1635757292268:user/release-keys");
                break;
        }

        Log.i(TAG, "Device template set: " + template.name());
    }

    /**
     * 设备模板枚举
     */
    public enum DeviceTemplate {
        XIAOMI_MI10,      // 小米 Mi 10
        HUAWEI_P40,       // 华为 P40
        SAMSUNG_S20,      // 三星 S20
        OPPO_FIND_X3      // OPPO Find X3
    }

    /**
     * 重置为真实设备信息
     */
    public void resetToReal() {
        spoofInfo.put("BRAND", realBrand);
        spoofInfo.put("MODEL", realModel);
        spoofInfo.put("DEVICE", realDevice);
        spoofInfo.put("MANUFACTURER", realManufacturer);
        spoofInfo.put("SERIAL", realSerial);
        spoofInfo.put("ANDROID_ID", realAndroidId);

        Log.i(TAG, "Reset to real device info");
    }

    /**
     * 检查Root状态（总是返回false）
     */
    public boolean isRooted() {
        // 总是返回未root
        return false;
    }

    /**
     * 检查设备是否已解锁Bootloader（总是返回false）
     */
    public boolean isBootloaderUnlocked() {
        // 总是返回未解锁
        return false;
    }

    /**
     * 获取伪装后的完整设备信息
     */
    public Map<String, String> getAllSpoofInfo() {
        return new HashMap<>(spoofInfo);
    }

    /**
     * 打印所有伪装信息
     */
    public void printSpoofInfo() {
        Log.d(TAG, "=== Device Spoof Info ===");
        for (Map.Entry<String, String> entry : spoofInfo.entrySet()) {
            Log.d(TAG, entry.getKey() + ": " + entry.getValue());
        }
        Log.d(TAG, "========================");
    }
}
