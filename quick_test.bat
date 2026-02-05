@echo off
chcp 65001 >nul
echo ========================================
echo RemoteCheckin v2.0 快速测试脚本
echo ========================================
echo.

echo [1/5] 检查Java环境...
java -version 2>&1 | findstr /i "version"
if %errorlevel% neq 0 (
    echo ❌ Java未安装或未配置PATH
    echo 请安装JDK 8或更高版本
    pause
    exit /b 1
)
echo ✅ Java环境正常
echo.

echo [2/5] 检查项目文件...
if not exist "app\src\main\AndroidManifest.xml" (
    echo ❌ 找不到AndroidManifest.xml
    pause
    exit /b 1
)
if not exist "app\build.gradle" (
    echo ❌ 找不到build.gradle
    pause
    exit /b 1
)
echo ✅ 项目文件完整
echo.

echo [3/5] 检查已连接的设备...
adb devices
if %errorlevel% neq 0 (
    echo ⚠️  adb命令不可用
    echo 请确保已安装Android SDK或连接Android设备
    echo.
    echo 如果没有Android Studio，请访问:
    echo https://developer.android.com/studio
)
echo.

echo [4/5] 检查Gradle...
if not exist "gradlew.bat" (
    echo ❌ gradlew.bat不存在
    pause
    exit /b 1
)
echo ✅ Gradle wrapper存在
echo.

echo [5/5] 检查项目结构...
echo 核心文件列表:
echo   - Hook框架: hook\LocationHook.java
if exist "app\src\main\java\com\example\remotecheckin\hook\LocationHook.java" (
    echo     ✅ LocationHook.java
) else (
    echo     ❌ LocationHook.java 不存在
)

echo   - Xposed集成: hook\XposedHook.java
if exist "app\src\main\java\com\example\remotecheckin\hook\XposedHook.java" (
    echo     ✅ XposedHook.java
) else (
    echo     ❌ XposedHook.java 不存在
)

echo   - 设备伪装: hook\DeviceInfoSpoof.java
if exist "app\src\main\java\com\example\remotecheckin\hook\DeviceInfoSpoof.java" (
    echo     ✅ DeviceInfoSpoof.java
) else (
    echo     ❌ DeviceInfoSpoof.java 不存在
)

echo   - MMKV存储: storage\MMKVStorage.java
if exist "app\src\main\java\com\example\remotecheckin\storage\MMKVStorage.java" (
    echo     ✅ MMKVStorage.java
) else (
    echo     ❌ MMKVStorage.java 不存在
)

echo   - 应用白名单: manager\AppWhitelistManager.java
if exist "app\src\main\java\com\example\remotecheckin\manager\AppWhitelistManager.java" (
    echo     ✅ AppWhitelistManager.java
) else (
    echo     ❌ AppWhitelistManager.java 不存在
)

echo   - 高级服务: service\AdvancedMockLocationService.java
if exist "app\src\main\java\com\example\remotecheckin\service\AdvancedMockLocationService.java" (
    echo     ✅ AdvancedMockLocationService.java
) else (
    echo     ❌ AdvancedMockLocationService.java 不存在
)
echo.

echo ========================================
echo 检查完成！
echo ========================================
echo.
echo 📋 测试选项:
echo.
echo 【方案1】使用Android Studio（推荐）
echo   1. 下载安装Android Studio
echo   2. 打开项目: D:\github\remote_dingding
echo   3. 同步Gradle
echo   4. 点击运行按钮
echo.
echo 【方案2】现有base.apk测试
echo   直接安装测试现有的幻影分身APK:
echo   adb install base.apk
echo.
echo 【方案3】查看源代码
echo   所有源代码已在: app\src\main\java\
echo   查看文档: README.md, CHANGELOG.md, TESTING_GUIDE.md
echo.
echo 💡 推荐下一步操作:
echo   1. 阅读测试指南: TESTING_GUIDE.md
echo   2. 查看技术文档: TECHNICAL_INTEGRATION.md
echo   3. 安装Android Studio进行完整测试
echo.
pause
