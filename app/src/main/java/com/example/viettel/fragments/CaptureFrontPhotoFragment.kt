package com.example.viettel.fragments

import androidx.fragment.app.activityViewModels
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.viettel.R
import com.example.viettel.activities.MainActivity
import com.example.viettel.viewmodel.DocumentViewModel
import com.google.common.util.concurrent.ListenableFuture
import vn.leeon.eidsdk.utils.ImageUtils

class CaptureFrontPhotoFragment : Fragment() {

    private lateinit var textViewTitle: TextView
    private lateinit var previewView: PreviewView
    private lateinit var successTick: ImageView
    private lateinit var captureButton: ImageButton
    private val docViewModel: DocumentViewModel by activityViewModels()


    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                previewView.post { startCamera() }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Bạn cần cho phép quyền truy cập camera để tiếp tục.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_capture_front_photo, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textViewTitle = view.findViewById(R.id.tvInstruction)
        previewView = view.findViewById(R.id.view_finder)
        successTick = view.findViewById(R.id.imgSuccessTick)
        captureButton = view.findViewById(R.id.btnCapture)

        textViewTitle.text = "Vui lòng chụp ảnh mặt trước của giấy tờ"
        animateProgressBar(view)

        captureButton.setOnClickListener {
            takePhoto()
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            previewView.post { startCamera() }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        previewView.scaleType = PreviewView.ScaleType.FIT_CENTER // Important fix for visual alignment

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .setTargetResolution(Size(1280, 720)) // Fallback for stable aspect ratio
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setTargetResolution(Size(1280, 720)) // Match preview
                .setTargetRotation(previewView.display.rotation)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Log.e("CaptureFrontPhoto", "Camera binding failed: ${exc.message}", exc)
                Toast.makeText(requireContext(), "Không thể mở camera", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }


    private fun takePhoto() {
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("CaptureFrontPhoto", "Photo capture failed: ${exc.message}", exc)
                    Toast.makeText(requireContext(), "Lỗi chụp ảnh: ${exc.message}", Toast.LENGTH_SHORT).show()
                }

                @androidx.camera.core.ExperimentalGetImage
                override fun onCaptureSuccess(imageProxy: androidx.camera.core.ImageProxy) {
                    val bmp = imageProxy.image?.let { img ->
                        ImageUtils.imageToByteArray(img)?.let { byteArray ->
                            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                        }
                    }

                    bmp?.let {
                        docViewModel.frontImage = it
                    }

                    requireActivity().runOnUiThread {
                        successTick.apply {
                            alpha = 0f
                            visibility = View.VISIBLE
                            animate().alpha(1f).setDuration(300).withEndAction {
                                postDelayed({
                                    animate().alpha(0f).setDuration(300).withEndAction {
                                        visibility = View.GONE
                                        (activity as? MainActivity)?.replaceFragment(
                                            com.example.viettel.fragments.CaptureBackPhotoFragment()
                                        )
                                    }.start()
                                }, 2000)
                            }.start()
                        }
                        Toast.makeText(requireContext(), "Ảnh mặt trước đã chụp xong!", Toast.LENGTH_SHORT).show()
                    }
                    imageProxy.close()
                }
            }
        )
    }

    private fun animateProgressBar(view: View) {
        val progressLine = view.findViewById<View>(R.id.progressLine)
        val progressBarContainer = view.findViewById<View>(R.id.progressBarContainer)
        progressBarContainer?.post {
            val totalSteps = 8
            val stepDistance = progressBarContainer.width.toFloat() / totalSteps
            val step3Width = (stepDistance * 3).toInt()

            val layoutParams = progressLine.layoutParams
            layoutParams.width = 0
            progressLine.layoutParams = layoutParams

            val animator = android.animation.ValueAnimator.ofInt(0, step3Width).apply {
                duration = 400
                addUpdateListener { anim ->
                    layoutParams.width = anim.animatedValue as Int
                    progressLine.layoutParams = layoutParams
                }
            }
            animator.start()
        }
    }
}