@echo off
chcp 65001 >nul
echo ========================================
echo    手动下载 Gradle Wrapper 指南
echo ========================================
echo.

echo 由于网络问题，无法自动下载 Gradle Wrapper
echo 请按照以下步骤手动设置：
echo.

echo ┌─────────────────────────────────────────┐
echo │ 方法 1：使用 Android Studio（推荐）     │
echo └─────────────────────────────────────────┘
echo.
echo 1. 下载 Android Studio
echo    https://developer.android.com/studio
echo.
echo 2. 打开项目：d:\github\remote_dingding
echo.
echo 3. Android Studio 会自动下载所需文件
echo.
echo 4. 点击 Build → Build APK
echo.

echo ┌─────────────────────────────────────────┐
echo │ 方法 2：手动下载 Gradle Wrapper        │
echo └─────────────────────────────────────────┘
echo.
echo 1. 访问以下链接下载 gradle-wrapper.jar：
echo    https://github.com/gradle/gradle/raw/v8.2.0/gradle/wrapper/gradle-wrapper.jar
echo.
echo 2. 将下载的文件放到以下位置：
echo    d:\github\remote_dingding\gradle\wrapper\gradle-wrapper.jar
echo.
echo 3. 运行以下命令构建：
echo    gradlew.bat assembleDebug
echo.

echo ┌─────────────────────────────────────────┐
echo │ 方法 3：使用浏览器下载                 │
echo └─────────────────────────────────────────┘
echo.
echo 正在尝试打开浏览器...
echo 如果浏览器没有打开，请手动访问：
echo https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar
echo.

start https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar

echo.
echo ┌─────────────────────────────────────────┐
echo │ 方法 4：使用已有的 base.apk            │
echo └─────────────────────────────────────────┘
echo.
echo 您的项目中已经有一个 base.apk（42.8 MB）
echo.
echo 位置：d:\github\remote_dingding\base.apk
echo.
echo 这是一个已经打包好的应用，可以直接安装测试：
echo   adb install -r base.apk
echo.

pause
