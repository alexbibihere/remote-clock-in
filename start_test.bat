@echo off
chcp 65001 >nul
echo.
echo ========================================
echo  🎯 RemoteCheckin v2.0 测试启动器
echo ========================================
echo.

REM 检查Java环境
echo [1/5] 检查Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java未安装或未配置PATH
    echo 请安装JDK 8或更高版本
    pause
    exit /b 1
)
echo ✅ Java环境正常

REM 检查项目文件
echo.
echo [2/5] 检查项目文件...
if not exist "app\src\main" (
    echo ❌ 项目文件不完整
    pause
    exit /b 1
)
echo ✅ 项目文件完整

REM 统计代码
echo.
echo [3/5] 统计项目信息...
for /f %%a in ('dir /s /b app\src\main\java\*.java 2^>nul ^| find /c /v ""') do set java_count=%%a
for /f %%a in ('dir /s /b *.md 2^>nul ^| find /c /v ""') do set doc_count=%%a
echo 📊 Java文件: %java_count% 个
echo 📚 文档文件: %doc_count% 个
echo ✅ 项目统计完成

REM 检查Android设备
echo.
echo [4/5] 检查测试设备...
where adb >nul 2>&1
if %errorlevel% neq 0 (
    echo ⚠️  未找到adb命令
    echo 尝试查找Android SDK...
    set "ADB_PATH="
    for /d %%i in (C:\Users\%USERNAME%\AppData\Local\Android\Sdk\platform-tools\adb.exe) do set "ADB_PATH=%%i"
    for /d %%i in (C:\Android\Sdk\platform-tools\adb.exe) do set "ADB_PATH=%%i"

    if defined ADB_PATH (
        echo ✅ 找到Android SDK
        "%ADB_PATH%" devices
    ) else (
        echo ❌ 未找到Android SDK
        echo.
        echo 💡 提示：
        echo    1. 安装Android Studio
        echo    2. 或连接Android设备
        goto :menu
    )
) else (
    adb devices
    echo ✅ adb命令可用
)

REM 测试选项菜单
echo.
echo [5/5] 选择测试方式...
echo.
goto :menu

:menu
echo ========================================
echo  请选择测试方式：
echo ========================================
echo.
echo  1. 测试现有base.apk（幻影分身）
echo  2. 构建并测试新APK（需要Android SDK）
echo  3. 查看项目文档
echo  4. 检查设备连接状态
echo  5. 显示测试指南
echo  0. 退出
echo.
set /p choice="请输入选项 (0-5): "

if "%choice%"=="1" goto :test_base_apk
if "%choice%"=="2" goto :build_new_apk
if "%choice%"=="3" goto :view_docs
if "%choice%"=="4" goto :check_device
if "%choice%"=="5" goto :show_guide
if "%choice%"=="0" goto :end
echo 无效选项，请重新选择
goto :menu

:test_base_apk
echo.
echo ========================================
echo  📱 测试幻影分身 base.apk
echo ========================================
echo.

if not exist "base.apk" (
    echo ❌ 找不到base.apk文件
    pause
    goto :menu
)

echo ✅ 找到base.apk (42.9 MB)
echo.
echo 📋 功能清单：
echo    ✅ 应用多开（28个应用）
echo    ✅ 虚拟定位
echo    ✅ 设备伪装
echo    ✅ WebRTC远程协助
echo.
echo 💡 测试步骤：
echo    1. 连接Android设备并启用USB调试
echo    2. 安装APK
echo    3. 在开发者选项中选择模拟位置应用
echo    4. 授予所有权限
echo    5. 开始测试
echo.

set /p install="是否立即安装？(Y/N): "
if /i "%install%"=="Y" (
    echo.
    echo 正在安装...
    if defined ADB_PATH (
        "%ADB_PATH%" install -r base.apk
    ) else (
        adb install -r base.apk
    )
    if %errorlevel% equ 0 (
        echo ✅ 安装成功！
        echo.
        echo 📱 下一步操作：
        echo    1. 在手机上打开"设置"
        echo    2. 进入"开发者选项"
        echo    3. 选择"模拟位置信息应用"
        echo    4. 选择刚安装的应用
        echo    5. 授予所有权限
        echo    6. 启动应用测试
    ) else (
        echo ❌ 安装失败
        echo 请检查：
        echo    - USB调试是否启用
        echo    - 是否授权计算机调试
        echo    - 存储空间是否足够
    )
)
echo.
pause
goto :menu

:build_new_apk
echo.
echo ========================================
echo  🔨 构建新APK
echo ========================================
echo.

REM 检查Gradle
if not exist "gradlew.bat" (
    echo ❌ 找不到gradlew.bat
    echo 需要使用Android Studio构建
    echo.
    echo 📖 使用Android Studio构建：
    echo    1. 打开Android Studio
    echo    2. File → Open → 选择本项目目录
    echo    3. 等待Gradle同步
    echo    4. Build → Build Bundle(s) / APK(s) → Build APK(s)
    echo    5. 在 app/build/outputs/apk/debug/ 找到APK
    pause
    goto :menu
)

