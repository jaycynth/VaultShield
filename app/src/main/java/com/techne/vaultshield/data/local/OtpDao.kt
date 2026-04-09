package com.techne.vaultshield.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OtpDao {
    @Query("SELECT * FROM otp_accounts")
    fun getAllAccounts(): Flow<List<OtpEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: OtpEntity)

    @Query("DELETE FROM otp_accounts WHERE id = :id")
    suspend fun deleteAccount(id: String)
}
