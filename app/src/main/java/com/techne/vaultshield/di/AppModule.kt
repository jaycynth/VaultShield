package com.techne.vaultshield.di

import com.techne.vaultshield.data.audit.AuditLogRepositoryImpl
import com.techne.vaultshield.data.repository.OtpRepository
import com.techne.vaultshield.data.repository.OtpRepositoryImpl
import com.techne.vaultshield.domain.audit.AuditLogRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindOtpRepository(
        otpRepositoryImpl: OtpRepositoryImpl
    ): OtpRepository

    @Binds
    @Singleton
    abstract fun bindAuditLogRepository(
        auditLogRepositoryImpl: AuditLogRepositoryImpl
    ): AuditLogRepository
}
