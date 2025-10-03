@echo off
echo Testing Gemini API Configuration...
echo.

echo 1. Checking environment variables...
if defined GEMINI_API_KEY (
    echo GEMINI_API_KEY is set: %GEMINI_API_KEY:~0,10%...
) else (
    echo GEMINI_API_KEY is not set
)

echo.
echo 2. Testing API endpoint with PowerShell...
powershell -Command "try { $response = Invoke-RestMethod -Uri 'https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=test' -Method POST -ContentType 'application/json' -Body '{\"contents\":[{\"parts\":[{\"text\":\"Hello\"}]}]}' -ErrorAction Stop; Write-Host 'API call successful' } catch { Write-Host 'API call failed:' $_.Exception.Message }"

echo.
echo 3. Starting application with enhanced error handling...
echo The application will now use enhanced mock responses if the API fails.
echo.

java -jar target/agriculture-backend-1.0.0.jar
