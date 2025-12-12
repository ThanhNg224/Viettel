package com.example.viettel.di

import com.example.viettel.feature.identity.data.repository.DocumentSessionRepositoryImpl
import com.example.viettel.feature.identity.domain.repository.DocumentSessionRepository
import com.example.viettel.feature.payment.data.repository.PaymentRepositoryImpl
import com.example.viettel.feature.payment.domain.repository.PaymentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class RetainedRepositoryModule {

    @Binds
    @ActivityRetainedScoped
    abstract fun bindDocumentSessionRepository(
        impl: DocumentSessionRepositoryImpl
    ): DocumentSessionRepository

    @Binds
    @ActivityRetainedScoped
    abstract fun bindPaymentRepository(
        impl: PaymentRepositoryImpl
    ): PaymentRepository
}

