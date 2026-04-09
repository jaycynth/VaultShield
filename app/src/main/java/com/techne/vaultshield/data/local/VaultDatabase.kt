package com.techne.vaultshield.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [OtpEntity::class], version = 1, exportSchema = false)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun otpDao(): OtpDao
}
