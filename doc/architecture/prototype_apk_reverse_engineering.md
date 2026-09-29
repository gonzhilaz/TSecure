# Prototype APK Reverse Engineering Report

## Target Overview
- **File**: `app-armeabi-v7a-production-release.apk`
- **Application Framework**: Flutter (Dart AOT with `libapp.so` & `libflutter.so`)
- **Package Name**: `com.taspenguard` / `com.huyula.av` (Middleware HYL-KSP)
- **Original Project Name**: `ksp_mobile`

---

## 1. Native Kaspersky B2B Mobile SDK Components

The prototype bundles the genuine **Kaspersky Lab Android B2B SDK** (`com.kavsdk` & `com.kaspersky.components`):

| Component Namespace | SDK Module | Architectural Responsibility |
| :--- | :--- | :--- |
| `com.kavsdk.SdkService` | Core Service | Background service hosting the daemon engine |
| `com.kavsdk.internal.activation.LicenseActivator` | License Engine | Validates and activates the B2B license key (`+= 1` license counter) |
| `com.kavsdk.antivirus.impl.RtpMonitor` | Real-Time Shield | File system and APK installation real-time hook |
| `com.kavsdk.antivirus.impl.ScannerImpl` | Malware Scanner | Deep scanner for files, storage, and installed packages |
| `com.kavsdk.updater.impl.UpdaterImpl` | Virus DB Updater | Automated download and verification of virus database updates |
| `com.kavsdk.wifi.impl` | Wi-Fi Safety | Checks Wi-Fi reputation, rogue access points, and WPA encryption |
| `com.kavsdk.compromised_accounts` | Data Breach | Checks account leaks against compromised identity bases |
| `com.kavsdk.compromised_passwords` | Password Audit | Checks password hashes against known leaked databases |
| `com.kaspersky.components.accessibility` | Web Filter | Intercepts browser URL navigations (Chrome/Yandex) to block phishing |
| `com.taspenguard.whocalls` | Caller ID / Spam | Kaspersky WhoCalls spam call detection and call screening |

---

## 2. Flutter Native Bridge (MethodChannel)

The Flutter application connects to the native Kaspersky engine via:
- **MethodChannel Identifier**: `com.taspenguard/ksp`
- **Dart SDK Controller**: `package:ksp_mobile/app/services/morp_sdk_service.dart`

### Core Native Methods
- `activateLicense`: Triggers B2B license activation on the device hardware ID.
- `setLicense`: Binds license credentials.
- `buildSDKStatus`: Queries current engine health, real-time protection state, and active shields.
- `startScan`: Launches asynchronous malware scan.
- `filesScan`: Scans file system storage.
- `PuaScan`: Evaluates installed applications for Potentially Unwanted Applications (PUA).
- `getLastDatabase`: Queries local virus definition timestamps and versions.
- `updateStatus`: Checks base download status.
- `checkAccessibilityStatus`: Checks Android accessibility service permissions for Web Filter.

---

## 3. Flutter Client Architecture (`ksp_mobile`)

- **State Management**: `flutter_riverpod` (`app_riverpod_observer.dart`).
- **Local Persistence**: `hive_ce` (`hive_ce/src/box/box_base.dart`).
- **Icons**: `phosphor_icons`.
- **Backend API**: `https://api.guard-panel.morp.co.id/v1`.
- **Feature Modules**:
  - `home/view/home_screen.dart`
  - `license_activation/view/license_activation_screen.dart`
  - `app/views/database_initialization_screen.dart`
  - `features/widgets/realtime_scan_card.dart`
  - `features/widgets/pua_scanner_card.dart`
  - `features/widgets/wifi_safety_card.dart`
  - `features/widgets/fake_apps_card.dart`
  - `features/widgets/device_fingerprint_card.dart`
  - `features/widgets/url_filter_card.dart`
  - `features/widgets/root_detection_card.dart`
  - `features/view/compromised_accounts_screen.dart`
  - `call_features/view/who_calls_screen.dart`
