package com.example.viettel.fragments.step6

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.view.PreviewView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.viettel.R
import com.example.viettel.core.camera.ImageProxyMapper
import com.example.viettel.feature.identity.presentation.viewmodel.IdentityViewModel
import com.example.viettel.utils.CameraHelper
import com.example.viettel.utils.ProgressUtils
import kotlinx.coroutines.launch

class PortraitLivenessFragment : Fragment() {

    private lateinit var previewView: PreviewView
    private lateinit var captureButton: ImageButton
    private lateinit var instructionText: TextView
    private lateinit var resultOverlay: ImageView
    private lateinit var emojiTicks: List<ImageView>
    private lateinit var cameraHelper: CameraHelper

    private val identityViewModel: IdentityViewModel by activityViewModels {
        IdentityViewModel.Factory(requireActivity().application)
    }

    private var lastLivenessEventId = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_portrait_liveness, container, false)

    @OptIn(ExperimentalGetImage::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        instructionText = view.findViewById(R.id.tvInstruction)
        previewView = view.findViewById(R.id.view_finder)
        captureButton = view.findViewById(R.id.btnCapture)
        resultOverlay = view.findViewById(R.id.imgResultOverlay)
        emojiTicks = listOf(
            view.findViewById(R.id.imgTickTL),
            view.findViewById(R.id.imgTickTR),
            view.findViewById(R.id.imgTickBL),
            view.findViewById(R.id.imgTickBR)
        )

        ProgressUtils.animateProgressToStep(view, 6)

        cameraHelper = CameraHelper(
            requireContext(),
            viewLifecycleOwner,
            previewView,
            facing = CameraSelector.DEFAULT_FRONT_CAMERA
        )
        cameraHelper.startCamera {
            Toast.makeText(requireContext(), "Khong the mo camera: ${it.message}", Toast.LENGTH_SHORT).show()
        }

        observeUiState()

        captureButton.setOnClickListener {
            cameraHelper.takePhoto(
                onCaptured = { proxy ->
                    val frame = ImageProxyMapper.toNv21Frame(proxy)
                    val jpeg = ImageProxyMapper.toJpegBytes(proxy)
                    proxy.close()
                    if (frame == null) {
                        Toast.makeText(requireContext(), "Khong the lay du lieu anh", Toast.LENGTH_SHORT).show()
                        return@takePhoto
                    }
                    identityViewModel.onLivenessImageCaptured(frame, jpeg)
                },
                onFail = { e ->
                    Toast.makeText(requireContext(), "Loi chup anh: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun showResultOverlay(success: Boolean) {
        resultOverlay.setImageResource(if (success) R.drawable.ic_success_tick else R.drawable.ic_fail_cross)
        resultOverlay.visibility = View.VISIBLE

        resultOverlay.animate()
            .scaleX(1.8f)
            .scaleY(1.8f)
            .alpha(1f)
            .setDuration(150)
            .withEndAction {
                resultOverlay.postDelayed({
                    resultOverlay.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            resultOverlay.visibility = View.GONE
                        }.start()
                }, 1000)
            }.start()
    }

    private fun playSound(resId: Int) {
        val mediaPlayer = MediaPlayer.create(requireContext(), resId)
        mediaPlayer.setOnCompletionListener {
            it.release()
        }
        mediaPlayer.start()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                identityViewModel.uiState.collect { state ->
                    instructionText.text = state.livenessInstruction
                    emojiTicks.forEachIndexed { index, imageView ->
                        imageView.visibility = if (index < state.livenessStepIndex) View.VISIBLE else View.INVISIBLE
                    }
                    captureButton.isEnabled = !state.livenessCompleted

                    val eventId = state.livenessEventId
                    if (eventId != lastLivenessEventId && state.lastLivenessSuccess != null) {
                        lastLivenessEventId = eventId
                        val success = state.lastLivenessSuccess
                        if (success) playSound(R.raw.success_sound) else playSound(R.raw.fail_sound)
                        showResultOverlay(success)
                        state.lastLivenessMessage?.let {
                            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraHelper.releaseCamera()
    }
}
