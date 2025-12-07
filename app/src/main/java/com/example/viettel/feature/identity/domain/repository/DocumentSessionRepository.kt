package com.example.viettel.feature.identity.domain.repository

import com.example.viettel.feature.identity.domain.entity.DocumentSession
import com.example.viettel.feature.identity.domain.entity.DocumentType
import com.example.viettel.feature.identity.domain.entity.EidData
import com.example.viettel.feature.identity.domain.entity.MrzData
import com.example.viettel.feature.identity.domain.entity.PortraitAction

interface DocumentSessionRepository {
    fun updateDocumentType(documentType: DocumentType)
    fun updateFrontImage(imageBytes: ByteArray, rotation: Int)
    fun updateBackImage(imageBytes: ByteArray, rotation: Int)
    fun updateMrzData(mrzData: MrzData)
    fun updateEidData(eidData: EidData)
    fun updatePortrait(action: PortraitAction, imageBytes: ByteArray)
    fun updateSignature(signatureBytes: ByteArray)

    fun getSession(): DocumentSession
    fun clearPortraits()
}
