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

    private lateinit var checkboxReason5: CheckBox
    private lateinit var editTextReason5: EditText
    private lateinit var allCheckboxes: List<CheckBox>

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
        checkboxReason5 = view.findViewById(R.id.checkbox_reason_5)
        editTextReason5 = view.findViewById(R.id.editText_checkBox5)

        allCheckboxes = listOf(
            view.findViewById(R.id.checkbox_reason_1),
            view.findViewById(R.id.checkbox_reason_2),
            view.findViewById(R.id.checkbox_reason_3),
            view.findViewById(R.id.checkbox_reason_4),
            checkboxReason5
        )
    }

    private fun setupListeners() {
        checkboxReason5.setOnCheckedChangeListener { _, isChecked ->
            editTextReason5.visibility = if (isChecked) View.VISIBLE else View.GONE
            feedbackViewModel.toggleReason(FeedbackReason.OTHER, isChecked)
            updateContinueState()
        }

        allCheckboxes.forEachIndexed { index, checkbox ->
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                val reason = when (index) {
                    0 -> FeedbackReason.FEATURE_ISSUE
                    1 -> FeedbackReason.INFORMATION_UNCLEAR
                    2 -> FeedbackReason.UI_LAYOUT
                    3 -> FeedbackReason.RESPONSE_TIME
                    else -> FeedbackReason.OTHER
                }
                feedbackViewModel.toggleReason(reason, isChecked)
                updateContinueState()
            }
        }
    }

    fun isFeedbackValid(): Boolean = feedbackViewModel.isFeedbackValid()

    private fun collectFeedback(): String = buildString {
        if (view?.findViewById<CheckBox>(R.id.checkbox_reason_1)?.isChecked == true)
            append("- Tinh nang kho su dung\n")
        if (view?.findViewById<CheckBox>(R.id.checkbox_reason_2)?.isChecked == true)
            append("- Thong tin ve san pham dich vu khong ro rang\n")
        if (view?.findViewById<CheckBox>(R.id.checkbox_reason_3)?.isChecked == true)
            append("- Giao dien, bo cuc sap xep chua hop ly\n")
        if (view?.findViewById<CheckBox>(R.id.checkbox_reason_4)?.isChecked == true)
            append("- Thoi gian phan hoi cua ung dung cham\n")
        if (checkboxReason5.isChecked)
            append("- Ly do khac: ${editTextReason5.text}\n")
    }

    private fun updateContinueState() {
        (activity as? MainActivity)?.setContinueEnabled(isFeedbackValid())
    }

    fun onContinuePressed() {
        val feedback = collectFeedback()
        feedbackViewModel.updateCustomReason(editTextReason5.text.toString())
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

