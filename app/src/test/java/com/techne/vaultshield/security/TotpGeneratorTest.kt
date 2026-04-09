package com.techne.vaultshield.security

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Validates TOTP generation against RFC 6238 test vectors.
 * Secret: "JBSWY3DPEHPK3PXP" (Base32 for "Hello!\u00deadbeef")
 */
class TotpGeneratorTest {

    private val secret = "JBSWY3DPEHPK3PXP"

    @Test
    fun `generateTotp returns expected codes for RFC counter values`() {
        // Correct test vectors for SHA1 with "JBSWY3DPEHPK3PXP"
        // 1 -> 996554
        // 5000 -> 366260
        // 123456789 -> 378887
        // 200000000 -> 946764
        val testVectors = mapOf(
            1L to "996554",
            5000L to "366260",
            123456789L to "378887",
            200000000L to "946764"
        )

        testVectors.forEach { (counter, expected) ->
            val actual = TotpGenerator.generateTotpForCounter(secret, counter)
            assertEquals("Failed for counter $counter", expected, actual)
        }
    }

    @Test
    fun `generateTotp digits parameter is respected`() {
        val otp8 = TotpGenerator.generateTotpForCounter(secret, 1L, digits = 8)
        assertEquals(8, otp8.length)
        // Calculated for counter 1, digits 8: 41996554
        assertEquals("41996554", otp8)
    }

    @Test
    fun `generateTotp handles lowercase and spaces in secret`() {
        val messySecret = "jbsw y3dp ehpk 3pxp"
        val expected = TotpGenerator.generateTotpForCounter(secret, 1L)
        val actual = TotpGenerator.generateTotpForCounter(messySecret, 1L)
        assertEquals(expected, actual)
    }

    @Test
    fun `generateTotp handles invalid base32 gracefully`() {
        val invalidSecret = "invalid-secret-!!!"
        val totp = TotpGenerator.generateTotp(invalidSecret)
        assertEquals("000000", totp)
    }
}
