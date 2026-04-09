# Security Architecture: VaultShield

VaultShield is designed with a **Local-First, Security-First** philosophy. This document describes the trust boundaries, data flow, and security layers.

## 1. Architectural Overview
The application follows **Clean Architecture** and **MVI (Model-View-Intent)** patterns.

- **UI (Jetpack Compose)**: modern, declarative UI.
- **Domain**: Pure Kotlin logic defining the business rules for TOTP and Security.
- **Data**: Implementation of repositories using AndroidX Security and the Hardware Keystore.
- **Security Services**: Dedicated managers for Cryptography, Integrity, and Biometrics.

## 2. Trust Boundaries
- **System Trust Boundary**: Separates the Android OS (which manages the Hardware Keystore/TEE) from the application process.
- **Sandbox Boundary**: Standard Android isolation preventing other apps from accessing VaultShield's data.
- **Biometric Boundary**: Prevents sensitive data (like TOTP secrets) from being decrypted or displayed without a successful biometric challenge.

## 3. Security Layers (Defense in Depth)

### Layer 1: Data at Rest
- **Storage**: All sensitive data (secrets, logs) is stored in `EncryptedSharedPreferences`.
- **Encryption**: AES-256 GCM (Authenticated Encryption).
- **Key Management**: Master keys are generated in the Android Keystore and are hardware-backed whenever possible.

### Layer 2: Runtime Integrity
- **Environment Checks**: Performs root, emulator, and debugger detection before sensitive operations.
- **Anti-Debugger**: Detects if a debugger is attached.
- **Anti-Overlay**: Uses `setHideOverlayWindows(true)` (Android 12+) to prevent Tapjacking.

### Layer 3: Presentation Security
- **Screenshot Protection**: `FLAG_SECURE` blocks screenshots and hides content in recent tasks.
- **Selective Reveal**: Secrets remain masked until explicit biometric re-authentication.

### Layer 4: Audit Logging
- **Secure Audit Log**: Encrypted local record of all sensitive actions (Add/Delete/Reveal/Export).
- **User Transparency**: A dedicated audit screen allows users to monitor their security history.

### Layer 5: Secure Backup & Restore
- **Client-Side Encryption**: Backups are encrypted before leaving the device.
- **Key Derivation**: Uses **PBKDF2WithHmacSHA256** with 65,536 iterations and a unique salt to derive keys from a user password.
- **Payload Protection**: Uses **AES-256 GCM** for the backup payload, ensuring both confidentiality and integrity.

## 4. Cryptographic Implementation Details
- **AES-GCM**: Used for all data protection to ensure against bit-flipping attacks.
- **PBKDF2**: Configured with industry-standard iteration counts to resist brute-force attacks on exported backups.
- **Hardware Keystore**: Used to protect the master keys for the app's local storage.
