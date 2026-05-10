# Script to temporarily add ADB to PATH and run launcher debug
# Usage: .\run-with-adb.ps1

Write-Host "=== Setting up ADB PATH ===" -ForegroundColor Cyan

# Add ADB to current session PATH
$adbPath = "$env:LOCALAPPDATA\Android\Sdk\platform-tools"
$env:PATH = "$adbPath;$env:PATH"

# Verify ADB is now available
try {
    $adbVersion = adb version 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "ADB is now available!" -ForegroundColor Green
        Write-Host $adbVersion -ForegroundColor Gray
    } else {
        throw "ADB still not working"
    }
} catch {
    Write-Host "ERROR: Could not initialize ADB" -ForegroundColor Red
    exit 1
}

Write-Host "`n=== nONE Launcher Debug ===" -ForegroundColor Cyan

# Clear logcat
Write-Host "Clearing logcat..." -ForegroundColor Green
adb logcat -c

# Check if APK exists and install
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $apkPath) {
    Write-Host "Installing APK..." -ForegroundColor Green
    adb install -r -d $apkPath
    if ($LASTEXITCODE -eq 0) {
        Write-Host "APK installed successfully" -ForegroundColor Green
    } else {
        Write-Host "Failed to install APK" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "APK not found. Building first..." -ForegroundColor Yellow
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
$defaultLauncherCheck = adb shell cmd activity resolve-activity -a android.intent.action.MAIN -c android.intent.category.HOME

if ($defaultLauncherCheck -match "com.example.nonelauncher") {
    Write-Host "nONE Launcher is set as default launcher!" -ForegroundColor Green
} else {
    Write-Host "nONE Launcher is NOT set as default launcher" -ForegroundColor Yellow
    Write-Host "Opening launcher settings..." -ForegroundColor Yellow
    adb shell am start -a android.settings.HOME_SETTINGS
    
    Write-Host "Please select 'nONE Launcher' as your default home app" -ForegroundColor Cyan
    Write-Host "Press any key to continue after setting as default..." -ForegroundColor Cyan
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
}

# Start the app and capture logs
Write-Host "Starting nONE Launcher..." -ForegroundColor Green
adb shell am start -n com.example.nonelauncher/.MainActivity

Write-Host "Capturing logs for 10 seconds..." -ForegroundColor Cyan
$logcatOutput = adb logcat -d -t 10 | Select-String -Pattern "nONE Launcher", "example.nonelauncher", "FATAL", "AndroidRuntime", "System.err"

if ($logcatOutput) {
    Write-Host "`n=== Logcat Output ===" -ForegroundColor Yellow
    $logcatOutput | ForEach-Object { Write-Host $_ -ForegroundColor White }
} else {
    Write-Host "No relevant logs found" -ForegroundColor Yellow
}

Write-Host "`n=== Debug Complete ===" -ForegroundColor Cyan
Write-Host "If the app still doesn't work, check:" -ForegroundColor Yellow
Write-Host "1. Is nONE Launcher set as default home app?" -ForegroundColor White
Write-Host "2. Are there any crash logs above?" -ForegroundColor White
Write-Host "3. Try manually opening from app drawer" -ForegroundColor White
