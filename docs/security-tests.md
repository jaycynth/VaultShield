# Security Test Plan: VaultShield

This document outlines the security-focused tests designed to verify the effectiveness of VaultShield's defensive controls.

## 1. Cryptographic Verification
- **TOTP Correctness**: Validate generated codes against RFC 6238 test vectors (HMAC-SHA1).
- **Key Derivation (PBKDF2)**: Verify that changing a single character in the backup password results in a completely different derived key and failed decryption.
- **Authenticated Encryption (GCM)**: Verify that tampering with a single byte of the encrypted backup string (IV or ciphertext) causes a `BadPaddingException` or `AEADBadTagException` during decryption.

## 2. Authentication & Authorization Tests
- **Biometric Gating**: Verify that calling the "reveal" function without a successful biometric result returns an error or remains masked.
- **Session Timeout**: Manually advance the system clock or wait 5 minutes to verify the vault state transitions to "Locked".
- **Background Re-auth**: Verify that the app requires authentication when returning from the background (if configured).

## 3. Tamper Resistance Tests (Manual)
- **Root Detection**: Run the app on a rooted device (e.g., via Magisk) and verify the "Security Warning" banner is displayed.
- **Debugger Detection**: Attempt to attach the Android Studio debugger to a release build and verify the app detects the connection.
- **Emulator Detection**: Run the app on a standard AVD and verify the integrity warning triggers.

## 4. UI & Platform Security Tests
- **Screenshot Blocking**: Attempt to take a screenshot via `adb shell screencap` or physical buttons. Verify the resulting file is black or the action is blocked.
- **Recent Apps Preview**: Open the task switcher and verify the app preview is blank or shows a secure placeholder.
- **Overlay Protection**: Attempt to use a "Blue Light Filter" app or similar overlay and verify if the app blocks interaction or warns (Android 12+).

## 5. Information Leakage Tests
- **Clipboard Auto-Clear**: Copy a code, wait 31 seconds, and verify that pasting results in empty or previous clipboard content.
- **Logcat Inspection**: Verify that no TOTP secrets or decrypted codes are printed to the system logs during normal operation.
- **Memory Analysis**: Use the Memory Profiler to ensure that large secret strings are not persisted indefinitely in the heap.
