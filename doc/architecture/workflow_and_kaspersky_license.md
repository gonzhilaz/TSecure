# Telkomsel Secure Architecture & Workflow

## Overview
Telkomsel Secure Mobile Security is an anti-virus and threat management client powered by the **Kaspersky B2B Mobile SDK**. It integrates with the Telkomsel backend and billing ecosystem to validate customer active subscription periods before initializing security engine licenses.

---

## System Architecture & Interaction Flow
*(Based on the blueprint documented in `Workflow.jpeg`)*

```mermaid
sequenceDiagram
    autonumber
    actor User as Subscriber (MyTelkomsel)
    participant App as Telkomsel Secure (Flutter)
    participant Backend as Backend Telkomsel Secure
    participant Middleware as Middleware HYL-KSP / NBP
    participant Kaspersky as Kaspersky B2B Mobile SDK

    User->>App: Login with MSISDN (Nomor HP)
    App->>Backend: Req: Cek Masa Aktif (MSISDN + Mobile ID)
    Backend->>Middleware: Query active package & subscription period
    Middleware-->>Backend: Resp: Masa Aktif (expiryDate, status)
    Backend-->>App: Res: Masa Aktif Nomor (activePeriod, packageTier)

    alt Masa Aktif is Valid & Not Expired
        critical Mobile ID License Guard
            App->>App: Verify Mobile ID activation status
            Note over App,Kaspersky: 1 activation on a distinct Mobile ID counts += 1 B2B license
            App->>Kaspersky: init(licenseKey, mobileId, config)
            Kaspersky-->>App: SDK Initialized & Ready (Real-time Shield, Scanner, Definitions)
        end
        App->>App: Calculate Dynamic Security Score & Open Dashboard
    else Masa Aktif Expired / Not Subscribed
        App->>App: Keep Kaspersky SDK Dormant (Zero License Consumption)
        App->>User: Display Subscription Expired / Direct to MyTelkomsel package purchase
    end
```

---

## Kaspersky B2B Mobile SDK Licensing Rules
1. **Per-Device Licensing (`Mobile ID += 1`)**:
   - Every activation bound to a unique `Mobile ID` permanently consumes/bills **+1 Kaspersky B2B license**.
   - Repeated initializations on the *same* verified device must reuse cached session credentials without issuing new activation handshakes.
2. **Pre-requisite Validation**:
   - The application **must never** invoke `Kaspersky.init()` blindly on app launch.
   - The SDK is strictly initialized **only after** `activePeriod` is verified as active and valid from `Backend Telkomsel Secure`.
3. **Graceful Deactivation**:
   - If the active period expires, the client transitions the local security engine to passive mode without triggering SDK re-activation.
