package com.example.viettel.fragments.step3_4

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
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
import vn.leeon.eidsdk.utils.ImageUtils

class CaptureFrontPhotoFragment : Fragment() {

    private lateinit var textViewTitle: TextView
    private lateinit var previewView: PreviewView
    private lateinit var successTick: ImageView
    private lateinit var captureButton: ImageButton
    private lateinit var cameraHelper: CameraHelper
    private val docViewModel: DocumentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_capture_front_photo, container, false)
    }

    @SuppressLint("SetTextI18n")
    @OptIn(ExperimentalGetImage::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textViewTitle = view.findViewById(R.id.tvInstruction)
        previewView = view.findViewById(R.id.view_finder)
        successTick = view.findViewById(R.id.imgSuccessTick)
        captureButton = view.findViewById(R.id.btnCapture)

        textViewTitle.text = "Vui lòng chụp ảnh mặt trước của giấy tờ"
        ProgressUtils.animateProgressToStep(view, 3)

        // Initialize CameraHelper
        cameraHelper = CameraHelper(requireContext(), viewLifecycleOwner, previewView)
        cameraHelper.startCamera {
            Toast.makeText(requireContext(), "Không thể mở camera: ${it.message}", Toast.LENGTH_SHORT).show()
        }

        // Capture Button
        captureButton.setOnClickListener {
            cameraHelper.takePhoto(
                onCaptured = { imageProxy ->
                    handleCaptureSuccess(imageProxy)
                },
                onFail = {
                    Toast.makeText(requireContext(), "Lỗi chụp ảnh: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    @ExperimentalGetImage
    private fun handleCaptureSuccess(imageProxy: ImageProxy) {
        val bmp = imageProxy.image?.let { img ->
            ImageUtils.imageToByteArray(img)?.let { byteArray ->
                BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            }
        }

        bmp?.let {
            docViewModel.frontImage = it
        }

        imageProxy.close()

        // Animate success tick
        requireActivity().runOnUiThread {
            successTick.apply {
                alpha = 0f
                visibility = View.VISIBLE
                animate().alpha(1f).setDuration(300).withEndAction {
                    postDelayed({
                        animate().alpha(0f).setDuration(300).withEndAction {
                            visibility = View.GONE
                            (activity as? MainActivity)?.replaceFragment(CaptureBackPhotoFragment())
                        }.start()
                    }, 2000)
                }.start()
            }

            Toast.makeText(requireContext(), "Ảnh mặt trước đã chụp xong!", Toast.LENGTH_SHORT).show()
        }
    }
}