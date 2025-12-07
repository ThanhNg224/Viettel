package com.example.viettel.feature.identity.domain.usecase

import com.example.viettel.feature.identity.domain.entity.FaceAttributes
import com.example.viettel.feature.identity.domain.entity.PortraitAction

class EvaluateLivenessStepUseCase {

    fun instructions(): List<String> = listOf(
        "Hay cuoi voi may anh",
        "Hay nham mat",
        "Quay dau sang trai",
        "Quay dau sang phai",
    )

    fun actions(): List<PortraitAction> = listOf(
        PortraitAction.SMILE,
        PortraitAction.BLINK,
        PortraitAction.TURN_LEFT,
        PortraitAction.TURN_RIGHT,
    )

    fun isStepPassed(action: PortraitAction, attrs: FaceAttributes): Boolean = when (action) {
        PortraitAction.SMILE -> attrs.smilingProbability > 0.4f
        PortraitAction.BLINK -> attrs.leftEyeOpenProbability < 0.3f && attrs.rightEyeOpenProbability < 0.3f
        PortraitAction.TURN_LEFT -> attrs.headYaw < -15f
        PortraitAction.TURN_RIGHT -> attrs.headYaw > 15f
    }
}
