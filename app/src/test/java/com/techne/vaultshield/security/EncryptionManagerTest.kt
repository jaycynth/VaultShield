package com.techne.vaultshield.security

import org.apache.commons.codec.binary.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Validates the client-side encrypted backup flow.
 * Tests PBKDF2 key derivation and AES-GCM authenticated encryption.
 */
class EncryptionManagerTest {

    private val testData = "{\"secret\": \"JBSWY3DPEHPK3PXP\", \"issuer\": \"VaultShield\"}"
    private val password = "strong-security-password".toCharArray()

    @Test
    fun `encryption and decryption cycle returns original data`() {
        val encrypted = EncryptionManager.encryptWithPassword(testData, password)
        val decrypted = EncryptionManager.decryptWithPassword(encrypted, password)
        
        assertEquals(testData, decrypted)
    }

    @Test
    fun `different salt results in different ciphertext for same data and password`() {
        val encrypted1 = EncryptionManager.encryptWithPassword(testData, password)
        val encrypted2 = EncryptionManager.encryptWithPassword(testData, password)
        
        // Since EncryptionManager uses SecureRandom for salt and IV, outputs should differ
        assertNotEquals(encrypted1, encrypted2)
    }

    @Test(expected = Exception::class)
    fun `decryption fails with wrong password`() {
        val encrypted = EncryptionManager.encryptWithPassword(testData, password)
        val wrongPassword = "wrong-password".toCharArray()
        
        EncryptionManager.decryptWithPassword(encrypted, wrongPassword)
    }

    @Test
    fun `tampering with ciphertext triggers integrity failure`() {
        val encrypted = EncryptionManager.encryptWithPassword(testData, password)
        
        // Tamper with the ciphertext part (last few bytes)
        val tamperedBytes = Base64.decodeBase64(encrypted)
        tamperedBytes[tamperedBytes.size - 1] = (tamperedBytes[tamperedBytes.size - 1] + 1).toByte()
        val tamperedEncrypted = Base64.encodeBase64String(tamperedBytes)

        try {
            EncryptionManager.decryptWithPassword(tamperedEncrypted, password)
            assert(false) { "Decryption should have failed due to GCM tag mismatch" }
        } catch (e: Exception) {
            // Expected - AES-GCM provides integrity
            assert(true)
        }
    }
}
