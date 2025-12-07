package com.example.viettel.feature.identity.data.mapper

import android.graphics.Bitmap
import com.example.viettel.feature.identity.domain.entity.EidData
import com.example.viettel.feature.identity.domain.entity.EidPersonalInfo
import vn.leeon.eidsdk.data.Eid
import vn.leeon.eidsdk.jmrtd.FeatureStatus
import vn.leeon.eidsdk.jmrtd.VerificationStatus
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale

class EidMapper {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)

    fun fromSdk(eid: Eid): EidData {
        val personalInfo = eid.personOptionalDetails?.let { details ->
            EidPersonalInfo(
                fullName = details.fullName,
                documentNumber = details.eidNumber,
                personalIdentification = details.personalIdentification,
                dateOfBirth = details.dateOfBirth,
                gender = details.gender,
                nationality = details.nationality,
                fatherName = details.fatherName,
                motherName = details.motherName,
                placeOfOrigin = details.placeOfOrigin,
                placeOfResidence = details.placeOfResidence,
                religion = details.religion,
                ethnicity = details.ethnicity,
                dateOfIssue = details.dateOfIssue,
                dateOfExpiry = details.dateOfExpiry,
            )
        }

        val certificateSummary = eid.sodFile?.docSigningCertificate?.let { cert ->
            val sha1 = try {
                MessageDigest.getInstance("SHA-1").digest(cert.encoded)
                    .joinToString("") { "%02X".format(it) }
            } catch (_: Exception) {
                "N/A"
            }
            buildString {
                appendLine("Serial: ${cert.serialNumber}")
                appendLine("Public Key: ${cert.publicKey.algorithm}")
                appendLine("Signature Algorithm: ${cert.sigAlgName}")
                appendLine("Thumbprint (SHA-1): $sha1")
                appendLine("Issuer: ${cert.issuerDN.name}")
                appendLine("Subject: ${cert.subjectDN.name}")
                appendLine("Valid From: ${dateFormat.format(cert.notBefore)}")
                appendLine("Valid To: ${dateFormat.format(cert.notAfter)}")
            }
        }

        val verificationSummary = buildVerificationStatusText(
            eid.verificationStatus,
            eid.featureStatus
        )

        val chipPortrait = eid.face?.let(::bitmapToByteArray)

        return EidData(
            personalInfo = personalInfo,
            chipPortrait = chipPortrait,
            certificateSummary = certificateSummary,
            verificationSummary = verificationSummary,
        )
    }

    private fun buildVerificationStatusText(vs: VerificationStatus?, fs: FeatureStatus?): String? {
        if (vs == null || fs == null) return null
        fun verdictToStr(v: VerificationStatus.Verdict?): String = when (v) {
            VerificationStatus.Verdict.SUCCEEDED -> "Success"
            VerificationStatus.Verdict.FAILED -> "Failed"
            else -> "Unknown"
        }
        return buildString {
            appendLine("Document check (HT): ${verdictToStr(vs.ht)}")
            appendLine("Chip Auth (CA): ${verdictToStr(vs.ca)}")
            appendLine("Country Signing (CS): ${verdictToStr(vs.cs)}")
            appendLine("Active Auth (AA): ${verdictToStr(vs.aa)}")
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 92, outputStream)
        return outputStream.toByteArray()
    }
}
