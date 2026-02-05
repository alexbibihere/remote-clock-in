# 📦 APK 打包完整指南

## 🚀 快速开始

### 方案 1：双击运行脚本（最简单）

1. **双击运行** `build_apk.bat`
2. 等待构建完成（首次需要下载依赖，可能需要 5-10 分钟）
3. 生成的 APK 会显示在窗口中

### 方案 2：使用 Android Studio（最稳定）

#### 步骤：
1. **下载 Android Studio**
   - 访问：https://developer.android.com/studio
   - 安装并启动

2. **打开项目**
   - 点击 `File` → `Open`
   - 选择：`d:\github\remote_dingding`
   - 等待 Gradle 同步完成

3. **构建 APK**
   - 点击菜单：`Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
   - 等待构建完成
   - 点击通知中的 `locate` 查看 APK 位置

### 方案 3：PowerShell 命令

右键点击 `Windows` 按钮 → `Windows PowerShell`（管理员）

```powershell
# 进入项目目录
cd d:\github\remote_dingding

# 检查环境
.\quick_test.bat

# 构建 Debug APK
.\gradlew.bat assembleDebug

# 查看生成的 APK
dir app\build\outputs\apk\debug\*.apk
```

### 方案 4：CMD 命令

```cmd
# 打开 CMD，进入项目目录
cd /d d:\github\remote_dingding

# 构建
gradlew.bat assembleDebug
```

---

## 📍 生成的 APK 位置

### Debug 版本（带调试信息）
```
app\build\outputs\apk\debug\app-debug.apk
```

### Release 版本（正式版，需要签名）
```
app\build\outputs\apk\release\app-release.apk
```

---

## 📱 安装到手机

### 方法 1：使用 ADB（推荐）
```bash
# 安装 APK
adb install -r app\build\outputs\apk\debug\app-debug.apk

# 如果安装失败，强制安装
adb install -r -d app\build\outputs\apk\debug\app-debug.apk
```

### 方法 2：直接传输
1. 将 APK 文件复制到手机
2. 在手机上使用文件管理器打开
3. 点击安装（需要允许"未知来源"）

### 方法 3：使用 QQ/微信
1. 将 APK 发送到手机
2. 在聊天中点击文件
3. 选择"安装应用"

---

## ⚙️ 构建配置

### Debug 构建特点
- ✅ 使用默认 debug 签名
- ✅ 包含调试信息
- ✅ 可以直接安装
- ❌ 不能发布到应用商店
- ❌ 文件较大

### Release 构建特点
- ✅ 代码混淆优化
- ✅ 文件较小
- ✅ 可用于发布
- ❌ 需要创建签名文件

---

## 🔑 Release 签名配置（可选）

### 创建签名文件

```bash
# 生成密钥库
keytool -genkey -v -keystore release.keystore -alias release -keyalg RSA -keysize 2048 -validity 10000

# 记住密码！这个密码以后每次打包都需要
```

### 配置签名

编辑 `app\build.gradle`，添加：

```gradle
android {
    signingConfigs {
        release {
            storeFile file("release.keystore")
            storePassword "你的密码"
            keyAlias "release"
            keyPassword "你的密码"
        }
    }

    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

然后构建：
```bash
gradlew.bat assembleRelease
```

---

## 🐛 常见问题

### 1. Gradle Wrapper 缺失
**错误**：`gradle\wrapper\gradle-wrapper.jar not found`

**解决**：
```powershell
# 方法 1：运行构建脚本（会自动下载）
.\build_apk.bat

# 方法 2：手动下载
Invoke-WebRequest -Uri "https://github.com/gradle/gradle/raw/v8.2.0/gradle/wrapper/gradle-wrapper.jar" -OutFile "gradle\wrapper\gradle-wrapper.jar"
```

### 2. Java 版本问题
**错误**：`Unsupported class file major version`

**解决**：
- 检查 Java 版本：`java -version`
- 需要 JDK 8 或更高版本
- 推荐使用 JDK 17：https://adoptium.net/

### 3. 依赖下载缓慢
**错误**：下载超时或速度很慢

**解决**：
```gradle
// 在项目根目录的 build.gradle 中添加国内镜像
buildscript {
    repositories {
        maven { url 'https://maven.aliyun.com/repository/google' }
        maven { url 'https://maven.aliyun.com/repository/public' }
        google()
        mavenCentral()
    }
}

allprojects {
    repositories {
        maven { url 'https://maven.aliyun.com/repository/google' }
        maven { url 'https://maven.aliyun.com/repository/public' }
        google()
        mavenCentral()
    }
}
```

### 4. 编译错误
**错误**：代码编译失败

**解决**：
- 使用 Android Studio 查看详细错误
- 检查代码语法
- 查看 `app\build\outputs\logs\` 中的日志

### 5. SDK 路径错误
**错误**：`SDK not found`

**解决**：
- 设置环境变量 `ANDROID_HOME`
- 或在 `local.properties` 中配置：
```properties
sdk.dir=D\:\\Android\\sdk
```

---

## 📊 构建变体说明

| 命令 | 说明 | 用途 |
|------|------|------|
| `gradlew.bat assembleDebug` | Debug 版本 | 开发测试 |
| `gradlew.bat assembleRelease` | Release 版本 | 正式发布 |
| `gradlew.bat installDebug` | 构建+安装 | 快速部署 |
| `gradlew.bat clean` | 清理构建 | 清理缓存 |

---

## 🎯 推荐流程

### 第一次构建
1. ✅ 运行 `quick_test.bat` 检查环境
2. ✅ 双击 `build_apk.bat` 开始构建
3. ☕ 等待 5-10 分钟（首次需要下载依赖）
4. ✅ 获取生成的 APK

### 后续构建
1. ✅ 直接运行 `gradlew.bat assembleDebug`
2. ⏱️ 等待 1-2 分钟

---

## 📝 注意事项

1. **首次构建**：需要下载 Gradle 和依赖，时间较长
2. **网络要求**：需要稳定的网络连接
3. **磁盘空间**：至少预留 2GB 空间
4. **Java 环境**：确保已安装并配置 JDK
5. **Debug APK**：不能直接发布到应用商店

---

## 🆘 获取帮助

如果遇到问题：

1. **查看日志**：`app\build\outputs\logs\`
2. **清理缓存**：`gradlew.bat clean`
3. **重启 Android Studio**：如果使用 IDE
4. **检查网络**：确保能访问 Maven 仓库

---

## 📚 相关文档

- [Android 官方构建指南](https://developer.android.com/build)
- [Gradle 官方文档](https://docs.gradle.org/)
- [项目 README](README.md)
- [更新日志](CHANGELOG.md)

---

## 🎉 构建成功后

### 验证 APK
```bash
# 查看 APK 信息
aapt dump badging app\build\outputs\apk\debug\app-debug.apk

# 查看 APK 内容
unzip -l app\build\outputs\apk\debug\app-debug.apk
```

### 测试安装
```bash
# 连接手机
adb devices

# 安装
adb install -r app\build\outputs\apk\debug\app-debug.apk

# 启动应用
adb shell am start -n com.example.remotecheckin/.MainActivity
```

### 卸载
```bash
adb uninstall com.example.remotecheckin
```

---

**祝您构建顺利！** 🎊
