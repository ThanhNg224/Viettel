package com.example.viettel.fragments.step7

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.viettel.R
import com.example.viettel.activities.MainActivity
import com.example.viettel.utils.ProgressUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//class PaymentFragment : Fragment(R.layout.fragment_payment) {
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // bước 7
//        ProgressUtils.animateProgressToStep(view, 7)
//
//        //  CardView
//        val qrOption = view.findViewById<CardView>(R.id.qrOptionCard)
//        val cashOption = view.findViewById<CardView>(R.id.cashOptionCard)
//        val qrText = view.findViewById<TextView>(R.id.textQrOption)
//
//        Log.d("DEBUG_UI", "QR TEXT = ${qrText.text}, visible = ${qrText.visibility}, height = ${qrText.height}")
//
//
//        // Click demo
//        qrOption.setOnClickListener {
//            Toast.makeText(requireContext(), "Đã chọn thanh toán bằng QR", Toast.LENGTH_SHORT).show()
//            (activity as? MainActivity)?.replaceFragment(QrCodePayment())
//        }
//
//        cashOption.setOnClickListener {
//            Toast.makeText(requireContext(), "Đã chọn thanh toán bằng Tiền mặt", Toast.LENGTH_SHORT).show()
//        }
//    }
//}

class PaymentFragment : Fragment(R.layout.fragment_payment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bước 7
        ProgressUtils.animateProgressToStep(view, 7)

        // CardView
        val qrOption = view.findViewById<CardView>(R.id.qrOptionCard)
        val cashOption = view.findViewById<CardView>(R.id.cashOptionCard)
        val qrText = view.findViewById<TextView>(R.id.textQrOption)

        Log.d(
            "DEBUG_UI",
            "QR TEXT = ${qrText.text}, visible = ${qrText.visibility}, height = ${qrText.height}"
        )

        // Click demo
        qrOption.setOnClickListener {
            Toast.makeText(requireContext(), "Đã chọn thanh toán bằng QR", Toast.LENGTH_SHORT)
                .show()
            (activity as? MainActivity)?.replaceFragment(QrCodePayment())
        }

        cashOption.setOnClickListener {
            Toast.makeText(requireContext(), "Đã chọn thanh toán bằng Tiền mặt", Toast.LENGTH_SHORT)
                .show()
        }
    }
}