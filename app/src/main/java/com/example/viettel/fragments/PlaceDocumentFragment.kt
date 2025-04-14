package com.example.viettel.fragments


import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.viettel.R
import com.example.viettel.utils.ProgressUtils


class PlaceDocumentFragment : Fragment(R.layout.fragment_place_document) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val docType = arguments?.getString("docType") ?: "cccd"  // default fallback
        val imgIllustration = view.findViewById<ImageView>(R.id.imgIllustration)

        // 🔄 Swap image based on docType
        when (docType) {
            "cccd" -> imgIllustration.setImageResource(R.drawable.ic_cccd_placement)
            "passport" -> imgIllustration.setImageResource(R.drawable.ic_passport_placement)
        }




        ProgressUtils.animateProgressToStep(view, 2)

    }
}


