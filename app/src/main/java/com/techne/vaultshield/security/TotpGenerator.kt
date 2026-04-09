package com.techne.vaultshield.security

import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow
import org.apache.commons.codec.binary.Base32

object TotpGenerator {
    fun generateTotp(
        secret: String,
        period: Int = 30,
        digits: Int = 6,
        algorithm: String = "SHA1"
    ): String {
        val counter = System.currentTimeMillis() / 1000 / period
        return generateTotpInternal(secret, counter, digits, algorithm)
    }

    private fun generateTotpInternal(
        secret: String,
        counter: Long,
        digits: Int,
        algorithm: String
    ): String {
        val keyBytes = try {
            Base32().decode(secret.uppercase())
        } catch (e: Exception) {
            return "000000"
        }

        val data = ByteBuffer.allocate(8).putLong(counter).array()
        val hmacAlgo = when (algorithm.uppercase()) {
            "SHA256" -> "HmacSHA256"
            "SHA512" -> "HmacSHA512"
            else -> "HmacSHA1"
        }

        val mac = Mac.getInstance(hmacAlgo)
        mac.init(SecretKeySpec(keyBytes, hmacAlgo))
        val hash = mac.doFinal(data)

        val offset = hash[hash.size - 1].toInt() and 0xf
        val truncatedHash = ByteBuffer.wrap(hash, offset, 4).int and 0x7fffffff

        val otp = truncatedHash % 10.0.pow(digits.toDouble()).toLong()
        return otp.toString().padStart(digits, '0')
    }
}
