@echo off
chcp 65001 >nul
echo.
echo ========================================
echo  🎯 APK快速构建工具
echo ========================================
echo.
echo [1/3] 检查Java...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java未安装
    pause
    exit /b 1
)
echo ✅ Java正常

echo.
echo [2/3] 检查项目...
if not exist "app\src\main" (
    echo ❌ 项目不完整
    pause
    exit /b 1
)
echo ✅ 项目完整

echo.
echo [3/3] 开始构建Debug APK...
echo.
echo ⏳ 首次构建需要下载依赖（5-10分钟）
echo.

call gradlew.bat assembleDebug

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo  ✅ 构建成功！
    echo ========================================
    echo.
    echo APK位置: app\build\outputs\apk\debug\app-debug.apk
    echo.
    
    if exist "app\build\outputs\apk\debug\app-debug.apk" (
        dir "app\build\outputs\apk\debug\app-debug.apk"
        echo.
        set /p install="安装到设备？(Y/N): "
        if /i "!install!"=="Y" (
            adb install -r "app\build\outputs\apk\debug\app-debug.apk"
        )
    )
) else (
    echo.
    echo ❌ 构建失败
    echo 请查看上方错误信息
)
echo.
pause
