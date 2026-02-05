# 快速开始指南

## 项目配置

### 1. Google Maps API密钥配置

在运行应用之前，需要配置Google Maps API密钥：

1. 访问 [Google Cloud Console](https://console.cloud.google.com/)
2. 创建新项目或选择现有项目
3. 启用以下API：
   - Maps SDK for Android
   - Places API
4. 创建API密钥并添加限制（Android应用）
5. 在 `app/src/main/AndroidManifest.xml` 中添加：

```xml
<application>
    <meta-data
        android:name="com.google.android.geo.API_KEY"
        android:value="YOUR_API_KEY_HERE" />
</application>
```

### 2. 开发者选项设置

在Android设备上启用位置模拟：

1. 进入设置 → 关于手机
2. 连续点击"版本号"7次，启用开发者选项
3. 进入设置 → 系统 → 开发者选项
4. 找到"选择模拟位置信息应用"
5. 选择"远程打卡助手"

### 3. 导入项目到Android Studio

1. 打开Android Studio
2. 选择 "Open" 或 "File → Open"
3. 选择项目根目录
4. 等待Gradle同步完成

## 功能使用说明

### 添加和使用位置

1. **添加位置**
   - 点击"添加位置"按钮
   - 地图打开后，点击任意位置
   - 拖动标记可精确调整位置
   - 输入位置名称（如"公司"、"家"等）
   - 点击"确认选择"保存

2. **模拟位置**
   - 在位置列表中点击选择一个位置
   - 点击"模拟位置"按钮
   - 通知栏会显示"位置模拟运行中"
   - 其他应用会获取到模拟的GPS位置

3. **停止模拟**
   - 在开发者选项中选择"无"停止模拟
   - 或重启手机

### 轨迹模拟

1. **规划轨迹**
   - 点击"轨迹模拟"按钮
   - 地图打开后点击"规划轨迹"
   - 依次点击地图添加路径点
   - 点击"完成轨迹"生成平滑路径

2. **轨迹类型**
   - 自由轨迹：在地图上手动点击多个点
   - 系统会自动在关键点之间插入中间点
   - 使移动路径更加平滑自然

3. **执行轨迹**
   - 轨迹规划完成后自动开始
   - 应用会按照轨迹点顺序移动位置
   - 默认每秒更新一次位置

### 定时打卡

1. **设置打卡任务**
   - 选择一个已保存的位置
   - 点击"定时打卡"按钮
   - 在时间选择器中设置打卡时间
   - 系统会自动在指定时间执行

2. **打卡流程**
   - 到达设定时间后自动启动
   - 切换到指定的GPS位置
   - 等待位置生效（约3秒）
   - 记录打卡结果到数据库

3. **查看打卡记录**
   - 点击"查看打卡记录"按钮
   - 显示所有打卡历史
   - 包含时间、位置、成功状态

## 数据库管理

### 查看数据库

```bash
# 连接到设备
adb shell

# 进入应用数据库目录
cd /data/data/com.example.remotecheckin/databases

# 使用sqlite3查看
sqlite3 remote_checkin.db

# 查看所有表
.tables

# 查看位置点
SELECT * FROM location_points;

# 查看打卡记录
SELECT * FROM checkin_records;
```

### 导出数据

```bash
# 从设备导出数据库
adb pull /data/data/com.example.remotecheckin/databases/remote_checkin.db ./
```

## 常见问题

### Q: 模拟位置不生效？
A:
1. 检查是否在开发者选项中选择了本应用
2. 检查位置权限是否已授予
3. 某些应用可能检测模拟位置，无法欺骗
4. 重启位置服务或重启手机

### Q: Google地图无法显示？
A:
1. 检查API密钥是否正确配置
2. 检查网络连接
3. 确认API已启用且配额未超限
4. 检查SHA-1指纹是否正确添加

### Q: 打卡失败？
A:
1. 检查权限是否完整授予
2. 确保应用在后台运行未被杀死
3. 关闭电池优化
4. 检查logcat日志查看错误信息

### Q: 应用闪退？
A:
1. 查看logcat日志
   ```bash
   adb logcat | grep RemoteCheckin
   ```
2. 检查最低SDK版本要求（API 24+）
3. 确保Google Play Services已安装

## 开发建议

### 扩展功能

1. **添加更多轨迹类型**
   - 在`RouteGenerator.java`中添加新方法
   - 如螺旋形、8字形等

2. **自动打卡优化**
   - 在`CheckInService.java`中添加具体打卡逻辑
   - 使用Accessibility Service模拟点击
   - 或直接调用相关打卡API

3. **UI改进**
   - 使用Material Design 3
   - 添加动画效果
   - 改进地图交互体验

### 调试技巧

```bash
# 查看实时日志
adb logcat | grep -E "MockLocation|CheckIn"

# 查看位置信息
adb shell dumpsys location

# 查看通知信息
adb shell dumpsys notification
```

## 安全提示

1. **仅用于学习目的**
   - 不要用于欺骗打卡系统
   - 遵守公司规章制度

2. **数据隐私**
   - 应用仅存储在本地
   - 不上传任何位置数据
   - 定期清理敏感数据

3. **法律风险**
   - 某些地区可能禁止使用位置模拟
   - 使用前了解相关法律法规
   - 后果自负

## 下一步开发

### 建议添加的功能

- [ ] Web管理界面
- [ ] 位置分享功能
- [ ] 导入导出数据
- [ ] 历史轨迹回放
- [ ] 更智能的打卡策略
- [ ] 支持多个打卡应用
- [ ] 云端数据备份
- [ ] 围栏功能

欢迎贡献代码和提出建议！
