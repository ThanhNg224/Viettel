package com.example.viettel.fragments.step6

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView
import androidx.fragment.app.Fragment
import com.example.viettel.R
import com.example.viettel.utils.CameraHelper
import com.example.viettel.utils.ProgressUtils

class VideoCallFragment : Fragment(R.layout.fragment_video_call) {

    private lateinit var cameraHelper: CameraHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // bước 6
        ProgressUtils.animateProgressToStep(view, 6)

        val previewView = view.findViewById<PreviewView>(R.id.customerCam)

        cameraHelper = CameraHelper(
            context = requireContext(),
            lifecycleOwner = viewLifecycleOwner,
            previewView = previewView,
            facing = CameraSelector.DEFAULT_FRONT_CAMERA
        )

        cameraHelper.startCamera {
            Toast.makeText(requireContext(), "Không thể mở camera: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}