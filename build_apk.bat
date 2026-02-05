@echo off
chcp 65001 >nul
echo ========================================
echo    开始构建 APK
echo ========================================
echo.

REM 检查 Java 环境
echo [1/4] 检查 Java 环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 错误: 未找到 Java，请先安装 JDK
    pause
    exit /b 1
)
echo ✓ Java 环境正常
echo.

REM 下载 Gradle Wrapper
echo [2/4] 检查 Gradle Wrapper...
if not exist "gradle\wrapper\gradle-wrapper.jar" (
    echo 正在下载 Gradle Wrapper...
    powershell -Command "& {Invoke-WebRequest -Uri 'https://github.com/gradle/gradle/raw/v8.2.0/gradle/wrapper/gradle-wrapper.jar' -OutFile 'gradle\wrapper\gradle-wrapper.jar'}"
    if %errorlevel% neq 0 (
        echo ❌ 下载失败，请手动下载或使用 Android Studio 构建
        pause
        exit /b 1
    )
    echo ✓ Gradle Wrapper 下载完成
) else (
    echo ✓ Gradle Wrapper 已存在
)
echo.

REM 构建 Debug APK
echo [3/4] 开始构建 Debug APK...
call gradlew.bat assembleDebug
if %errorlevel% neq 0 (
    echo.
    echo ❌ 构建失败！
    echo.
    echo 可能的原因：
    echo 1. 网络问题导致依赖下载失败
    echo 2. SDK 路径配置错误
    echo 3. 代码编译错误
    echo.
    echo 建议使用 Android Studio 打开项目进行构建
    pause
    exit /b 1
)
echo.

REM 查找生成的 APK
echo [4/4] 查找生成的 APK...
set "APK_PATH="
for /r "app\build\outputs\apk" %%f in (debug\*.apk) do (
    set "APK_PATH=%%f"
)

if defined APK_PATH (
    echo.
    echo ========================================
    echo    ✓ 构建成功！
    echo ========================================
    echo.
    echo APK 文件位置：
    echo %APK_PATH%
    echo.
    echo 文件大小：
    for %%A in ("%APK_PATH%") do echo %%~zA 字节
    echo.
    echo 您可以使用以下命令安装到设备：
    echo adb install -r "%APK_PATH%"
    echo.
) else (
    echo ⚠ 未找到生成的 APK 文件
    echo 请查看 app\build\outputs\apk\ 目录
    echo.
)

pause