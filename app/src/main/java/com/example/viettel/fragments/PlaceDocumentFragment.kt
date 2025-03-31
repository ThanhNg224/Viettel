package com.example.viettel.fragments

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.viettel.R

class PlaceDocumentFragment : Fragment(R.layout.fragment_place_document) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val progressLine = view.findViewById<View>(R.id.progressLine)
        val progressBarContainer = view.findViewById<View>(R.id.progressBarContainer)

        progressBarContainer.post {
            val totalSteps = 8
            val stepDistance = progressBarContainer.width.toFloat() / totalSteps
            val newWidth = (stepDistance * 2).toInt()
            progressLine.layoutParams.width = 0
            val layoutParams = progressLine.layoutParams

            ValueAnimator.ofInt(0, newWidth).apply {
                duration = 400
                addUpdateListener { anim ->
                    layoutParams.width = anim.animatedValue as Int
                    progressLine.layoutParams = layoutParams
                }
                start()
            }
        }
    }
}
