package com.techne.vaultshield.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.techne.vaultshield.domain.model.OtpAccount

@Entity(tableName = "otp_accounts")
data class OtpEntity(
    @field:PrimaryKey val id: String,
    val issuer: String,
    val accountName: String,
    val digits: Int,
    val period: Int,
    val algorithm: String
) {
    fun toDomain(secret: String): OtpAccount {
        return OtpAccount(
            id = id,
            issuer = issuer,
            accountName = accountName,
            secret = secret,
            digits = digits,
            period = period,
            algorithm = algorithm
        )
    }

    companion object {
        fun fromDomain(account: OtpAccount): OtpEntity {
            return OtpEntity(
                id = account.id,
                issuer = account.issuer,
                accountName = account.accountName,
                digits = account.digits,
                period = account.period,
                algorithm = account.algorithm
            )
        }
    }
}
