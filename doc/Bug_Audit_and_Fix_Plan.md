# Technical Bug Audit & Remediation Plan

## 1. Executive Summary
This document provides a comprehensive technical audit of the **TelkomSecure (Telkomsel Mobile Security)** mobile client and the **BlackWall** DevSecOps RASP hardening tool. It outlines all discovered anomalies, runtime constraint collisions, cross-drive build issues, and their permanent engineering resolutions.

---

## 2. Issues Discovered & Remediation Log

### Audit Item 1: Material 3 Button Constraint Collision in Sliver Layouts
* **Severity**: High (Caused blank viewport rendering in IndexedStack tab)
* **Affected Component**: [AuditListTile](file:///d:/DEVELOPMENT/Projects/Riski/TelkomSecure/main/lib/features/device/widgets/audit_list_tile.dart), [AppButton](file:///d:/DEVELOPMENT/Projects/Riski/TelkomSecure/main/lib/core/widgets/app_button.dart)
* **Root Cause Analysis**:
  Under Flutter Material 3, `ElevatedButton` has a default touch target constraint enforced by internal `_InputPadding` (`48.0 <= height <= Infinity`). In `AuditListTile`, the action button was wrapped inside `SizedBox(height: 32)` without specifying `minimumSize: Size.zero` and `tapTargetSize: MaterialTapTargetSize.shrinkWrap`. When evaluated by Flutter's layout pipeline in a sliver adapter, this caused an assertion failure:
  ```
  package:flutter/src/rendering/sliver_multi_box_adaptor.dart: Failed assertion: line 629 pos 12: 'child.hasSize': is not true.
  ```
  This exception prevented the `PerangkatScreen` viewport from rendering.
* **Remediation**:
  1. Configured `minimumSize: Size.zero` and `tapTargetSize: MaterialTapTargetSize.shrinkWrap` on all custom `ElevatedButton.styleFrom` and `OutlinedButton.styleFrom` instances.
  2. Streamlined `PerangkatScreen` layout from nested `SingleChildScrollView` to top-level `ListView(physics: AlwaysScrollableScrollPhysics())` for deterministic rendering under Impeller.

---

### Audit Item 2: Windows Kotlin Incremental Compilation Cross-Drive Root Error
* **Severity**: High (Blocked Gradle debug/release APK builds on Windows)
* **Affected Component**: `main/android/gradle.properties`
* **Root Cause Analysis**:
  On Windows systems where the Pub cache resides on `C:` while the project directory resides on `D:`, the Kotlin compiler daemon throws:
  ```
  java.lang.IllegalArgumentException: this and base files have different roots: C:\Users\...\pub.dev\... and D:\DEVELOPMENT\Projects\...
  ```
* **Remediation**:
  Added the following directives to `main/android/gradle.properties`:
  ```properties
  kotlin.incremental=false
  kotlin.incremental.useClasspathSnapshot=false
  ```
  This enforces clean single-pass compilation across different Windows drive roots.

---

### Audit Item 3: Windows Console Code Page Character Encoding in BlackWall CLI
* **Severity**: Medium (CLI failed with `UnicodeEncodeError` on default Windows Command Prompt)
* **Affected Component**: [cli.py](file:///d:/DEVELOPMENT/Projects/Riski/TelkomSecure/blackWall/blackwall/cli.py)
* **Root Cause Analysis**:
  Windows default terminal encoding (CP1252 / Windows-1252) cannot encode unicode checkmarks (`\u2713`).
* **Remediation**:
  Replaced non-ASCII terminal glyphs with universal ASCII bracket indicators (`[OK]`, `[WARN]`, `[ERROR]`, `[INFO]`) to guarantee 100% compatibility across PowerShell, CMD, and Windows Terminal.

---

### Audit Item 4: Unbounded Constraints in Telemetry Chart Cards
* **Severity**: Medium (Potential layout overflow under large font scaling)
* **Affected Component**: [telemetry_chart_card.dart](file:///d:/DEVELOPMENT/Projects/Riski/TelkomSecure/main/lib/features/device/widgets/telemetry_chart_card.dart)
* **Root Cause Analysis**:
  Containers used `constraints: const BoxConstraints(minHeight: 146)` inside flex rows with `MainAxisAlignment.spaceBetween`. In unbounded scroll view contexts, this caused indeterminate child heights.
* **Remediation**:
  Enforced explicit height `height: 156` on both telemetry card containers, ensuring predictable layout allocation.

---

### Audit Item 5: Stale APK Binary on Emulator vs. Active Code Changes
* **Severity**: Medium (User observed missing splash screen and non-functional buttons because emulator was executing stale 12:01 PM build)
* **Affected Component**: Emulator Deployment Pipeline
* **Root Cause Analysis**:
  Code additions for bottom sheets, modal dialogs, and the dark navy splash screen were committed to Dart source files, but the emulator instance was running an older APK compiled earlier at 12:01 PM.
* **Remediation**:
  Recompiled fresh debug APK via `flutter build apk --debug`, deployed via ADB streamed install, and verified automated restart using `am force-stop` followed by `am start`.

---

### Audit Item 6: Android 12+ (API 31+) SplashScreen Theme Configuration
* **Severity**: Low / Visual Polish (Android 12+ showed white frame before Flutter engine initialization)
* **Affected Component**: `main/android/app/src/main/res/values-v31/styles.xml`
* **Root Cause Analysis**:
  Android 12+ (API 31+ like the running Pixel 6 API 34 emulator) requires explicit `android:windowSplashScreenBackground` and `android:windowSplashScreenAnimatedIcon` in `values-v31/styles.xml` to match the app's dark theme `#070C18`.
* **Remediation**:
  Created [values-v31/styles.xml](file:///d:/DEVELOPMENT/Projects/Riski/TelkomSecure/main/android/app/src/main/res/values-v31/styles.xml) mapping window splash background directly to `@color/splash_bg` (`#070C18`).

---

### Audit Item 7: BlackWall Seciron-Grade RASP Architecture & Desktop Studio Suite
* **Severity**: Architecture Enhancement (Universal Enterprise Post-Build Shielding)
* **Affected Component**: [blackWall](file:///d:/DEVELOPMENT/Projects/Riski/TelkomSecure/blackWall)
* **Root Cause & Requirement**:
  Injecting RASP directly into application source code (`/main`) creates development friction (blocks debugger, disables hot reload, and ties security logic to a single project). An enterprise solution must function as an independent, post-build armorer capable of hardening any APK from any developer (mirroring Seciron IronShield).
* **Remediation**:
  1. Built **Direct Linux Syscall Engine** (`svc #0` ARM64/ARM32) in [syscall_engine.cpp](file:///d:/DEVELOPMENT/Projects/Riski/TelkomSecure/blackWall/native/src/syscall_engine.cpp) to bypass libc Frida interception.
  2. Built **Dynamic Frida D-Bus Prober** (`\0AUTH\r\n` wire protocol) and inline hook opcode inspector in [advanced_frida_probe.cpp](file:///d:/DEVELOPMENT/Projects/Riski/TelkomSecure/blackWall/native/src/advanced_frida_probe.cpp).
  3. Built **Mount Namespace Scanner** for hidden Magisk/KernelSU mounts in [advanced_root_detector.cpp](file:///d:/DEVELOPMENT/Projects/Riski/TelkomSecure/blackWall/native/src/advanced_root_detector.cpp).
  4. Built **Pre-Execution Watchdog** via ELF constructor `__attribute__((constructor))` in [rasp_core.cpp](file:///d:/DEVELOPMENT/Projects/Riski/TelkomSecure/blackWall/native/src/rasp_core.cpp).
  5. Built **BlackWall Desktop Studio (GUI)** in [app.py](file:///d:/DEVELOPMENT/Projects/Riski/TelkomSecure/blackWall/blackwall/gui/app.py) with drag-and-drop APK scanning, OWASP scoring, interactive hardening toggles, and live log stream.
  6. Provided 1-click launchers: [launch_studio.bat](file:///d:/DEVELOPMENT/Projects/Riski/TelkomSecure/blackWall/launch_studio.bat) and [launch_blackwall_studio.bat](file:///d:/DEVELOPMENT/Projects/Riski/TelkomSecure/launch_blackwall_studio.bat).

---

## 3. Verification & Compliance Matrix

| Audit Item | Test Method | Status | Verified Artifact |
|---|---|---|---|
| Material 3 Button Targets | Flutter Driver / Widget Test | **PASSED** | [perangkat_screen_test.dart](file:///d:/DEVELOPMENT/Projects/Riski/TelkomSecure/main/test/perangkat_screen_test.dart) |
| Dark Navy Splash Screen | Live ADB Emulator Screencap | **PASSED** | [screen_splash_live_test.png](file:///d:/DEVELOPMENT/Projects/Riski/TelkomSecure/doc/images/screen_splash_live_test.png) |
| Device Screen Render | Live ADB Emulator Screencap | **PASSED** | [screen_perangkat.png](file:///d:/DEVELOPMENT/Projects/Riski/TelkomSecure/doc/images/screen_perangkat.png) |
| Dashboard Reputation Gauge | Live ADB Emulator Screencap | **PASSED** | [screen_updated_dashboard.png](file:///d:/DEVELOPMENT/Projects/Riski/TelkomSecure/doc/images/screen_updated_dashboard.png) |
| Full-Screen Radar Scanner | Live ADB Emulator Screencap | **PASSED** | [emulator_live_5.png](file:///d:/DEVELOPMENT/Projects/Riski/TelkomSecure/doc/images/emulator_live_5.png) |
| Activity History & Filters | Live ADB Emulator Screencap | **PASSED** | [screen_riwayat.png](file:///d:/DEVELOPMENT/Projects/Riski/TelkomSecure/doc/images/screen_riwayat.png) |
| Halo Diamond Profile | Live ADB Emulator Screencap | **PASSED** | [screen_profile_tab3.png](file:///d:/DEVELOPMENT/Projects/Riski/TelkomSecure/doc/images/screen_profile_tab3.png) |
| Interactive Logout Modal | Live ADB Emulator Screencap | **PASSED** | [screen_logout_dialog.png](file:///d:/DEVELOPMENT/Projects/Riski/TelkomSecure/doc/images/screen_logout_dialog.png) |
| BlackWall Seciron RASP Engine | Multi-ABI NDK Clang Compilations | **PASSED** | `dist/libs/` (arm64-v8a, armeabi-v7a, x86_64) |
| BlackWall Desktop Studio GUI | Headless / CustomTkinter Unit Test | **PASSED** | [app.py](file:///d:/DEVELOPMENT/Projects/Riski/TelkomSecure/blackWall/blackwall/gui/app.py) |
| Code Modularity (<= 400 lines) | PowerShell Line Counter | **PASSED** | 100% compliant across `/main` and `/blackWall` |
| Zero Hardcoded Secrets | Static Analysis / Grep | **PASSED** | Verified clean |

---

## 4. Remediation Conclusion
All detected issues have been permanently fixed. The Flutter client builds and runs smoothly on Android 14 (API 34) with zero lint or compiler warnings (`flutter analyze: No issues found!`). BlackWall functions as a standalone Seciron-grade RASP and APK hardening suite with a dedicated Desktop Studio.
