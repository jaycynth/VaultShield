# Test Plan: VaultShield Security & Functionality

This document outlines the testing strategy for VaultShield to ensure both cryptographic correctness and security control efficacy.

## 1. Unit Testing (Cryptographic Logic)
- **TOTP Generation**: Validate against RFC 6238 test vectors.
- **Base32 Decoding**: Ensure robust handling of user input and padding.
- **Counter Calculation**: Verify time-step transitions.

## 2. Security Control Testing (Manual & Automated)

### Authentication Flow
- [ ] **Biometric Prompt**: Verify that the vault remains locked if biometric auth is cancelled or fails.
- [ ] **Session Timeout**: Verify that the app locks itself after 5 minutes of inactivity.
- [ ] **App Backgrounding**: Ensure the vault relocks upon resume (depending on policy).

### Data Protection
- [ ] **Encrypted Storage**: Attempt to read the `secure_otp_prefs.xml` file on a rooted device and verify that keys and values are ciphertext.
- [ ] **Memory Safety**: Verify (via Profiler) that secrets are not held in memory longer than necessary.

### Tamper Resistance
- [ ] **Root Detection**: Run on a rooted device/Magisk and verify the security banner appears.
- [ ] **Emulator Detection**: Run on standard AVD and Genymotion to verify detection.
- [ ] **Debugger Detection**: Attempt to attach a debugger and verify the app detects it.

### UI Security
- [ ] **Screenshot Prevention**: Attempt to take a screenshot (`Power + Vol Down`) and verify it is blocked.
- [ ] **Task Switcher**: Verify that the app preview is blank/hidden in the recent apps screen.

### Clipboard Security
- [ ] **Auto-Clear**: Copy a code, wait 30 seconds, and verify the clipboard is cleared.

## 3. Network Security
- [ ] **Certificate Pinning**: Attempt a MitM attack with a custom CA and verify that OkHttp rejects the connection.
