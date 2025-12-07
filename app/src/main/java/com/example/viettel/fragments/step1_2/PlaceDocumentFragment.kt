package com.example.viettel.fragments.step1_2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.viettel.activities.MainActivity
import com.example.viettel.databinding.FragmentPlaceDocumentBinding
import com.example.viettel.feature.identity.domain.entity.DocumentType
import com.example.viettel.feature.identity.presentation.viewmodel.IdentityViewModel
import com.example.viettel.utils.ProgressUtils

class PlaceDocumentFragment : Fragment() {

    private var _binding: FragmentPlaceDocumentBinding? = null
    private val binding get() = _binding!!

    private val identityViewModel: IdentityViewModel by activityViewModels {
        IdentityViewModel.Factory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaceDocumentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.apply {
            setBackVisible(true)
            setContinueVisible(true)
            setContinueEnabled(true)
        }

        val docType = identityViewModel.uiState.value.documentType

        when (docType) {
            DocumentType.CCCD -> binding.imgIllustration.setImageResource(com.example.viettel.R.drawable.ic_cccd_placement)
            DocumentType.PASSPORT -> binding.imgIllustration.setImageResource(com.example.viettel.R.drawable.ic_passport_placement)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
