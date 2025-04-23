package com.example.viettel.fragments.step3_4

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.viettel.R
import com.example.viettel.activities.MainActivity
import com.example.viettel.utils.CameraHelper
import com.example.viettel.utils.ProgressUtils
import com.example.viettel.viewmodel.DocumentViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.jmrtd.lds.icao.MRZInfo
import vn.leeon.eidsdk.utils.ImageUtils
import vn.leeon.eidsdk.utils.OcrUtils
import com.joyusing.controllight.ControlLightUtil
import com.example.viettel.utils.NavigationButtonHelper


class CaptureBackPhotoFragment : Fragment(R.layout.fragment_capture_back_photo) {

    private lateinit var previewView: PreviewView
    private lateinit var captureButton: ImageButton
    private lateinit var textViewTitle: TextView
    private lateinit var successTick: ImageView
    private lateinit var cameraHelper: CameraHelper
    private val docViewModel: DocumentViewModel by activityViewModels()

    @OptIn(ExperimentalGetImage::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔥 Initialize your views first
        previewView   = view.findViewById(R.id.view_finder)
        captureButton = view.findViewById(R.id.btnCapture)
        textViewTitle = view.findViewById(R.id.tvInstruction)
        successTick   = view.findViewById(R.id.imgSuccessTick)
        ProgressUtils.animateProgressToStep(view, 4)
        // 🔄 Set up back/continue buttons
        (activity as? MainActivity)?.apply {
            setBackVisible(true)
            setContinueVisible(true)
            setContinueEnabled(isMRZReady())
        }



        // 📸 Only start camera in the normal (no-MRZ) flow
        cameraHelper = CameraHelper(requireContext(), viewLifecycleOwner, previewView)
        cameraHelper.startCamera { e ->
            Toast.makeText(requireContext(),
                "Không thể mở camera: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }

        captureButton.setOnClickListener {
            (activity as? MainActivity)?.setContinueEnabled(false)
            cameraHelper.takePhoto(
                onCaptured = { imageProxy -> handleCapture(imageProxy) },
                onFail     = { e -> Toast.makeText(
                    requireContext(),
                    "Lỗi chụp ảnh: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                    (activity as? MainActivity)?.setContinueEnabled(isMRZReady())}
            )
        }
    }


    override fun onResume() {
        super.onResume()
        // 💡 Re-trigger LED when returning to fragment
        ControlLightUtil.openLight()
        ControlLightUtil.setLight("5")
    }


    @ExperimentalGetImage
    private fun handleCapture(imageProxy: ImageProxy) {
        // save back‐side bitmap
        imageProxy.image?.let { img ->
            ImageUtils.imageToByteArray(img)?.let { bytes ->
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    .also { bmp -> docViewModel.backImage = bmp }
            }
        }

        requireActivity().runOnUiThread {
            successTick.apply {
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
                                    // —— enable Continue now that capture is done ——
                                    (activity as? MainActivity)?.setContinueEnabled(true)
                                    // then proceed with your existing OCR logic
                                    processOcrWithEidSDK(imageProxy)
                                }
                                .start()
                        }, 2000)
                    }
                    .start()
            }

            Toast.makeText(requireContext(),
                "Ảnh mặt sau đã chụp xong!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    @ExperimentalGetImage
    private fun processOcrWithEidSDK(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val startTime = System.currentTimeMillis()

        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val timeRequired = System.currentTimeMillis() - startTime
                OcrUtils.processOcr(visionText, timeRequired, object : OcrUtils.MRZCallback {
                    override fun onMRZRead(mrz: MRZInfo, timeRequired: Long) {
                        Log.d("CaptureBackPhoto", "MRZ success: ${mrz.documentNumber}")
                        docViewModel.mrzInfo = mrz
                        (activity as? MainActivity)?.animateToStep(5)
                        (activity as? MainActivity)?.launchNFCStep(mrz)
                        imageProxy.close()
                    }

                    override fun onMRZReadFailure(timeRequired: Long) {
                        Log.e("CaptureBackPhoto", "MRZ failed")
                        Toast.makeText(requireContext(), "Không tìm thấy MRZ", Toast.LENGTH_SHORT).show()
                        imageProxy.close()
                    }

                    override fun onFailure(e: Exception, timeRequired: Long) {
                        Log.e("CaptureBackPhoto", "OCR error: ${e.message}", e)
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
    fun getMRZ(): MRZInfo? {
        return docViewModel.mrzInfo
    }
    fun isMRZReady(): Boolean {
        return docViewModel.mrzInfo != null
    }
    override fun onDestroyView() {
        super.onDestroyView()

        //Turn off LED
        ControlLightUtil.closeLight()
        cameraHelper.releaseCamera()
    }


}