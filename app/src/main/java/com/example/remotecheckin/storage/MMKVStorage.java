package com.example.remotecheckin.storage;

import android.content.Context;
import android.util.Log;

import com.example.remotecheckin.model.CheckInRecord;
import com.example.remotecheckin.model.LocationPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MMKV存储管理类
 * 高性能的KV存储，替代SharedPreferences
 *
 * 注意：此实现使用SharedPreferences作为后备方案
 * 如需使用真实MMKV，请添加依赖：implementation 'com.tencent:mmkv-static:1.2.15'
 */
public class MMKVStorage {
    private static final String TAG = "MMKVStorage";
    private static MMKVStorage instance;

    private static final String SP_NAME = "remote_checkin_mmkv";
    private static final String KEY_LOCATIONS = "locations";
    private static final String KEY_RECORDS = "records";
    private static final String KEY_APP_WHITELIST = "app_whitelist";
    private static final String KEY_SETTINGS = "settings";
    private static final String KEY_DEVICE_INFO = "device_info";

    private android.content.SharedPreferences preferences;
    private android.content.SharedPreferences.Editor editor;

    // 内存缓存
    private Map<String, Object> memoryCache = new ConcurrentHashMap<>();

    private MMKVStorage(Context context) {
        preferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
        loadCache();
    }

    public static synchronized MMKVStorage getInstance(Context context) {
        if (instance == null) {
            instance = new MMKVStorage(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * 加载缓存
     */
    private void loadCache() {
        // 将常用数据加载到内存
        memoryCache.put(KEY_LOCATIONS, preferences.getString(KEY_LOCATIONS, "[]"));
        memoryCache.put(KEY_RECORDS, preferences.getString(KEY_RECORDS, "[]"));
        memoryCache.put(KEY_APP_WHITELIST, preferences.getString(KEY_APP_WHITELIST, "[]"));
        Log.d(TAG, "Cache loaded");
    }

    // ==================== 基础KV操作 ====================

    /**
     * 保存字符串
     */
    public boolean encode(String key, String value) {
        memoryCache.put(key, value);
        editor.putString(key, value);
        return editor.commit();
    }

    /**
     * 保存整数
     */
    public boolean encode(String key, int value) {
        memoryCache.put(key, value);
        editor.putInt(key, value);
        return editor.commit();
    }

    /**
     * 保存长整数
     */
    public boolean encode(String key, long value) {
        memoryCache.put(key, value);
        editor.putLong(key, value);
        return editor.commit();
    }

    /**
     * 保存浮点数
     */
    public boolean encode(String key, float value) {
        memoryCache.put(key, value);
        editor.putFloat(key, value);
        return editor.commit();
    }

    /**
     * 保存布尔值
     */
    public boolean encode(String key, boolean value) {
        memoryCache.put(key, value);
        editor.putBoolean(key, value);
        return editor.commit();
    }

    /**
     * 保存字节数组
     */
    public boolean encode(String key, byte[] value) {
        memoryCache.put(key, value);
        editor.putString(key, android.util.Base64.encodeToString(value, android.util.Base64.DEFAULT));
        return editor.commit();
    }

    /**
     * 读取字符串
     */
    public String decodeString(String key, String defaultValue) {
        Object cached = memoryCache.get(key);
        if (cached instanceof String) {
            return (String) cached;
        }
        return preferences.getString(key, defaultValue);
    }

    /**
     * 读取整数
     */
    public int decodeInt(String key, int defaultValue) {
        Object cached = memoryCache.get(key);
        if (cached instanceof Integer) {
            return (Integer) cached;
        }
        return preferences.getInt(key, defaultValue);
    }

    /**
     * 读取长整数
     */
    public long decodeLong(String key, long defaultValue) {
        Object cached = memoryCache.get(key);
        if (cached instanceof Long) {
            return (Long) cached;
        }
        return preferences.getLong(key, defaultValue);
    }

    /**
     * 读取浮点数
     */
    public float decodeFloat(String key, float defaultValue) {
        Object cached = memoryCache.get(key);
        if (cached instanceof Float) {
            return (Float) cached;
        }
        return preferences.getFloat(key, defaultValue);
    }

    /**
     * 读取布尔值
     */
    public boolean decodeBool(String key, boolean defaultValue) {
        Object cached = memoryCache.get(key);
        if (cached instanceof Boolean) {
            return (Boolean) cached;
        }
        return preferences.getBoolean(key, defaultValue);
    }

    /**
     * 读取字节数组
     */
    public byte[] decodeBytes(String key) {
        String encoded = preferences.getString(key, null);
        if (encoded != null) {
            return android.util.Base64.decode(encoded, android.util.Base64.DEFAULT);
        }
        return null;
    }

    /**
     * 删除key
     */
    public void remove(String key) {
        memoryCache.remove(key);
        editor.remove(key);
        editor.commit();
    }

    /**
     * 清空所有数据
     */
    public void clearAll() {
        memoryCache.clear();
        editor.clear();
        editor.commit();
        Log.i(TAG, "All data cleared");
    }

    // ==================== 高级功能 ====================

    /**
     * 保存位置点列表
     */
    public boolean saveLocations(List<LocationPoint> locations) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (LocationPoint location : locations) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", location.getId());
                jsonObject.put("name", location.getName());
                jsonObject.put("latitude", location.getLatitude());
                jsonObject.put("longitude", location.getLongitude());
                jsonObject.put("accuracy", location.getAccuracy());
                jsonObject.put("timestamp", location.getTimestamp());
                jsonArray.put(jsonObject);
            }

            String json = jsonArray.toString();
            encode(KEY_LOCATIONS, json);
            Log.d(TAG, "Saved " + locations.size() + " locations");
            return true;

        } catch (JSONException e) {
            Log.e(TAG, "Failed to save locations: " + e.getMessage());
            return false;
        }
    }

