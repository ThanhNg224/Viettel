package com.example.viettel.fragments.step6

import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.viettel.R
import com.example.viettel.utils.CameraHelper
import com.example.viettel.utils.ProgressUtils
import com.example.viettel.viewmodel.DocumentViewModel
import com.example.viettel.viewmodel.PortraitAction
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import vn.leeon.eidsdk.utils.ImageUtils

class PortraitLivenessFragment : Fragment() {

    private lateinit var previewView: PreviewView
    private lateinit var captureButton: ImageButton
    private lateinit var instructionText: TextView
    private lateinit var resultOverlay: ImageView
    private lateinit var emojiTicks: List<ImageView>
    private lateinit var cameraHelper: CameraHelper

    private val instructions = listOf(
        "Vui lòng chụp ảnh chân dung đang cười 😄",
        "Vui lòng chụp ảnh chân dung đang chớp mắt 😌",
        "Vui lòng chụp ảnh chân dung quay đầu sang trái 😎",
        "Vui lòng chụp ảnh chân dung quay đầu sang phải 😁"
    )
    private var currentIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_portrait_liveness, container, false)
    }

    @ExperimentalGetImage
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

        // ✅ Không bao giờ out-of-bounds
        if (currentIndex >= instructions.size) {
            instructionText.text = "Đã xong tất cả thao tác 🎉"
            captureButton.isEnabled = false
        } else {
            val safeIdx = currentIndex.coerceIn(0, instructions.lastIndex)
            instructionText.text = instructions.getOrNull(safeIdx) ?: instructions.last()
        }

        ProgressUtils.animateProgressToStep(view, 6)

        cameraHelper = CameraHelper(
            requireContext(),
            viewLifecycleOwner,
            previewView,
            facing = CameraSelector.DEFAULT_FRONT_CAMERA
        )
        cameraHelper.startCamera {
            Toast.makeText(requireContext(), "Không thể mở camera: ${it.message}", Toast.LENGTH_SHORT).show()
        }

        captureButton.setOnClickListener {
            // ✅ Chặn khi đã hoàn thành
            if (currentIndex >= instructions.size) {
                Log.d("PortraitLiveness", "All actions done. Ignoring capture.")
                return@setOnClickListener
            }

            cameraHelper.takePhoto(
                onCaptured = { analyzeFace(it) },
                onFail = { e ->
                    Toast.makeText(requireContext(), "Lỗi chụp ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }


    @ExperimentalGetImage
    private fun analyzeFace(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            Toast.makeText(requireContext(), "Lỗi: Không thể lấy ảnh từ camera!", Toast.LENGTH_SHORT).show()
            return
        }

        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .enableTracking()
            .build()

        val detector = FaceDetection.getClient(options)

        detector.process(image)
            .addOnSuccessListener { faces ->
                imageProxy.close()

                if (faces.isEmpty()) {
                    Toast.makeText(requireContext(), "Không thấy khuôn mặt nào!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                checkFaceAction(faces[0])
            }
            .addOnFailureListener {
                imageProxy.close()
                Toast.makeText(requireContext(), "Lỗi nhận diện khuôn mặt: ${it.message}", Toast.LENGTH_SHORT).show()
                Log.e("PortraitLiveness", "Face detection failed", it)
            }
    }


    @ExperimentalGetImage
    private fun checkFaceAction(face: Face) {
        val instruction = instructions[currentIndex]
        val smilingProb = face.smilingProbability ?: -1f
        val leftEyeOpenProb = face.leftEyeOpenProbability ?: -1f
        val rightEyeOpenProb = face.rightEyeOpenProbability ?: -1f
        val headY = face.headEulerAngleY

        val pass = when {
            instruction.contains("cười") -> smilingProb > 0.4
            instruction.contains("chớp mắt") -> leftEyeOpenProb < 0.3 && rightEyeOpenProb < 0.3
            instruction.contains("trái") -> headY < -15
            instruction.contains("phải") -> headY > 15
            else -> false
        }

        if (pass) {
            if (instruction.contains("cười")) {
                cameraHelper.takePhoto(
                    onCaptured = { proxy ->
                        val mediaImage = proxy.image
                        if (mediaImage != null) {
                            val bmp = ImageUtils.imageToByteArray(mediaImage)?.let { bytes ->
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            }

                            if (bmp != null) {
                                val docViewModel: DocumentViewModel by activityViewModels()
                                docViewModel.portraitActions[PortraitAction.SMILE] = bmp
                                Log.d("PortraitLiveness", "✅ SMILE photo saved")
                            }
                        }
                        proxy.close()
                    },
                    onFail = {
                        Log.e("PortraitLiveness", "❌ Failed to capture SMILE bitmap: ${it.message}")
                    }
                )
            }


            emojiTicks[currentIndex].visibility = View.VISIBLE
            showResultOverlay(true)
            playSound(R.raw.success_sound)
            currentIndex++
            if (currentIndex < instructions.size) {
                instructionText.text = instructions[currentIndex]
            } else {
                instructionText.text = "Bạn đã hoàn thành 🎉"
                Toast.makeText(requireContext(), "Tất cả hành động đã hoàn thành!", Toast.LENGTH_SHORT).show()
            }
        }
        else {
            // Handle failure case
            showResultOverlay(false)
            playSound(R.raw.fail_sound)
            Toast.makeText(requireContext(), "Thử lại! Hãy làm theo hướng dẫn.", Toast.LENGTH_SHORT).show()
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
    override fun onDestroyView() {
        super.onDestroyView()
        cameraHelper.releaseCamera()
    }

}