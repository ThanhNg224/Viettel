package com.example.viettel.fragments

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.viettel.R
import com.example.viettel.utils.ProgressUtils
import com.example.viettel.viewmodel.DocumentViewModel
import com.example.viettel.viewmodel.PortraitAction

class PortraitComparisonFragment : Fragment() {

    private val docViewModel: DocumentViewModel by activityViewModels()

    private lateinit var txtMatchResult: TextView
    private lateinit var imgSmilePortrait: ImageView
    private lateinit var imgChipPortrait: ImageView
    private lateinit var txtChipSuccess: TextView
    private lateinit var imgIdFront: ImageView
    private lateinit var imgTickSuccess: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_portrait_comparison, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        ProgressUtils.animateProgressToStep(view, 6)

        // Find views
        txtMatchResult = view.findViewById(R.id.txtMatchResult)
        imgSmilePortrait = view.findViewById(R.id.imgSmilePortrait)
        imgChipPortrait = view.findViewById(R.id.imgChipPortrait)
        txtChipSuccess = view.findViewById(R.id.txtChipSuccess)
        imgIdFront = view.findViewById(R.id.imgIdFront)
        imgTickSuccess = view.findViewById(R.id.imgTickSuccess)

        // Set front ID image
        docViewModel.frontImage?.let { imgIdFront.setImageBitmap(it) }

        // Set chip + selfie images
        docViewModel.portraitActions[PortraitAction.SMILE]?.let {
            imgSmilePortrait.setImageBitmap(it)
        }

        docViewModel.chipPortrait?.let {
            imgChipPortrait.setImageBitmap(it)
        }

        // Show chip status
        if (docViewModel.chipPortrait == null) {
            txtChipSuccess.text = "Không có ảnh chip!"
            imgTickSuccess.visibility = View.GONE
        } else {
            txtChipSuccess.text = "Đọc thông tin trên chip thành công"
            imgTickSuccess.visibility = View.VISIBLE
        }

        // Calculate and show match % (hardcoded for now)
        val matchPercent = computeFaceMatch()
        updateMatchUI(matchPercent)
    }

    private fun computeFaceMatch(): Int {
        // Simulate a 90% match (or integrate real logic later)
        return 90
    }

    private fun updateMatchUI(percent: Int) {
        txtMatchResult.text = "Chân dung khách hàng: " +
                if (percent >= 60) "trùng khớp $percent%" else "không trùng khớp $percent%"

        val colorRes = if (percent >= 60) R.color.green_light else R.color.red_dark
        txtMatchResult.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
    }
}
