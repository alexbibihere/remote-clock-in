@echo off
chcp 65001 >nul
cls
echo.
echo ╔════════════════════════════════════════╗
echo ║   添加并推送所有文件到GitHub          ║
echo ╚════════════════════════════════════════╝
echo.

cd /d "%~dp0"

echo [1/6] 检查当前状态...
echo.
git status --short
echo.

echo [2/6] 添加所有文件...
echo.
git add .
echo ✅ 文件已添加

echo.
echo [3/6] 提交更改...
echo.
git commit -m "Update: 添加GitHub Actions配置和文档

- 添加GitHub Actions自动构建工作流
- 添加Git忽略配置
- 添加推送脚本
- 添加完整的使用指南文档
- 更新构建状态说明"

if %errorlevel% neq 0 (
    echo.
    echo ⚠️  没有新的更改需要提交
    echo 或提交失败，请检查上方错误信息
)

echo.
echo ═══════════════════════════════════════
echo.
echo  [4/6] 连接GitHub远程仓库
echo ═══════════════════════════════════════
echo.
echo  请提供您的GitHub仓库信息:
echo.
set /p github_user="GitHub用户名: "
set /p repo_name="仓库名 (直接回车=remote_dingding): "

if "%repo_name%"=="" set repo_name=remote_dingding

set repo_url=https://github.com/%github_user%/%repo_name%.git

echo.
echo 仓库URL: %repo_url%
echo.

set /p confirm="确认正确？(Y/N): "
if /i not "%confirm%"=="Y" (
    echo 已取消
    pause
    exit /b 1
)

echo.
echo 正在连接远程仓库...
git remote add origin %repo_url% 2>nul
git remote set-url origin %repo_url% 2>nul
echo ✅ 远程仓库已连接

echo.
echo ═══════════════════════════════════════
echo.
echo  [5/6] 切换到main分支
echo ═══════════════════════════════════════
echo.

git branch -M main
echo ✅ 已切换到main分支

echo.
echo ═══════════════════════════════════════
echo.
echo  [6/6] 推送到GitHub
echo ═══════════════════════════════════════
echo.
echo 正在推送，请稍候...
echo.

git push -u origin main

if %errorlevel% equ 0 (
    cls
    echo.
    echo ╔════════════════════════════════════════╗
    echo ║                                        ║
    echo ║      🎉 推送成功！                      ║
    echo ║                                        ║
    echo ╚════════════════════════════════════════╝
    echo.
    echo 仓库地址: %repo_url%
    echo.
    echo  ✅ 代码已推送到GitHub
    echo  ✅ GitHub Actions将自动开始构建
    echo.
    echo  下一步操作:
    echo.
    echo  1. 点击Actions标签查看构建
    echo     %repo_url%/actions
    echo.
    echo  2. 等待5-10分钟直到构建完成
    echo.
    echo  3. 下载生成的APK
    echo.
    echo ═══════════════════════════════════════
    echo.
    echo 按任意键打开GitHub Actions...
    pause >nul

    start %repo_url%
    start %repo_url%/actions

) else (
    cls
    echo.
    echo ╔════════════════════════════════════════╗
    echo ║                                        ║
    echo ║      ❌ 推送失败                        ║
    echo ║                                        ║
    echo ╚════════════════════════════════════════╝
    echo.
    echo  可能需要身份验证...
    echo.
    echo  解决方法:
    echo.
    echo  1. 创建Personal Access Token:
    echo     访问: https://github.com/settings/tokens
    echo     点击: Generate new token (classic)
    echo     勾选: repo 权限
    echo     生成并复制token
    echo.
    echo  2. 重新推送:
    echo     git push -u origin main
    echo.
    echo     用户名: %github_user%
    echo     密码: [粘贴Token，不是GitHub密码]
    echo.
)

echo.
pause
