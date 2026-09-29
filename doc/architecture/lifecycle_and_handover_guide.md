# Application Lifecycle & Handover Guide

## Overview
This guide provides explicit procedures for maintaining the Telkomsel Secure application through Android OS updates, Flutter upgrades, Kaspersky SDK renewals, and organizational developer transitions.

---

## 1. Android OS Upgrades (e.g., Android 15, Android 16)
- **Scope**: Native Android permissions, foreground service restrictions, and notification policies.
- **Procedure**:
  1. Update `compileSdk` and `targetSdk` in `android/app/build.gradle`.
  2. Verify foreground service types in `AndroidManifest.xml` (e.g., `android:foregroundServiceType="dataSync"` for background updates).
  3. Run `flutter build apk --debug` and test service lifecycles.
- **Risk Level**: Minimal. No changes required to core UI or Flutter state models.

---

## 2. Flutter Framework Upgrades
- **Scope**: Flutter SDK and Dart runtime updates.
- **Procedure**:
  1. Run `flutter upgrade` in terminal.
  2. Execute `flutter pub outdated` to identify compatible package versions.
  3. Run `flutter analyze` to ensure zero deprecated API usages.
  4. Run `flutter test` to ensure smoke test suite passes.
- **Risk Level**: Minimal. Flutter `MethodChannel` remains backwards-compatible across major Flutter engine generations.

---

## 3. Kaspersky B2B Mobile SDK Updates
- **Scope**: Annual antivirus definition engine upgrades and new security APIs.
- **Procedure**:
  1. Replace native `.aar` / library binaries in `android/app/libs/`.
  2. Verify MethodChannel handlers for any newly introduced features (e.g., updated scan options).
  3. Test active period activation and license check (`Mobile ID += 1` guard).
- **Risk Level**: Isolated. UI components continue binding to high-level abstractions without breakage.

---

## 4. Developer Handover & Succession Protocol
- **Access Tiers**:
  - **Tier 1 (Application Developers)**: Work in `/main` with standard Flutter commands (`flutter run`, `flutter build`). No sensitive keys or private access needed.
  - **Tier 2 (Security Architects)**: Maintain backend contracts and native SDK build scripts.
- **Handover Checklist**:
  1. Grant repository access on the corporate Git platform.
  2. Share build signing credentials securely (via corporate key vault).
  3. Verify new maintainer can clone and run `flutter analyze` and `flutter test` cleanly.
