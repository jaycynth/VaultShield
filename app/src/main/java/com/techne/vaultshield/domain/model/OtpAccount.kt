package com.techne.vaultshield.domain.model

data class OtpAccount(
    val id: String,
    val issuer: String,
    val accountName: String,
    val secret: String,
    val digits: Int = 6,
    val period: Int = 30,
    val algorithm: String = "SHA1",
    val isRevealed: Boolean = false // Track if the code is currently visible to the user
)
