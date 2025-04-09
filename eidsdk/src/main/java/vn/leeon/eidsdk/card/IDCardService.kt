package vn.leeon.eidsdk.card

import android.util.Log
import com.card.manager.RFCardManager
import net.sf.scuba.smartcards.APDUEvent
import net.sf.scuba.smartcards.CardService
import net.sf.scuba.smartcards.CardServiceException
import net.sf.scuba.smartcards.CommandAPDU
import net.sf.scuba.smartcards.ResponseAPDU

class IDCardService(private val mManager: RFCardManager) : CardService() {

    //    private val mManager: RFCardManager by lazy {
//        RFCardManager.getInstance()
//    }
    var bytes: ByteArray? = null
    var started = false
    fun send(bytes: ByteArray) {

    }

//    init {
//        mManager.open(context)
//
//        mManager.reset()
//    }


    val cmd1 = byteArrayOf(0, -92, 2, 12, 2, 1, 28)
    val cmd2 = byteArrayOf(0, -80, 0, 0, 8)
    val cmd3 = byteArrayOf(0, -80, 0, 8, 46)

    private var chipPowerOn: Boolean = false
    private var apduCount: Int = 0

    override fun open() {
        chipPowerOn = true
//        mManager.setReadListener {
//            if (bytes != null) {
//                mManager.apdu(bytes)
//            } else null
////            return@setReadListener
//        }
//        mManager.start { it: ByteArray? ->
//            Log.d("TAG", "open: ${it.contentToString()}")
//        }
//        apduCount = 0
//        mManager.stop()
        Log.d("TAG111", "open: 1111")
//        if (mManager.open(context)) {
//            chipPowerOn = true
//        } else {
//            chipPowerOn = false
//            throw CardServiceException("Failed to connect")
//        }

    }

    fun send(string: String) {

    }

    override fun isOpen(): Boolean {
        if (chipPowerOn) {
            this.state = 1
        } else {
            this.state = 0
        }
        return chipPowerOn
    }

    override fun transmit(commandAPDU: CommandAPDU): ResponseAPDU {
        return try {

//            val ulOutChipDataLength = IntArray(2)
//            val lpOutbChipData = ByteArray(512)
//            val lpInbChipData: ByteArray = commandAPDU.bytes
//            val ulInChipDataLength = lpInbChipData.size
            val command = bytesToHexString(commandAPDU.bytes)
            var res = mManager.apdu(commandAPDU.bytes)
            var bytes = res
//            if (iRet == 0) {
            var recv = ""
//                val resLen = ulOutChipDataLength[0]
//                val responseBytes = ByteArray(resLen)
//                System.arraycopy(lpOutbChipData, 0, responseBytes, 0, resLen)
//                for (i in 0 until ulOutChipDataLength[0]) {
//                    val ch = lpOutbChipData[i].toInt()
//                    val ch1 = ch shr 4 and 0x0000000f
//                    val ch2 = ch and 0x0000000f
//                    recv += Integer.toHexString(ch1) + Integer.toHexString(ch2)
//                }

            Log.d(
                "TAG",
                "transmit: ${commandAPDU.bytes.contentToString()} ${bytes.contentToString()}"
            )
            if (res == null) {
                Log.d(
                    "TAG11",
                    "transmit: ${mManager.isOpen} ${mManager.isContinue} ${commandAPDU.bytes.contentToString()} $res"
                )
                mManager.reset()
                bytes = mManager.apdu(commandAPDU.bytes)

            }
//            val bytes = hexStringToBytes(res.replace(" ", ""))
            if (bytes.size >= 2) {

                val outResponseAPDU = ResponseAPDU(bytes)
                notifyExchangedAPDU(
                    APDUEvent(
                        this,
                        "ISODep",
                        ++apduCount,
                        commandAPDU,
                        outResponseAPDU
                    )
                )
                outResponseAPDU
            } else {
                throw CardServiceException("Failed response")
            }
//            } else {
//                throw CardServiceException("Failed response")
//            }
        } catch (var4: CardServiceException) {
            throw var4
        } catch (var5: java.lang.Exception) {
            Log.e("TAG", var5.stackTraceToString())
            throw CardServiceException("Could not tranceive APDU", var5)
        }
    }

    override fun getATR(): ByteArray? {
        return null
    }

    override fun close() {
//        mManager.close()
    }

    override fun isConnectionLost(e: Exception?): Boolean {
        return if (this.isDirectConnectionLost(e)) {
            true
        } else if (e == null) {
            false
        } else {
            var cause: Throwable?
            var rootCause: Throwable? = e
            while (null != rootCause!!.cause.also { cause = it } && rootCause !== cause) {
                rootCause = cause
                if (this.isDirectConnectionLost(cause)) {
                    return true
                }
            }
            false
        }
    }

    private fun isDirectConnectionLost(e: Throwable?): Boolean {
        if (!chipPowerOn) {
            return true
        } else if (e == null) {
            return false
        } else {
            if (e.javaClass.name.contains("TagLostException")) {
                return true
            } else {
                val message = e.message ?: ""
                if (message.lowercase().contains("tag was lost")) {
                    return true
                } else {
                    if (e is CardServiceException) {
                        if (message.lowercase().contains("not connected")) {
                            return true
                        }
                        if (message.lowercase().contains("failed response")) {
                            return true
                        }
                    }
                    return false
                }
            }
        }
    }

    fun hexStringToBytes(string: String): ByteArray {

        return string.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }

    fun bytesToHexString(bytes: ByteArray): String {
        val res = StringBuilder()
        for (ch in bytes) {
            val ch1 = ch.toInt() shr 4 and 0x0000000f
            val ch2 = ch.toInt() and 0x0000000f
            res.append(Integer.toHexString(ch1)).append(Integer.toHexString(ch2))
        }
        return res.toString()
    }
}