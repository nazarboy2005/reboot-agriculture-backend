@echo off
echo ========================================
echo DEPLOYING API KEY FIX
echo ========================================

echo.
echo [1/2] Building application with new API key...
call mvnw.cmd clean package -DskipTests

if %ERRORLEVEL% neq 0 (
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo [2/2] Deploying to Railway...
railway deploy --detach

echo.
echo ========================================
echo DEPLOYMENT COMPLETE!
echo ========================================
echo.
echo The new API key and improved chat functionality should now be live!
echo Test the chat: https://agriculture-backend-1077945709935.europe-west1.run.app/api/v1/chat/send
echo.
pause
