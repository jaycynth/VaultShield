# Threat Model: VaultShield

This document outlines the threat landscape for VaultShield using the **STRIDE** methodology and maps them to **OWASP Mobile Top 10** risks.

## 1. Asset Analysis
- **TOTP Seeds (Primary Asset)**: The cryptographic secrets used to generate codes. If compromised, the attacker can impersonate the user.
- **Generated OTP Codes**: Short-lived secrets. Sensitive but lower impact than secrets.
- **Audit Logs**: Information about when secrets were accessed or modified. **Now visible to users for forensic review.**
- **Backup Files**: Encrypted exports of the entire vault.

## 2. STRIDE Threat Analysis

| Category | Threat Scenario | Mitigation in VaultShield |
| :--- | :--- | :--- |
| **Spoofing** | Attacker uses a stolen device to view codes. | **Biometric Unlock**: Mandatory `BiometricPrompt` before vault entry and per-account reveal. |
| **Tampering** | Malware modifies the app binary or local database. | **Integrity Checks**: Detection of Root, Emulators, and Debuggers. **EncryptedSharedPreferences**: AES-256 GCM provides authenticated encryption (integrity + confidentiality). |
| **Repudiation** | User denies performing a sensitive action or claims the app revealed secrets without consent. | **Security Audit Log**: User-facing, immutable (encrypted) record of all sensitive actions (Add/Delete/Reveal/Export). |
| **Information Disclosure** | Secrets extracted via ADB or physical storage dump. | **Hardware-Backed Keystore**: Keys for `EncryptedSharedPreferences` are stored in TEE/StrongBox. |
| **Information Disclosure** | Shoulder surfing or background task snapshots. | **FLAG_SECURE**: Prevents screenshots and hides app content in the recent apps switcher. |
| **Information Disclosure** | Clipboard sniffing by malicious background apps. | **Secure Clipboard**: 30-second auto-clear timer for all copied codes. |
| **Information Disclosure** | Overlays/Tapjacking to trick user. | **HIDE_OVERLAY_WINDOWS**: Prevents non-system overlays from appearing over the app. |
| **Denial of Service** | App crash preventing access to 2FA. | **Offline-First**: Zero dependency on external APIs for core code generation. |

## 3. OWASP Mobile Top 10 Mapping

- **M1: Improper Platform Usage**: Mitigated by `BiometricPrompt`, `FLAG_SECURE`, and `HIDE_OVERLAY_WINDOWS`.
- **M2: Insecure Data Storage**: Mitigated by `EncryptedSharedPreferences` and PBKDF2-derived backup encryption.
- **M3: Insecure Communication**: Mitigated by Certificate Pinning in OkHttp.
- **M8: Code Tampering**: Mitigated by RootBeer integration and ProGuard/R8 obfuscation.
- **M9: Reverse Engineering**: Mitigated by R8 hardening and debugger detection.
- **M10: Insufficient Logging & Monitoring**: Mitigated by the **Secure Audit Log** system.
