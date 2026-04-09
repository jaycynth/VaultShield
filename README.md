# VaultShield: Secure Android 2FA Authenticator

VaultShield is a project demonstrating a hardened Android TOTP (RFC 6238) authenticator. It prioritizes data integrity, local-first security, and resistance to common mobile attack vectors.

## Security Architecture Highlights

VaultShield maps concrete mobile threats to specific technical mitigations:

| Threat Scenario | Mitigation Control | Implementation Details |
| :--- | :--- | :--- |
| **Physical Access / Theft** | Biometric Gating | Mandatory `BiometricPrompt` for vault entry and per-account secret reveal. |
| **Data Extraction / Root Dump** | Authenticated Encryption | `EncryptedSharedPreferences` (AES-256 GCM) with keys in Android Keystore (Hardware-backed where supported). |
| **Overlay / Tapjacking** | UI Hardening | `HIDE_OVERLAY_WINDOWS` (Android 12+) to block malicious screen overlays. |
| **Screen Scraping / Recents** | Prevent Capture | `FLAG_SECURE` to block screenshots and screen recording. |
| **Clipboard Sniffing** | Secure Clipboard | Custom manager with a 30-second auto-clear timer for OTP codes. |
| **Compromised Environment** | Runtime Integrity Checks | Periodic checks for Root (RootBeer), Emulators, and active Debuggers. |
| **Malicious Backups** | Encrypted Export | Client-side password-based key derivation (PBKDF2) + AES-GCM for exports. |
| **Internal Repudiation** | Forensic Audit Log | Secure local logging of sensitive state changes (e.g., secret reveal, export). |

## Technical Implementation

### Data Protection & Storage
VaultShield uses a defense-in-depth storage model:
- **Secrets & Metadata**: Stored using `EncryptedSharedPreferences` within the application sandbox. Keys are managed via the Android Keystore, utilizing the **TEE (Trusted Execution Environment)** or **StrongBox** hardware where available.
- **Backup Security**: Implements a **client-side encrypted backup flow**. Keys are derived via **PBKDF2WithHmacSHA256** (65,536 iterations + 16-byte salt) and data is encrypted with **AES-256 GCM**.

### Security Audit System (Forensics)
In this demo, the **Security Audit Log** is user-visible to demonstrate the capture of sensitive events.
- **Enterprise Deployment Note**: In a production environment, these logs would typically be non-user-facing and offloaded to a secure remote SIEM or kept in protected partitions for forensic auditors.
- **Efficiency**: Implements a circular buffer (100 entries) to prevent unbounded storage and memory consumption.

### Environment Integrity
The app performs **runtime checks** for:
- **Root/Tamper**: Basic detection via `RootBeer` and build-property analysis.
- **Emulators**: Checking for known virtualized hardware signals.
- **Debuggers**: Detection of attached JDWP debuggers.
*Note: These are defensive signals and, while robust, are not a 100% guarantee against sophisticated kernel-level hooks.*

## Verification & Testing
- **Unit Tests**: `TotpGeneratorTest` validates HMAC-SHA1 output against RFC vectors.
- **Security Tests**: See [docs/security-tests.md](docs/security-tests.md) for manual validation procedures for biometric gating and screenshot prevention.

## OWASP MASVS Mapping
VaultShield targets **MASVS-L2** (Standard Security + Defense-in-Depth):
- **MSTG-STORAGE-1**: Sensitive data never stored in plaintext.
- **MSTG-AUTH-1**: Biometrics enforced for all sensitive state transitions.
- **MSTG-CRYPTO-1**: Industry-standard primitives (AES-GCM, PBKDF2).
- **MSTG-PLATFORM-2**: Implements `FLAG_SECURE` and Anti-Overlay.
- **MSTG-RESILIENCE-1**: Multi-layered environment detection.

## Technical Stack
- **Kotlin & Jetpack Compose**: Modern, reactive UI.
- **Dagger Hilt**: Dependency injection for clean, testable architecture.
- **AndroidX Security & Biometric**: Uses AndroidX security APIs backed by platform keystore where available.
- **OkHttp**: Configured with Certificate Pinning for network resilience.

## Limitations
1. **OS Compromise**: Sophisticated kernel-level spyware may bypass user-space integrity checks.
2. **User Password**: The security of portable backups is bound by the complexity of the user's chosen password.
3. **Physical Coercion**: Biometric controls do not protect against a user being physically forced to unlock the device.

---
