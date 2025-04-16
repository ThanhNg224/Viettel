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

class CaptureBackPhotoFragment : Fragment() {

    private lateinit var previewView: PreviewView
    private lateinit var captureButton: ImageButton
    private lateinit var textViewTitle: TextView
    private lateinit var successTick: ImageView
    private lateinit var cameraHelper: CameraHelper
    private val docViewModel: DocumentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_capture_back_photo, container, false)
    }

    @ExperimentalGetImage
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        previewView = view.findViewById(R.id.view_finder)
        captureButton = view.findViewById(R.id.btnCapture)
        textViewTitle = view.findViewById(R.id.tvInstruction)
        successTick = view.findViewById(R.id.imgSuccessTick)

        textViewTitle.text = "Vui lòng chụp ảnh mặt sau của giấy tờ"
        ProgressUtils.animateProgressToStep(view, 4)

        cameraHelper = CameraHelper(requireContext(), viewLifecycleOwner, previewView)
        cameraHelper.startCamera {
            Toast.makeText(requireContext(), "Không thể mở camera: ${it.message}", Toast.LENGTH_SHORT).show()
        }

        captureButton.setOnClickListener {
            cameraHelper.takePhoto(
                onCaptured = { imageProxy ->
                    handleCapture(imageProxy)
                },
                onFail = {
                    Toast.makeText(requireContext(), "Lỗi chụp ảnh: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    @ExperimentalGetImage
    private fun handleCapture(imageProxy: ImageProxy) {
        val bmp = imageProxy.image?.let { img ->
            ImageUtils.imageToByteArray(img)?.let { byteArray ->
                BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            }
        }

        bmp?.let { docViewModel.backImage = it }

        showSuccessTick()
        Toast.makeText(requireContext(), "Ảnh mặt sau đã chụp xong!", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({
            processOcrWithEidSDK(imageProxy)
        }, 500)
    }

    private fun showSuccessTick() {
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
}