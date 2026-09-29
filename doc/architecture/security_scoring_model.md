# Dynamic Security Scoring Model

## Overview
The device security percentage gauge on the Dashboard and Pemindaian screens is **strictly dynamic**. It is computed in real-time by the Security Scoring Engine based on actual device telemetry, active Kaspersky shields, and recent diagnostic results.

---

## Scoring Factors & Weight Distribution

The maximum possible security score is **100%**. Individual security dimensions contribute specific point weights:

| Dimension | Weight | Criteria for Full Score | Impact if Failed / Inactive |
| :--- | :---: | :--- | :--- |
| **Real-time Antivirus Protection** | 25% | Kaspersky Real-time File & App Shield active | -25% (Drops score instantly) |
| **Recent Malware & PUA Scan** | 20% | Deep scan executed within the last 72 hours with 0 threats | -20% (Stale scan or threats detected) |
| **Web Filter & Anti-Phishing** | 15% | Safe browsing filter enabled | -15% |
| **Wi-Fi & Network Security** | 10% | Connected network encrypted (WPA2/WPA3), no ARP spoofing | -10% |
| **System Integrity (Bootloader & Root)** | 15% | Stock firmware, bootloader locked, no su binary detected | -15% |
| **Device Storage Encryption** | 10% | Internal storage encrypted (AES-256) | -10% |
| **Password / Biometric Lock** | 5% | Lock screen PIN/Pattern/Biometrics enabled | -5% |

---

## Dynamic State Mapping & Color Thresholds
Matching the `Vigilance Modern` design tokens:

- **Score >= 90%**: 
  - Status: `"Perangkat Sangat Aman"` / `"Terlindungi Maksimal"`
  - Theme Color: Emerald Green (`#00C853` / `#10B981`)
- **Score 70% – 89%**:
  - Status: `"Perlu Perhatian"` / `"Optimalisasi Tersedia"`
  - Theme Color: Warning Amber (`#FFA000` / `#F59E0B`)
- **Score < 70%**:
  - Status: `"Rentan Terhadap Ancaman"` / `"Tindakan Diperlukan Segera"`
  - Theme Color: Telkomsel Brand Red (`#ED0226` / `#E11424`)
