package com.example.viettel.fragments.step5

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.viettel.databinding.FragmentEidDetailsBinding
import com.example.viettel.feature.identity.presentation.mapper.BitmapMapper
import com.example.viettel.feature.identity.presentation.viewmodel.IdentityViewModel
import kotlinx.coroutines.launch

class EidDetailsFragment : Fragment() {

    private val identityViewModel: IdentityViewModel by activityViewModels {
        IdentityViewModel.Factory(requireActivity().application)
    }

    private var _binding: FragmentEidDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEidDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        renderState()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                identityViewModel.uiState.collect {
                    renderState()
                }
            }
        }
    }

    private fun renderState() {
        val state = identityViewModel.uiState.value

        binding.imgFront.setImageBitmap(BitmapMapper.fromBytes(state.frontImage, state.frontImageRotation))
        binding.imgBack.setImageBitmap(BitmapMapper.fromBytes(state.backImage, state.backImageRotation))
        binding.imgChipFace.setImageBitmap(BitmapMapper.fromBytes(state.eidData?.chipPortrait))

        val info = state.eidData?.personalInfo
        binding.txtName.text = info?.fullName ?: "-"
        binding.txtDocNumber.text = info?.documentNumber ?: "-"
        binding.txtPersonalIdentification.text = info?.personalIdentification ?: "-"
        binding.txtDob.text = info?.dateOfBirth ?: "-"
        binding.txtGender.text = info?.gender ?: "-"
        binding.txtNationality.text = info?.nationality ?: "-"
        binding.txtFatherName.text = info?.fatherName ?: "-"
        binding.txtMotherName.text = info?.motherName ?: "-"
        binding.txtPlaceOfOrigin.text = info?.placeOfOrigin ?: "-"
        binding.txtPlaceOfResidence.text = info?.placeOfResidence ?: "-"
        binding.txtReligion.text = info?.religion ?: "-"
        binding.txtEthnicity.text = info?.ethnicity ?: "-"
        binding.txtDateOfIssue.text = info?.dateOfIssue ?: "-"
        binding.txtDateExpiry.text = info?.dateOfExpiry ?: "-"

        binding.txtSignatureInfo.text = state.eidData?.certificateSummary ?: "(Khong co thong tin chu ky)"
        binding.txtVerificationStatus.text = state.eidData?.verificationSummary ?: "Khong co ket qua xac thuc"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
