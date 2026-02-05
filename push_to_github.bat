@echo off
chcp 65001 >nul
cls
echo.
echo ╔══════════════════════════════════════════════════════╗
echo ║                                                    ║
echo ║        ✅ 前面步骤已完成！                           ║
echo ║                                                    ║
echo ║   ✓ Git环境检查                                    ║
echo ║   ✓ Git仓库初始化                                  ║
echo ║   ✓ 代码已提交 (1580个文件, 36199行代码)            ║
echo ║                                                    ║
echo ╚══════════════════════════════════════════════════════╝
echo.
echo  接下来需要您完成以下步骤:
echo.
echo ═══════════════════════════════════════════════════════
echo.
echo  📋 步骤4: 创建GitHub仓库
echo ═══════════════════════════════════════════════════════
echo.
echo  请按以下步骤操作:
echo.
echo  1. 访问 https://github.com
echo  2. 登录或注册账号（免费）
echo  3. 点击右上角 "+" → "New repository"
echo.
pause
echo.
echo  4. 填写仓库信息:
echo.
echo     ┌─────────────────────────────────────────┐
echo     │ Repository name: remote_dingding         │
echo     │ Description: Android远程打卡应用          │
echo     │                                         │
echo     │ 选择可见性:                              │
echo     │ ⚪ Public  (公开，任何人可见)            │
echo     │ ⚪ Private (私有，仅自己可见)           │
echo     │                                         │
echo     │ ⚠️  不要勾选任何选项！                   │
echo     │ ☐ Add a README file                     │
echo     │ ☐ Add .gitignore                        │
echo     │ ☐ Choose a license                      │
echo     └─────────────────────────────────────────┘
echo.
pause
echo.
echo  5. 点击绿色按钮 "Create repository"
echo.
echo  6. 创建后会显示一个页面，快速滚动到顶部
echo.
pause
echo.
echo ═══════════════════════════════════════════════════════
echo.
echo  📋 步骤5: 获取仓库URL
echo ═══════════════════════════════════════════════════════
echo.
echo  在新创建的仓库页面，您会看到:
echo.
echo     ┌─────────────────────────────────────────┐
echo     │ …or push an existing repository from    │
echo     │ the command line                         │
echo     │                                         │
echo     │ git remote add origin https://github.com│
echo     │ 你的用户名/remote_dingding.git          │
echo     │                                         │
echo     │ git branch -M main                      │
echo     │ git push -u origin main                 │
echo     └─────────────────────────────────────────┘
echo.
echo  复制那个URL（类似下面这样）:
echo.
echo     https://github.com/你的用户名/remote_dingding.git
echo.
pause
echo.
echo ═══════════════════════════════════════════════════════
echo.
echo  📋 步骤6: 连接并推送代码
echo ═══════════════════════════════════════════════════════
echo.

set /p repo_url="请输入您的仓库URL: "

echo.
echo 您输入的URL: %repo_url%
echo.

set /p confirm="确认正确吗？(Y/N): "
if /i not "%confirm%"=="Y" (
    echo.
    echo 请重新运行此脚本
    pause
    exit /b 1
)

cd /d "%~dp0"

echo.
echo 正在连接远程仓库...
git remote add origin %repo_url% 2>nul || git remote set-url origin %repo_url%
echo ✅ 远程仓库已连接

echo.
echo 正在推送代码到GitHub...
echo ⏳ 请稍候...
echo.

git branch -M main
git push -u origin main

if %errorlevel% equ 0 (
    cls
    echo.
    echo ╔══════════════════════════════════════════════════════╗
    echo ║                                                    ║
    echo ║            🎉 代码推送成功！                        ║
    echo ║                                                    ║
    echo ╚══════════════════════════════════════════════════════╝
    echo.
    echo  下一步操作:
    echo.
    echo  1. 访问您的GitHub仓库
    echo     %repo_url%
    echo.
    echo  2. 点击顶部的 "Actions" 标签
    echo.
    echo  3. 看到正在运行的构建任务（黄色圆点）
    echo.
    echo  4. 等待5-10分钟，直到变成绿色勾 ✅
    echo.
    echo  5. 点击构建记录
    echo.
    echo  6. 滚动到底部 "Artifacts" 部分
    echo.
    echo  7. 下载 "app-debug-apk"
    echo.
    echo  8. 解压ZIP得到 app-debug.apk
    echo.
    echo  9. 安装到手机测试！
    echo.
    echo ═══════════════════════════════════════════════════════
    echo.
    echo  如需查看完整指南，请打开:
    echo  GITHUB_ACTIONS_GUIDE.md
    echo.
) else (
    cls
    echo.
    echo ╔══════════════════════════════════════════════════════╗
    echo ║                                                    ║
    echo ║            ❌ 推送失败                              ║
    echo ║                                                    ║
    echo ╚══════════════════════════════════════════════════════╝
    echo.
    echo  可能的原因:
    echo.
    echo  1. 需要GitHub身份验证
    echo     → GitHub不再支持密码登录
    echo     → 需要使用Personal Access Token
    echo.
    echo  解决方法:
    echo.
    echo  1. 创建Personal Access Token:
    echo.
    echo     a. 访问: https://github.com/settings/tokens
    echo.
    echo     b. 点击 "Generate new token" → "Generate new token (classic)"
    echo.
    echo     c. Note: 输入 RemoteCheckin
    echo.
    echo     d. Expiration: 选择过期时间（如90 days）
    echo.
    echo     e. 勾选权限: ☑️ repo (全部勾选)
    echo.
    echo     f. 点击 "Generate token"
    echo.
    echo     g. 复制token（只显示一次！）
    echo.
    echo  2. 重新推送代码:
    echo.
    echo     a. 在CMD中执行:
    echo        git push -u origin main
    echo.
    echo     b. Username: 输入你的GitHub用户名
    echo.
    echo     c. Password: 粘贴Personal Access Token
    echo.
    echo     d. 等待推送完成
    echo.
    echo  详细说明请查看: GITHUB_ACTIONS_GUIDE.md
    echo.
)

pause
