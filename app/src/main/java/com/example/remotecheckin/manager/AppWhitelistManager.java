package com.example.remotecheckin.manager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.example.remotecheckin.hook.DeviceInfoSpoof;
import com.example.remotecheckin.model.AppWhitelist;
import com.example.remotecheckin.storage.MMKVStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用白名单管理器
 * 管理哪些应用使用虚拟定位
 */
public class AppWhitelistManager {
    private static final String TAG = "AppWhitelistManager";
    private static AppWhitelistManager instance;

    private Context context;
    private MMKVStorage storage;
    private List<AppWhitelist> whitelistCache;

    // 预设的热门打卡应用
    private static final String[][] PRESET_APPS = {
            {"com.ss.android.ugc.aweme", "抖音"},
            {"com.smile.gifmaker", "快手"},
            {"tv.danmaku.bili", "B站"},
            {"com.sina.weibo", "微博"},
            {"com.xingin.xhs", "小红书"},
            {"com.tencent.mm", "微信"},
            {"com.tencent.mobileqq", "QQ"},
            {"com.alibaba.android.rimet", "钉钉"},
            {"com.jingdong.app.mall", "京东"},
            {"com.taobao.taobao", "淘宝"},
            {"com.tencent.wework", "企业微信"},
            {"com.feicui.vdhelper", "学习通"},
            {"cn.chaozh.iqiyi", "爱奇艺"},
            {"com.tencent.qqlive", "腾讯视频"},
            {"com.youku.phone", "优酷"}
    };

    private AppWhitelistManager(Context context) {
        this.context = context.getApplicationContext();
        this.storage = MMKVStorage.getInstance(context);
        this.whitelistCache = new ArrayList<>();
        loadWhitelist();
    }

    public static synchronized AppWhitelistManager getInstance(Context context) {
        if (instance == null) {
            instance = new AppWhitelistManager(context);
        }
        return instance;
    }

    /**
     * 加载白名单
     */
    private void loadWhitelist() {
        List<String> packageNames = storage.loadAppWhitelist();
        whitelistCache.clear();

        for (String packageName : packageNames) {
            AppWhitelist app = getAppInfo(packageName);
            if (app != null) {
                whitelistCache.add(app);
            }
        }

        Log.d(TAG, "Loaded " + whitelistCache.size() + " apps in whitelist");
    }

