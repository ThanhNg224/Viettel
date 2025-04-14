package com.example.viettel.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.viettel.R

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.media.MediaPlayer
import androidx.fragment.app.activityViewModels
import com.example.viettel.utils.ProgressUtils
import com.example.viettel.viewmodel.DocumentViewModel
import com.example.viettel.viewmodel.PortraitAction
import kotlin.getValue


class PortraitLivenessFragment : Fragment() {
    private var lastCapturedBitmap: Bitmap? = null

    private val docViewModel: DocumentViewModel by activityViewModels()
    private lateinit var previewView: PreviewView
    private lateinit var captureButton: ImageButton
    private lateinit var instructionText: TextView

    // The camera executor & imageCapture
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageCapture: ImageCapture

    // The 4 ticks in corners
    private lateinit var emojiTicks: List<ImageView>
    // The list of instructions
    private val instructions = listOf(
        "Vui lòng chụp ảnh chân dung đang cười 😄",
        "Vui lòng chụp ảnh chân dung đang chớp mắt 😌",
        "Vui lòng chụp ảnh chân dung quay đầu sang trái 😎",
        "Vui lòng chụp ảnh chân dung quay đầu sang phải 😁"
    )
    private var currentIndex = 0
// success or not
    private lateinit var resultOverlay: ImageView


    // Camera permission
    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCamera()
            else Toast.makeText(requireContext(), "Cần quyền camera để tiếp tục", Toast.LENGTH_SHORT).show()
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_portrait_liveness, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UI references
        instructionText = view.findViewById(R.id.tvInstruction)
        previewView = view.findViewById(R.id.view_finder)
        captureButton = view.findViewById(R.id.btnCapture)
        resultOverlay = view.findViewById(R.id.imgResultOverlay)


        // Ticks
        emojiTicks = listOf(
            view.findViewById(R.id.imgTickTL),
            view.findViewById(R.id.imgTickTR),
            view.findViewById(R.id.imgTickBL),
            view.findViewById(R.id.imgTickBR)
        )

        // Show first instruction
        instructionText.text = instructions[currentIndex]

        // Camera executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Start camera or request permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        // Handle capture
        captureButton.setOnClickListener {
            takePhoto()
        }

        // Animate progress bar to step 6
        ProgressUtils.animateProgressToStep(view, 5)

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Build preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // Build imageCapture
            @Suppress("DEPRECATION")
            imageCapture = ImageCapture.Builder()
                .setTargetResolution(Size(1280, 720))
                .setTargetRotation(requireActivity().windowManager.defaultDisplay.rotation)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e(TAG, "Camera start failed", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /**
     * Takes a photo, then checks if face matches the current challenge
     */
    private fun takePhoto() {
        if (!::imageCapture.isInitialized) {
            Toast.makeText(requireContext(), "Camera chưa sẵn sàng", Toast.LENGTH_SHORT).show()
            return
        }

        // Capture image in memory
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {

                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    // Convert image to ByteArray
                    val byteBuffer = imageProxy.planes[0].buffer
                    val byteArray = ByteArray(byteBuffer.remaining())
                    byteBuffer.get(byteArray)

                    // Freed the buffer
                    imageProxy.close()

                    // Now analyze with MLKit
                    analyzeImage(byteArray)
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Toast.makeText(requireContext(), "Chụp ảnh thất bại: ${exception.message}", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Photo capture failed", exception)
                }
            }
        )
    }

    /**
     * Analyze the captured image using MLKit FaceDetection
     */
    private fun analyzeImage(byteArray: ByteArray) {
        // Convert the byte array to a Bitmap and save it as the last captured image
        val bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        lastCapturedBitmap = bmp

        // Create an ML Kit InputImage from the bitmap
        val image = InputImage.fromBitmap(bmp, 0)

        val highAccuracyOpts = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .enableTracking() // optional
            .build()
        val detector = FaceDetection.getClient(highAccuracyOpts)

        detector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isEmpty()) {
                    Toast.makeText(requireContext(), "Không thấy khuôn mặt nào!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                // Use the first detected face for comparison
                val face = faces[0]
                checkFaceAction(face)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Lỗi nhận diện khuôn mặt: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Face detection failed", e)
            }
    }

    private fun checkFaceAction(face: Face) {
        val instruction = instructions[currentIndex]
        val pass = when {
            instruction.contains("cười") -> (face.smilingProbability ?: -1f) > 0.4f
            instruction.contains("chớp mắt") -> (face.leftEyeOpenProbability ?: 1f) < 0.3f && (face.rightEyeOpenProbability ?: 1f) < 0.3f
            instruction.contains("trái") -> face.headEulerAngleY < -15
            instruction.contains("phải") -> face.headEulerAngleY > 15
            else -> false
        }

        if (pass) {
            // Show success tick and feedback
            emojiTicks[currentIndex].visibility = View.VISIBLE
            showResultOverlay(true)
            playSound(R.raw.siuuu)

            // Save the currently captured image for this action into the ViewModel
            getCurrentAction()?.let {
                docViewModel.portraitActions[it] = lastCapturedBitmap
            }


            // Move to the next challenge
            currentIndex++
            if (currentIndex < instructions.size) {
                instructionText.text = instructions[currentIndex]
            } else {
                instructionText.text = "Siuuuuuu! Bạn đã hoàn thành 🎉"
                Toast.makeText(requireContext(), "Tất cả hành động đã hoàn thành!", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Show failure feedback
            showResultOverlay(false)
            playSound(R.raw.fail_sound)
            Toast.makeText(requireContext(), "Bạn chưa thực hiện đúng. Hãy thử lại!", Toast.LENGTH_SHORT).show()
        }

        if (currentIndex >= instructions.size) {
            Log.d("PortraitLiveness", "All actions done. Ignoring capture.")
            return
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }



    companion object {
        private const val TAG = "PortraitLiveness"
    }
    private fun showResultOverlay(success: Boolean) {
        resultOverlay.setImageResource(
            if (success) R.drawable.ic_success_tick else R.drawable.ic_fail_cross
        )
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

    private fun getCurrentAction(): PortraitAction? {
        return when (currentIndex) {
            0 -> PortraitAction.SMILE
            1 -> PortraitAction.BLINK
            2 -> PortraitAction.TURN_LEFT
            3 -> PortraitAction.TURN_RIGHT
            else -> null
        }
    }

}
