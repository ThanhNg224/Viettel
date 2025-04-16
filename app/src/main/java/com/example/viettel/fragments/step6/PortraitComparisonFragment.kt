package com.example.viettel.fragments.step6

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.viettel.R
import com.example.viettel.utils.ProgressUtils
import com.example.viettel.viewmodel.DocumentViewModel
import com.example.viettel.viewmodel.PortraitAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

class PortraitComparisonFragment : Fragment() {

    private val docViewModel: DocumentViewModel by activityViewModels()

    // Left side views (face comparison)
    private lateinit var txtMatchResult: TextView
    private lateinit var imgSmilePortrait: ImageView
    private lateinit var imgChipPortrait: ImageView

    // Right side views (Customer Info)
    private lateinit var txtUserNameValue: TextView
    private lateinit var txtDOBValue: TextView
    private lateinit var txtGenderValue: TextView
    private lateinit var txtNationalityValue: TextView
    private lateinit var txtDocNumberValue: TextView
    private lateinit var txtPersonalIdValue: TextView
    // Additional info fields:
    private lateinit var txtFatherNameValue: TextView
    private lateinit var txtMotherNameValue: TextView
    private lateinit var txtPlaceOfOriginValue: TextView
    private lateinit var txtPlaceOfResidenceValue: TextView
    private lateinit var txtReligionValue: TextView
    private lateinit var txtEthnicityValue: TextView
    private lateinit var txtDateOfIssueValue: TextView
    private lateinit var txtDateExpiryValue: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_portrait_comparison, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ProgressUtils.animateProgressToStep(view, 6)

        // Left side initialization
        txtMatchResult = view.findViewById(R.id.txtMatchResult)
        imgSmilePortrait = view.findViewById(R.id.imgSmilePortrait)
        imgChipPortrait = view.findViewById(R.id.imgChipPortrait)

        val smileBitmap = docViewModel.portraitActions[PortraitAction.SMILE]
        val chipBitmap = docViewModel.chipPortrait

        smileBitmap?.let { imgSmilePortrait.setImageBitmap(it) }
        chipBitmap?.let { imgChipPortrait.setImageBitmap(it) }

        if (smileBitmap != null && chipBitmap != null) {
            callFaceMatchApi(smileBitmap, chipBitmap)
        } else {
            updateMatchUI((-1).toDouble())
        }

        // Right side: Initialize all customer info views
        txtUserNameValue = view.findViewById(R.id.txtUserNameValue)
        txtDOBValue = view.findViewById(R.id.txtDOBValue)
        txtGenderValue = view.findViewById(R.id.txtGenderValue)
        txtNationalityValue = view.findViewById(R.id.txtNationalityValue)
        txtDocNumberValue = view.findViewById(R.id.txtDocNumberValue)
        txtPersonalIdValue = view.findViewById(R.id.txtPersonalIdValue)
        txtFatherNameValue = view.findViewById(R.id.txtFatherNameValue)
        txtMotherNameValue = view.findViewById(R.id.txtMotherNameValue)
        txtPlaceOfOriginValue = view.findViewById(R.id.txtPlaceOfOriginValue)
        txtPlaceOfResidenceValue = view.findViewById(R.id.txtPlaceOfResidenceValue)
        txtReligionValue = view.findViewById(R.id.txtReligionValue)
        txtEthnicityValue = view.findViewById(R.id.txtEthnicityValue)
        txtDateOfIssueValue = view.findViewById(R.id.txtDateOfIssueValue)
        txtDateExpiryValue = view.findViewById(R.id.txtDateExpiryValue)

        // Update the UI with the saved user info (if available)
        docViewModel.userInfo?.let { info ->
            txtUserNameValue.text = info.fullName ?: "-"
            txtDOBValue.text = info.dateOfBirth ?: "-"
            txtGenderValue.text = info.gender ?: "-"
            txtNationalityValue.text = info.nationality ?: "-"
            txtDocNumberValue.text = info.eidNumber ?: "-"
            txtPersonalIdValue.text = info.personalIdentification ?: "-"
            txtFatherNameValue.text = info.fatherName ?: "-"
            txtMotherNameValue.text = info.motherName ?: "-"
            txtPlaceOfOriginValue.text = info.placeOfOrigin ?: "-"
            txtPlaceOfResidenceValue.text = info.placeOfResidence ?: "-"
            txtReligionValue.text = info.religion ?: "-"
            txtEthnicityValue.text = info.ethnicity ?: "-"
            txtDateOfIssueValue.text = info.dateOfIssue ?: "-"
            txtDateExpiryValue.text = info.dateOfExpiry ?: "-"
        } ?: run {
            txtUserNameValue.text = "Chưa có thông tin"
        }
    }

    private fun callFaceMatchApi(img1: Bitmap, img2: Bitmap) {
        lifecycleScope.launch(Dispatchers.IO) {
            var conn: HttpURLConnection? = null
            var errorMessage = "Lỗi không xác định khi gọi API"
            var matchScore = -1.0

            try {
                val base64_1 = bitmapToBase64(img1)
                val base64_2 = bitmapToBase64(img2)

                val jsonBody = JSONObject().apply {
                    put("img1_base64", base64_1)
                    put("img2_base64", base64_2)
                }

                val url = URL("https://face-engine-api.atin.vn/api/v1/match")
                conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.connectTimeout = 15000
                conn.readTimeout = 10000
                conn.doOutput = true
                conn.doInput = true

                conn.outputStream.use { os ->
                    os.write(jsonBody.toString().toByteArray(Charsets.UTF_8))
                    os.flush()
                }

                val responseCode = conn.responseCode
                Log.d("PortraitMatch", "API Response Code: $responseCode")

                if (responseCode in 200..299) {
                    val response = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    Log.d("PortraitMatch", "API Response Success: $response")
                    val jsonResponse = JSONObject(response)
                    matchScore = jsonResponse.optDouble("data", -1.0)
                } else {
                    val errorResponse = conn.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                        ?: "Không có thông tin lỗi từ server."
                    Log.e("PortraitMatch", "API Response Error ($responseCode): $errorResponse")
                    errorMessage = "Lỗi từ server ($responseCode): $errorResponse"
                }
            } catch (e: Exception) {
                Log.e("PortraitMatch", "API call failed Exception", e)
                errorMessage = "Lỗi khi gọi API: ${e.message}"
            } finally {
                conn?.disconnect()
                withContext(Dispatchers.Main) {
                    if (matchScore != -1.0) {
                        updateMatchUI(matchScore)
                    } else {
                        txtMatchResult.text = errorMessage
                        txtMatchResult.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.red_dark
                            )
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun updateMatchUI(score: Double) {
        if (score < 0) {
            txtMatchResult.text = "Không thể so sánh chân dung"
            txtMatchResult.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_dark))
            return
        }
        val percent = (score * 100)
        val percentText = String.format("%.1f", percent)
        val isMatch = score >= 0.6

        txtMatchResult.text = if (isMatch) {
            "Chân dung khách hàng trùng khớp: $percentText%"
        } else {
            "Chân dung khách hàng không trùng khớp: $percentText%"
        }

        val colorRes = if (isMatch) R.color.green_light else R.color.red_dark
        txtMatchResult.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}