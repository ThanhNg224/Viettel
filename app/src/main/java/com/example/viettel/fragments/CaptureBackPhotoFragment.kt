package com.example.viettel.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.viettel.viewmodel.DocumentViewModel

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
import com.example.viettel.activities.MainActivity
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.jmrtd.lds.icao.MRZInfo
import vn.leeon.eidsdk.utils.ImageUtils
import vn.leeon.eidsdk.utils.OcrUtils

class CaptureBackPhotoFragment : Fragment() {

    private lateinit var previewView: PreviewView
    private lateinit var captureButton: ImageButton
    private lateinit var textViewTitle: TextView
    private lateinit var successTick: ImageView
    private val docViewModel: DocumentViewModel by activityViewModels()


    // ImageCapture object initialized inside startCamera()
    private lateinit var imageCapture: ImageCapture

    // ProcessCameraProvider future instance
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    // Permission launcher for Camera permission
    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                previewView.post { startCamera() }
            } else {
                Toast.makeText(requireContext(), "Bạn cần cấp quyền camera.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_capture_back_photo, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        previewView = view.findViewById(R.id.view_finder)
        captureButton = view.findViewById(R.id.btnCapture)
        textViewTitle = view.findViewById(R.id.tvInstruction)
        successTick = view.findViewById(R.id.imgSuccessTick)

        textViewTitle.text = "Vui lòng chụp ảnh mặt sau của giấy tờ"

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            previewView.post { startCamera() }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        captureButton.setOnClickListener { takePhoto() }

        // Animate progress bar to step 4 (for example)
        animateProgressBar(view)
    }

    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        previewView.scaleType = PreviewView.ScaleType.FIT_CENTER
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .setTargetResolution(Size(1280, 720))
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setTargetResolution(Size(1280, 720))
                .setTargetRotation(previewView.display.rotation)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e("CaptureBackPhoto", "Camera binding failed: ${exc.message}", exc)
                Toast.makeText(requireContext(), "Không thể mở camera", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        // Ensure imageCapture has been initialized
        if (!::imageCapture.isInitialized) {
            Toast.makeText(requireContext(), "Camera chưa sẵn sàng", Toast.LENGTH_SHORT).show()
            return
        }
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("CaptureBackPhoto", "Photo capture failed: ${exc.message}", exc)
                    Toast.makeText(requireContext(), "Lỗi chụp ảnh: ${exc.message}", Toast.LENGTH_SHORT).show()
                }

                @ExperimentalGetImage
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    // Convert the captured image to a Bitmap
                    val bmp = ImageUtils.imageToByteArray(imageProxy.image!!)?.let { byteArray ->
                        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    }
                    bmp?.let {
                        docViewModel.backImage = it
                    }

                    // Provide UI feedback
                    requireActivity().runOnUiThread {
                        successTick.apply {
                            alpha = 0f
                            visibility = View.VISIBLE
                            animate().alpha(1f).setDuration(300).withEndAction {
                                postDelayed({
                                    animate().alpha(0f).setDuration(300).withEndAction {
                                        visibility = View.GONE

                                    }.start()
                                }, 2000)
                            }.start()
                        }
                        Toast.makeText(requireContext(), "Ảnh mặt sau đã chụp xong!", Toast.LENGTH_SHORT).show()
                    }

                    // Delay OCR processing so that the image is stable. (Old code delayed about 500ms.)
                    Handler(Looper.getMainLooper()).postDelayed({
                        processOcrWithEidSDK(imageProxy)
                    }, 500)
                }
            }
        )
    }

    @ExperimentalGetImage
    private fun processOcrWithEidSDK(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val startTime = System.currentTimeMillis()

        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val timeRequired = System.currentTimeMillis() - startTime

                OcrUtils.processOcr(visionText, timeRequired, object : OcrUtils.MRZCallback {
                    override fun onMRZRead(mrz: MRZInfo, timeRequired: Long) {
                        // Do NOT call mrz.toString() (it will trigger errors if the fields are too wide).
                        Log.d("CaptureBackPhoto", "MRZ read success: Document Number = ${mrz.documentNumber}")
                        docViewModel.mrzInfo = mrz
                        (activity as? MainActivity)?.animateToStep(5)
                        (activity as? MainActivity)?.launchNFCStep(mrz)
                        // Close the image proxy if not already closed.
                        imageProxy.close()
                    }

                    override fun onMRZReadFailure(timeRequired: Long) {
                        Log.e("CaptureBackPhoto", "MRZ read failure. Possibly no MRZ lines recognized.")
                        Toast.makeText(requireContext(), "Không tìm thấy MRZ trong ảnh", Toast.LENGTH_SHORT).show()
                        imageProxy.close()
                    }

                    override fun onFailure(e: Exception, timeRequired: Long) {
                        Log.e("CaptureBackPhoto", "Error analyzing MRZ: ${e.message}", e)
                        Toast.makeText(requireContext(), "Lỗi OCR: ${e.message}", Toast.LENGTH_SHORT).show()
                        imageProxy.close()
                    }
                })
            }
            .addOnFailureListener { e ->
                Log.e("CaptureBackPhoto", "Vision OCR failed: ${e.message}", e)
                Toast.makeText(requireContext(), "Lỗi OCR: ${e.message}", Toast.LENGTH_SHORT).show()
                imageProxy.close()
            }
    }

    /**
     * Animates the progress bar (red line) to the desired step.
     * In this case, we animate to step 4.
     */
    private fun animateProgressBar(view: View) {
        val progressLine = view.findViewById<View>(R.id.progressLine)
        val progressBarContainer = view.findViewById<View>(R.id.progressBarContainer)
        progressBarContainer?.post {
            val totalSteps = 8
            val stepDistance = progressBarContainer.width.toFloat() / totalSteps
            val step4Width = (stepDistance * 4).toInt()

            val lp = progressLine.layoutParams
            lp.width = 0
            progressLine.layoutParams = lp

            val animator = android.animation.ValueAnimator.ofInt(0, step4Width)
            animator.duration = 400
            animator.addUpdateListener { anim ->
                lp.width = anim.animatedValue as Int
                progressLine.layoutParams = lp
            }
            animator.start()
        }
    }
}
