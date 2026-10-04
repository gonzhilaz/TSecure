$ErrorActionPreference = "Stop"

# Auto-detect version from pubspec.yaml
$pubspecPath = "d:\DEVELOPMENT\Projects\Riski\TelkomSecure\main\pubspec.yaml"
$pubspecContent = Get-Content $pubspecPath -Raw
if ($pubspecContent -match 'version:\s*([^\r\n]+)') {
    $appVersion = $matches[1].Trim()
} else {
    $appVersion = "1.0.1+2"
}

Write-Host "=== Step 1: Building Release APK v$appVersion ===" -ForegroundColor Cyan
Set-Location "d:\DEVELOPMENT\Projects\Riski\TelkomSecure\main"
flutter build apk --release
if ($LASTEXITCODE -ne 0) {
    Write-Error "Flutter build failed with exit code $LASTEXITCODE"
}

Write-Host "=== Step 2: Locating Built APK ===" -ForegroundColor Cyan
$apkFile = Get-ChildItem -Path "d:\DEVELOPMENT\Projects\Riski\TelkomSecure\main\build\app\outputs\flutter-apk\app-release.apk" -ErrorAction SilentlyContinue

if (-not $apkFile) {
    $apkFile = Get-ChildItem -Path "d:\DEVELOPMENT\Projects\Riski\TelkomSecure\main\build" -Recurse -Filter "app-release.apk" | Sort-Object LastWriteTime -Descending | Select-Object -First 1
}

if (-not $apkFile) {
    Write-Error "app-release.apk was not found in build directory!"
}

Write-Host "Found APK at: $($apkFile.FullName) (Size: $($apkFile.Length) bytes, Modified: $($apkFile.LastWriteTime))" -ForegroundColor Green

Write-Host "=== Step 3: Copying to /releases/ with Versioning ===" -ForegroundColor Cyan
$releaseDir = "d:\DEVELOPMENT\Projects\Riski\TelkomSecure\releases"
if (-not (Test-Path $releaseDir)) {
    New-Item -ItemType Directory -Path $releaseDir | Out-Null
}

$cleanVersion = ($appVersion -split '\+')[0]
$versionedTarget = Join-Path $releaseDir "TelkomSecure-v$appVersion-Release.apk"
$cleanTarget = Join-Path $releaseDir "TelkomSecure-v$cleanVersion.apk"
$latestTarget = Join-Path $releaseDir "TelkomSecure-Release.apk"

Copy-Item -Path $apkFile.FullName -Destination $versionedTarget -Force
Copy-Item -Path $apkFile.FullName -Destination $cleanTarget -Force
Copy-Item -Path $apkFile.FullName -Destination $latestTarget -Force

Write-Host "Saved to: $versionedTarget" -ForegroundColor Green
Write-Host "Saved to: $cleanTarget" -ForegroundColor Green
Write-Host "Saved to: $latestTarget" -ForegroundColor Green

Write-Host "=== Step 4: Transferring & Installing to Android Device ===" -ForegroundColor Cyan
$adbPath = "C:\Users\gemil\AppData\Local\Android\Sdk\platform-tools\adb.exe"
if (-not (Test-Path $adbPath)) {
    $adbPath = (Get-Command adb -ErrorAction SilentlyContinue).Source
}

$device = (& $adbPath devices | Select-String "device$" | ForEach-Object { ($_ -split '\s+')[0] } | Select-Object -First 1)

if ($device) {
    Write-Host "Target Device: $device" -ForegroundColor Yellow
    # Clean up old APK installers from phone download folder to prevent false detections
    & $adbPath -s $device shell "rm -f /sdcard/Download/TelkomSecure*.apk /sdcard/Download/.trashed*TelkomSecure*.apk"

    # Direct stream install
    Write-Host "Installing APK to device..." -ForegroundColor Cyan
    & $adbPath -s $device install -r -d $versionedTarget

    # Auto-grant runtime permissions & enable Web Filter Accessibility
    Write-Host "Configuring security permissions and Accessibility Service..." -ForegroundColor Cyan
    & $adbPath -s $device shell "appops set com.telkomsel.secure.telkomsel_secure MANAGE_EXTERNAL_STORAGE allow"
    & $adbPath -s $device shell "pm grant com.telkomsel.secure.telkomsel_secure android.permission.POST_NOTIFICATIONS"
    & $adbPath -s $device shell "settings put secure enabled_accessibility_services com.telkomsel.secure.telkomsel_secure/com.telkomsel.secure.accessibility.TelkomWebFilterAccessibilityService"
    & $adbPath -s $device shell "settings put secure accessibility_enabled 1"

    # Restart app to guarantee fresh runtime state
    & $adbPath -s $device shell am force-stop com.telkomsel.secure.telkomsel_secure
    & $adbPath -s $device shell am start -n com.telkomsel.secure.telkomsel_secure/.MainActivity
    Write-Host "=== Deployment to $device Complete! Web Filter & Storage permissions ACTIVE ===" -ForegroundColor Green
} else {
    Write-Host "No connected Android device found for auto-install." -ForegroundColor Yellow
}
