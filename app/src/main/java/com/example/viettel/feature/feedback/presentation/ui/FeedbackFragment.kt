package com.example.viettel.feature.feedback.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.viettel.R
import com.example.viettel.activities.MainActivity
import com.example.viettel.core.extensions.gone
import com.example.viettel.core.extensions.visible
import com.example.viettel.feature.feedback.domain.entity.FeedbackReason
import com.example.viettel.feature.feedback.presentation.viewmodel.FeedbackViewModel
import com.example.viettel.utils.ProgressUtils
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment thu thập phản hồi chi tiết từ người dùng khi họ đánh giá dịch vụ thấp (≤4 sao).
 * Người dùng chọn lý do không hài lòng và có thể nhập lý do tùy chỉnh.
 */
@AndroidEntryPoint
class FeedbackFragment : Fragment() {

    private lateinit var checkboxOther: CheckBox
    private lateinit var editTextOther: EditText
    private lateinit var reasonCheckboxes: List<Pair<CheckBox, FeedbackReason>>

    private val feedbackViewModel: FeedbackViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_feedback, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ProgressUtils.animateProgressToStep(view, 8)
        (activity as? MainActivity)?.setContinueEnabled(false)

        val rating = arguments?.getInt(ARG_RATING) ?: 0
        val tvInstruction = view.findViewById<TextView>(R.id.tvInstruction)
        tvInstruction.text = "Ban da danh gia $rating sao. Vui long chon ly do khong hai long:"
        feedbackViewModel.setRating(rating)

        initializeViews(view)
        setupListeners()
    }

    private fun initializeViews(view: View) {
        checkboxOther = view.findViewById(R.id.checkbox_reason_5)
        editTextOther = view.findViewById(R.id.editText_checkBox5)
        editTextOther.gone()

        reasonCheckboxes = listOf(
            view.findViewById<CheckBox>(R.id.checkbox_reason_1) to FeedbackReason.FEATURE_ISSUE,
            view.findViewById<CheckBox>(R.id.checkbox_reason_2) to FeedbackReason.INFORMATION_UNCLEAR,
            view.findViewById<CheckBox>(R.id.checkbox_reason_3) to FeedbackReason.UI_LAYOUT,
            view.findViewById<CheckBox>(R.id.checkbox_reason_4) to FeedbackReason.RESPONSE_TIME,
            checkboxOther to FeedbackReason.OTHER,
        )
    }

    private fun setupListeners() {
        reasonCheckboxes.forEach { (checkbox, reason) ->
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (reason == FeedbackReason.OTHER) {
                    if (isChecked) editTextOther.visible() else editTextOther.gone()
                }
                feedbackViewModel.toggleReason(reason, isChecked)
                updateContinueState()
            }
        }
    }

    fun isFeedbackValid(): Boolean = feedbackViewModel.isFeedbackValid()

    private fun collectFeedback(): String = buildString {
        val reasonLines = mapOf(
            FeedbackReason.FEATURE_ISSUE to "- Tinh nang kho su dung",
            FeedbackReason.INFORMATION_UNCLEAR to "- Thong tin ve san pham dich vu khong ro rang",
            FeedbackReason.UI_LAYOUT to "- Giao dien, bo cuc sap xep chua hop ly",
            FeedbackReason.RESPONSE_TIME to "- Thoi gian phan hoi cua ung dung cham",
        )

        reasonCheckboxes.forEach { (checkbox, reason) ->
            if (!checkbox.isChecked) return@forEach

            if (reason == FeedbackReason.OTHER) {
                append("- Ly do khac: ${editTextOther.text}\n")
                return@forEach
            }

            val line = reasonLines[reason] ?: return@forEach
            append(line).append('\n')
        }
    }

    private fun updateContinueState() {
        (activity as? MainActivity)?.setContinueEnabled(isFeedbackValid())
    }

    fun onContinuePressed() {
        val feedback = collectFeedback()
        feedbackViewModel.updateCustomReason(editTextOther.text.toString())
        feedbackViewModel.submitFeedback()

        val fragment = EndFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_FEEDBACK, feedback)
            }
        }
        (activity as? MainActivity)?.replaceFragment(fragment)
    }

    companion object {
        private const val ARG_RATING = "rating"
        private const val ARG_FEEDBACK = "feedback"
    }
}
