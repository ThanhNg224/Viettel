package com.example.viettel.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.viettel.feature.identity.data.repository.DocumentSessionRepositoryImpl
import com.example.viettel.feature.identity.data.repository.EidRepositoryImpl
import com.example.viettel.feature.identity.data.repository.FaceDetectionRepositoryImpl
import com.example.viettel.feature.identity.data.repository.FaceMatchRepositoryImpl
import com.example.viettel.feature.identity.data.repository.MrzRepositoryImpl
import com.example.viettel.feature.identity.domain.usecase.ComparePortraitUseCase
import com.example.viettel.feature.identity.domain.usecase.DetectFaceUseCase
import com.example.viettel.feature.identity.domain.usecase.EvaluateLivenessStepUseCase
import com.example.viettel.feature.identity.domain.usecase.ExtractMrzUseCase
import com.example.viettel.feature.identity.domain.usecase.GetDocumentSessionUseCase
import com.example.viettel.feature.identity.domain.usecase.ReadEidUseCase
import com.example.viettel.feature.identity.domain.usecase.SaveBackImageUseCase
import com.example.viettel.feature.identity.domain.usecase.SaveDocumentTypeUseCase
import com.example.viettel.feature.identity.domain.usecase.SaveFrontImageUseCase
import com.example.viettel.feature.identity.domain.usecase.SavePortraitUseCase
import com.example.viettel.feature.identity.domain.usecase.SaveSignatureUseCase
import com.example.viettel.feature.identity.integration.eid.EidReaderDataSource
import com.example.viettel.feature.identity.integration.facematch.FaceMatchRemoteDataSource
import com.example.viettel.feature.identity.integration.face.FaceDetectionDataSource
import com.example.viettel.feature.identity.integration.ocr.OcrMrzDataSource
import com.example.viettel.feature.identity.presentation.viewmodel.IdentityViewModel

class IdentityViewModelFactory(
    private val appContext: Application,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val sessionRepository = DocumentSessionRepositoryImpl()
        val mrzRepository = MrzRepositoryImpl(OcrMrzDataSource())
        val eidRepository = EidRepositoryImpl(EidReaderDataSource(appContext))
        val faceMatchRepository = FaceMatchRepositoryImpl(FaceMatchRemoteDataSource())
        val faceDetectionRepository = FaceDetectionRepositoryImpl(FaceDetectionDataSource())

        val saveDocumentTypeUseCase = SaveDocumentTypeUseCase(sessionRepository)
        val saveFrontImageUseCase = SaveFrontImageUseCase(sessionRepository)
        val saveBackImageUseCase = SaveBackImageUseCase(sessionRepository)
        val extractMrzUseCase = ExtractMrzUseCase(mrzRepository, sessionRepository)
        val readEidUseCase = ReadEidUseCase(eidRepository, sessionRepository)
        val savePortraitUseCase = SavePortraitUseCase(sessionRepository)
        val saveSignatureUseCase = SaveSignatureUseCase(sessionRepository)
        val getSessionUseCase = GetDocumentSessionUseCase(sessionRepository)
        val comparePortraitUseCase = ComparePortraitUseCase(faceMatchRepository)
        val detectFaceUseCase = DetectFaceUseCase(faceDetectionRepository)
        val evaluateLivenessStepUseCase = EvaluateLivenessStepUseCase()

        return IdentityViewModel(
            saveDocumentTypeUseCase,
            saveFrontImageUseCase,
            saveBackImageUseCase,
            extractMrzUseCase,
            readEidUseCase,
            savePortraitUseCase,
            saveSignatureUseCase,
            getSessionUseCase,
            comparePortraitUseCase,
            detectFaceUseCase,
            evaluateLivenessStepUseCase,
        ) as T
    }
}

