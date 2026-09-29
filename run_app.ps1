<#
.SYNOPSIS
    Automated launcher for Android Emulator and TelkomSecure Mobile APK.
.DESCRIPTION
    Checks Android SDK, boots the Pixel 6 AVD if not running, installs APK, and launches the app.
.PARAMETER Build
    Force a rebuild of the Flutter APK before launching.
.PARAMETER Shielded
    Install and run the BlackWall-shielded APK instead of standard debug APK.
.PARAMETER Avd
    Specific AVD name to launch (defaults to Pixel_6_API_34).
#>
param(
    [switch]$Build,
    [switch]$Shielded,
    [string]$Avd = ""
)

$ErrorActionPreference = "Stop"

function Write-Step ([string]$msg) {
    Write-Host "[INFO] $msg" -ForegroundColor Cyan
}

function Write-Success ([string]$msg) {
    Write-Host "[OK]   $msg" -ForegroundColor Green
}

function Write-Warn ([string]$msg) {
    Write-Host "[WARN] $msg" -ForegroundColor Yellow
}

function Write-Err ([string]$msg) {
    Write-Host "[ERR]  $msg" -ForegroundColor Red
}

# 1. Resolve Android SDK Paths
$sdkPath = "C:\android-sdk"
if ($env:ANDROID_HOME -and (Test-Path $env:ANDROID_HOME)) {
    $sdkPath = $env:ANDROID_HOME
} elseif ($env:ANDROID_SDK_ROOT -and (Test-Path $env:ANDROID_SDK_ROOT)) {
    $sdkPath = $env:ANDROID_SDK_ROOT
}

$adb = Join-Path $sdkPath "platform-tools\adb.exe"
$emulator = Join-Path $sdkPath "emulator\emulator.exe"

if (-not (Test-Path $adb)) {
    Write-Err "adb.exe not found at: $adb"
    exit 1
}
if (-not (Test-Path $emulator)) {
    Write-Err "emulator.exe not found at: $emulator"
    exit 1
}

# 2. Resolve Target AVD
if ([string]::IsNullOrWhiteSpace($Avd)) {
    $avdList = & $emulator -list-avds
    if ($avdList -contains "Pixel_6_API_34") {
        $Avd = "Pixel_6_API_34"
    } elseif ($avdList.Count -gt 0) {
        $Avd = $avdList[0]
    } else {
        Write-Err "No Android Virtual Devices (AVD) found! Create one in Android Studio."
        exit 1
    }
}

# 3. Check / Start Emulator
Write-Step "Checking running devices via ADB..."
$devices = & $adb devices | Out-String
$isEmulatorRunning = $devices -match "emulator-\d+\s+device"

if (-not $isEmulatorRunning) {
    Write-Step "Starting emulator '$Avd' as an independent detached process..."
    $emuCmd = "`"$emulator`" -avd $Avd -gpu host"
    Invoke-CimMethod -ClassName Win32_Process -MethodName Create -Arguments @{CommandLine = $emuCmd} | Out-Null

    Write-Step "Waiting for emulator to connect to ADB..."
    & $adb wait-for-device

    Write-Step "Waiting for Android system boot completion..."
    $bootCompleted = $false
    $timeout = 120
    $elapsed = 0

    while (-not $bootCompleted -and $elapsed -lt $timeout) {
        Start-Sleep -Seconds 3
        $elapsed += 3
        try {
            $bootProp = (& $adb shell getprop sys.boot_completed 2>$null).Trim()
            if ($bootProp -eq "1") {
                $bootCompleted = $true
            }
        } catch {
            # Still booting
        }
        Write-Host -NoNewline "."
    }
    Write-Host ""

    if (-not $bootCompleted) {
        Write-Err "Emulator failed to boot within $timeout seconds."
        exit 1
    }
    Write-Success "Emulator is online and fully booted!"
} else {
    Write-Success "Active emulator instance detected."
}

# 4. Determine APK Path & Build if needed
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$mainDir = Join-Path $projectRoot "main"
$debugApk = Join-Path $mainDir "build\app\outputs\flutter-apk\app-debug.apk"
$shieldedApk = Join-Path $projectRoot "doc\releases\app-debug-shielded.apk"

$targetApk = $debugApk
if ($Shielded) {
    $targetApk = $shieldedApk
    Write-Step "Target: BlackWall-shielded APK ($targetApk)"
} else {
    Write-Step "Target: Flutter Debug APK ($targetApk)"
}

if ($Build -or (-not (Test-Path $targetApk))) {
    if ($Shielded) {
        Write-Step "Re-shielding APK via BlackWall..."
        python -m blackwall.cli shield $debugApk -o $shieldedApk
    } else {
        Write-Step "Building debug APK (flutter build apk --debug)..."
        Push-Location $mainDir
        try {
            flutter build apk --debug
        } finally {
            Pop-Location
        }
    }
}

if (-not (Test-Path $targetApk)) {
    Write-Err "Target APK not found at: $targetApk"
    exit 1
}

# 5. Install APK
Write-Step "Installing APK to emulator..."
$installOutput = & $adb install -r $targetApk 2>&1 | Out-String
if ($installOutput -notmatch "Success") {
    Write-Err "Installation failed: $installOutput"
    exit 1
}
Write-Success "APK installed successfully!"

# 6. Launch Application
$packageName = "com.telkomsel.secure.telkomsel_secure"
$activityName = ".MainActivity"
Write-Step "Launching $packageName/$activityName..."
& $adb shell am force-stop $packageName
& $adb shell am start -n "$packageName/$activityName"

Write-Success "TelkomSecure is running on the emulator!"
