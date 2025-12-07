package com.example.viettel.feature.feedback.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.viettel.R
import com.example.viettel.activities.MainActivity
import com.example.viettel.feature.feedback.presentation.viewmodel.FeedbackViewModel
import com.example.viettel.utils.ProgressUtils
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment cho phép người dùng đánh giá dịch vụ bằng cách chọn emoji (1-7 sao).
 * Nếu đánh giá thấp (≤4 sao), chuyển sang FeedbackFragment để thu thập lý do chi tiết.
 * Nếu đánh giá cao (>4 sao), chuyển thẳng sang EndFragment.
 */
@AndroidEntryPoint
class ServiceEvaluationFragment : Fragment() {

    private lateinit var emojiButtons: List<ImageButton>
    private val feedbackViewModel: FeedbackViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_service_evaluation, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ProgressUtils.animateProgressToStep(view, 8)

        initializeEmojiButtons(view)
        setupEmojiClickListeners()
    }

    private fun initializeEmojiButtons(view: View) {
        emojiButtons = listOf(
            view.findViewById(R.id.emoji1),
            view.findViewById(R.id.emoji2),
            view.findViewById(R.id.emoji3),
            view.findViewById(R.id.emoji4),
            view.findViewById(R.id.emoji5),
            view.findViewById(R.id.emoji6),
            view.findViewById(R.id.emoji7)
        )
    }

    private fun setupEmojiClickListeners() {
        emojiButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                val rating = index + 1
                handleRatingSelection(rating)
            }
        }
    }

    private fun handleRatingSelection(rating: Int) {
        feedbackViewModel.setRating(rating)

        val targetFragment = if (rating <= RATING_THRESHOLD_FOR_FEEDBACK) {
            // Đánh giá thấp: yêu cầu feedback chi tiết
            FeedbackFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_RATING, rating)
                }
            }
        } else {
            // Đánh giá cao: kết thúc luôn
            EndFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_FEEDBACK, "Nguoi dung hai long (danh gia $rating sao)")
                }
            }
        }

        (activity as? MainActivity)?.replaceFragment(targetFragment)
    }

    companion object {
        private const val RATING_THRESHOLD_FOR_FEEDBACK = 4
        private const val ARG_RATING = "rating"
        private const val ARG_FEEDBACK = "feedback"
    }
}

