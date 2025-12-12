package com.example.viettel.feature.identity.data.repository

import com.example.viettel.feature.identity.domain.entity.DocumentSession
import com.example.viettel.feature.identity.domain.entity.DocumentType
import com.example.viettel.feature.identity.domain.entity.EidData
import com.example.viettel.feature.identity.domain.entity.MrzData
import com.example.viettel.feature.identity.domain.repository.DocumentSessionRepository
import com.example.viettel.feature.identity.domain.entity.PortraitAction
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

class DocumentSessionRepositoryImpl @Inject constructor() : DocumentSessionRepository {

    private val cache = AtomicReference(DocumentSession())

    override fun updateDocumentType(documentType: DocumentType) {
        cache.updateAndGet { it.copy(documentType = documentType) }
    }

    override fun updateFrontImage(imageBytes: ByteArray, rotation: Int) {
        cache.updateAndGet { it.copy(frontImage = imageBytes, frontImageRotation = rotation) }
    }

    override fun updateBackImage(imageBytes: ByteArray, rotation: Int) {
        cache.updateAndGet { it.copy(backImage = imageBytes, backImageRotation = rotation) }
    }

    override fun updateMrzData(mrzData: MrzData) {
        cache.updateAndGet { it.copy(mrzData = mrzData) }
    }

    override fun updateEidData(eidData: EidData) {
        cache.updateAndGet { it.copy(eidData = eidData) }
    }

    override fun updatePortrait(action: PortraitAction, imageBytes: ByteArray) {
        cache.updateAndGet { session ->
            val updated = session.portraitActions.toMutableMap().apply { put(action, imageBytes) }
            session.copy(portraitActions = updated)
        }
    }

    override fun updateSignature(signatureBytes: ByteArray) {
        cache.updateAndGet { it.copy(signature = signatureBytes) }
    }

    override fun getSession(): DocumentSession = cache.get()

    override fun clearPortraits() {
        cache.updateAndGet { it.copy(portraitActions = emptyMap()) }
    }
}
