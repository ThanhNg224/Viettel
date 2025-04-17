package com.example.viettel.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.viettel.R
import com.example.viettel.activities.MainActivity

class ServiceEvaluationFragment : Fragment() {

    private lateinit var emojiButtons: List<ImageButton>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_service_evaluation, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emojiButtons = listOf(
            view.findViewById(R.id.emoji1),
            view.findViewById(R.id.emoji2),
            view.findViewById(R.id.emoji3),
            view.findViewById(R.id.emoji4),
            view.findViewById(R.id.emoji5),
            view.findViewById(R.id.emoji6),
            view.findViewById(R.id.emoji7)
        )

        emojiButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                val rating = index + 1

                val targetFragment = if (rating <= 4) {
                    FeedbackFragment().apply {
                        arguments = Bundle().apply {
                            putInt("rating", rating)
                        }
                    }
                } else {
                    EndFragment().apply {
                        arguments = Bundle().apply {
                            putString("feedback", "Người dùng hài lòng (đánh giá $rating sao)")
                        }
                    }
                }

                (activity as? MainActivity)?.replaceFragment(targetFragment)
            }

        }
    }
}
