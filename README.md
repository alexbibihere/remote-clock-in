# 远程打卡助手

一个功能完整的Android远程打卡应用，支持位置模拟、轨迹规划和自动打卡。

## 功能特性

### 1. 位置模拟
- 在地图上选择任意位置进行GPS模拟
- 支持实时位置切换
- 可保存和管理多个位置点
- 模拟真实GPS精度

### 2. 轨迹模拟
- 在地图上规划自定义轨迹
- 自动生成平滑移动路径
- 支持圆形、矩形等预设轨迹
- 可调节移动速度和更新频率

### 3. 自动打卡
- 定时打卡任务调度
- 精确到分钟的打卡时间设置
- 自动位置切换和打卡执行
- 打卡结果记录和历史查询

### 4. 数据管理
- SQLite数据库存储位置和记录
- 打卡历史查询
- 位置点管理（添加、删除、编辑）
- 数据统计和可视化

## 技术架构

### 核心技术栈
- **开发语言**: Java
- **最低SDK**: Android 7.0 (API 24)
- **目标SDK**: Android 14 (API 34)
- **构建工具**: Gradle 8.2.0

### 主要依赖
```
androidx.appcompat:appcompat:1.6.1
com.google.android.material:material:1.11.0
com.google.android.gms:play-services-maps:18.2.0
com.google.android.gms:play-services-location:21.1.0
```

### 项目结构
```
app/src/main/java/com/example/remotecheckin/
├── MainActivity.java              # 主界面
├── MapPickerActivity.java         # 地图选点
├── model/
│   ├── LocationPoint.java         # 位置数据模型
│   └── CheckInRecord.java         # 打卡记录模型
├── service/
│   ├── MockLocationService.java   # 位置模拟服务
│   └── CheckInService.java        # 自动打卡服务
├── database/
│   └── DatabaseHelper.java        # 数据库管理
└── utils/
    ├── RouteGenerator.java        # 轨迹生成工具
    └── LocationUtils.java         # 位置工具类
```

## 使用说明

### 前置要求

1. **开发者选项**
   - 启用"开发者选项"
   - 开启"选择模拟位置信息应用"
   - 选择本应用作为模拟位置应用

2. **位置权限**
   - 授予精确位置权限
   - 授予后台位置权限（Android 12+）
   - 授予存储权限（Android 10及以下）

3. **Google Maps API**
   - 申请Google Maps API密钥
   - 在`AndroidManifest.xml`中配置API密钥

### 基本使用

#### 1. 添加位置
- 点击"添加位置"按钮
- 在地图上选择位置
- 输入位置名称（可选）
- 点击"确认选择"

#### 2. 模拟位置
- 在位置列表中选择一个位置
- 点击"模拟位置"按钮
- 应用开始模拟GPS位置

#### 3. 轨迹模拟
- 点击"轨迹模拟"按钮
- 在地图上点击添加轨迹点
- 点击"完成轨迹"生成平滑路径
- 自动开始轨迹模拟

#### 4. 定时打卡
- 选择一个位置点
- 点击"定时打卡"按钮
- 选择打卡时间
- 系统自动在指定时间执行打卡

#### 5. 查看记录
- 点击"查看打卡记录"按钮
- 查看所有打卡历史
- 长按位置可删除

## 数据库结构

### 位置点表 (location_points)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER | 主键 |
| name | TEXT | 位置名称 |
| latitude | REAL | 纬度 |
| longitude | REAL | 经度 |
| accuracy | REAL | 精度 |
| timestamp | INTEGER | 时间戳 |

### 打卡记录表 (checkin_records)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER | 主键 |
| location_point_id | INTEGER | 位置点ID |
| location_name | TEXT | 位置名称 |
| latitude | REAL | 纬度 |
| longitude | REAL | 经度 |
| scheduled_time | INTEGER | 计划时间 |
| actual_time | INTEGER | 实际时间 |
| success | INTEGER | 成功标志 |
| error_message | TEXT | 错误信息 |

## 权限说明

应用需要以下权限：
- `ACCESS_FINE_LOCATION` - 精确位置
- `ACCESS_COARSE_LOCATION` - 粗略位置
- `ACCESS_BACKGROUND_LOCATION` - 后台位置（Android 12+）
- `WRITE_EXTERNAL_STORAGE` - 存储权限（Android 10及以下）
- `FOREGROUND_SERVICE` - 前台服务
- `POST_NOTIFICATIONS` - 通知权限（Android 13+）

## 注意事项

1. **合法使用**: 仅用于个人学习和授权测试，不要用于违法用途
2. **API密钥**: 需要申请有效的Google Maps API密钥
3. **电池优化**: 建议将应用加入电池优化白名单
4. **系统限制**: 某些系统可能限制位置模拟功能

## 编译和安装

### 编译要求
- Android Studio 2022.1或更高版本
- JDK 8或更高版本
- Android SDK 34

### 编译步骤
1. 克隆项目到本地
2. 使用Android Studio打开项目
3. 配置Google Maps API密钥
4. 同步Gradle依赖
5. 连接Android设备或启动模拟器
6. 点击运行按钮编译安装

### 生成APK
```bash
# Debug版本
./gradlew assembleDebug

# Release版本
./gradlew assembleRelease
```

## 开发说明

### 添加新功能
1. 在相应的包中创建新类
2. 实现核心逻辑
3. 在AndroidManifest.xml中注册（如需要）
4. 更新UI界面

### 修改数据库
1. 增加`DATABASE_VERSION`版本号
2. 在`onUpgrade()`方法中添加迁移逻辑
3. 测试数据迁移

### 调试技巧
- 使用adb logcat查看日志
- 检查数据库: `adb shell dumpsys dbinfo`
- 查看位置: `adb shell dumpsys location`

## 许可证

仅供学习和研究使用。

## 免责声明

本应用仅供学习和技术研究使用。使用本应用造成的任何后果由使用者自行承担。

## 更新日志

### v1.0.0 (2024)
- 初始版本发布
- 实现位置模拟功能
- 实现轨迹模拟功能
- 实现自动打卡功能
- 实现数据库管理
