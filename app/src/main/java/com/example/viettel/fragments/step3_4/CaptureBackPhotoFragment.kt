package com.example.viettel.fragments.step3_4

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.viettel.core.camera.ImageProxyMapper
import com.example.viettel.databinding.FragmentCaptureBackPhotoBinding
import com.example.viettel.di.IdentityViewModelFactory
import com.example.viettel.feature.identity.presentation.viewmodel.IdentityViewModel
import com.example.viettel.fragments.step5.NfcFragment
import com.example.viettel.utils.CameraHelper
import com.example.viettel.utils.ProgressUtils
import com.example.viettel.utils.mainActivity
import com.example.viettel.utils.navigateTo
import com.example.viettel.utils.updateNavigationControls
import com.joyusing.controllight.ControlLightUtil
import kotlinx.coroutines.launch

class CaptureBackPhotoFragment : Fragment() {

    private var _binding: FragmentCaptureBackPhotoBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraHelper: CameraHelper

    private val identityViewModel: IdentityViewModel by activityViewModels {
        IdentityViewModelFactory(requireActivity().application)
    }

    private var navigatedToNfc = false
    private var lastError: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaptureBackPhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    @OptIn(ExperimentalGetImage::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ProgressUtils.animateProgressToStep(view, 4)

        updateNavigationControls(
            isBackVisible = true,
            isContinueVisible = true,
            isContinueEnabled = identityViewModel.uiState.value.mrzData != null
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                identityViewModel.uiState.collect { state ->
                    updateNavigationControls(
                        isBackVisible = true,
                        isContinueVisible = true,
                        isContinueEnabled = state.mrzData != null && !state.isLoading
                    )
                    state.errorMessage?.let { error ->
                        if (error != lastError) {
                            lastError = error
                            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                        }
                    }

                    if (!navigatedToNfc && state.mrzData != null) {
                        navigatedToNfc = true
                        mainActivity()?.animateToStep(5)
                        navigateTo(NfcFragment())
                    }
                }
            }
        }

        cameraHelper = CameraHelper(requireContext(), viewLifecycleOwner, binding.viewFinder)
        cameraHelper.startCamera { e ->
            Toast.makeText(
                requireContext(),
                "Khong the mo camera: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.btnCapture.setOnClickListener {
            updateNavigationControls(
                isBackVisible = true,
                isContinueVisible = true,
                isContinueEnabled = false
            )
            cameraHelper.takePhoto(
                onCaptured = { imageProxy ->
                    val imageBytes = ImageProxyMapper.toJpegBytes(imageProxy)
                    val rotation = imageProxy.imageInfo.rotationDegrees
                    imageProxy.close()

                    if (imageBytes == null) {
                        Toast.makeText(requireContext(), "Khong the xu ly anh", Toast.LENGTH_SHORT).show()
                        updateNavigationControls(
                            isBackVisible = true,
                            isContinueVisible = true,
                            isContinueEnabled = identityViewModel.uiState.value.mrzData != null
                        )
                        return@takePhoto
                    }

                    identityViewModel.onBackCaptured(imageBytes, rotation)
                    identityViewModel.extractMrz(imageBytes, rotation)
                    requireActivity().runOnUiThread { showSuccessTick() }
                },
                onFail = {
                    Toast.makeText(
                        requireContext(),
                        "Loi chup anh: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateNavigationControls(
                        isBackVisible = true,
                        isContinueVisible = true,
                        isContinueEnabled = identityViewModel.uiState.value.mrzData != null
                    )
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        ControlLightUtil.openLight()
        ControlLightUtil.setLight("5")
    }

    private fun showSuccessTick() {
        binding.imgSuccessTick.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .setDuration(300)
                .withEndAction {
                    postDelayed({
                        animate()
                            .alpha(0f)
                            .setDuration(300)
                            .withEndAction {
                                visibility = View.GONE
                                updateNavigationControls(
                                    isBackVisible = true,
                                    isContinueVisible = true,
                                    isContinueEnabled = true
                                )
                            }
                            .start()
                    }, 2000)
                }
                .start()
        }

        Toast.makeText(
            requireContext(),
            "Anh mat sau da chup xong!",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ControlLightUtil.closeLight()
        cameraHelper.releaseCamera()
        _binding = null
    }
}
