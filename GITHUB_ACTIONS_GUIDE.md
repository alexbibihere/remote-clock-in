# 🚀 GitHub Actions 自动构建APK - 完整指南

## ✅ 已完成的配置

```
✅ GitHub Actions工作流已创建
✅ .gitignore文件已创建
✅ Git初始化脚本已创建
✅ 构建配置已就绪
```

## 📋 前置要求

1. **GitHub账号**
   - 访问 https://github.com
   - 注册或登录（免费）

2. **Git工具**
   - 下载: https://git-scm.com/download/win
   - 安装: 使用默认设置安装

## 🎯 快速开始（3步完成）

### 方式1: 使用自动脚本（推荐）⭐

```
1. 双击运行: setup_github.bat
2. 按照提示操作
3. 完成！
```

### 方式2: 手动操作

## 📖 详细步骤

### 步骤1: 创建GitHub仓库

1. **访问GitHub**
   ```
   打开浏览器，访问: https://github.com
   ```

2. **创建新仓库**
   ```
   1. 点击右上角 "+" 图标
   2. 选择 "New repository"
   ```

3. **填写仓库信息**
   ```
   Repository name: remote_dingding
   Description: Android远程打卡应用 - RemoteCheckin v2.0
   
   选择可见性:
   ☑ Public  (公开，任何人可见)
   ☐ Private (私有，仅自己可见)
   
   ⚠️  不要勾选:
   ☐ Add a README file
   ☐ Add .gitignore
   ☐ Choose a license
   ```

4. **创建仓库**
   ```
   点击 "Create repository"
   ```

5. **复制仓库URL**
   ```
   HTTPS: https://github.com/你的用户名/remote_dingding.git
   
   记住这个URL，后面要用！
   ```

### 步骤2: 安装Git（如果未安装）

1. **下载Git**
   ```
   访问: https://git-scm.com/download/win
   点击: Download for Windows
   ```

2. **安装Git**
   ```
   1. 运行下载的安装程序
   2. 点击 "Next"
   3. 选择默认编辑器（推荐VS Code）
   4. 选择 "Git from the command line..."
   5. 点击 "Next" 使用默认设置
   6. 点击 "Install"
   7. 点击 "Finish"
   ```

3. **验证安装**
   ```bash
   # 打开CMD或PowerShell
   git --version
   # 应显示: git version x.x.x
   ```

### 步骤3: 推送代码到GitHub

#### 方法A: 使用自动脚本（推荐）

```
1. 双击运行: setup_github.bat
2. 输入仓库URL
3. 按提示操作
4. 等待推送完成
```

#### 方法B: 手动命令

```bash
# 打开Git Bash或CMD
cd D:\github\remote_dingding

# 初始化Git仓库
git init

# 添加所有文件
git add .

# 提交代码
git commit -m "Initial commit: RemoteCheckin v2.0"

# 连接远程仓库（替换成你的URL）
git remote add origin https://github.com/你的用户名/remote_dingding.git

# 推送代码
git branch -M main
git push -u origin main
```

**如果推送失败，需要认证**:

GitHub现在需要Personal Access Token:

1. **创建Token**
   ```
   1. GitHub → 右上角头像 → Settings
   2. 左侧菜单最下方 → Developer settings
   3. Personal access tokens → Tokens (classic)
   4. Generate new token → Generate new token (classic)
   5. Note: RemoteCheckin Build
   6. Expiration: 选择过期时间（如90 days）
   7. 勾选权限: repo (全部勾选)
   8. 点击 "Generate token"
   9. 复制token（只显示一次！）
   ```

2. **使用Token推送**
   ```bash
   git push -u origin main
   # Username: 你的GitHub用户名
   # Password: 粘贴Personal Access Token（不是密码）
   ```

### 步骤4: 触发自动构建

代码推送后，GitHub Actions会自动开始构建！

1. **查看构建状态**
   ```
   1. 访问你的仓库: https://github.com/你的用户名/remote_dingding
   2. 点击顶部的 "Actions" 标签
   3. 看到 "Build APK" 工作流正在运行
   ```

2. **等待构建完成**
   ```
   时间: 约5-10分钟
   状态: 黄色圆点（运行中）→ 绿色勾（成功）
   ```