    /**
     * 读取位置点列表
     */
    public List<LocationPoint> loadLocations() {
        List<LocationPoint> locations = new ArrayList<>();
        try {
            String json = decodeString(KEY_LOCATIONS, "[]");
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                LocationPoint location = new LocationPoint();
                location.setId(jsonObject.getLong("id"));
                location.setName(jsonObject.getString("name"));
                location.setLatitude(jsonObject.getDouble("latitude"));
                location.setLongitude(jsonObject.getDouble("longitude"));
                location.setAccuracy((float) jsonObject.getDouble("accuracy"));
                location.setTimestamp(jsonObject.getLong("timestamp"));
                locations.add(location);
            }

            Log.d(TAG, "Loaded " + locations.size() + " locations");
            return locations;

        } catch (JSONException e) {
            Log.e(TAG, "Failed to load locations: " + e.getMessage());
            return locations;
        }
    }

    /**
     * 保存打卡记录列表
     */
    public boolean saveRecords(List<CheckInRecord> records) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (CheckInRecord record : records) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", record.getId());
                jsonObject.put("locationPointId", record.getLocationPointId());
                jsonObject.put("locationName", record.getLocationName());
                jsonObject.put("latitude", record.getLatitude());
                jsonObject.put("longitude", record.getLongitude());
                jsonObject.put("scheduledTime", record.getScheduledTime());
                jsonObject.put("actualTime", record.getActualTime());
                jsonObject.put("success", record.isSuccess());
                jsonObject.put("errorMessage", record.getErrorMessage());
                jsonArray.put(jsonObject);
            }

            String json = jsonArray.toString();
            encode(KEY_RECORDS, json);
            Log.d(TAG, "Saved " + records.size() + " records");
            return true;

        } catch (JSONException e) {
            Log.e(TAG, "Failed to save records: " + e.getMessage());
            return false;
        }
    }

    /**
     * 读取打卡记录列表
     */
    public List<CheckInRecord> loadRecords() {
        List<CheckInRecord> records = new ArrayList<>();
        try {
            String json = decodeString(KEY_RECORDS, "[]");
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                CheckInRecord record = new CheckInRecord();
                record.setId(jsonObject.getLong("id"));
                record.setLocationPointId(jsonObject.getLong("locationPointId"));
                record.setLocationName(jsonObject.getString("locationName"));
                record.setLatitude(jsonObject.getDouble("latitude"));
                record.setLongitude(jsonObject.getDouble("longitude"));
                record.setScheduledTime(jsonObject.getLong("scheduledTime"));
                record.setActualTime(jsonObject.getLong("actualTime"));
                record.setSuccess(jsonObject.getBoolean("success"));
                record.setErrorMessage(jsonObject.optString("errorMessage"));
                records.add(record);
            }

            Log.d(TAG, "Loaded " + records.size() + " records");
            return records;

        } catch (JSONException e) {
            Log.e(TAG, "Failed to load records: " + e.getMessage());
            return records;
        }
    }

    /**
     * 保存应用白名单
     */
    public boolean saveAppWhitelist(List<String> packages) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (String pkg : packages) {
                jsonArray.put(pkg);
            }

            String json = jsonArray.toString();
            encode(KEY_APP_WHITELIST, json);
            Log.d(TAG, "Saved " + packages.size() + " whitelisted apps");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Failed to save app whitelist: " + e.getMessage());
            return false;
        }
    }

    /**
     * 读取应用白名单
     */
    public List<String> loadAppWhitelist() {
        List<String> packages = new ArrayList<>();
        try {
            String json = decodeString(KEY_APP_WHITELIST, "[]");
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                packages.add(jsonArray.getString(i));
            }

            Log.d(TAG, "Loaded " + packages.size() + " whitelisted apps");
            return packages;

        } catch (JSONException e) {
            Log.e(TAG, "Failed to load app whitelist: " + e.getMessage());
            return packages;
        }
    }

    /**
     * 添加应用到白名单
     */
    public boolean addAppToWhitelist(String packageName) {
        List<String> packages = loadAppWhitelist();
        if (!packages.contains(packageName)) {
            packages.add(packageName);
            return saveAppWhitelist(packages);
        }
        return true;
    }

    /**
     * 从白名单移除应用
     */
    public boolean removeAppFromWhitelist(String packageName) {
        List<String> packages = loadAppWhitelist();
        if (packages.remove(packageName)) {
            return saveAppWhitelist(packages);
        }
        return true;
    }

    /**
     * 检查应用是否在白名单中
     */
    public boolean isAppInWhitelist(String packageName) {
        List<String> packages = loadAppWhitelist();
        return packages.contains(packageName);
    }

    /**
     * 保存设置
     */
    public boolean saveSetting(String key, Object value) {
        try {
            JSONObject settings = new JSONObject(decodeString(KEY_SETTINGS, "{}"));
            if (value instanceof String) {
                settings.put(key, (String) value);
            } else if (value instanceof Integer) {
                settings.put(key, (Integer) value);
            } else if (value instanceof Long) {
                settings.put(key, (Long) value);
            } else if (value instanceof Boolean) {
                settings.put(key, (Boolean) value);
            } else if (value instanceof Double) {
                settings.put(key, (Double) value);
            }

            encode(KEY_SETTINGS, settings.toString());
            return true;

        } catch (JSONException e) {
            Log.e(TAG, "Failed to save setting: " + e.getMessage());
            return false;
        }
    }

    /**
     * 读取设置
     */
    public String loadSetting(String key, String defaultValue) {
        try {
            JSONObject settings = new JSONObject(decodeString(KEY_SETTINGS, "{}"));
            if (settings.has(key)) {
                return settings.getString(key);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to load setting: " + e.getMessage());
        }
        return defaultValue;
    }

    /**
     * 获取存储大小（字节）
     */
    public long getTotalSize() {
        // 简单估算
        long size = 0;
        Map<String, ?> all = preferences.getAll();
        for (Map.Entry<String, ?> entry : all.entrySet()) {
            String value = entry.getValue().toString();
            size += entry.getKey().getBytes().length;
            size += value.getBytes().length;
        }
        return size;
    }

    /**
     * 导出数据为JSON
     */
    public String exportToJson() {
        try {
            JSONObject json = new JSONObject();
            json.put(KEY_LOCATIONS, decodeString(KEY_LOCATIONS, "[]"));
            json.put(KEY_RECORDS, decodeString(KEY_RECORDS, "[]"));
            json.put(KEY_APP_WHITELIST, decodeString(KEY_APP_WHITELIST, "[]"));
            json.put(KEY_SETTINGS, decodeString(KEY_SETTINGS, "{}"));
            json.put("exportTime", System.currentTimeMillis());
            return json.toString(2);

        } catch (JSONException e) {
            Log.e(TAG, "Failed to export JSON: " + e.getMessage());
            return "{}";
        }
    }

    /**
     * 从JSON导入数据
     */
    public boolean importFromJson(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);

            if (json.has(KEY_LOCATIONS)) {
                encode(KEY_LOCATIONS, json.getString(KEY_LOCATIONS));
            }
            if (json.has(KEY_RECORDS)) {
                encode(KEY_RECORDS, json.getString(KEY_RECORDS));
            }
            if (json.has(KEY_APP_WHITELIST)) {
                encode(KEY_APP_WHITELIST, json.getString(KEY_APP_WHITELIST));
            }
            if (json.has(KEY_SETTINGS)) {
                encode(KEY_SETTINGS, json.getString(KEY_SETTINGS));
            }

            loadCache();
            Log.i(TAG, "Data imported successfully");
            return true;

        } catch (JSONException e) {
            Log.e(TAG, "Failed to import JSON: " + e.getMessage());
            return false;
        }
    }
}
