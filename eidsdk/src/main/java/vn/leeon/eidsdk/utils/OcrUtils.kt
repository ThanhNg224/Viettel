package vn.leeon.eidsdk.utils

import com.google.mlkit.vision.text.Text
import net.sf.scuba.data.Gender
import org.jmrtd.lds.icao.MRZInfo
import java.util.*
import java.util.regex.Pattern

object OcrUtils {

    private val patternLine1 = Pattern.compile("[0-9IDVNM]{5}(?<documentNumber>[0-9ILDSOG]{9})(?<checkDigitDocumentNumber>[0-9ILDSOG])(?<fullDocumentNumber>[0-9ILDSOG]{12})[A-Z<]{2}[0-9]")
    private val patternLine2 = Pattern.compile("(?<dateOfBirth>[0-9ILDSOG]{6})(?<checkDigitDateOfBirth>[0-9ILDSOG])(?<sex>[FM<])(?<expirationDate>[0-9ILDSOG]{6})(?<checkDigitExpiration>[0-9ILDSOG])(?<nationality>[A-Z<]{3}).+[0-9]")
    private val patternLine3 = Pattern.compile("-\\w+[A-Z]<[A-Z<]\\w+[A-Z<].+-")

    fun processOcr(
        results: Text,
        timeRequired: Long,
        callback: MRZCallback
    ){
        var fullRead = ""
        val blocks = results.textBlocks
        for (i in blocks.indices) {
            var temp = ""
            val lines = blocks[i].lines
            for (j in lines.indices) {
                temp += lines[j].text + "-"
            }
            temp = temp.replace("\r".toRegex(), "").replace("\n".toRegex(), "").replace("\t".toRegex(), "").replace(" ", "")
            fullRead += "$temp-"
        }
        fullRead = fullRead.uppercase(Locale.getDefault())

        val matcherLineIeIDTypeLine1 = patternLine1.matcher(fullRead)
        val matcherLineIeIDTypeLine2 = patternLine2.matcher(fullRead)
        val matcherLineIeIDTypeLine3 = patternLine3.matcher(fullRead)

        if (matcherLineIeIDTypeLine1.find() && matcherLineIeIDTypeLine2.find() && matcherLineIeIDTypeLine3.find()) {

            val documentNumber = cleanDate(matcherLineIeIDTypeLine1.group(1) ?: "")
            val dateOfBirthDay = cleanDate(matcherLineIeIDTypeLine2.group(1) ?: "")
            val sex = matcherLineIeIDTypeLine2.group(3) ?: ""
            val dateOfExpiry = cleanDate(matcherLineIeIDTypeLine2.group(4) ?: "")
            val nationality = matcherLineIeIDTypeLine2.group(6) ?: ""

            var gender = Gender.UNKNOWN
            if (sex.equals("M", ignoreCase = true)) {
                gender = Gender.MALE
            } else if (sex.equals("F", ignoreCase = true)) {
                gender = Gender.FEMALE
            }
            val mrzInfo = createMRZTD(
                issuingState = nationality,
                documentNumber = documentNumber,
                dateOfBirth = dateOfBirthDay,
                gender = gender,
                dateOfExpiry = dateOfExpiry,
                nationality = nationality
            )
            callback.onMRZRead(mrzInfo, timeRequired)
        } else { // No Success
            callback.onMRZReadFailure(timeRequired)
        }
    }

    private fun createMRZTD(
        issuingState: String,
        documentNumber: String,
        dateOfBirth: String,
        gender: Gender,
        dateOfExpiry: String,
        nationality: String
    ): MRZInfo {
        return MRZInfo.createTD1MRZInfo(
            "I",
            issuingState,
            documentNumber,
            null,
            dateOfBirth,
            gender,
            dateOfExpiry,
            nationality,
            null,
            "primaryIdentifier",
            "secondaryIdentifier"
        )
    }

    private fun cleanDate(date: String): String {
        var tempDate = date
        tempDate = tempDate.replace("I".toRegex(), "1")
        tempDate = tempDate.replace("L".toRegex(), "1")
        tempDate = tempDate.replace("D".toRegex(), "0")
        tempDate = tempDate.replace("O".toRegex(), "0")
        tempDate = tempDate.replace("S".toRegex(), "5")
        tempDate = tempDate.replace("G".toRegex(), "6")
        return tempDate
    }

    interface MRZCallback {
        fun onMRZRead(mrzInfo: MRZInfo, timeRequired: Long)
        fun onMRZReadFailure(timeRequired: Long)
        fun onFailure(e: Exception, timeRequired: Long)
    }
}