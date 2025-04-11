package com.example.viettel.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
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
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PortraitLivenessFragment : Fragment() {

    private lateinit var previewView: PreviewView
    private lateinit var captureButton: ImageButton

    private lateinit var emojiTicks: List<ImageView>
    private var currentIndex = 0

    private lateinit var cameraExecutor: ExecutorService

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCamera()
            else Toast.makeText(requireContext(), "Cần quyền camera để tiếp tục", Toast.LENGTH_SHORT).show()
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_portrait_liveness, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val instructionText = view.findViewById<TextView>(R.id.tvInstruction)

// List of challenges (you can shuffle this later if needed)
        val instructions = listOf(
            "Vui lòng chụp ảnh chân dung đang cười 😄",
            "Vui lòng chụp ảnh chân dung đang chớp mắt 😌",
            "Vui lòng chụp ảnh chân dung quay đầu sang trái 😎",
            "Vui lòng chụp ảnh chân dung quay đầu sang phải 😁"
        )

// Set the initial instruction
        instructionText.text = instructions[0]

        super.onViewCreated(view, savedInstanceState)

        previewView = view.findViewById(R.id.view_finder)
        captureButton = view.findViewById(R.id.btnCapture)

        // Tick views
        emojiTicks = listOf(
            view.findViewById(R.id.imgTickTL),
            view.findViewById(R.id.imgTickTR),
            view.findViewById(R.id.imgTickBL),
            view.findViewById(R.id.imgTickBR)
        )

        captureButton.setOnClickListener {
            if (currentIndex < emojiTicks.size) {
                emojiTicks[currentIndex].visibility = View.VISIBLE
                currentIndex++

                if (currentIndex < instructions.size) {
                    instructionText.text = instructions[currentIndex]
                } else {
                    instructionText.text = "Tuyệt vời! Bạn đã hoàn thành 🎉"
                    Toast.makeText(requireContext(), "Tất cả hành động đã hoàn thành!", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(requireContext(), "Tất cả hành động đã hoàn thành!", Toast.LENGTH_SHORT).show()
            }
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        animateProgressBar(view)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview)
            } catch (e: Exception) {
                Log.e("PortraitLiveness", "Camera start failed", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }
    private fun animateProgressBar(view: View) {
        val progressLine = view.findViewById<View>(R.id.progressLine)
        val progressBarContainer = view.findViewById<View>(R.id.progressBarContainer)
        progressBarContainer?.post {
            val totalSteps = 8
            val stepDistance = progressBarContainer.width.toFloat() / totalSteps
            val step6Width = (stepDistance * 6).toInt()

            val lp = progressLine.layoutParams
            lp.width = 0
            progressLine.layoutParams = lp

            val animator = android.animation.ValueAnimator.ofInt(0, step6Width)
            animator.duration = 500
            animator.addUpdateListener { anim ->
                lp.width = anim.animatedValue as Int
                progressLine.layoutParams = lp
            }
            animator.start()
        }
    }
}
