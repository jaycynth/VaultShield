# Security Architecture: VaultShield

VaultShield is built on a **Zero-Trust, Local-First** security model. This document details how we protect sensitive data using industry-standard cryptographic primitives.

## 1. Data Protection Strategy

### What we protect:
- **TOTP Seeds**: The "master keys" for 2FA accounts.
- **Audit Logs**: Secure forensic evidence of app usage.
- **Backups**: Vault data exported for recovery.

### How we protect it:

#### A. Internal Storage (At Rest)
We use **AES-256-GCM** authenticated encryption via `EncryptedSharedPreferences`. 
- **Confidentiality**: Data is unreadable without the key.
- **Integrity**: GCM (Galois/Counter Mode) ensures that if even one bit of the encrypted data is modified, decryption will fail (tag mismatch).
- **Key Isolation**: The encryption keys never leave the **Android Hardware Keystore**. On modern devices, these are stored in the **TEE (Trusted Execution Environment)** or a **StrongBox** (dedicated security chip), isolated from the main Android OS.

#### B. Secure Backups (Portable Data)
Backups are protected using a **Zero-Knowledge** encryption flow in `EncryptionManager.kt`:
1. **Key Derivation**: We don't use the user's password directly as a key. We pass it through **PBKDF2WithHmacSHA256** with **65,536 iterations** and a random **16-byte salt**. This prevents pre-computation (rainbow tables) and significantly slows down brute-force attempts.
2. **Encryption**: The derived key is used with **AES-256-GCM**.
3. **Packaging**: The resulting export contains `[Salt] + [IV] + [Ciphertext]`. This is self-contained for recovery but useless to an attacker without the password.

## 2. Trust Boundaries
- **TEE/StrongBox Boundary**: Our highest security tier. Encryption keys are generated and used inside secure hardware.
- **Application Sandbox**: Android's Linux-level isolation keeps our encrypted files private from other apps.
- **User Presence Boundary**: Sensitive operations (Reveal/Export) are gated by `BiometricPrompt`, ensuring the owner is physically present.

## 3. Data Flow for Code Generation
1. User requests a code.
2. App requests **Biometric Auth**.
3. Hardware Keystore releases the AES key to the app's memory (temporary).
4. App decrypts the TOTP Seed.
5. **HMAC-SHA1** is calculated to generate the 6-digit code.
6. The seed is wiped from memory (where possible) and the code is displayed.
