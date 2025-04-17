package com.example.viettel.utils

import android.animation.ValueAnimator
import android.view.View
import com.example.viettel.R

object ProgressUtils {
    fun animateProgressToStep(
        view: View,
        toStep: Int,
        totalSteps: Int = 8,
        duration: Long = 1000L
    ) {
        val progressLine = view.findViewById<View>(R.id.progressLine)
        val container = view.findViewById<View>(R.id.progressBarContainer)

        container?.post {
            val stepDistance = container.width.toFloat() / totalSteps
            val fromWidth = (stepDistance * (toStep - 1)).toInt()
            val toWidth = (stepDistance * toStep).toInt()

            // 🧼 Set initial width to toStep - 1 BEFORE animation starts (no full flash)
            progressLine.layoutParams = progressLine.layoutParams.apply {
                width = fromWidth
            }
            progressLine.requestLayout()

            // ✅ Now animate smoothly from (toStep - 1) to toStep
            ValueAnimator.ofInt(fromWidth, toWidth).apply {
                this.duration = duration
                addUpdateListener { anim ->
                    progressLine.layoutParams = progressLine.layoutParams.apply {
                        width = anim.animatedValue as Int
                    }
                    progressLine.requestLayout()
                }
                start()
            }
        }
    }
}
