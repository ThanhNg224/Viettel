package com.example.viettel.feature.identity.domain.entity

import com.example.viettel.feature.identity.domain.entity.PortraitAction

data class DocumentSession(
    val documentType: DocumentType = DocumentType.CCCD,
    val frontImage: ByteArray? = null,
    val frontImageRotation: Int = 0,
    val backImage: ByteArray? = null,
    val backImageRotation: Int = 0,
    val mrzData: MrzData? = null,
    val eidData: EidData? = null,
    val portraitActions: Map<PortraitAction, ByteArray> = emptyMap(),
    val signature: ByteArray? = null,
) {
    fun isMrzReady(): Boolean = mrzData != null
    fun hasPortrait(action: PortraitAction): Boolean = portraitActions.containsKey(action)
}
