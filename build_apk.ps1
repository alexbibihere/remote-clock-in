# APK 构建说明

## 方法一：使用构建脚本（推荐）

### Windows 批处理
双击运行 `build_apk.bat`，脚本会自动：
1. 检查 Java 环境
2. 下载缺失的 Gradle Wrapper
3. 构建 Debug APK
4. 显示生成的 APK 路径

### PowerShell
右键 `build_apk.ps1` → 使用 PowerShell 运行

---

## 方法二：手动命令行构建

### 1. 打开命令提示符（CMD）
在项目根目录下打开 CMD

### 2. 下载 Gradle Wrapper（如果缺失）
```powershell
# 使用 PowerShell 下载
Invoke-WebRequest -Uri "https://github.com/gradle/gradle/raw/v8.2.0/gradle/wrapper/gradle-wrapper.jar" -OutFile "gradle\wrapper\gradle-wrapper.jar"
```

### 3. 构建 Debug APK
```bash
gradlew.bat assembleDebug
```

### 4. 构建 Release APK
```bash
gradlew.bat assembleRelease
```

---

## 方法三：使用 Android Studio（最简单）

1. 打开 Android Studio
2. 点击 `File` → `Open`
3. 选择项目根目录 `d:\github\remote_dingding`
4. 等待 Gradle 同步完成
5. 点击菜单 `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
6. 构建完成后点击通知中的 `locate` 查看 APK

---

## 方法四：使用现有的 Gradle（如果已安装）

```bash
gradle assembleDebug
```

---

## 生成的 APK 位置

### Debug 版本
```
app\build\outputs\apk\debug\app-debug.apk
```

### Release 版本
```
app\build\outputs\apk\release\app-release.apk
```

---

## 安装 APK 到设备

### 使用 ADB
```bash
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### 直接传输
将 APK 文件复制到手机，使用文件管理器打开安装

---

## 常见问题

### 1. Gradle Wrapper 下载失败
**解决方案**：
- 检查网络连接
- 使用 VPN 或代理
- 手动从 GitHub 下载 `gradle-wrapper.jar`

### 2. Java 版本不兼容
**要求**：
- JDK 8 或更高版本
- 建议使用 JDK 17

**检查 Java 版本**：
```bash
java -version
```

### 3. 依赖下载缓慢
**解决方案**：
- 配置国内镜像源（阿里云）
- 编辑 `build.gradle` 添加镜像仓库

### 4. 编译错误
**解决方案**：
- 使用 Android Studio 查看详细错误信息
- 检查 `AndroidManifest.xml` 配置
- 检查代码语法错误

---

## 签名说明

### Debug APK
- 使用默认的 debug 签名
- 可以直接安装和调试
- 不能发布到应用商店

### Release APK
- 需要创建正式签名文件
- 用于正式发布

**创建签名文件**：
```bash
keytool -genkey -v -keystore release.keystore -alias release -keyalg RSA -keysize 2048 -validity 10000
```

然后在 `app\build.gradle` 中配置签名。

---

## 快速测试

双击运行 `quick_test.bat` 可以快速测试构建环境。

---

## 需要帮助？

如果遇到问题，请提供：
1. 完整的错误信息
2. Java 版本：`java -version`
3. 操作系统版本
4. 使用的构建方法