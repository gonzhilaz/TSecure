---
name: Vigilance Modern
colors:
  surface: '#fff8f7'
  surface-dim: '#f6d2ce'
  surface-bright: '#fff8f7'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#fff0ef'
  surface-container: '#ffe9e7'
  surface-container-high: '#ffe2df'
  surface-container-highest: '#ffdad7'
  on-surface: '#2a1615'
  on-surface-variant: '#5e3f3c'
  inverse-surface: '#412b29'
  inverse-on-surface: '#ffedeb'
  outline: '#936e6b'
  outline-variant: '#e9bcb8'
  surface-tint: '#c0001c'
  primary: '#be001c'
  on-primary: '#ffffff'
  primary-container: '#ed0226'
  on-primary-container: '#ffffff'
  inverse-primary: '#ffb3ad'
  secondary: '#545d7c'
  on-secondary: '#ffffff'
  secondary-container: '#d0d9fd'
  on-secondary-container: '#555e7d'
  tertiary: '#006490'
  on-tertiary: '#ffffff'
  tertiary-container: '#007eb4'
  on-tertiary-container: '#ffffff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#ffdad7'
  primary-fixed-dim: '#ffb3ad'
  on-primary-fixed: '#410004'
  on-primary-fixed-variant: '#930013'
  secondary-fixed: '#dbe1ff'
  secondary-fixed-dim: '#bdc5e9'
  on-secondary-fixed: '#111a36'
  on-secondary-fixed-variant: '#3d4664'
  tertiary-fixed: '#c9e6ff'
  tertiary-fixed-dim: '#89ceff'
  on-tertiary-fixed: '#001e2f'
  on-tertiary-fixed-variant: '#004c6e'
  background: '#fff8f7'
  on-background: '#2a1615'
  surface-variant: '#ffdad7'
  brand-red-dark: '#E11424'
  navy-deep: '#0B132B'
  slate-mid: '#3A405A'
  slate-muted: '#778CA2'
  slate-surface: '#F4F6F9'
  status-safe: '#00C853'
  status-safe-emerald: '#10B981'
  status-warning: '#FFA000'
  status-warning-amber: '#F59E0B'
typography:
  headline-xl:
    fontFamily: Public Sans
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
  headline-xl-mobile:
    fontFamily: Public Sans
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
  headline-lg:
    fontFamily: Public Sans
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
  headline-lg-mobile:
    fontFamily: Public Sans
    fontSize: 22px
    fontWeight: '700'
    lineHeight: 30px
  headline-md:
    fontFamily: Public Sans
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  headline-sm:
    fontFamily: Public Sans
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Public Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Public Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 22px
  body-sm:
    fontFamily: Public Sans
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 18px
  label-lg:
    fontFamily: Public Sans
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
  label-md:
    fontFamily: Public Sans
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
  label-sm:
    fontFamily: Public Sans
    fontSize: 10px
    fontWeight: '700'
    lineHeight: 14px
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-mobile: 0.75rem
  margin: 1.5rem
  margin-mobile: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

This design system establishes a high-trust, telecommunications-grade mobile security interface. It merges the institutional clarity of crisp utility frameworks with the bold visual energy of primary brand red, backed by defensive deep-slate navy and functional telemetry indicators. The system serves consumer and enterprise telecommunication subscribers requiring real-time device protection, scam detection, network integrity auditing, and identity shielding.

The aesthetic philosophy is **Corporate / Modern High-Precision**:
- **Unwavering Security Credibility**: Employs institutional typography (Public Sans) with structured micro-metrics and clear statuses.
- **Defensive Clarity**: High contrast without cognitive fatigue. Critical alerts strike instantly with assertive crimson, while routine operations remain serene against soft slate backdrops.
- **Modular Precision**: Compact badge containers, defined 1px structural outlines, calibrated status indicators, and dedicated telemetry metadata tags provide tactical utility.

## Colors

The palette balances authoritative telecom crimson with high-security tactical slate tones and calibrated security status indicators.

- **Primary (`#ED0226` / `#E11424`)**: Reserved for critical actions, immediate threats, high-priority scan execution, and primary identity anchors. Used sparingly on backgrounds to preserve maximum alert value.
- **Secondary (`#1C2541`) & Deep Navy (`#0B132B`)**: Powers structural framing, active navigation states, primary text headings, and telemetry card surfaces. Communicates impenetrable architecture.
- **Slate Mid (`#3A405A`) & Slate Muted (`#778CA2`)**: Establishes body typography hierarchy, inactive button states, divider strokes, and auxiliary icon tinting.
- **Neutral & Backgrounds (`#FFFFFF`, `#F4F6F9`)**: Canvas and surface container foundation. Clean white surfaces sit over soft slate backdrops to produce distinct card boundaries.
- **Security Semantics**:
  - `status-safe` (`#00C853` / `#10B981`): Network safe, device encrypted, malware clean, SIM secured.
  - `status-warning` (`#FFA000` / `#F59E0B`): Wi-Fi unverified, permission leaks, outdated firmware, spam risk.

## Typography

The typography is built around **Public Sans**, delivering neutral, highly legible glyph metrics optimized for both data-dense telemetry and clear security alerts.

