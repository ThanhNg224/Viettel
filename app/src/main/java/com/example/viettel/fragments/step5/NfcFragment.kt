package com.example.viettel.fragments.step5

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.viettel.activities.MainActivity
import com.example.viettel.databinding.FragmentNfcBinding
import com.example.viettel.feature.identity.presentation.viewmodel.IdentityViewModel
import com.example.viettel.utils.ProgressUtils
import kotlinx.coroutines.launch

class NfcFragment : Fragment() {

    private var _binding: FragmentNfcBinding? = null
    private val binding get() = _binding!!

    private val identityViewModel: IdentityViewModel by activityViewModels {
        IdentityViewModel.Factory(requireActivity().application)
    }
    private var startedReading = false
    private var navigated = false
    private var lastError: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNfcBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ProgressUtils.animateProgressToStep(view, 5)
        (activity as? MainActivity)?.apply {
            setBackVisible(true)
            setContinueVisible(true)
            setContinueEnabled(false)
        }

        if (identityViewModel.uiState.value.mrzData == null) {
            Toast.makeText(requireContext(), "Khong co du lieu MRZ", Toast.LENGTH_SHORT).show()
            @Suppress("SetTextI18n")
            binding.txtStatus.text = "Khong co du lieu MRZ"
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                identityViewModel.uiState.collect { state ->
                    binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    if (state.isLoading) {
                        @Suppress("SetTextI18n")
                        binding.txtStatus.text = "Dang doc du lieu chip, vui long giu tai lieu tai NFC..."
                    }
                    state.errorMessage?.let { error ->
                        if (error != lastError) {
                            lastError = error
                            binding.txtStatus.text = error
                            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                        }
                    }
                    if (state.eidData != null) {
                        startedReading = true
                    }
                    if (!startedReading && state.mrzData != null) {
                        startedReading = true
                        identityViewModel.readEidFromChip()
                    }
                    if (!navigated && state.eidData != null) {
                        navigated = true
                        @Suppress("SetTextI18n")
                        binding.txtStatus.text = "Doc du lieu chip thanh cong"
                        (activity as? MainActivity)?.setContinueEnabled(true)
                        (activity as? MainActivity)?.replaceFragment(EidDetailsFragment())
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
