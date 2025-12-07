package com.example.viettel.di

import com.example.viettel.feature.feedback.data.repository.FeedbackRepositoryImpl
import com.example.viettel.feature.feedback.domain.repository.FeedbackRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module cung cấp các dependency cho feature feedback.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class FeedbackModule {

    @Binds
    @Singleton
    abstract fun bindFeedbackRepository(
        impl: FeedbackRepositoryImpl
    ): FeedbackRepository
}