- **Weight Distribution**: Semi-bold (`600`) and Bold (`700`) are strictly applied to headings, numerical counters, and action triggers. Body text relies on Regular (`400`) to guarantee fast optical scannability.
- **Label Hierarchy**: Interactive controls normalize around `label-lg` (14px/600), matching the baseline button system. Status badges, counters, and metadata chips utilize `label-md` and `label-sm` with tabular lining for data integrity.
- **Mobile Responsive Scaling**: Large hero typography scales down seamlessly below `480px` to maintain strict layout boundaries on mobile viewports.

## Layout & Spacing

The layout is built on a 4px mathematical sub-grid, scaling to an 8px macro layout model:

- **Mobile Viewport (Base)**: 4-column fluid layout with `1rem` (16px) margins and `0.75rem` (12px) gutters. Handheld safety margins prevent edge clipping.
- **Tablet / Large Form Factor**: 8-column layout with `1.5rem` (24px) gutters and margins, keeping card modules within clear scanning columns.
- **Component Gap Rhythm**:
  - `space-xs` (4px): Inline indicator separation, label-to-badge gaps, micro icon offsets.
  - `space-sm` (8px): Stacked button internals, list item secondary line separation, chip padding.
  - `space-md` (16px): Card internal content padding, default form field gap, module stack rhythm.
  - `space-lg` (24px): Inter-card sectional padding and dashboard cluster splits.
  - `space-xl` (32px): Primary security status hero module separation.

## Elevation & Depth

This design system avoids heavy shadows, using a **structured boundary technique** featuring crisp low-contrast borders, solid surface tiers, and calibrated state tints.

- **Layer 0 (Canvas Base)**: `#F4F6F9` soft slate backdrop for neutral visual resting.
- **Layer 1 (Card & Module Surface)**: Pure `#FFFFFF` panels defined by a `1px` continuous border (`#E2E8F0` or `#1C2541` at 8% alpha). No diffuse shadows; clarity is maintained through structural boundaries.
- **Layer 2 (Elevated Sheets & Floating Controls)**: Solid `#FFFFFF` elevated with a clinical, low-blur ambient shadow: `0 4px 12px rgba(11, 19, 43, 0.06)`, maintaining a crisp perimeter.
- **Layer 3 (Modal Alerts & Security Interstitials)**: `#0B132B` backdrop overlay at 60% opacity with centered card containment bordered with a 1px protective stroke.

## Shapes

The shape architecture is calibrated to **Soft (Level 1)**, projecting technical precision, structural firmness, and high reliability:

- **Primary Interactive Controls & Form Inputs**: `0.375rem` (6px) corner radius. Delivers a modern, crisp boundary without roundness distortions.
- **Cards & Surface Modules**: `0.5rem` (8px) corner radius with unified `1px` structural outline borders.
- **Micro Badges, Service Icons & Payment Indicators**: `0.125rem` (2px) to `0.25rem` (4px) tight radius.
- **Status Pills & Numerical Alert Counters**: `9999px` (pill-shape) for standalone numeric threat counts and active network shields.

## Components

### Buttons
- **Primary / Action**: Height `46px`. Background `#ED0226`, text `#FFFFFF`, typography `label-lg` (14px, 600). Hover/Active: `#E11424`. Radius `6px`. Inline icons spaced at `5px`.
- **Secondary / Tactical**: Background `#1C2541`, text `#FFFFFF`. Used for secondary diagnostic actions and network logs.
- **Muted / Resting**: Background `#F2F4F6`, text `#778CA2`. Radius `6px`. Switches to active filled state upon interaction.
- **Outline**: Background transparent, `1.5px` border in `#1C2541` or `#778CA2`, text `#1C2541`.
- **Icon / Square Buttons**: `46px × 46px` boundary, centered 18px glyph, matching button corner radius.

### Badges & Status Indicators
- **Outline Counter Badges**: `9999px` pill radius, internal padding `2px 8px`, typography `label-sm` (10px/700).
- **Status: Protected**: `#00C853` background (or `10%` alpha fill with `#00C853` border and text).
- **Status: Vulnerable / Threat**: `#ED0226` background, white text.
- **Status: Warning / Attention**: `#FFA000` text with amber-tinted surface (`#FFF8E1`).

### Cards & Security Tiles
- **Structure**: Surface `#FFFFFF`, border `1px solid #E2E8F0`, corner radius `8px`, padding `16px`.
- **Threat Card**: Accentuated with a `3px` left-edge boundary highlight (`#ED0226` for vulnerability, `#10B981` for safe perimeter).
- **Telemetry Metric Card**: Monospaced security figures, labeled top-left with subtext in `#778CA2`.

### Lists & Activity Feeds
- Clean single-pixel divider lines (`#F2F4F6`).
- Left-aligned security glyph inside a `32px × 32px` rounded square container (`4px` radius) tinted to current status.
- Primary text `14px/600` (`#0B132B`), secondary timestamp and payload `12px/400` (`#778CA2`).

### Form Inputs & Toggles
- **Input Fields**: Height `46px`, border `1px solid #778CA2` (50% opacity), radius `6px`, background `#FFFFFF`. Focus ring: `2px` solid `#ED0226` at 20% alpha with `#ED0226` border.
- **Shield Toggles (Switches)**: Width `48px`, height `26px`, track `#F2F4F6` (resting) / `#00C853` (protected), thumb `#FFFFFF` with 2px offset.

### Security Shield Hero
- Prominent mobile top component featuring real-time diagnostic shield state.
- Dynamic color state shift: Emerald `#00C853` for "Device Secured", Telkomsel Red `#ED0226` for "Threats Detected", Warning Amber `#FFA000` for "Action Required".