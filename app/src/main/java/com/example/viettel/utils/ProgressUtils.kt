package com.example.viettel.utils

import android.animation.ValueAnimator
import android.view.View
import com.example.viettel.R

object ProgressUtils {
    fun animateProgressToStep(view: View, toStep: Int, totalSteps: Int = 8, duration: Long = 400) {
        val progressLine = view.findViewById<View>(R.id.progressLine)
        val container = view.findViewById<View>(R.id.progressBarContainer)

        container?.post {
            val stepDistance = container.width.toFloat() / totalSteps
            val fromWidth = (stepDistance * (toStep - 1)).toInt()
            val toWidth = (stepDistance * toStep).toInt()

            val lp = progressLine.layoutParams
            lp.width = fromWidth
            progressLine.layoutParams = lp

            ValueAnimator.ofInt(fromWidth, toWidth).apply {
                this.duration = duration
                addUpdateListener { anim ->
                    lp.width = anim.animatedValue as Int
                    progressLine.layoutParams = lp
                }
                start()
            }
        }
    }
}
