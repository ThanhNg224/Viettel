package com.example.viettel.fragments.step6

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.viettel.R
import com.example.viettel.databinding.FragmentPortraitComparisonBinding
import com.example.viettel.di.IdentityViewModelFactory
import com.example.viettel.feature.identity.domain.entity.PortraitAction
import com.example.viettel.feature.identity.presentation.mapper.BitmapMapper
import com.example.viettel.feature.identity.presentation.viewmodel.IdentityViewModel
import com.example.viettel.utils.ProgressUtils
import com.example.viettel.utils.updateNavigationControls
import kotlinx.coroutines.launch

class PortraitComparisonFragment : Fragment() {

    private val identityViewModel: IdentityViewModel by activityViewModels {
        IdentityViewModelFactory(requireActivity().application)
    }

    private var _binding: FragmentPortraitComparisonBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPortraitComparisonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ProgressUtils.animateProgressToStep(view, 6)
        renderStaticInfo()
        observeState()
    }

    private fun renderStaticInfo() {
        binding.txtMatchResult.text = "Dang thuc hien so sanh khuon mat..."
        binding.txtMatchResult.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        binding.progressMatchLoading.visibility = View.VISIBLE

        val state = identityViewModel.uiState.value
        val smileBytes = state.portraitActions[PortraitAction.SMILE]
        val chipBytes = state.eidData?.chipPortrait
        binding.imgSmilePortrait.setImageBitmap(BitmapMapper.fromBytes(smileBytes))
        binding.imgChipPortrait.setImageBitmap(BitmapMapper.fromBytes(chipBytes))

        val info = state.eidData?.personalInfo
        binding.txtUserNameValue.text = info?.fullName ?: "-"
        binding.txtDOBValue.text = info?.dateOfBirth ?: "-"
        binding.txtGenderValue.text = info?.gender ?: "-"
        binding.txtNationalityValue.text = info?.nationality ?: "-"
        binding.txtDocNumberValue.text = info?.documentNumber ?: "-"
        binding.txtPersonalIdValue.text = info?.personalIdentification ?: "-"
        binding.txtFatherNameValue.text = info?.fatherName ?: "-"
        binding.txtMotherNameValue.text = info?.motherName ?: "-"
        binding.txtPlaceOfOriginValue.text = info?.placeOfOrigin ?: "-"
        binding.txtPlaceOfResidenceValue.text = info?.placeOfResidence ?: "-"
        binding.txtReligionValue.text = info?.religion ?: "-"
        binding.txtEthnicityValue.text = info?.ethnicity ?: "-"
        binding.txtDateOfIssueValue.text = info?.dateOfIssue ?: "-"
        binding.txtDateExpiryValue.text = info?.dateOfExpiry ?: "-"

        updateNavigationControls(
            isBackVisible = true,
            isContinueVisible = true,
            isContinueEnabled = false
        )

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
                    binding.progressMatchLoading.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    state.errorMessage?.let { error ->
                        binding.txtMatchResult.text = error
                        binding.txtMatchResult.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_dark))
                        updateNavigationControls(
                            isBackVisible = true,
                            isContinueVisible = true,
                            isContinueEnabled = true
                        )
                    }

                    state.faceMatchScore?.let { score ->
                        updateMatchUI(score)
                        updateNavigationControls(
                            isBackVisible = true,
                            isContinueVisible = true,
                            isContinueEnabled = true
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun updateMatchUI(score: Double) {
        if (score < 0) {
            binding.txtMatchResult.text = "Khong the so sanh chan dung"
            binding.txtMatchResult.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_dark))
            return
        }
        val percent = (score * 100)
        val percentText = String.format("%.1f", percent)
        val isMatch = score >= 0.6

        binding.txtMatchResult.text = if (isMatch) {
            "Chan dung khach hang trung khop: $percentText%"
        } else {
            "Chan dung khach hang khong trung khop: $percentText%"
        }

        val colorRes = if (isMatch) R.color.green_light else R.color.red_dark
        binding.txtMatchResult.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
