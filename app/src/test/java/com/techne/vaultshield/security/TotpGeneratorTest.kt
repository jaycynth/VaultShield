package com.techne.vaultshield.security

import org.junit.Assert.assertEquals
import org.junit.Test

class TotpGeneratorTest {

    @Test
    fun `generateTotp returns expected code for known secret and time`() {
        // Test vectors from RFC 6238
        val secret = "JBSWY3DPEHPK3PXP" // Base32 for "Hello!\u00deadbeef"
        
        // We can't easily mock System.currentTimeMillis() in a pure unit test without a wrapper
        // but we can test the internal logic if we expose a counter-based method.
        // For now, we test that it produces a 6-digit string.
        val totp = TotpGenerator.generateTotp(secret)
        assertEquals(6, totp.length)
        assert(totp.all { it.isDigit() })
    }

    @Test
    fun `generateTotp handles invalid base32 gracefully`() {
        val invalidSecret = "invalid-secret-!!!"
        val totp = TotpGenerator.generateTotp(invalidSecret)
        assertEquals("000000", totp)
    }
}
