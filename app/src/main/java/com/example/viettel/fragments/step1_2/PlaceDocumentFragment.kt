package com.example.viettel.fragments.step1_2

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.viettel.R
import com.example.viettel.activities.MainActivity
import com.example.viettel.utils.ProgressUtils

class PlaceDocumentFragment : Fragment(R.layout.fragment_place_document) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.apply {
            findViewById<View>(R.id.btnContinue)?.visibility = View.VISIBLE
            findViewById<View>(R.id.btnBack)?.visibility = View.VISIBLE
        }

        val docType = arguments?.getString("docType") ?: "cccd"  // default fallback
        val imgIllustration = view.findViewById<ImageView>(R.id.imgIllustration)

        // 🔄 Swap image based on docType
        when (docType) {
            "cccd" -> imgIllustration.setImageResource(R.drawable.ic_cccd_placement)
            "passport" -> imgIllustration.setImageResource(R.drawable.ic_passport_placement)
        }

        ProgressUtils.animateProgressToStep(view, 2)

    }
    override fun onResume() {
        super.onResume()

        (activity as? MainActivity)?.apply {
            setBackVisible(true)
            setContinueVisible(true)
            setContinueEnabled(true)
        }
    }
}