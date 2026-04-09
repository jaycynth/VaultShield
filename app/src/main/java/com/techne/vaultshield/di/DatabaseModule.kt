package com.techne.vaultshield.di

import android.content.Context
import androidx.room.Room
import com.techne.vaultshield.data.local.OtpDao
import com.techne.vaultshield.data.local.VaultDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VaultDatabase {
        return Room.databaseBuilder(
            context,
            VaultDatabase::class.java,
            "vault_database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideOtpDao(database: VaultDatabase): OtpDao {
        return database.otpDao()
    }
}