echo ⚠️  注意：首次构建需要下载依赖，可能需要较长时间
echo.
set /p build="开始构建？(Y/N): "
if /i "%build%"=="Y" (
    echo.
    echo 开始构建...
    call gradlew.bat assembleDebug
    if %errorlevel% equ 0 (
        echo ✅ 构建成功！
        echo APK位置: app\build\outputs\apk\debug\app-debug.apk
        echo.
        set /p install_new="是否立即安装？(Y/N): "
        if /i "%install_new%"=="Y" (
            if exist "app\build\outputs\apk\debug\app-debug.apk" (
                if defined ADB_PATH (
                    "%ADB_PATH%" install -r app\build\outputs\apk\debug\app-debug.apk
                ) else (
                    adb install -r app\build\outputs\apk\debug\app-debug.apk
                )
            ) else (
                echo ❌ 找不到APK文件
            )
        )
    ) else (
        echo ❌ 构建失败
        echo 请检查错误信息
    )
)
echo.
pause
goto :menu

:view_docs
echo.
echo ========================================
echo  📚 项目文档列表
echo ========================================
echo.
echo  核心文档：
echo    1. TEST_READY.md              - 测试准备总结 ⭐
echo    2. PROJECT_SUMMARY.md         - 项目概览 ⭐
echo    3. TESTING_GUIDE.md           - 完整测试指南
echo    4. TECHNICAL_INTEGRATION.md   - 技术整合报告
echo    5. CHANGELOG.md               - 版本更新日志
echo.
echo  分析报告：
echo    6. APK_ANALYSIS_REPORT.md     - 幻影分身分析
echo    7. APK_TEST_GUIDE.md          - APK测试指南
echo.
echo  使用说明：
echo    8. README.md                  - 项目说明
echo    9. USAGE.md                   - 使用指南
echo.
set /p doc_choice="请输入文档编号打开（0返回）: "

if "%doc_choice%"=="1" start TEST_READY.md
if "%doc_choice%"=="2" start PROJECT_SUMMARY.md
if "%doc_choice%"=="3" start TESTING_GUIDE.md
if "%doc_choice%"=="4" start TECHNICAL_INTEGRATION.md
if "%doc_choice%"=="5" start CHANGELOG.md
if "%doc_choice%"=="6" start APK_ANALYSIS_REPORT.md
if "%doc_choice%"=="7" start APK_TEST_GUIDE.md
if "%doc_choice%"=="8" start README.md
if "%doc_choice%"=="9" start USAGE.md
if "%doc_choice%"=="0" goto :menu

echo.
pause
goto :menu

:check_device
echo.
echo ========================================
echo  📱 检查设备状态
echo ========================================
echo.

if defined ADB_PATH (
    "%ADB_PATH%" devices -l
) else (
    adb devices -l
)

echo.
echo 设备信息：
for /f "tokens=1,2" %%a in ('adb shell getprop ro.build.version.release 2^>nul') do set android_version=%%b
for /f "tokens=1,2" %%a in ('adb shell getprop ro.product.model 2^>nul') do set device_model=%%b
for /f "tokens=1,2" %%a in ('adb shell getprop ro.product.brand 2^>nul') do set device_brand=%%b

echo Android版本: %android_version%
echo 设备型号: %device_brand% %device_model%
echo.

echo 检查开发者选项...
adb shell settings get global development_settings_enabled 2>nul | findstr "1" >nul
if %errorlevel% equ 0 (
    echo ✅ 开发者选项已启用
) else (
    echo ⚠️  开发者选项未启用
)

echo 检查模拟位置设置...
for /f %%i in ('adb shell settings get secure mock_location 2^>nul') do set mock_loc=%%i
if "%mock_loc%"=="0" (
    echo ⚠️  未选择模拟位置应用
) else (
    echo ✅ 模拟位置应用: %mock_loc%
)

echo.
pause
goto :menu

:show_guide
echo.
echo ========================================
echo  📖 快速测试指南
echo ========================================
echo.
echo 🎯 测试目标：
echo    1. 验证位置模拟功能
echo    2. 测试打卡功能
echo    3. 检查应用稳定性
echo.
echo 📋 测试步骤：
echo.
echo 【基础测试】
echo   1. 安装APK到手机
echo   2. 启用开发者选项：
echo      设置 → 关于手机 → 连续点击"版本号"7次
echo   3. 选择模拟位置应用：
echo      设置 → 系统 → 开发者选项 → 选择模拟位置信息应用
echo   4. 授予位置权限
echo   5. 打开应用测试功能
echo.
echo 【功能测试】
echo   ☐ 添加位置点
echo   ☐ 模拟位置
echo   ☐ 规划轨迹
echo   ☐ 定时打卡
echo   ☐ 查看记录
echo.
echo 【验证方法】
echo   1. 打开Google Maps
echo   2. 查看当前位置是否为模拟位置
echo   3. 检查位置蓝点是否移动
echo.
echo 📝 测试记录：
echo   - 成功：记录功能正常
echo   - 失败：记录错误信息和日志
echo.
echo 💡 获取帮助：
echo   - 查看TESTING_GUIDE.md
echo   - 运行: adb logcat ^| grep RemoteCheckin
echo.
pause
goto :menu

:end
echo.
echo ========================================
echo  测试启动器已退出
echo ========================================
echo.
echo 📚 相关文档：
echo    - TEST_READY.md        测试准备总结
echo    - TESTING_GUIDE.md      完整测试指南
echo    - PROJECT_SUMMARY.md    项目概览
echo.
timeout /t 3 >nul
