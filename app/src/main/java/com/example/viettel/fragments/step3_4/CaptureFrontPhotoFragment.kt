package com.example.viettel.fragments.step3_4

import android.annotation.SuppressLint
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
import com.example.viettel.databinding.FragmentCaptureFrontPhotoBinding
import com.example.viettel.feature.identity.presentation.viewmodel.IdentityViewModel
import com.example.viettel.utils.CameraHelper
import com.example.viettel.utils.ProgressUtils
import com.example.viettel.utils.navigateTo
import com.example.viettel.utils.updateNavigationControls
import dagger.hilt.android.AndroidEntryPoint
import com.joyusing.controllight.ControlLightUtil
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CaptureFrontPhotoFragment : Fragment() {

    private var _binding: FragmentCaptureFrontPhotoBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraHelper: CameraHelper

    private val identityViewModel: IdentityViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaptureFrontPhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    @OptIn(ExperimentalGetImage::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ControlLightUtil.openLight()
        ControlLightUtil.setLight("5")

        binding.tvInstruction.text = "Vui long chup anh mat truoc cua giay to"
        ProgressUtils.animateProgressToStep(view, 3)
        updateNavigationControls(
            isBackVisible = true,
            isContinueVisible = true,
            isContinueEnabled = identityViewModel.uiState.value.frontImage != null
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                identityViewModel.uiState.collect { state ->
                    updateNavigationControls(
                        isBackVisible = true,
                        isContinueVisible = true,
                        isContinueEnabled = state.frontImage != null
                    )
                }
            }
        }

        cameraHelper = CameraHelper(requireContext(), viewLifecycleOwner, binding.viewFinder)
        cameraHelper.startCamera {
            Toast.makeText(requireContext(), "Khong the mo camera: ${it.message}", Toast.LENGTH_SHORT).show()
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
                            isContinueEnabled = identityViewModel.uiState.value.frontImage != null
                        )
                        return@takePhoto
                    }

                    identityViewModel.onFrontCaptured(imageBytes, rotation)
                    requireActivity().runOnUiThread { showSuccessAndMoveNext() }
                },
                onFail = {
                    Toast.makeText(requireContext(), "Loi chup anh: ${it.message}", Toast.LENGTH_SHORT).show()
                    updateNavigationControls(
                        isBackVisible = true,
                        isContinueVisible = true,
                        isContinueEnabled = identityViewModel.uiState.value.frontImage != null
                    )
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        ControlLightUtil.openLight()
        ControlLightUtil.setLight("5")
        if (::cameraHelper.isInitialized) {
            cameraHelper.startCamera { e ->
                Toast.makeText(requireContext(), "Khong the mo camera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSuccessAndMoveNext() {
        binding.imgSuccessTick.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(300).withEndAction {
                postDelayed({
                    animate().alpha(0f).setDuration(300).withEndAction {
                        visibility = View.GONE
                        navigateTo(CaptureBackPhotoFragment())
                    }.start()
                }, 2000)
            }.start()
        }

        Toast.makeText(requireContext(), "Anh mat truoc da chup xong!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ControlLightUtil.closeLight()
        _binding = null
    }
}
