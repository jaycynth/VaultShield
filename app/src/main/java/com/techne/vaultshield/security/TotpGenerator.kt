package com.techne.vaultshield.security

import org.apache.commons.codec.binary.Base32
import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow

object TotpGenerator {
    /**
     * Generates a TOTP code for the current time.
     */
    fun generateTotp(
        secret: String,
        period: Int = 30,
        digits: Int = 6,
        algorithm: String = "SHA1"
    ): String {
        val counter = System.currentTimeMillis() / 1000 / period
        return generateTotpInternal(secret, counter, digits, algorithm)
    }

    /**
     * Generates a TOTP code for a specific counter value.
     * Exposed for unit testing with RFC test vectors.
     */
    fun generateTotpForCounter(
        secret: String,
        counter: Long,
        digits: Int = 6,
        algorithm: String = "SHA1"
    ): String {
        return generateTotpInternal(secret, counter, digits, algorithm)
    }

    private fun generateTotpInternal(
        secret: String,
        counter: Long,
        digits: Int,
        algorithm: String
    ): String {
        val normalizedSecret = secret.replace(" ", "").uppercase()
        
        // Strict Base32 validation to prevent accidental decoding of invalid secrets
        if (!normalizedSecret.matches(Regex("^[A-Z2-7]+=*$"))) {
            return "0".repeat(digits)
        }

        val keyBytes = try {
            Base32().decode(normalizedSecret)
        } catch (e: Exception) {
            null
        }

        if (keyBytes == null || keyBytes.isEmpty()) return "0".repeat(digits)

        val data = ByteBuffer.allocate(8).putLong(counter).array()
        val hmacAlgo = when (algorithm.uppercase()) {
            "SHA256" -> "HmacSHA256"
            "SHA512" -> "HmacSHA512"
            else -> "HmacSHA1"
        }

        return try {
            val mac = Mac.getInstance(hmacAlgo)
            mac.init(SecretKeySpec(keyBytes, hmacAlgo))
            val hash = mac.doFinal(data)

            val offset = (hash[hash.size - 1].toInt() and 0xf)
            val binary = ((hash[offset].toInt() and 0x7f) shl 24) or
                         ((hash[offset + 1].toInt() and 0xff) shl 16) or
                         ((hash[offset + 2].toInt() and 0xff) shl 8) or
                         (hash[offset + 3].toInt() and 0xff)

            val otp = binary.toLong() % 10.0.pow(digits.toDouble()).toLong()
            otp.toString().padStart(digits, '0')
        } catch (e: Exception) {
            "0".repeat(digits)
        }
    }
}
