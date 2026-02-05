@echo off
chcp 65001 >nul
echo.
echo ╔════════════════════════════════════════╗
echo ║  GitHub Actions 自动构建设置工具       ║
echo ╚════════════════════════════════════════╝
echo.

REM 检查git是否安装
where git >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Git未安装
    echo.
    echo 请先安装Git:
    echo 下载地址: https://git-scm.com/download/win
    pause
    exit /b 1
)
echo ✅ Git已安装

echo.
echo ═══════════════════════════════════════
echo  步骤 1: 创建GitHub仓库
echo ═══════════════════════════════════════
echo.
echo 请按以下步骤操作:
echo.
echo 1. 访问 https://github.com
echo 2. 登录或注册账号
echo 3. 点击右上角 "+" → "New repository"
echo 4. 填写仓库信息:
echo    - Repository name: remote_dingding
echo    - Description: Android远程打卡应用
echo    - 选择 Public 或 Private
echo 5. 点击 "Create repository"
echo 6. 不要勾选任何初始化选项
echo.
set /p repo_url="创建完成后，请输入仓库URL (https://github.com/用户名/remote_dingding.git): "
echo.

echo ═══════════════════════════════════════
echo  步骤 2: 初始化Git仓库
echo ═══════════════════════════════════════
echo.

cd /d "%~dp0"

if exist .git (
    echo ⚠️  已经是Git仓库
    choice /C YN /M "是否重新初始化"
    if errorlevel 2 goto :commit
    rd /s /q .git
)

echo 初始化Git仓库...
git init
echo ✅ Git仓库已初始化

:commit
echo.
echo ═══════════════════════════════════════
echo  步骤 3: 提交代码
echo ═══════════════════════════════════════
echo.

echo 添加所有文件...
git add .
echo ✅ 文件已添加

echo.
echo 提交更改...
git commit -m "Initial commit: RemoteCheckin v2.0

- 整合幻影分身所有核心技术
- Hook框架支持 (LocationHook + XposedHook)
- 设备信息伪装 (DeviceInfoSpoof)
- MMKV高性能存储
- 应用白名单系统
- 高级位置模拟服务
- 完整的项目文档"
echo ✅ 代码已提交

echo.
echo ═══════════════════════════════════════
echo  步骤 4: 连接远程仓库
echo ═══════════════════════════════════════
echo.

git remote add origin %repo_url% 2>nul
git remote set-url origin %repo_url%
echo ✅ 远程仓库已连接
echo.

echo ═══════════════════════════════════════
echo  步骤 5: 推送代码到GitHub
echo ═══════════════════════════════════════
echo.

echo 推送代码...
git branch -M main
git push -u origin main

if %errorlevel% equ 0 (
    echo.
    echo ╔══════════════════════════════════════╗
    echo ║        ✅ 代码推送成功！              ║
    echo ╚══════════════════════════════════════╝
    echo.
    echo 下一步操作:
    echo 1. 访问您的GitHub仓库
    echo 2. 点击 "Actions" 标签
    echo 3. 查看构建状态
    echo 4. 等待构建完成（约5-10分钟）
    echo 5. 在 "Actions" → "构建任务" → "Artifacts" 下载APK
    echo.
    echo 仓库地址: %repo_url%
    echo Actions地址: %repo_url%/actions
) else (
    echo.
    echo ❌ 推送失败
    echo.
    echo 可能的原因:
    echo 1. 认证失败 - 需要使用Personal Access Token
    echo 2. 网络问题 - 请检查网络连接
    echo 3. URL错误 - 请确认仓库URL正确
    echo.
    echo 解决方法:
    echo 1. 创建Personal Access Token:
    echo    GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
    echo    Generate new token → 勾选 repo → Generate
    echo.
    echo 2. 使用Token代替密码:
    echo    git push -u origin main
    echo    用户名: 输入GitHub用户名
    echo    密码: 输入Personal Access Token (不是GitHub密码)
)

echo.
pause
