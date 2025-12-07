package com.example.viettel.fragments.step1_2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.viettel.R
import com.example.viettel.activities.MainActivity
import androidx.fragment.app.activityViewModels
import com.example.viettel.feature.identity.domain.entity.DocumentType
import com.example.viettel.feature.identity.presentation.viewmodel.IdentityViewModel
import com.example.viettel.utils.ProgressUtils

class DocumentSelectionFragment : Fragment() {

    private val identityViewModel: IdentityViewModel by activityViewModels {
        IdentityViewModel.Factory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_document_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.apply {
            setBackVisible(false)
            setContinueVisible(false)
        }



        val optionCCCD = view.findViewById<ConstraintLayout>(R.id.option1)
        val optionPassport = view.findViewById<ConstraintLayout>(R.id.option2)

        val imageOption1 = optionCCCD.findViewById<ImageView>(R.id.imageOption1)
        val imageOption2 = optionPassport.findViewById<ImageView>(R.id.imageOption2)

        ProgressUtils.animateProgressToStep(view, 1)

        optionCCCD.setOnClickListener {
            imageOption1.setBackgroundResource(R.drawable.red_circle)
            imageOption2.setBackgroundResource(R.drawable.white_circle)

            Toast.makeText(requireContext(), "CCCD selected", Toast.LENGTH_SHORT).show()

            identityViewModel.selectDocumentType(DocumentType.CCCD)
            (activity as? MainActivity)?.apply {
                setBackVisible(true)
                setContinueVisible(true)
                setContinueEnabled(true)  // once picked, continue is clickable
            }

            (activity as? MainActivity)?.replaceFragment(PlaceDocumentFragment())
        }

        optionPassport.setOnClickListener {
            imageOption1.setBackgroundResource(R.drawable.white_circle)
            imageOption2.setBackgroundResource(R.drawable.red_circle)

            Toast.makeText(requireContext(), "Passport selected", Toast.LENGTH_SHORT).show()

            identityViewModel.selectDocumentType(DocumentType.PASSPORT)

            (activity as? MainActivity)?.replaceFragment(PlaceDocumentFragment())
        }

    }
}
