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
import com.example.viettel.databinding.FragmentPaymentBinding
import com.example.viettel.di.PaymentViewModelFactory
import com.example.viettel.feature.payment.domain.entity.PaymentMethod
import com.example.viettel.feature.payment.domain.entity.PaymentStatus
import com.example.viettel.feature.payment.presentation.PaymentViewModel
import com.example.viettel.feature.feedback.presentation.ui.ServiceEvaluationFragment
import com.example.viettel.utils.ProgressUtils
import com.example.viettel.utils.navigateTo
import com.example.viettel.utils.updateNavigationControls
import kotlinx.coroutines.launch

class PaymentFragment : Fragment() {

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    private val paymentViewModel: PaymentViewModel by activityViewModels { PaymentViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ProgressUtils.animateProgressToStep(view, 7)
        updateNavigationControls(isBackVisible = true, isContinueVisible = false, isContinueEnabled = false)

        binding.qrOptionCard.setOnClickListener {
            paymentViewModel.selectMethod(PaymentMethod.QR)
            Toast.makeText(requireContext(), "Da chon thanh toan QR", Toast.LENGTH_SHORT).show()
            navigateTo(QrCodePaymentFragment())
        }

        binding.cashOptionCard.setOnClickListener {
            paymentViewModel.selectMethod(PaymentMethod.CASH)
            Toast.makeText(requireContext(), "Da chon thanh toan Tien mat", Toast.LENGTH_SHORT).show()
        }

        observePayment()
    }

    private fun observePayment() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                paymentViewModel.uiState.collect { state ->
                    if (state.status is PaymentStatus.Success) {
                        navigateTo(ServiceEvaluationFragment())
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
