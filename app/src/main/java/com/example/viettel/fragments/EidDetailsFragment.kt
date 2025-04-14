package com.example.viettel.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.viettel.R

import com.example.viettel.viewmodel.DocumentViewModel
import vn.leeon.eidsdk.data.Eid
import vn.leeon.eidsdk.jmrtd.FeatureStatus
import vn.leeon.eidsdk.jmrtd.VerificationStatus
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale

class EidDetailsFragment : Fragment() {

    private val eid: Eid? get() = docViewModel.eid


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
    private val docViewModel: DocumentViewModel by activityViewModels()



    private lateinit var txtSignatureInfo: TextView
    private lateinit var txtVerificationStatus: TextView

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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

        view.postDelayed({
            displayCapturedImages()
            Log.d("EidDetails", "front=${docViewModel.frontImage}, back=${docViewModel.backImage}, chip=${docViewModel.chipPortrait}")

        }, 100) // wait 100ms just to ensure view + ViewModel are ready

        displayEidInfo()
    }

    private fun displayCapturedImages() {
        Log.d("EidDetails", "chipPortrait = ${docViewModel.chipPortrait}")

        docViewModel.frontImage?.let { imgFront.setImageBitmap(it) }
        docViewModel.backImage?.let { imgBack.setImageBitmap(it) }

        if (docViewModel.chipPortrait != null) {
            imgChipFace.setImageBitmap(docViewModel.chipPortrait)
            Log.d("EidDetails", "✅ chipPortrait applied to imgChipFace")
        } else {
            Log.w("EidDetails", "chipPortrait is NULL, retrying in 200ms...")
            view?.postDelayed({ displayCapturedImages() }, 200)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun displayEidInfo() {
        val pod = eid?.personOptionalDetails
        if (pod != null) {
            txtName.text = pod.fullName ?: "-"
            txtDocNumber.text = pod.eidNumber ?: "-"
            txtPersonalIdentification.text = pod.personalIdentification ?: "-"
            txtDob.text = pod.dateOfBirth ?: "-"
            txtGender.text = pod.gender ?: "-"
            txtNationality.text = pod.nationality ?: "-"
            txtFatherName.text = pod.fatherName ?: "-"
            txtMotherName.text = pod.motherName ?: "-"

            txtPlaceOfOrigin.text = pod.placeOfOrigin ?: "-"
            txtPlaceOfResidence.text = pod.placeOfResidence ?: "-"
            txtReligion.text = pod.religion ?: "-"
            txtEthnicity.text = pod.ethnicity ?: "-"
            txtDateOfIssue.text = pod.dateOfIssue ?: "-"
            txtDateExpiry.text = pod.dateOfExpiry ?: "-"
        }

        val cert = eid?.sodFile?.docSigningCertificate
        if (cert != null) {
            val sha1 = try {
                MessageDigest.getInstance("SHA-1").digest(cert.encoded).joinToString("") { "%02X".format(it) }
            } catch (e: Exception) {
                "Không thể tạo SHA-1"
            }

            txtSignatureInfo.text = buildString {
                appendLine("Serial: ${cert.serialNumber}")
                appendLine("Public Key: ${cert.publicKey.algorithm}")
                appendLine("Signature Algorithm: ${cert.sigAlgName}")
                appendLine("Thumbprint (SHA-1): $sha1")
                appendLine("Issuer DN: ${cert.issuerDN.name}")
                appendLine("Subject DN: ${cert.subjectDN.name}")
                appendLine("Valid From: ${dateFormat.format(cert.notBefore)}")
                appendLine("Valid To: ${dateFormat.format(cert.notAfter)}")
            }
        } else {
            txtSignatureInfo.text = "(Không có thông tin chứng chỉ)"
        }

        txtVerificationStatus.text = buildVerificationStatusText(
            eid?.verificationStatus,
            eid?.featureStatus
        )
    }

    private fun buildVerificationStatusText(vs: VerificationStatus?, fs: FeatureStatus?): String {
        if (vs == null || fs == null) return "Không có kết quả xác thực"

        fun verdictToStr(v: VerificationStatus.Verdict?): String {
            return when (v) {
                VerificationStatus.Verdict.SUCCEEDED -> "✔️ Thành công"
                VerificationStatus.Verdict.FAILED -> "❌ Thất bại"
                else -> "❔ Không rõ"
            }
        }

        return buildString {
            appendLine("🛡️ Kết quả xác thực chip:")
            appendLine("• Document check (HT): ${verdictToStr(vs.ht)}")
            appendLine("• Chip Auth (CA): ${verdictToStr(vs.ca)}")
            appendLine("• Country Signing (CS): ${verdictToStr(vs.cs)}")
            appendLine("• Active Auth (AA): ${verdictToStr(vs.aa)}")
        }
    }


}
