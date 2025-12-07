package com.example.viettel.fragments.step6

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.viettel.R
import com.example.viettel.activities.MainActivity
import com.example.viettel.feature.identity.domain.entity.PortraitAction
import com.example.viettel.feature.identity.presentation.mapper.BitmapMapper
import com.example.viettel.feature.identity.presentation.viewmodel.IdentityViewModel
import com.example.viettel.utils.ProgressUtils
import kotlinx.coroutines.launch

class PortraitComparisonFragment : Fragment() {

    private val identityViewModel: IdentityViewModel by activityViewModels {
        IdentityViewModel.Factory(requireActivity().application)
    }

    private lateinit var txtMatchResult: TextView
    private lateinit var imgSmilePortrait: ImageView
    private lateinit var imgChipPortrait: ImageView
    private lateinit var txtUserNameValue: TextView
    private lateinit var txtDOBValue: TextView
    private lateinit var txtGenderValue: TextView
    private lateinit var txtNationalityValue: TextView
    private lateinit var txtDocNumberValue: TextView
    private lateinit var txtPersonalIdValue: TextView
    private lateinit var txtFatherNameValue: TextView
    private lateinit var txtMotherNameValue: TextView
    private lateinit var txtPlaceOfOriginValue: TextView
    private lateinit var txtPlaceOfResidenceValue: TextView
    private lateinit var txtReligionValue: TextView
    private lateinit var txtEthnicityValue: TextView
    private lateinit var txtDateOfIssueValue: TextView
    private lateinit var txtDateExpiryValue: TextView
    private lateinit var progressMatchLoading: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_portrait_comparison, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ProgressUtils.animateProgressToStep(view, 6)
        bindViews(view)
        renderStaticInfo()
        observeState()
    }

    private fun bindViews(view: View) {
        progressMatchLoading = view.findViewById(R.id.progressMatchLoading)
        txtMatchResult = view.findViewById(R.id.txtMatchResult)
        imgSmilePortrait = view.findViewById(R.id.imgSmilePortrait)
        imgChipPortrait = view.findViewById(R.id.imgChipPortrait)
        txtUserNameValue = view.findViewById(R.id.txtUserNameValue)
        txtDOBValue = view.findViewById(R.id.txtDOBValue)
        txtGenderValue = view.findViewById(R.id.txtGenderValue)
        txtNationalityValue = view.findViewById(R.id.txtNationalityValue)
        txtDocNumberValue = view.findViewById(R.id.txtDocNumberValue)
        txtPersonalIdValue = view.findViewById(R.id.txtPersonalIdValue)
        txtFatherNameValue = view.findViewById(R.id.txtFatherNameValue)
        txtMotherNameValue = view.findViewById(R.id.txtMotherNameValue)
        txtPlaceOfOriginValue = view.findViewById(R.id.txtPlaceOfOriginValue)
        txtPlaceOfResidenceValue = view.findViewById(R.id.txtPlaceOfResidenceValue)
        txtReligionValue = view.findViewById(R.id.txtReligionValue)
        txtEthnicityValue = view.findViewById(R.id.txtEthnicityValue)
        txtDateOfIssueValue = view.findViewById(R.id.txtDateOfIssueValue)
        txtDateExpiryValue = view.findViewById(R.id.txtDateExpiryValue)
    }

    private fun renderStaticInfo() {
        txtMatchResult.text = "Dang thuc hien so sanh khuon mat..."
        txtMatchResult.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        progressMatchLoading.visibility = View.VISIBLE

        val state = identityViewModel.uiState.value
        val smileBytes = state.portraitActions[PortraitAction.SMILE]
        val chipBytes = state.eidData?.chipPortrait
        imgSmilePortrait.setImageBitmap(BitmapMapper.fromBytes(smileBytes))
        imgChipPortrait.setImageBitmap(BitmapMapper.fromBytes(chipBytes))

        val info = state.eidData?.personalInfo
        txtUserNameValue.text = info?.fullName ?: "-"
        txtDOBValue.text = info?.dateOfBirth ?: "-"
        txtGenderValue.text = info?.gender ?: "-"
        txtNationalityValue.text = info?.nationality ?: "-"
        txtDocNumberValue.text = info?.documentNumber ?: "-"
        txtPersonalIdValue.text = info?.personalIdentification ?: "-"
        txtFatherNameValue.text = info?.fatherName ?: "-"
        txtMotherNameValue.text = info?.motherName ?: "-"
        txtPlaceOfOriginValue.text = info?.placeOfOrigin ?: "-"
        txtPlaceOfResidenceValue.text = info?.placeOfResidence ?: "-"
        txtReligionValue.text = info?.religion ?: "-"
        txtEthnicityValue.text = info?.ethnicity ?: "-"
        txtDateOfIssueValue.text = info?.dateOfIssue ?: "-"
        txtDateExpiryValue.text = info?.dateOfExpiry ?: "-"

        (activity as? MainActivity)?.apply {
            setBackVisible(true)
            setContinueVisible(true)
            setContinueEnabled(false)
            setBackEnabled(false)
        }

        if (smileBytes != null && chipBytes != null) {
            identityViewModel.comparePortraits()
        } else {
            updateMatchUI(-1.0)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                identityViewModel.uiState.collect { state ->
                    progressMatchLoading.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    state.errorMessage?.let { error ->
                        txtMatchResult.text = error
                        txtMatchResult.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_dark))
                        (activity as? MainActivity)?.apply {
                            setContinueEnabled(true)
                            setBackEnabled(true)
                        }
                    }

                    state.faceMatchScore?.let { score ->
                        updateMatchUI(score)
                        (activity as? MainActivity)?.apply {
                            setContinueEnabled(true)
                            setBackEnabled(true)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun updateMatchUI(score: Double) {
        if (score < 0) {
            txtMatchResult.text = "Khong the so sanh chan dung"
            txtMatchResult.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_dark))
            return
        }
        val percent = (score * 100)
        val percentText = String.format("%.1f", percent)
        val isMatch = score >= 0.6

        txtMatchResult.text = if (isMatch) {
            "Chan dung khach hang trung khop: $percentText%"
        } else {
            "Chan dung khach hang khong trung khop: $percentText%"
        }

        val colorRes = if (isMatch) R.color.green_light else R.color.red_dark
        txtMatchResult.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
    }
}
