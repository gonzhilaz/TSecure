# Telkomsel Secure Implementation Task Checklist

## Phase 1: Foundations & Architecture
- [x] Analyze design specifications in `stitch_telkomsel_mobile_security_app (34)`.
- [x] Analyze system workflow & Kaspersky B2B licensing constraints in `Workflow.jpeg`.
- [x] Set up documentation structure in `/doc` (`architecture/`, `todos/`, `releases/`, `fixes/`).
- [x] Initialize standard Flutter project in `/main`.
- [x] Install dependencies (`provider`, `google_fonts`, `intl`, `shared_preferences`).
- [x] Configure `Vigilance Modern` design tokens (Color constants, typography, card shapes).

## Phase 2: Core Services & Mock Layer
- [x] `MobileIdService`: Stable device/mobile identifier management bound to Kaspersky license counting.
- [x] `TelkomselBackendService`: Mock & contract for `Cek Masa Aktif (MSISDN + Mobile ID)`.
- [x] `KasperskySdkBridge`: Safe license-guarded SDK initializer (`init()`), scanner engine, and shield state.
- [x] `SecurityScoreService`: Dynamic security scoring algorithm based on device telemetry factors.
- [x] Mock repositories for activity history, device audit metrics, and user profile data (`lib/data/mocks/`).

## Phase 3: Screen Implementations (Strict <= 400 lines/file)
- [x] **Splash Screen**: Telkomsel brand splash with automated session & `activePeriod` check.
- [x] **Masuk (Login) Screen**: MSISDN input, PDP Law checkbox, instant validation trigger.
- [x] **Main Shell**: Floating bottom navigation bar with central highlighted Quick-Scan button.
- [x] **Beranda (Dashboard) Screen**: Dynamic security score arc gauge, real-time protection switch, 8 security shortcuts, recent activity feed.
- [x] **Pemindaian (Scanner) Screen**: Concentric radar scanning animation, scan results, scan execution loop.
- [x] **Perangkat Screen**: Last scan status, 24/7 protection activity telemetry bar chart, security audit checklist.
- [x] **Riwayat Aktivitas Screen**: Monthly summary card, category filter tabs, date-grouped audit logs with tri-state handling.
- [x] **Profil Screen**: User identity, Halo tier, active subscription period card, security badge tags.

## Phase 4: Verification & Hardening
- [x] Static analysis & lint verification (`flutter analyze` - 0 issues found).
- [x] Test suite passing (`flutter test` - all tests passed).
- [x] Zero hardcoded secrets audit.
- [x] Verify 400-line limit across all `.dart` files (all files between 15 and 288 lines).
- [x] Document release notes in `doc/releases/v1.0.0_initial_scaffold.md`.
