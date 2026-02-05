@echo off
chcp 65001 >nul
echo.
echo ╔════════════════════════════════════════╗
echo ║   快速推送到GitHub                    ║
echo ╚════════════════════════════════════════╝
echo.
echo 当前状态:
echo ✅ 代码已提交到本地Git仓库
echo ❌ 还未推送到GitHub远程仓库
echo.
echo ═══════════════════════════════════════
echo.
echo 请提供您的GitHub仓库信息:
echo.
set /p github_user="请输入GitHub用户名: "
set /p repo_name="仓库名称 (直接回车使用 remote_dingding): "

if "%repo_name%"=="" set repo_name=remote_dingding

set repo_url=https://github.com/%github_user%/%repo_name%.git

echo.
echo 仓库URL: %repo_url%
echo.
set /p confirm="确认正确吗？(Y/N): "

if /i not "%confirm%"=="Y" (
    echo 已取消
    pause
    exit /b 1
)

cd /d "%~dp0"

echo.
echo [1/3] 连接远程仓库...
git remote add origin %repo_url% 2>nul
if %errorlevel% neq 0 (
    git remote set-url origin %repo_url%
)
echo ✅ 远程仓库已连接

echo.
echo [2/3] 切换到main分支...
git branch -M main
echo ✅ 分支已切换

echo.
echo [3/3] 推送代码到GitHub...
echo ⏳ 正在推送，请稍候...
echo.

git push -u origin main

if %errorlevel% equ 0 (
    cls
    echo.
    echo ╔════════════════════════════════════════╗
    echo ║                                        ║
    echo ║        🎉 推送成功！                    ║
    echo ║                                        ║
    echo ╚════════════════════════════════════════╝
    echo.
    echo 仓库地址: %repo_url%
    echo.
    echo 下一步操作:
    echo.
    echo  1️⃣  点击下面的链接查看Actions构建:
    echo     %repo_url%/actions
    echo.
    echo  2️⃣  等待5-10分钟，直到看到绿色的勾 ✅
    echo.
    echo  3️⃣  点击构建记录，滚动到底部
    echo.
    echo  4️⃣  在 "Artifacts" 部分下载APK
    echo.
    echo  5️⃣  解压ZIP文件得到 app-debug.apk
    echo.
    echo ═══════════════════════════════════════
    echo.
    echo 按任意键打开仓库...
    pause >nul

    start %repo_url%
    start %repo_url%/actions

) else (
    echo.
    echo ❌ 推送失败
    echo.
    echo 可能需要身份验证:
    echo.
    echo 1. 创建Personal Access Token:
    echo    https://github.com/settings/tokens
    echo.
    echo 2. 重新执行:
    echo    git push -u origin main
    echo.
    echo 3. 输入:
    echo    Username: %github_user%
    echo    Password: [粘贴Token]
    echo.
)

pause
