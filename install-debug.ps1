# PowerShell script to install debug APK and capture logcat
# Usage: .\install-debug.ps1

Write-Host "Clearing existing logcat..." -ForegroundColor Green
adb logcat -c

Write-Host "Starting logcat capture to install-logcat.txt..." -ForegroundColor Green
Start-Job -ScriptBlock { adb logcat > install-logcat.txt } -Name LogcatCapture

Write-Host "Building debug APK..." -ForegroundColor Green
.\gradlew assembleDebug

if ($LASTEXITCODE -eq 0) {
    $apkPath = "app\build\outputs\apk\debug\app-debug.apk"
    Write-Host "Installing debug APK: $apkPath" -ForegroundColor Green
    adb install -r -d $apkPath
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "APK installed successfully!" -ForegroundColor Green
        Write-Host "Logcat is being captured to install-logcat.txt" -ForegroundColor Yellow
        Write-Host "Stop logcat capture with: Stop-Job -Name LogcatCapture; Remove-Job -Name LogcatCapture" -ForegroundColor Yellow
    } else {
        Write-Host "Failed to install APK" -ForegroundColor Red
        Stop-Job -Name LogcatCapture
        Remove-Job -Name LogcatCapture
    }
} else {
    Write-Host "Failed to build APK" -ForegroundColor Red
    Stop-Job -Name LogcatCapture
    Remove-Job -Name LogcatCapture
}
