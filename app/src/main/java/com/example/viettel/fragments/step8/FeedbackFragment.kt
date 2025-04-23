package com.example.viettel.fragments.step8

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView

import androidx.fragment.app.Fragment
import com.example.viettel.R
import com.example.viettel.activities.MainActivity

import com.example.viettel.utils.ProgressUtils

class FeedbackFragment : Fragment() {

    private lateinit var checkboxReason5: CheckBox
    private lateinit var editTextReason5: EditText
    private lateinit var allCheckboxes: List<CheckBox>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_feedback, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ProgressUtils.animateProgressToStep(view, 8)

        val rating = arguments?.getInt("rating") ?: 0
        val tvInstruction = view.findViewById<TextView>(R.id.tvInstruction)
        tvInstruction.text = "Bạn đã đánh giá $rating sao. Vui lòng chọn lý do không hài lòng dưới đây:"

        checkboxReason5 = view.findViewById(R.id.checkbox_reason_5)
        editTextReason5 = view.findViewById(R.id.editText_checkBox5)

        allCheckboxes = listOf(
            view.findViewById(R.id.checkbox_reason_1),
            view.findViewById(R.id.checkbox_reason_2),
            view.findViewById(R.id.checkbox_reason_3),
            view.findViewById(R.id.checkbox_reason_4),
            checkboxReason5
        )

        checkboxReason5.setOnCheckedChangeListener { _, isChecked ->
            editTextReason5.visibility = if (isChecked) View.VISIBLE else View.GONE
            updateContinueState()
        }

        allCheckboxes.forEach { checkbox ->
            checkbox.setOnCheckedChangeListener { _, _ ->
                updateContinueState()
            }
        }

    }

    // 🔍 MainActivity checks this to know if it should enable Continue button
    fun isFeedbackValid(): Boolean {
        return allCheckboxes.any { it.isChecked }
    }

    private fun collectFeedback(): String = buildString {
        if (view?.findViewById<CheckBox>(R.id.checkbox_reason_1)?.isChecked == true)
            append("- Tính năng khó sử dụng\n")
        if (view?.findViewById<CheckBox>(R.id.checkbox_reason_2)?.isChecked == true)
            append("- Thông tin về sản phẩm, dịch vụ không rõ ràng\n")
        if (view?.findViewById<CheckBox>(R.id.checkbox_reason_3)?.isChecked == true)
            append("- Giao diện, bố cục sắp xếp chưa hợp lý\n")
        if (view?.findViewById<CheckBox>(R.id.checkbox_reason_4)?.isChecked == true)
            append("- Thời gian phản hồi của ứng dụng chậm\n")
        if (checkboxReason5.isChecked)
            append("- Lý do khác: ${editTextReason5.text}\n")
    }
    private fun updateContinueState() {
        val enabled = isFeedbackValid()
        (activity as? MainActivity)?.setContinueEnabled(enabled)
    }
    fun onContinuePressed() {
        val feedback = collectFeedback()
        val fragment = EndFragment().apply {
            arguments = Bundle().apply {
                putString("feedback", feedback)
            }
        }
        (activity as? MainActivity)?.replaceFragment(fragment)
    }


}
