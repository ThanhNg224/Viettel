package com.example.viettel.di

import com.example.viettel.feature.feedback.data.repository.FeedbackRepositoryImpl
import com.example.viettel.feature.feedback.domain.repository.FeedbackRepository
import com.example.viettel.feature.identity.data.repository.EidRepositoryImpl
import com.example.viettel.feature.identity.data.repository.FaceDetectionRepositoryImpl
import com.example.viettel.feature.identity.data.repository.FaceMatchRepositoryImpl
import com.example.viettel.feature.identity.data.repository.MrzRepositoryImpl
import com.example.viettel.feature.identity.domain.repository.EidRepository
import com.example.viettel.feature.identity.domain.repository.FaceDetectionRepository
import com.example.viettel.feature.identity.domain.repository.FaceMatchRepository
import com.example.viettel.feature.identity.domain.repository.MrzRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFeedbackRepository(
        impl: FeedbackRepositoryImpl
    ): FeedbackRepository

    @Binds
    @Singleton
    abstract fun bindEidRepository(
        impl: EidRepositoryImpl
    ): EidRepository

    @Binds
    @Singleton
    abstract fun bindMrzRepository(
        impl: MrzRepositoryImpl
    ): MrzRepository

    @Binds
    @Singleton
    abstract fun bindFaceMatchRepository(
        impl: FaceMatchRepositoryImpl
    ): FaceMatchRepository

    @Binds
    @Singleton
    abstract fun bindFaceDetectionRepository(
        impl: FaceDetectionRepositoryImpl
    ): FaceDetectionRepository
}

