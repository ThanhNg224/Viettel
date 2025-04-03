package com.example.viettel.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
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
import java.io.ByteArrayOutputStream
import com.example.viettel.utils.toBitmapSafe


@SuppressLint("SetTextI18n")
class CaptureBackPhotoFragment : Fragment() {

    private lateinit var textViewTitle: TextView
    private lateinit var btnCapture: Button
    private lateinit var previewView: PreviewView
    private lateinit var imageCapture: ImageCapture

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                previewView.post { startCamera() }
            } else {
                Toast.makeText(requireContext(), "Bạn cần cấp quyền camera.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_capture_back_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        textViewTitle = view.findViewById(R.id.tvInstruction)
        btnCapture = view.findViewById(R.id.btnCapture)
        previewView = view.findViewById(R.id.view_finder)

        textViewTitle.text = "Vui lòng chụp ảnh mặt sau của giấy tờ"
        animateProgressBar(view)

        btnCapture.setOnClickListener {
            takePhoto()
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            previewView.post { startCamera() }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        imageCapture = ImageCapture.Builder().build()

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("CaptureBackPhoto", "Failed: ${exc.message}", exc)
                }

                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    val bmp = imageProxy.toBitmapSafe()
                    (activity as? MainActivity)?.setBackBitmap(bmp)
                    imageProxy.close()
                    Toast.makeText(requireContext(), "Ảnh mặt sau đã chụp xong!", Toast.LENGTH_SHORT).show()

                    // TODO: Optional - move to next step
                }
            }
        )
    }

    private fun animateProgressBar(view: View) {
        val progressLine = view.findViewById<View>(R.id.progressLine)
        val progressBarContainer = view.findViewById<View>(R.id.progressBarContainer)
        progressBarContainer?.post {
            val totalSteps = 8
            val stepWidth = (progressBarContainer.width.toFloat() / totalSteps) * 4
            val layoutParams = progressLine.layoutParams
            layoutParams.width = 0
            progressLine.layoutParams = layoutParams

            val animator = android.animation.ValueAnimator.ofInt(0, stepWidth.toInt())
            animator.duration = 400
            animator.addUpdateListener {
                layoutParams.width = it.animatedValue as Int
                progressLine.layoutParams = layoutParams
            }
            animator.start()
        }
    }


}
