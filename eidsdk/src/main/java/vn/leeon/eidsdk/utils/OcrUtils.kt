package vn.leeon.eidsdk.utils

import android.util.Log
import com.google.mlkit.vision.text.Text
import net.sf.scuba.data.Gender
import org.jmrtd.lds.icao.MRZInfo
import java.util.*
import java.util.regex.Pattern

object OcrUtils {

    private val TAG = OcrUtils::class.java.simpleName

    val patternLine1 = Pattern.compile("[0-9IDVNM]{5}(?<documentNumber>[0-9ILDSOG]{9})(?<checkDigitDocumentNumber>[0-9ILDSOG]{1})(?<fullDocumentNumber>[0-9ILDSOG]{12})[A-Z<]{2}[0-9]{1}")
    val patternLine2 = Pattern.compile("(?<dateOfBirth>[0-9ILDSOG]{6})(?<checkDigitDateOfBirth>[0-9ILDSOG]{1})(?<sex>[FM<]){1}(?<expirationDate>[0-9ILDSOG]{6})(?<checkDigitExpiration>[0-9ILDSOG]{1})(?<nationality>[A-Z<]{3}).+[0-9]{1}")
    val patternLine3 = Pattern.compile("[-]\\w+[A-Z][<]{1}[A-Z<]\\w+[A-Z<]{1}.+[-]")

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

            val line1 = matcherLineIeIDTypeLine1.group(0)
            val line2 = matcherLineIeIDTypeLine2.group(0)
            val line3 = matcherLineIeIDTypeLine3.group(0)

            val documentNumber = cleanDate(matcherLineIeIDTypeLine1.group(1))
            val checkDigitDocumentNumber = cleanDate(matcherLineIeIDTypeLine1.group(2))
            val fullDocumentNumber= cleanDate(matcherLineIeIDTypeLine1.group(3))

            val dateOfBirthDay = cleanDate(matcherLineIeIDTypeLine2.group(1))
            val checkDigitDateOfBirth = cleanDate(matcherLineIeIDTypeLine2.group(2))
            val sex = matcherLineIeIDTypeLine2.group(3)
            val dateOfExpiry = cleanDate(matcherLineIeIDTypeLine2.group(4))
            val checkDigitExpiration = cleanDate((matcherLineIeIDTypeLine2.group(5)))
            val nationality = matcherLineIeIDTypeLine2.group(6)

            //Log.d("TAG", "line1 = $line1 - documentNumber = $documentNumber; check = $checkDigitDocumentNumber - full: $fullDocumentNumber" )
            //Log.d("TAG", "line2 = $line2 - dateofbirth = $dateOfBirthDay; checkDigit = $checkDigitDateOfBirth; sex = $sex; dateOfExpiry = $dateOfExpiry; checkDigitExpire = $checkDigitExpiration; nationality = $nationality")
            //Log.d("TAG", "line3 = $line3")

            var gender = Gender.UNKNOWN
            if (sex.equals("M")) {
                gender = Gender.MALE
            } else if (sex.equals("F")) {
                gender = Gender.FEMALE
            }
            val mrzInfo = createMRZTD(issuingState = nationality, documentNumber = documentNumber, dateOfBirth = dateOfBirthDay, gender = gender, dateOfExpiry = dateOfExpiry, nationality = nationality)
            callback.onMRZRead(mrzInfo, timeRequired)
        } else { // No Success
            callback.onMRZReadFailure(timeRequired)
        }
    }

    private fun cleanDocumentNumber(documentNumber: String, checkDigit:Int):String?{
        //first we replace all O per 0
        var tempDcumentNumber = documentNumber.replace("O".toRegex(), "0")
        //Calculate check digit of the document number
        var checkDigitCalculated = MRZInfo.checkDigit(tempDcumentNumber).toString().toInt()
        Log.d("TAG", "checkDigitCalculated = $checkDigitCalculated")
        if (checkDigit == checkDigitCalculated) {
            //If check digits match we return the document number
            return tempDcumentNumber
        }
        //if no match, we try to replace once at a time the first 0 per O as the alpha part comes first, and check if the digits match
        var indexOfZero = tempDcumentNumber.indexOf("0")
        while (indexOfZero>-1) {
            checkDigitCalculated = MRZInfo.checkDigit(tempDcumentNumber).toString().toInt()
            if (checkDigit != checkDigitCalculated) {
                //Some countries like Spain uses a letter O before the numeric part
                indexOfZero = tempDcumentNumber.indexOf("0")
                tempDcumentNumber = tempDcumentNumber.replaceFirst("0", "O")
            }else{
                return tempDcumentNumber
            }
        }
        return null
    }

    private fun createMRZTD(issuingState: String, documentNumber: String, dateOfBirth : String, gender: Gender, dateOfExpiry : String, nationality : String) :MRZInfo {
        return MRZInfo.createTD1MRZInfo("I",
            issuingState ,
            documentNumber ,
            null,
            dateOfBirth ,
            gender ,
            dateOfExpiry ,
            nationality ,
            null,
            "primaryIdentifier",
            "secondaryIdentifier")
    }

    private fun cleanDate(date:String):String{
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