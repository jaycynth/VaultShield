# VaultShield: Android 2FA Authenticator

VaultShield is a production-grade Android 2FA application designed to demonstrate advanced mobile security engineering. It implements industry-standard TOTP (RFC 6238) while enforcing a multi-layered defense-in-depth strategy.

## Security Architecture Highlights

VaultShield maps major mobile threats to concrete technical mitigations:

| Threat Category | Mitigation Control | Implementation Details |
| :--- | :--- | :--- |
| **Credential Theft** | Hardware-Backed Encryption | `EncryptedSharedPreferences` (AES-256 GCM) with keys in Android TEE. |
| **Unauthorized Access** | Biometric Gating | Mandatory `BiometricPrompt` for vault entry and per-account reveal. |
| **Tapjacking** | Anti-Overlay Protection | `HIDE_OVERLAY_WINDOWS` (Android 12+) to block malicious overlays. |
| **Screen Scraping** | Prevent Capture | `FLAG_SECURE` blocks screenshots and screen recording. |
| **Clipboard Sniffing** | Secure Clipboard Manager | Automatic 30-second clipboard clearing. |
| **Environment Risk** | Integrity Monitoring | Real-time detection of Root, Emulators, and Debuggers. |
| **Insecure Backup** | End-to-End Encryption | Password-based key derivation (PBKDF2) + AES-GCM for exports. |
| **Repudiation** | Security Audit Log | Secure local logging of all sensitive state changes and access events. |

## Feature Deep Dive: Security Audit & Forensics

Unlike standard authenticators, VaultShield implements a **Security Audit Log** system. While the logs are visible in the app for user transparency, their primary purpose in a real-world high-security scenario is **Forensic Analysis**.

- **Transparency**: Users can monitor when their secrets were added, deleted, or revealed.
- **Forensics**: In the event of a device compromise, these logs provide a tamper-evident record of activity, helping security teams understand the scope of a breach.
- **MASVS Compliance**: Addresses **MASVS-L2** requirements for logging and monitoring (M10: Insufficient Logging & Monitoring).

## Secure Backup & Recovery

VaultShield implements a zero-knowledge backup system:
- **Client-Side Encryption**: Data is encrypted *before* it leaves the application sandbox.
- **Key Derivation**: Uses **PBKDF2WithHmacSHA256** with 65,536 iterations and a unique salt.
- **Authenticated Encryption**: Uses **AES-256 GCM** to ensure the backup payload cannot be tampered with without detection.

## Project Structure & Documentation

This repository is designed to be a "Security Portfolio" piece:

- **[Threat Model](docs/threat-model.md)**: STRIDE analysis and risk assessment.
- **[Architecture](docs/architecture.md)**: Data flow, trust boundaries, and cryptographic details.
- **[Attack Surface](docs/attack-surface.md)**: Analysis of entry points and vectors.
- **[Security Tests](docs/security-tests.md)**: Plan for verifying defensive controls.

## OWASP MASVS Mapping

VaultShield targets **MASVS-L2** compliance:
- **MSTG-STORAGE-1/2**: Sensitive data stored in encrypted local storage using secure platform APIs.
- **MSTG-AUTH-1**: Biometric authentication enforced for all sensitive state transitions.
- **MSTG-CRYPTO-1**: Uses strong, industry-standard cryptographic primitives (AES-GCM, PBKDF2).
- **MSTG-PLATFORM-2**: Implements `FLAG_SECURE` and Anti-Overlay protection.
- **MSTG-RESILIENCE-1**: Integrated root and debugger detection.

## Technical Stack
- **Kotlin & Jetpack Compose**: Modern, reactive UI.
- **Dagger Hilt**: Dependency injection for clean, testable architecture.
- **AndroidX Security & Biometric**: Direct interface with hardware security modules.
- **OkHttp**: Configured with Certificate Pinning for network resilience.

## What VaultShield Does Not Protect Against

1. **Sophisticated OS-Level Hooks**: High-end spyware with kernel-level access may bypass user-space root detection.
2. **Compromised Keystore**: If the OS itself is compromised, hardware-backed keys can still be abused if the authentication is bypassed.
3. **User Password Strength**: Backup security relies on the complexity of the user's chosen password.
4. **Physical Coercion**: Security controls are bypassed if a user is physically forced to authenticate.

---
*Developed as a demonstration of Mobile Security Engineering proficiency.*
