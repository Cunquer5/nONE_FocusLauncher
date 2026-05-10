# Script to debug launcher setup and capture logs
# Usage: .\debug-launcher.ps1

Write-Host "=== nONE Launcher Debug Script ===" -ForegroundColor Cyan

# Check if adb is available
try {
    $adbVersion = adb version 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "ADB is available" -ForegroundColor Green
    } else {
        throw "ADB not found"
    }
} catch {
    Write-Host "ERROR: ADB not found. Please ensure Android SDK platform-tools are in your PATH" -ForegroundColor Red
    Write-Host "You can add them by adding this to your PATH environment variable:" -ForegroundColor Yellow
    Write-Host "%LOCALAPPDATA%\Android\Sdk\platform-tools" -ForegroundColor Yellow
    exit 1
}

# Clear logcat
Write-Host "Clearing logcat..." -ForegroundColor Green
adb logcat -c

# Install the APK
Write-Host "Installing APK..." -ForegroundColor Green
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    adb install -r -d $apkPath
    if ($LASTEXITCODE -eq 0) {
        Write-Host "APK installed successfully" -ForegroundColor Green
    } else {
        Write-Host "Failed to install APK" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "APK not found at $apkPath. Building first..." -ForegroundColor Yellow
    .\gradlew assembleDebug
    if ($LASTEXITCODE -eq 0) {
        adb install -r -d $apkPath
        if ($LASTEXITCODE -eq 0) {
            Write-Host "APK installed successfully" -ForegroundColor Green
        } else {
            Write-Host "Failed to install APK" -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "Failed to build APK" -ForegroundColor Red
        exit 1
    }
}

# Check if launcher is default
Write-Host "Checking if nONE Launcher is set as default..." -ForegroundColor Green
$defaultLauncher = adb shell cmd activity resolve-activity -a android.intent.action.MAIN -c android.intent.category.HOME | Select-String "com.example.nonelauncher"

if ($defaultLauncher) {
    Write-Host "nONE Launcher is set as default launcher!" -ForegroundColor Green
} else {
    Write-Host "nONE Launcher is NOT set as default launcher" -ForegroundColor Yellow
    Write-Host "Opening launcher settings..." -ForegroundColor Yellow
    adb shell am start -a android.settings.HOME_SETTINGS
    
    Write-Host "Please select 'nONE Launcher' as your default home app" -ForegroundColor Cyan
    Write-Host "Press any key to continue after setting as default..." -ForegroundColor Cyan
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
}

# Start logcat capture
Write-Host "Starting logcat capture (press Ctrl+C to stop)..." -ForegroundColor Green
Write-Host "Launching nONE Launcher app..." -ForegroundColor Green
adb shell am start -n com.example.nonelauncher/.MainActivity

# Capture logs in background
$logcatJob = Start-Job -ScriptBlock {
    adb logcat | Select-String -Pattern "nonelauncher", "example.nonelauncher", "FATAL", "AndroidRuntime", "System.err"
} -Name LogcatCapture

Write-Host "Capturing logs... Press Ctrl+C to stop" -ForegroundColor Cyan
try {
    while ($true) {
        Receive-Job -Name LogcatCapture | Write-Host
        Start-Sleep -Milliseconds 500
    }
} finally {
    Stop-Job -Name LogcatCapture -ErrorAction SilentlyContinue
    Remove-Job -Name LogcatCapture -ErrorAction SilentlyContinue
    Write-Host "Logcat capture stopped" -ForegroundColor Yellow
}
