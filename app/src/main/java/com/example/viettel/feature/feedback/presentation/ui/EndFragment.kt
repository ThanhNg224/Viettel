package com.example.viettel.feature.feedback.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.viettel.R
import com.example.viettel.utils.ProgressUtils

/**
 * Fragment hiển thị màn hình kết thúc sau khi hoàn thành quy trình đánh giá dịch vụ.
 * Đây là bước cuối cùng (step 8) trong flow eKYC/feedback.
 */
class EndFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_end, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ProgressUtils.animateProgressToStep(view, 8)
    }
}

