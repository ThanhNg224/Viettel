package com.example.viettel.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.viettel.R
import com.example.viettel.viewmodel.DocumentViewModel

class PortraitComparisonFragment : Fragment() {

    private val docViewModel: DocumentViewModel by activityViewModels()

    // UI references
    private lateinit var txtMatchResult: TextView
    private lateinit var imgSmilePortrait: ImageView
    private lateinit var imgChipPortrait: ImageView
    private lateinit var txtChipSuccess: TextView
    private lateinit var imgIdFront: ImageView
    private lateinit var imgTickSuccess: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // If your layout file is named fragment_portrait_comparison.xml:
        return inflater.inflate(R.layout.fragment_portrait_comparison, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Grab references
        txtMatchResult = view.findViewById(R.id.txtMatchResult)
        imgSmilePortrait = view.findViewById(R.id.imgSmilePortrait)
        imgChipPortrait = view.findViewById(R.id.imgChipPortrait)

        txtChipSuccess = view.findViewById(R.id.txtChipSuccess)
        imgIdFront = view.findViewById(R.id.imgIdFront)
        imgTickSuccess = view.findViewById(R.id.imgTickSuccess)

        // 1) Show the ID front image from ViewModel
        docViewModel.frontImage?.let { bmp ->
            imgIdFront.setImageBitmap(bmp)
        }

        // 2) Show the chip portrait + user portrait
        // Suppose docViewModel.smilePortrait is the user’s captured selfie
        docViewModel.smilePortrait?.let { userBmp ->
            imgSmilePortrait.setImageBitmap(userBmp)
        }

        docViewModel.chipPortrait?.let { chipBmp ->
            imgChipPortrait.setImageBitmap(chipBmp)
        }

        // 3) Compute or get the match percentage
        val matchPercent = computeFaceMatch() // Or docViewModel.faceMatchScore?

        // 4) Update text and color based on match
        updateMatchUI(matchPercent)

        // 5) If for some reason the chip read was not good, you can show/hide UI
        if (docViewModel.chipPortrait == null) {
            txtChipSuccess.text = "Không có ảnh chip!"
            imgTickSuccess.visibility = View.GONE
        } else {
            txtChipSuccess.text = "Đọc thông tin trên chip thành công"
            imgTickSuccess.visibility = View.VISIBLE
        }
    }

    /**
     * For demo, we just generate a random or fixed 95% match.
     * In real usage, you'd compute from docViewModel or an AI service.
     */
    private fun computeFaceMatch(): Int {
        // Fake a 90% match for now
        return 90
    }

    /**
     * Update the match text with color.
     */
    private fun updateMatchUI(matchPercent: Int) {
        txtMatchResult.text = "Chân dung khách hàng: trùng khớp $matchPercent%"

        val colorRes = if (matchPercent >= 60) R.color.green_light else R.color.red_dark
        val color = ContextCompat.getColor(requireContext(), colorRes)
        txtMatchResult.setTextColor(color)
    }
}
