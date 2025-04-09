package vn.leeon.eidsdk.utils

import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import java.io.IOException
import java.io.StringWriter
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.*

object StringUtils {
    private val hexArray = "0123456789ABCDEF".toCharArray()
    fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = hexArray[v.ushr(4)]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    /**
     * Converts a [X509Certificate] instance into a Base-64 encoded string (PEM format).
     *
     * @param x509Cert A X509 Certificate instance
     * @return PEM formatted String
     * @throws CertificateEncodingException
     */
    @Throws(IOException::class)
    fun convertToBase64PEMString(x509Cert: Certificate?): String? {
        val sw = StringWriter()

        JcaPEMWriter(sw).use { pw ->
            pw.writeObject(x509Cert)
        }

        return sw.toString()
    }

    /**
     * Convert cert to Base 64.
     *
     * @param cert - Certificate string
     * @returns Base64 string
     */
    @Throws(IOException::class)
    fun encodeToBase64String(x509Cert: Certificate?): String? {
        val pem = convertToBase64PEMString(x509Cert)
        if (pem != null) {
            return Base64.getEncoder().encodeToString(pem.toByteArray())
        } else {
            return null
        }
    }

    /**
     * Get the province of the address.
     *
     * @param address
     * @return province
     */
    fun getProvince(address: String?): String? {
        val strs = address?.split(",")
        if (strs != null) {
            return normalizeVietnamese(strs.last().lowercase().replace("\\s".toRegex(),""))
        }
        return null
    }

    /**
     * Remove all Vietnamese characters.
     *
     * @input str
     * @return str
     */
    fun normalizeVietnamese(str: String): String? {
        var str = str
        str = str.replace("à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ".toRegex(), "a")
        str = str.replace("è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ".toRegex(), "e")
        str = str.replace("ì|í|ị|ỉ|ĩ".toRegex(), "i")
        str = str.replace("ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ".toRegex(), "o")
        str = str.replace("ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ".toRegex(), "u")
        str = str.replace("ỳ|ý|ỵ|ỷ|ỹ".toRegex(), "y")
        str = str.replace("đ".toRegex(), "d")
        str = str.replace("À|Á|Ạ|Ả|Ã|Â|Ầ|Ấ|Ậ|Ẩ|Ẫ|Ă|Ằ|Ắ|Ặ|Ẳ|Ẵ".toRegex(), "A")
        str = str.replace("È|É|Ẹ|Ẻ|Ẽ|Ê|Ề|Ế|Ệ|Ể|Ễ".toRegex(), "E")
        str = str.replace("Ì|Í|Ị|Ỉ|Ĩ".toRegex(), "I")
        str = str.replace("Ò|Ó|Ọ|Ỏ|Õ|Ô|Ồ|Ố|Ộ|Ổ|Ỗ|Ơ|Ờ|Ớ|Ợ|Ở|Ỡ".toRegex(), "O")
        str = str.replace("Ù|Ú|Ụ|Ủ|Ũ|Ư|Ừ|Ứ|Ự|Ử|Ữ".toRegex(), "U")
        str = str.replace("Ỳ|Ý|Ỵ|Ỷ|Ỹ".toRegex(), "Y")
        str = str.replace("Đ".toRegex(), "D")
        return str
    }

}
