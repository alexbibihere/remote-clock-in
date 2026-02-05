@echo off
chcp 65001 >nul
cls
echo.
echo ╔════════════════════════════════════════════════════════╗
echo ║                                                        ║
echo ║          推送代码到 GitHub                             ║
echo ║                                                        ║
echo ╚════════════════════════════════════════════════════════╝
echo.
echo  GitHub用户: alexbibihere
echo  仓库名称: remote_dingding
echo  仓库URL: https://github.com/alexbibihere/remote_dingding
echo.
echo ═════════════════════════════════════════════════════════
echo.
echo  重要提示：
echo.
echo  如果推送失败，可能需要创建 Personal Access Token
echo.
echo  创建步骤：
echo  1. 访问: https://github.com/settings/tokens
echo  2. 点击: Generate new token (classic)
echo  3. Note: RemoteCheckin
echo  4. Expiration: 90 days
echo  5. 勾选: repo 权限（全部勾选）
echo  6. 点击: Generate token
echo  7. 复制 token（只显示一次！）
echo.
echo ═════════════════════════════════════════════════════════
echo.

cd /d "%~dp0"

echo  准备推送...
echo.

git push -u origin main

if %errorlevel% equ 0 (
    cls
    echo.
    echo ╔════════════════════════════════════════════════════════╗
    echo ║                                                        ║
    echo ║              🎉 推送成功！                             ║
    echo ║                                                        ║
    echo ╚════════════════════════════════════════════════════════╝
    echo.
    echo  ✅ 代码已推送到GitHub
    echo  ✅ GitHub Actions 将自动开始构建
    echo.
    echo  仓库地址: https://github.com/alexbibihere/remote_dingding
    echo.
    echo  下一步操作:
    echo.
    echo  1. 点击下面的链接查看 Actions 构建:
    echo     https://github.com/alexbibihere/remote_dingding/actions
    echo.
    echo  2. 等待 5-10 分钟，直到看到绿色勾 ✅
    echo.
    echo  3. 点击构建记录，滚动到底部
    echo.
    echo  4. 在 "Artifacts" 部分下载 APK
    echo.
    echo  5. 解压 ZIP 文件得到 app-debug.apk
    echo.
    echo  6. 安装到手机测试！
    echo.
    echo ═════════════════════════════════════════════════════════
    echo.
    echo  按任意键打开 GitHub Actions...
    pause >nul

    start https://github.com/alexbibihere/remote_dingding
    start https://github.com/alexbibihere/remote_dingding/actions

    echo.
    echo  ✅ 已打开 GitHub 页面
    echo.

) else (
    cls
    echo.
    echo ╔════════════════════════════════════════════════════════╗
    echo ║                                                        ║
    echo ║              ❌ 推送失败                               ║
    echo ║                                                        ║
    echo ╚════════════════════════════════════════════════════════╝
    echo.
    echo  可能的原因:
    echo.
    echo  1. 需要身份验证 - GitHub 不再支持密码登录
    echo  2. 仓库不存在 - 需要先在 GitHub 创建仓库
    echo  3. 网络问题 - 检查网络连接
    echo.
    echo ═════════════════════════════════════════════════════════
    echo.
    echo  解决方法:
    echo.
    echo  【方法1】创建 GitHub 仓库（推荐）
    echo.
    echo  1. 访问: https://github.com/new
    echo.
    echo  2. 填写仓库信息:
    echo     - Repository name: remote_dingding
    echo     - Description: Android远程打卡应用
    echo     - 选择 Public 或 Private
    echo     - ⚠️ 不要勾选任何其他选项
    echo.
    echo  3. 点击 "Create repository"
    echo.
    echo  4. 重新运行此脚本
    echo.
    echo ═════════════════════════════════════════════════════════
    echo.
    echo  【方法2】使用 Personal Access Token
    echo.
    echo  如果仓库已存在但需要认证:
    echo.
    echo  1. 创建 Token:
    echo     https://github.com/settings/tokens
    echo.
    echo  2. 在新的 CMD 窗口执行:
    echo     git push -u origin main
    echo.
    echo  3. 输入:
    echo     Username: alexbibihere
    echo     Password: [粘贴 Token，不是密码]
    echo.
    echo ═════════════════════════════════════════════════════════
    echo.

    set /p choice="按 1 创建仓库，按 2 使用Token，按 0 退出: "

    if "%choice%"=="1" (
        echo.
        echo 正在打开 GitHub 创建仓库页面...
        start https://github.com/new
        echo.
        echo 创建完成后，请重新运行此脚本。
    )

    if "%choice%"=="2" (
        echo.
        echo 正在打开 Token 创建页面...
        start https://github.com/settings/tokens
        echo.
        echo Token 创建后，在 CMD 中执行:
        echo   git push -u origin main
        echo.
    )
)

echo.
pause
