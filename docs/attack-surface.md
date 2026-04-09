# Attack Surface Analysis: VaultShield

This document describes the potential entry points and attack vectors for VaultShield.

## 1. Local Storage (At Rest)
- **Shared Preferences**: Where encrypted TOTP seeds and audit logs are stored.
  - *Risk*: A root user or physical dump might extract the `secure_otp_prefs.xml`.
  - *Mitigation*: Authenticated encryption (AES-256 GCM) with keys stored in the Android Keystore (Hardware-backed TEE).

## 2. User Interface (Input/Output)
- **Manual Seed Entry**: Keylogging or shoulder surfing during entry.
  - *Mitigation*: Biometric re-authentication before sensitive state transitions.
- **QR Code Scanning**: Malicious QR codes containing invalid or exploit-laden URIs.
  - *Mitigation*: Strict `otpauth://` URI validation and schema enforcement.
- **Screen Display**: Malicious screen scrapers or accessibility services reading codes.
  - *Mitigation*: `FLAG_SECURE` to block screenshots and screen recording. `HIDE_OVERLAY_WINDOWS` to prevent tapjacking.

## 3. Operating System / Runtime
- **Compromised Environment**: Running on a rooted device or emulator with hooking tools (Frida, Xposed).
  - *Mitigation*: Multi-layered integrity checks (RootBeer, build-prop checks, debugger detection).
- **Inter-Process Communication (IPC)**: Intent sniffing or spoofing.
  - *Mitigation*: No exported activities except for the main launcher. Strict permissions on internal providers.

## 4. Cryptographic Operations
- **Key Derivation (Backup)**: Weak password leading to brute-force of backup files.
  - *Mitigation*: PBKDF2 with 65,536 iterations and a unique salt.
- **HMAC Timing Attacks**: Inferring secrets from the time taken to generate a code.
  - *Mitigation*: Standard library implementations typically use constant-time operations for HMAC.

## 5. Network (Future Sync)
- **MitM Attack**: Intercepting traffic to a backup/sync server.
  - *Mitigation*: Certificate Pinning with OkHttp.
