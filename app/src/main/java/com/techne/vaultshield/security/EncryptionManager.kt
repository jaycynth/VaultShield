package com.techne.vaultshield.security

import org.apache.commons.codec.binary.Base64
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Handles end-to-end encryption for backups.
 * Demonstrates secure key derivation (PBKDF2) and authenticated encryption (AES-GCM).
 */
object EncryptionManager {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BIT = 128
    private const val IV_LENGTH_BYTE = 12
    private const val SALT_LENGTH_BYTE = 16
    private const val ITERATIONS = 65536
    private const val KEY_LENGTH_BIT = 256

    /**
     * Encrypts data using a password-derived key.
     * Returns a Base64 string containing: Salt + IV + Ciphertext
     */
    fun encryptWithPassword(data: String, password: CharArray): String {
        val salt = ByteArray(SALT_LENGTH_BYTE).apply { SecureRandom().nextBytes(this) }
        val secretKey = deriveKey(password, salt)
        
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        
        val combined = salt + iv + encryptedData
        return Base64.encodeBase64String(combined)
    }

    /**
     * Decrypts data using a password-derived key.
     */
    fun decryptWithPassword(encryptedBase64: String, password: CharArray): String {
        val combined = Base64.decodeBase64(encryptedBase64)
        
        val salt = combined.sliceArray(0 until SALT_LENGTH_BYTE)
        val iv = combined.sliceArray(SALT_LENGTH_BYTE until SALT_LENGTH_BYTE + IV_LENGTH_BYTE)
        val encryptedData = combined.sliceArray(SALT_LENGTH_BYTE + IV_LENGTH_BYTE until combined.size)

        val secretKey = deriveKey(password, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        val spec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        
        return String(cipher.doFinal(encryptedData), Charsets.UTF_8)
    }

    private fun deriveKey(password: CharArray, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec: KeySpec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH_BIT)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }

    fun generateRandomKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        return keyGenerator.generateKey()
    }
}
