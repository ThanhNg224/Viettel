package com.example.viettel.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
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
import com.google.common.util.concurrent.ListenableFuture
import java.io.ByteArrayOutputStream
import com.example.viettel.activities.MainActivity
import com.example.viettel.utils.toBitmapSafe


@SuppressLint("SetTextI18n")
class CaptureFrontPhotoFragment : Fragment() {

    private lateinit var textViewTitle: TextView
    private lateinit var btnCapture: Button
    private lateinit var previewView: PreviewView
    private lateinit var successTick: ImageView


    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    // 🔒 Permission launcher
    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d("CaptureFrontPhoto", "Permission granted, starting camera.")
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textViewTitle = view.findViewById(R.id.tvInstruction)
        btnCapture = view.findViewById(R.id.btnCapture)
        previewView = view.findViewById(R.id.view_finder)
        successTick = view.findViewById(R.id.imgSuccessTick)


        textViewTitle.text = "Vui lòng chụp ảnh mặt trước của giấy tờ"
        animateProgressBar(view)

        btnCapture.setOnClickListener {
            takePhoto()
        }

        // ✅ Request permission if needed
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
        imageCapture = ImageCapture.Builder().build()


        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            try {
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                Log.d("CaptureFrontPhoto", "Camera bound successfully.")
            } catch (exc: Exception) {
                Log.e("CaptureFrontPhoto", "Camera binding failed: ${exc.message}", exc)
                Toast.makeText(
                    requireContext(),
                    "Không thể mở camera: ${exc.message}",
                    Toast.LENGTH_SHORT
                ).show()
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

                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    Log.d("CaptureFrontPhoto", "Photo captured successfully!")
                    val bmp = imageProxy.toBitmapSafe()
                    (activity as? MainActivity)?.setFrontBitmap(bmp)
                    // 1) Store in MainActivity
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

                    // 2) Provide feedback

                        Toast.makeText(requireContext(), "Ảnh mặt trước đã chụp xong!", Toast.LENGTH_SHORT).show()
                    }

                    // 3) Close the image proxy to free memory
                    imageProxy.close()

                    // 4) (Optional) Move to step 4
                    // (activity as? MainActivity)?.replaceFragment(CaptureBackPhotoFragment())
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
