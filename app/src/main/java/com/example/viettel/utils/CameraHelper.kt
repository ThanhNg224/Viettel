package com.example.viettel.utils

import android.content.Context
import android.util.Log
import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

class CameraHelper(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView,
    private val facing: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    private val targetResolution: Size = Size(1280, 720),
) {
    lateinit var imageCapture: ImageCapture

    fun startCamera(onError: (Exception) -> Unit = {}) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        previewView.scaleType = PreviewView.ScaleType.FIT_CENTER

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

            imageCapture = ImageCapture.Builder()
                .setTargetResolution(targetResolution)
                .setTargetRotation(previewView.display.rotation)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    facing,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Log.e("CameraHelper", "Camera binding failed: ${exc.message}", exc)
                onError(exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun takePhoto(
        onCaptured: (ImageProxy) -> Unit,
        onFail: (ImageCaptureException) -> Unit
    ) {
        if (!::imageCapture.isInitialized) {
            ImageCaptureException(ImageCapture.ERROR_UNKNOWN, "Camera not initialized", null)
            return
        }

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    onCaptured(imageProxy)
                }

                override fun onError(exception: ImageCaptureException) {
                    onFail(exception)
                }
            }
        )
    }
}
