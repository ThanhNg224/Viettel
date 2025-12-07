package com.example.viettel.feature.identity.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.viettel.feature.identity.data.repository.DocumentSessionRepositoryImpl
import com.example.viettel.feature.identity.data.repository.EidRepositoryImpl
import com.example.viettel.feature.identity.data.repository.FaceDetectionRepositoryImpl
import com.example.viettel.feature.identity.data.repository.FaceMatchRepositoryImpl
import com.example.viettel.feature.identity.data.repository.MrzRepositoryImpl
import com.example.viettel.feature.identity.domain.entity.CapturedImage
import com.example.viettel.feature.identity.domain.entity.DocumentType
import com.example.viettel.feature.identity.domain.entity.EidData
import com.example.viettel.feature.identity.domain.entity.ImageFrame
import com.example.viettel.feature.identity.domain.entity.MrzData
import com.example.viettel.feature.identity.domain.entity.PortraitAction
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class IdentityViewModel(
    private val saveDocumentTypeUseCase: SaveDocumentTypeUseCase,
    private val saveFrontImageUseCase: SaveFrontImageUseCase,
    private val saveBackImageUseCase: SaveBackImageUseCase,
    private val extractMrzUseCase: ExtractMrzUseCase,
    private val readEidUseCase: ReadEidUseCase,
    private val savePortraitUseCase: SavePortraitUseCase,
    private val saveSignatureUseCase: SaveSignatureUseCase,
    private val getDocumentSessionUseCase: GetDocumentSessionUseCase,
    private val comparePortraitUseCase: ComparePortraitUseCase,
    private val detectFaceUseCase: DetectFaceUseCase,
    private val evaluateLivenessStepUseCase: EvaluateLivenessStepUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentUiState())
    val uiState: StateFlow<DocumentUiState> = _uiState

    private val livenessActions = evaluateLivenessStepUseCase.actions()
    private val livenessInstructions = evaluateLivenessStepUseCase.instructions()

    init {
        _uiState.value = _uiState.value.copy(
            livenessTotalSteps = livenessActions.size,
            livenessInstruction = livenessInstructions.firstOrNull().orEmpty(),
        )
    }

    fun selectDocumentType(documentType: DocumentType) {
        saveDocumentTypeUseCase(documentType)
        refreshState()
    }

    fun onFrontCaptured(imageBytes: ByteArray, rotation: Int) {
        saveFrontImageUseCase(imageBytes, rotation)
        refreshState()
    }

    fun onBackCaptured(imageBytes: ByteArray, rotation: Int) {
        saveBackImageUseCase(imageBytes, rotation)
        refreshState()
    }

    fun extractMrz(imageBytes: ByteArray, rotation: Int) {
        viewModelScope.launch {
            setLoading(true)
            val result = extractMrzUseCase(CapturedImage(imageBytes, rotation))
            val error = result.exceptionOrNull()?.localizedMessage
            refreshState(errorMessage = error, isLoading = false)
        }
    }

    fun readEidFromChip() {
        viewModelScope.launch {
            setLoading(true)
            val result = readEidUseCase()
            val error = result.exceptionOrNull()?.localizedMessage
            refreshState(errorMessage = error, isLoading = false)
        }
    }

    fun savePortrait(action: PortraitAction, imageBytes: ByteArray) {
        savePortraitUseCase(action, imageBytes)
        refreshState()
    }

    fun saveSignature(signatureBytes: ByteArray) {
        saveSignatureUseCase(signatureBytes)
        refreshState()
    }

    fun comparePortraits() {
        val session = getDocumentSessionUseCase()
        val smile = session.portraitActions[PortraitAction.SMILE]
        val chip = session.eidData?.chipPortrait

        if (smile == null || chip == null) {
            refreshState(errorMessage = "Missing portraits for comparison")
            return
        }

        viewModelScope.launch {
            setLoading(true)
            val result = comparePortraitUseCase(smile, chip)
            val error = result.exceptionOrNull()?.localizedMessage
            refreshState(
                errorMessage = error,
                isLoading = false,
                faceMatchScore = result.getOrNull()
            )
        }
    }

    fun onLivenessImageCaptured(frame: ImageFrame, portraitJpeg: ByteArray?) {
        val currentState = _uiState.value
        val action = livenessActions.getOrNull(currentState.livenessStepIndex) ?: return
        viewModelScope.launch {
            setLoading(true)
            val detection = detectFaceUseCase(frame)
            val attrs = detection.getOrNull()
            if (attrs == null) {
                refreshState(
                    errorMessage = detection.exceptionOrNull()?.localizedMessage,
                    isLoading = false,
                    livenessUpdate = LivenessUpdate(
                        stepIndex = currentState.livenessStepIndex,
                        completed = false,
                        lastSuccess = false,
                        lastMessage = detection.exceptionOrNull()?.localizedMessage ?: "Khong thay khuon mat",
                        eventId = currentState.livenessEventId + 1,
                    )
                )
                return@launch
            }

            val passed = evaluateLivenessStepUseCase.isStepPassed(action, attrs)
            val nextIndex = if (passed) currentState.livenessStepIndex + 1 else currentState.livenessStepIndex
            val completed = nextIndex >= livenessActions.size

            if (passed && action == PortraitAction.SMILE && portraitJpeg != null) {
                savePortraitUseCase(PortraitAction.SMILE, portraitJpeg)
            }

            refreshState(
                isLoading = false,
                livenessUpdate = LivenessUpdate(
                    stepIndex = nextIndex.coerceAtMost(livenessActions.lastIndex),
                    completed = completed,
                    lastSuccess = passed,
                    lastMessage = if (passed) "Buoc ${currentState.livenessStepIndex + 1} hoan thanh" else "Thu lai buoc ${currentState.livenessStepIndex + 1}",
                    eventId = currentState.livenessEventId + 1,
                )
            )
        }
    }

    private fun setLoading(loading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = loading, errorMessage = null)
    }

    private fun refreshState(
        errorMessage: String? = null,
        isLoading: Boolean? = null,
        faceMatchScore: Double? = _uiState.value.faceMatchScore,
        livenessUpdate: LivenessUpdate? = null,
    ) {
        val session = getDocumentSessionUseCase()
        _uiState.value = DocumentUiState(
            documentType = session.documentType,
            frontImage = session.frontImage,
            frontImageRotation = session.frontImageRotation,
            backImage = session.backImage,
            backImageRotation = session.backImageRotation,
            mrzData = session.mrzData,
            eidData = session.eidData,
            portraitActions = session.portraitActions,
            signature = session.signature,
            isLoading = isLoading ?: false,
            errorMessage = errorMessage,
            faceMatchScore = faceMatchScore,
            livenessStepIndex = livenessUpdate?.stepIndex ?: _uiState.value.livenessStepIndex,
            livenessTotalSteps = livenessActions.size,
            livenessCompleted = livenessUpdate?.completed ?: _uiState.value.livenessCompleted,
            livenessInstruction = resolveInstruction(
                livenessUpdate?.stepIndex ?: _uiState.value.livenessStepIndex,
                livenessUpdate?.completed ?: _uiState.value.livenessCompleted
            ),
            lastLivenessSuccess = livenessUpdate?.lastSuccess ?: _uiState.value.lastLivenessSuccess,
            lastLivenessMessage = livenessUpdate?.lastMessage ?: _uiState.value.lastLivenessMessage,
            livenessEventId = livenessUpdate?.eventId ?: _uiState.value.livenessEventId,
        )
    }

    private fun resolveInstruction(stepIndex: Int, completed: Boolean): String {
        return if (completed) "Ban da hoan thanh cac buoc"
        else livenessInstructions.getOrNull(stepIndex).orEmpty()
    }

    data class DocumentUiState(
        val documentType: DocumentType = DocumentType.CCCD,
        val frontImage: ByteArray? = null,
        val frontImageRotation: Int = 0,
        val backImage: ByteArray? = null,
        val backImageRotation: Int = 0,
        val mrzData: MrzData? = null,
        val eidData: EidData? = null,
        val portraitActions: Map<PortraitAction, ByteArray> = emptyMap(),
        val signature: ByteArray? = null,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val faceMatchScore: Double? = null,
        val livenessStepIndex: Int = 0,
        val livenessTotalSteps: Int = 4,
        val livenessCompleted: Boolean = false,
        val livenessInstruction: String = "",
        val lastLivenessSuccess: Boolean? = null,
        val lastLivenessMessage: String? = null,
        val livenessEventId: Int = 0,
    )

    class Factory(
        private val appContext: android.app.Application,
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

    data class LivenessUpdate(
        val stepIndex: Int,
        val completed: Boolean,
        val lastSuccess: Boolean,
        val lastMessage: String?,
        val eventId: Int,
    )
}
