package com.example.viettel.fragments

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.viettel.R
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.viettel.activities.MainActivity

class DocumentSelectionFragment : Fragment() {

    private var stepDistance: Float = 0f
    private val totalSteps = 8

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_document_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val optionCCCD = view.findViewById<ConstraintLayout>(R.id.option1)
        val optionPassport = view.findViewById<ConstraintLayout>(R.id.option2)

        val progressLine = view.findViewById<View>(R.id.progressLine)
        val progressBarContainer = view.findViewById<View>(R.id.progressBarContainer)
        val imageOption1 = optionCCCD.findViewById<ImageView>(R.id.imageOption1)
        val imageOption2 = optionPassport.findViewById<ImageView>(R.id.imageOption2)

        progressBarContainer.post {
            stepDistance = progressBarContainer.width.toFloat() / totalSteps
            val width = (stepDistance * 1).toInt()
            val layoutParams = progressLine.layoutParams
            ValueAnimator.ofInt(0, width).apply {
                duration = 400
                addUpdateListener {
                    layoutParams.width = it.animatedValue as Int
                    progressLine.layoutParams = layoutParams
                }
                start()
            }
        }

        optionCCCD.setOnClickListener {
            imageOption1.setBackgroundResource(R.drawable.red_circle)
            imageOption2.setBackgroundResource(R.drawable.white_circle)

            Toast.makeText(requireContext(), "CCCD selected", Toast.LENGTH_SHORT).show()

            val fragment = PlaceDocumentFragment().apply {
                arguments = Bundle().apply {
                    putString("docType", "cccd")
                }
            }

            (activity as? MainActivity)?.replaceFragment(fragment)
        }

        optionPassport.setOnClickListener {
            imageOption1.setBackgroundResource(R.drawable.white_circle)
            imageOption2.setBackgroundResource(R.drawable.red_circle)

            Toast.makeText(requireContext(), "Passport selected", Toast.LENGTH_SHORT).show()

            val fragment = PlaceDocumentFragment().apply {
                arguments = Bundle().apply {
                    putString("docType", "passport")
                }
            }

            (activity as? MainActivity)?.replaceFragment(fragment)
        }

    }
}
