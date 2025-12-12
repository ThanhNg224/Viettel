package com.example.viettel.fragments.step1_2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.viettel.databinding.FragmentDocumentSelectionBinding
import com.example.viettel.di.IdentityViewModelFactory
import com.example.viettel.feature.identity.domain.entity.DocumentType
import com.example.viettel.feature.identity.presentation.viewmodel.IdentityViewModel
import com.example.viettel.utils.ProgressUtils
import com.example.viettel.utils.navigateTo
import com.example.viettel.utils.updateNavigationControls

class DocumentSelectionFragment : Fragment() {

    private var _binding: FragmentDocumentSelectionBinding? = null
    private val binding get() = _binding!!

    private val identityViewModel: IdentityViewModel by activityViewModels {
        IdentityViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDocumentSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateNavigationControls(isBackVisible = false, isContinueVisible = false)

        ProgressUtils.animateProgressToStep(view, 1)

        binding.option1.setOnClickListener {
            binding.imageOption1.setBackgroundResource(com.example.viettel.R.drawable.red_circle)
            binding.imageOption2.setBackgroundResource(com.example.viettel.R.drawable.white_circle)

            identityViewModel.selectDocumentType(DocumentType.CCCD)
            updateNavigationControls(isBackVisible = true, isContinueVisible = true, isContinueEnabled = true)
            Toast.makeText(requireContext(), "CCCD selected", Toast.LENGTH_SHORT).show()

            navigateTo(PlaceDocumentFragment())
        }

        binding.option2.setOnClickListener {
            binding.imageOption1.setBackgroundResource(com.example.viettel.R.drawable.white_circle)
            binding.imageOption2.setBackgroundResource(com.example.viettel.R.drawable.red_circle)

            identityViewModel.selectDocumentType(DocumentType.PASSPORT)
            updateNavigationControls(isBackVisible = true, isContinueVisible = true, isContinueEnabled = true)
            Toast.makeText(requireContext(), "Passport selected", Toast.LENGTH_SHORT).show()
            navigateTo(PlaceDocumentFragment())
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
