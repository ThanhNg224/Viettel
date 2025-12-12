package com.example.viettel.fragments.step7

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
import com.example.viettel.databinding.FragmentQrPaymentBinding
import com.example.viettel.di.PaymentViewModelFactory
import com.example.viettel.feature.payment.domain.entity.PaymentStatus
import com.example.viettel.feature.payment.presentation.PaymentViewModel
import com.example.viettel.feature.feedback.presentation.ui.ServiceEvaluationFragment
import com.example.viettel.utils.ProgressUtils
import com.example.viettel.utils.navigateTo
import com.example.viettel.utils.updateNavigationControls
import kotlinx.coroutines.launch

class QrCodePaymentFragment : Fragment() {

    private var _binding: FragmentQrPaymentBinding? = null
    private val binding get() = _binding!!

    private val paymentViewModel: PaymentViewModel by activityViewModels { PaymentViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQrPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ProgressUtils.animateProgressToStep(view, 7)
        updateNavigationControls(isBackVisible = true, isContinueVisible = false, isContinueEnabled = false)

        binding.btnCancelPayment.setOnClickListener {
            paymentViewModel.cancelPayment()
            requireActivity().supportFragmentManager.popBackStack()
        }

        observeState()
        paymentViewModel.startPaymentIfNeeded()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                paymentViewModel.uiState.collect { state ->
                    when (state.status) {
                        PaymentStatus.Processing -> binding.circularProgress.visibility = View.VISIBLE
                        PaymentStatus.Success -> {
                            binding.circularProgress.visibility = View.GONE
                            navigateTo(ServiceEvaluationFragment())
                        }
                        is PaymentStatus.Error -> {
                            binding.circularProgress.visibility = View.GONE
                            Toast.makeText(requireContext(), "Thanh toan loi", Toast.LENGTH_SHORT).show()
                        }
                        PaymentStatus.Cancelled -> {
                            binding.circularProgress.visibility = View.GONE
                        }
                        PaymentStatus.Idle -> binding.circularProgress.visibility = View.GONE
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
