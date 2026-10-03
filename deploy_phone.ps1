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
    # Push to device storage for direct update access
    & $adbPath -s $device push $versionedTarget "/sdcard/Download/TelkomSecure-v$appVersion-Release.apk"
    
    # Attempt direct install or notify
    & $adbPath -s $device install -r -d $versionedTarget
    
    # Restart app to guarantee clean state
    & $adbPath -s $device shell am force-stop com.telkomsel.secure.telkomsel_secure
    & $adbPath -s $device shell am start -n com.telkomsel.secure.telkomsel_secure/.MainActivity
    Write-Host "=== Deployment to $device Complete! ===" -ForegroundColor Green
} else {
    Write-Host "No connected Android device found for auto-install." -ForegroundColor Yellow
}
