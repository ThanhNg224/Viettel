package com.example.viettel.fragments.step5

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.viettel.R
import com.example.viettel.feature.identity.presentation.mapper.BitmapMapper
import com.example.viettel.feature.identity.presentation.viewmodel.IdentityViewModel
import kotlinx.coroutines.launch

class EidDetailsFragment : Fragment() {

    private val identityViewModel: IdentityViewModel by activityViewModels {
        IdentityViewModel.Factory(requireActivity().application)
    }

    private lateinit var imgFront: ImageView
    private lateinit var imgBack: ImageView
    private lateinit var imgChipFace: ImageView

    private lateinit var txtName: TextView
    private lateinit var txtDob: TextView
    private lateinit var txtGender: TextView
    private lateinit var txtNationality: TextView
    private lateinit var txtDocNumber: TextView
    private lateinit var txtFatherName: TextView
    private lateinit var txtMotherName: TextView
    private lateinit var txtPlaceOfOrigin: TextView
    private lateinit var txtPlaceOfResidence: TextView
    private lateinit var txtReligion: TextView
    private lateinit var txtEthnicity: TextView
    private lateinit var txtDateOfIssue: TextView
    private lateinit var txtDateExpiry: TextView
    private lateinit var txtPersonalIdentification: TextView
    private lateinit var txtSignatureInfo: TextView
    private lateinit var txtVerificationStatus: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_eid_details, container, false)

        imgFront = view.findViewById(R.id.imgFront)
        imgBack = view.findViewById(R.id.imgBack)
        imgChipFace = view.findViewById(R.id.imgChipFace)

        txtName = view.findViewById(R.id.txtName)
        txtDob = view.findViewById(R.id.txtDob)
        txtGender = view.findViewById(R.id.txtGender)
        txtNationality = view.findViewById(R.id.txtNationality)
        txtDocNumber = view.findViewById(R.id.txtDocNumber)

        txtFatherName = view.findViewById(R.id.txtFatherName)
        txtMotherName = view.findViewById(R.id.txtMotherName)
        txtPersonalIdentification = view.findViewById(R.id.txtPersonalIdentification)

        txtPlaceOfOrigin = view.findViewById(R.id.txtPlaceOfOrigin)
        txtPlaceOfResidence = view.findViewById(R.id.txtPlaceOfResidence)
        txtReligion = view.findViewById(R.id.txtReligion)
        txtEthnicity = view.findViewById(R.id.txtEthnicity)
        txtDateOfIssue = view.findViewById(R.id.txtDateOfIssue)
        txtDateExpiry = view.findViewById(R.id.txtDateExpiry)
        txtSignatureInfo = view.findViewById(R.id.txtSignatureInfo)
        txtVerificationStatus = view.findViewById(R.id.txtVerificationStatus)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        renderState()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                identityViewModel.uiState.collect {
                    renderState()
                }
            }
        }
    }

    private fun renderState() {
        val state = identityViewModel.uiState.value

        imgFront.setImageBitmap(BitmapMapper.fromBytes(state.frontImage, state.frontImageRotation))
        imgBack.setImageBitmap(BitmapMapper.fromBytes(state.backImage, state.backImageRotation))
        imgChipFace.setImageBitmap(BitmapMapper.fromBytes(state.eidData?.chipPortrait))

        val info = state.eidData?.personalInfo
        txtName.text = info?.fullName ?: "-"
        txtDocNumber.text = info?.documentNumber ?: "-"
        txtPersonalIdentification.text = info?.personalIdentification ?: "-"
        txtDob.text = info?.dateOfBirth ?: "-"
        txtGender.text = info?.gender ?: "-"
        txtNationality.text = info?.nationality ?: "-"
        txtFatherName.text = info?.fatherName ?: "-"
        txtMotherName.text = info?.motherName ?: "-"
        txtPlaceOfOrigin.text = info?.placeOfOrigin ?: "-"
        txtPlaceOfResidence.text = info?.placeOfResidence ?: "-"
        txtReligion.text = info?.religion ?: "-"
        txtEthnicity.text = info?.ethnicity ?: "-"
        txtDateOfIssue.text = info?.dateOfIssue ?: "-"
        txtDateExpiry.text = info?.dateOfExpiry ?: "-"

        txtSignatureInfo.text = state.eidData?.certificateSummary ?: "(Khong co thong tin chu ky)"
        txtVerificationStatus.text = state.eidData?.verificationSummary ?: "Khong co ket qua xac thuc"
    }
}