    /**
     * 获取应用信息
     */
    private AppWhitelist getAppInfo(String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
            ApplicationInfo appInfo = packageInfo.applicationInfo;

            String appName = appInfo.loadLabel(pm).toString();
            Drawable icon = appInfo.loadIcon(pm);

            AppWhitelist app = new AppWhitelist(packageName, appName);
            app.setIcon(icon);

            return app;

        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "App not found: " + packageName);
            return null;
        }
    }

    /**
     * 获取所有白名单应用
     */
    public List<AppWhitelist> getWhitelist() {
        return new ArrayList<>(whitelistCache);
    }

    /**
     * 添加应用到白名单
     */
    public boolean addApp(String packageName) {
        if (isAppInWhitelist(packageName)) {
            Log.w(TAG, "App already in whitelist: " + packageName);
            return true;
        }

        AppWhitelist app = getAppInfo(packageName);
        if (app == null) {
            Log.e(TAG, "Cannot get app info: " + packageName);
            return false;
        }

        whitelistCache.add(app);
        saveWhitelist();

        Log.i(TAG, "Added app to whitelist: " + packageName);
        return true;
    }

    /**
     * 从白名单移除应用
     */
    public boolean removeApp(String packageName) {
        AppWhitelist toRemove = null;
        for (AppWhitelist app : whitelistCache) {
            if (app.getPackageName().equals(packageName)) {
                toRemove = app;
                break;
            }
        }

        if (toRemove != null) {
            whitelistCache.remove(toRemove);
            saveWhitelist();
            Log.i(TAG, "Removed app from whitelist: " + packageName);
            return true;
        }

        return false;
    }

    /**
     * 保存白名单
     */
    private void saveWhitelist() {
        List<String> packageNames = new ArrayList<>();
        for (AppWhitelist app : whitelistCache) {
            packageNames.add(app.getPackageName());
        }
        storage.saveAppWhitelist(packageNames);
    }

    /**
     * 检查应用是否在白名单中
     */
    public boolean isAppInWhitelist(String packageName) {
        for (AppWhitelist app : whitelistCache) {
            if (app.getPackageName().equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 启用/禁用应用
     */
    public void setAppEnabled(String packageName, boolean enabled) {
        for (AppWhitelist app : whitelistCache) {
            if (app.getPackageName().equals(packageName)) {
                app.setEnabled(enabled);
                saveWhitelist();
                Log.d(TAG, "App " + packageName + " set to " + enabled);
                return;
            }
        }
    }

    /**
     * 获取已启用的应用列表
     */
    public List<AppWhitelist> getEnabledApps() {
        List<AppWhitelist> enabledApps = new ArrayList<>();
        for (AppWhitelist app : whitelistCache) {
            if (app.isEnabled()) {
                enabledApps.add(app);
            }
        }
        return enabledApps;
    }

    /**
     * 获取已禁用的应用列表
     */
    public List<AppWhitelist> getDisabledApps() {
        List<AppWhitelist> disabledApps = new ArrayList<>();
        for (AppWhitelist app : whitelistCache) {
            if (!app.isEnabled()) {
                disabledApps.add(app);
            }
        }
        return disabledApps;
    }

    /**
     * 获取已安装的应用列表
     */
    public List<AppWhitelist> getInstalledApps() {
        List<AppWhitelist> apps = new ArrayList<>();
        PackageManager pm = context.getPackageManager();

        List<PackageInfo> packageInfos = pm.getInstalledPackages(
                PackageManager.GET_ACTIVITIES);

        for (PackageInfo packageInfo : packageInfos) {
            // 过滤系统应用
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                String packageName = packageInfo.packageName;
                String appName = packageInfo.applicationInfo.loadLabel(pm).toString();
                Drawable icon = packageInfo.applicationInfo.loadIcon(pm);

                AppWhitelist app = new AppWhitelist(packageName, appName);
                app.setIcon(icon);
                apps.add(app);
            }
        }

        return apps;
    }

    /**
     * 获取预设应用列表
     */
    public List<AppWhitelist> getPresetApps() {
        List<AppWhitelist> apps = new ArrayList<>();
        PackageManager pm = context.getPackageManager();

        for (String[] preset : PRESET_APPS) {
            String packageName = preset[0];
            String appName = preset[1];

            try {
                PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
                Drawable icon = packageInfo.applicationInfo.loadIcon(pm);

                AppWhitelist app = new AppWhitelist(packageName, appName);
                app.setIcon(icon);
                apps.add(app);

            } catch (PackageManager.NameNotFoundException e) {
                // 应用未安装，跳过
                Log.d(TAG, "Preset app not installed: " + packageName);
            }
        }

        return apps;
    }

    /**
     * 批量添加预设应用
     */
    public int addPresetApps() {
        int addedCount = 0;
        List<AppWhitelist> presetApps = getPresetApps();

        for (AppWhitelist app : presetApps) {
            if (!isAppInWhitelist(app.getPackageName())) {
                whitelistCache.add(app);
                addedCount++;
            }
        }

        if (addedCount > 0) {
            saveWhitelist();
            Log.i(TAG, "Added " + addedCount + " preset apps");
        }

        return addedCount;
    }

    /**
     * 清空白名单
     */
    public void clearWhitelist() {
        whitelistCache.clear();
        saveWhitelist();
        Log.i(TAG, "Whitelist cleared");
    }

    /**
     * 搜索应用
     */
    public List<AppWhitelist> searchApps(String query) {
        List<AppWhitelist> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        // 搜索已安装的应用
        List<AppWhitelist> installedApps = getInstalledApps();
        for (AppWhitelist app : installedApps) {
            if (app.getAppName().toLowerCase().contains(lowerQuery) ||
                    app.getPackageName().toLowerCase().contains(lowerQuery)) {
                results.add(app);
            }
        }

        return results;
    }

    /**
     * 获取白名单统计信息
     */
    public WhitelistStats getStats() {
        int totalApps = whitelistCache.size();
        int enabledApps = getEnabledApps().size();
        int disabledApps = totalApps - enabledApps;

        return new WhitelistStats(totalApps, enabledApps, disabledApps);
    }

    /**
     * 白名单统计信息
     */
    public static class WhitelistStats {
        public int totalApps;
        public int enabledApps;
        public int disabledApps;

        public WhitelistStats(int totalApps, int enabledApps, int disabledApps) {
            this.totalApps = totalApps;
            this.enabledApps = enabledApps;
            this.disabledApps = disabledApps;
        }

        @Override
        public String toString() {
            return "WhitelistStats{" +
                    "total=" + totalApps +
                    ", enabled=" + enabledApps +
                    ", disabled=" + disabledApps +
                    '}';
        }
    }

    /**
     * 设置设备模板（会更新白名单中所有应用的设备信息）
     */
    public void setDeviceTemplateForAll(DeviceInfoSpoof.DeviceTemplate template) {
        DeviceInfoSpoof spoof = DeviceInfoSpoof.getInstance();
        spoof.setDeviceTemplate(template);

        // 保存设置
        storage.saveSetting("device_template", template.name());

        Log.i(TAG, "Device template set for all apps: " + template.name());
    }

    /**
     * 导入白名单
     */
    public boolean importWhitelist(List<String> packageNames) {
        whitelistCache.clear();

        for (String packageName : packageNames) {
            AppWhitelist app = getAppInfo(packageName);
            if (app != null) {
                whitelistCache.add(app);
            }
        }

        saveWhitelist();
        Log.i(TAG, "Imported " + whitelistCache.size() + " apps");
        return true;
    }

    /**
     * 导出白名单
     */
    public List<String> exportWhitelist() {
        List<String> packageNames = new ArrayList<>();
        for (AppWhitelist app : whitelistCache) {
            packageNames.add(app.getPackageName());
        }
        return packageNames;
    }
}