3. **查看构建日志**
   ```
   1. 点击工作流运行记录
   2. 点击 "构建Debug APK" 任务
   3. 查看各个步骤的日志
   ```

### 步骤5: 下载APK

构建成功后，下载APK：

1. **进入Artifacts**
   ```
   1. 在Actions页面
   2. 点击成功的构建记录
   3. 滚动到底部
   4. 找到 "Artifacts" 部分
   ```

2. **下载APK**
   ```
   1. 点击 "app-debug-apk"
   2. 下载 ZIP 文件
   3. 解压 ZIP 文件
   4. 得到 app-debug.apk
   ```

## 🔄 手动触发构建

除了推送代码自动触发，也可以手动构建：

1. **进入Actions**
   ```
   仓库 → Actions 标签
   ```

2. **选择工作流**
   ```
   左侧: "Build APK"
   ```

3. **手动运行**
   ```
   右侧: "Run workflow" 按钮
   选择分支: main
   点击: "Run workflow" 绿色按钮
   ```

## 📱 安装APK到手机

下载APK后，安装到手机：

### 方法1: 使用ADB
```bash
# 连接手机（USB调试）
adb devices

# 安装APK
adb install app-debug.apk
```

### 方法2: 直接传输
```
1. 通过QQ/微信发送APK到手机
2. 在手机上点击文件
3. 允许安装未知来源应用
4. 点击安装
```

### 方法3: 云盘
```
1. 上传APK到百度网盘/Google Drive
2. 在手机上下载
3. 安装
```

## 🛠️ 工作流说明

### 构建任务
```
任务1: 构建Debug APK
- 自动触发: 推送代码、Pull Request
- 构建类型: Debug（带调试信息）
- 产物: app-debug.apk

任务2: 构建Release APK
- 触发方式: 仅手动触发
- 构建类型: Release（优化混淆）
- 产物: app-release-unsigned.apk
```

### 保存期限
```
APK文件保存: 30天
30天后自动删除，需重新构建
```

## 📊 构建时间

```
首次构建: 8-10分钟（下载依赖）
后续构建: 5-7分钟（使用缓存）
```

## ⚠️ 常见问题

### Q1: Actions没有自动运行？
**A**:
```
1. 检查工作流文件是否正确提交
2. 查看Actions标签是否有错误
3. 确认推送到了正确的分支（main）
```

### Q2: 构建失败？
**A**:
```
1. 点击红色的构建记录
2. 查看失败的步骤和错误日志
3. 常见原因:
   - 代码编译错误
   - 依赖下载失败
   - 配置文件错误
4. 修复后重新推送代码
```

### Q3: 推送时要求密码？
**A**:
```
GitHub不再支持密码认证，需要Personal Access Token:
1. 按上面"创建Token"步骤操作
2. 使用Token代替密码
```

### Q4: 下载APK时提示过期？
**A**:
```
APK保存30天后自动删除
解决方法:
1. 重新运行工作流
2. 下载新的APK
```

### Q5: 如何修改构建配置？
**A**:
```
编辑文件: .github/workflows/build-apk.yml
修改后提交推送，自动生效
```

## 🎓 进阶技巧

### 1. 添加自动发布到Release

修改工作流文件，添加:

```yaml
- name: 创建Release
  uses: softprops/action-gh-release@v1
  with:
    files: app/build/outputs/apk/debug/app-debug.apk
    draft: false
    prerelease: true
  env:
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

### 2. 添加APK签名

创建keystore并配置到Secrets:

```yaml
env:
  KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
```

### 3. 多架构构建

```yaml
- name: 构建所有架构
  run: ./gradlew assembleDebug
```

## 📚 相关链接

- **GitHub Actions文档**: https://docs.github.com/en/actions
- **个人访问令牌**: https://github.com/settings/tokens
- **项目仓库**: 创建后查看

## 🎉 总结

使用GitHub Actions自动构建的优势：

✅ **无需本地环境** - 不需要安装Android SDK
✅ **完全自动** - 推送代码自动构建
✅ **云端构建** - 不占用本地资源
✅ **历史记录** - 所有构建都有记录
✅ **免费使用** - 公开仓库免费
✅ **可重复** - 随时重新构建

**总时间**: 
- 首次设置: 15分钟
- 后续构建: 5-7分钟（自动）

**准备好开始了吗？双击运行 `setup_github.bat`！** 🚀
