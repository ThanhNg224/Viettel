package com.example.viettel.fragments.step7

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.viettel.R
import com.example.viettel.fragments.step8.EndFragment
import com.google.android.material.progressindicator.CircularProgressIndicator
import android.view.WindowInsets
import com.example.viettel.activities.MainActivity
import com.example.viettel.fragments.step8.ServiceEvaluationFragment

class QrCodePayment : Fragment(R.layout.fragment_qr_payment) {

    private lateinit var circularProgress: CircularProgressIndicator
    private lateinit var btnCancelPayment: Button
    private var countdownTimer: CountDownTimer? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ánh xạ các view
        circularProgress = view.findViewById(R.id.circularProgress)
        btnCancelPayment = view.findViewById(R.id.btnCancelPayment)

        // Xử lý sự kiện hủy thanh toán
        btnCancelPayment.setOnClickListener {
            countdownTimer?.cancel()
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Bắt đầu đếm thời gian 10 giây
        startPaymentTimer()
    }

    private fun startPaymentTimer() {
        countdownTimer = object : CountDownTimer(10000, 1000) {
            @OptIn(UnstableApi::class)
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                Log.d("PaymentTimer", "Còn lại: ${secondsRemaining}s")
            }

            @OptIn(UnstableApi::class)
            override fun onFinish() {
                Log.d("PaymentTimer", "Showing CircularProgress")
                circularProgress.apply {
                    visibility = View.VISIBLE
                    requestLayout()
                }
                requireActivity().runOnUiThread { circularProgress.invalidate() }

                // Chờ 300ms trước khi ẩn và hiển thị dialog
                Handler(Looper.getMainLooper()).postDelayed({
                    Log.d("PaymentTimer", "Hiding CircularProgress and showing Dialog")
                    circularProgress.visibility = View.GONE
                    showSuccessDialog() // Comment để kiểm tra
                }, 2000)
            }
        }.start()
    }

//    private fun showSuccessDialog() {
//        val dialog = android.app.Dialog(requireContext())
//
//        dialog.setOnShowListener {
//            (requireActivity() as? MainActivity)?.fullScreenMore()
//        }
//
//        dialog.setContentView(R.layout.dialog_payment_success)
//        dialog.setCancelable(false)
//
//        val btnClose = dialog.findViewById<Button>(R.id.btnClose)
//        val ivSuccess = dialog.findViewById<ImageView>(R.id.ivSuccess)
//
//        val scaleAnimation = ScaleAnimation(
//            0.8f, 1f, 0.8f, 1f,
//            Animation.RELATIVE_TO_SELF, 0.5f,
//            Animation.RELATIVE_TO_SELF, 0.5f
//        )
//        scaleAnimation.duration = 300
//        ivSuccess.startAnimation(scaleAnimation)
//
//        btnClose.setOnClickListener {
//            dialog.dismiss()
//            //requireActivity().supportFragmentManager.popBackStack()
//
//            //Chuyển tới fragment khảo sát
//            val endFragment = EndFragment()
//            parentFragmentManager.beginTransaction()
//                .replace(R.id.fragmentContainer, endFragment)  // fragment_container là id của FrameLayout trong activity
//                .addToBackStack(null)  // thêm vào backstack để có thể nhấn nút back quay lại
//                .commit()
//        }
//
//        dialog.show()
//
//        dialog.window?.setLayout(
//            WindowManager.LayoutParams.MATCH_PARENT,
//            WindowManager.LayoutParams.WRAP_CONTENT
//        )
//    }

    private fun showSuccessDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_payment_success)
        dialog.setCancelable(false)

        val btnClose = dialog.findViewById<Button>(R.id.btnClose)
        val ivSuccess = dialog.findViewById<ImageView>(R.id.ivSuccess)

        // Animation scale cho icon success
        val scaleAnimation = ScaleAnimation(
            0.8f, 1f, 0.8f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        scaleAnimation.duration = 300
        ivSuccess.startAnimation(scaleAnimation)

        btnClose.setOnClickListener {
            dialog.dismiss()

            // Gọi lại fullscreen ngay khi đóng
            (requireActivity() as? MainActivity)?.fullScreenMore()

            // Chuyển tới fragment khảo sát
            val serviceEvaluationFragment = ServiceEvaluationFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, serviceEvaluationFragment)
                .addToBackStack(null)
                .commit()
        }

        dialog.show()

        // 👉 Làm dialog rộng ra 90% màn hình, bo góc
        dialog.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(), // rộng 90%
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            setBackgroundDrawable(ColorDrawable(Color.WHITE)) // bỏ viền xám mặc định
            setGravity(Gravity.CENTER) // căn giữa màn hình
        }

        // Giữ immersive fullscreen cho dialog
        dialog.setOnShowListener {
            (requireActivity() as? MainActivity)?.fullScreenMore()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        countdownTimer?.cancel()
    }
}