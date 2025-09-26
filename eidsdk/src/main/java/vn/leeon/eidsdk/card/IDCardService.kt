package vn.leeon.eidsdk.card

import android.util.Log
import com.card.manager.RFCardManager
import net.sf.scuba.smartcards.APDUEvent
import net.sf.scuba.smartcards.CardService
import net.sf.scuba.smartcards.CardServiceException
import net.sf.scuba.smartcards.CommandAPDU
import net.sf.scuba.smartcards.ResponseAPDU
import android.content.Context
import com.crt.Crt900x
import net.sf.scuba.smartcards.*
import java.util.concurrent.locks.ReentrantLock
import vn.leeon.eidsdk.BuildConfig


class IDCardService(
    private val context: Context,
    private val mManager: RFCardManager,
    private val readerVid: Int = 9176,
    private val readerPid: Int = 36865
) : CardService() {

    private val txLock = ReentrantLock()
    private var chipPowerOn = false
    private var rfActive = false
    private var apduCount = 0
    private val crt = Crt900x(context)

    override fun open() {
        try { crt.CrtReaderDisConnect() } catch (_: Throwable) {}
        try {
            crt.CrtSetBaseDir("/storage/emulated/0/crt_driver_Log")
            crt.CrtSetLogLevel(2)
        } catch (_: Throwable) {}
        val ret = try { crt.CrtReaderConnect(readerPid, readerVid) } catch (_: Throwable) { -1 }
        if (ret != 0) throw CardServiceException("CrtReaderConnect failed: $ret")
        try { Thread.sleep(150) } catch (_: InterruptedException) {}
        chipPowerOn = true
        rfActive = false
        apduCount = 0
        Log.d("IDCardService", "open(): reader connected (VID=$readerVid, PID=$readerPid)")
    }

    override fun close() {
        try { if (rfActive) crt.CrtReaderRFRelease() } catch (_: Throwable) {}
        try { crt.CrtCloseReader() } catch (_: Throwable) {}
        try { crt.CrtReaderDisConnect() } catch (_: Throwable) {}
        chipPowerOn = false
        rfActive = false
    }

    override fun isOpen(): Boolean = chipPowerOn

    private fun ensureReady() {
        if (!chipPowerOn) open()
        if (!rfActive) {
            val atr = ByteArray(128)
            val len = try { crt.CrtReaderRFActive(atr) } catch (_: Throwable) { -1 }
            if (len <= 0) throw CardServiceException("CrtReaderRFActive failed (len=$len)")
            Log.d("IDCardService", "ATR: ${atr.copyOf(len).contentToString()}")
            try { Thread.sleep(80) } catch (_: InterruptedException) {}
            rfActive = true
        }
    }

    override fun transmit(commandAPDU: CommandAPDU): ResponseAPDU {
        txLock.lock()
        try {
            ensureReady() // đã connect + RF active

            val cmd = commandAPDU.bytes
            if (cmd.isEmpty()) throw CardServiceException("Empty APDU command")

            // 1) GỬI TRỰC TIẾP QUA CRT NATIVE
            var resp: ByteArray? = try {
                val outBuf = ByteArray(4096)
                val outLen = IntArray(1)
                // slot '0' là phổ biến; nếu vẫn không có resp, thử '1'
                val ret = crt.CrtSendAPDU('0', cmd.size, cmd, outLen, outBuf)
                Log.d("CRT", "send ret=$ret outLen=${outLen.getOrNull(0) ?: -1}")
                if (ret == 0 && outLen.isNotEmpty() && outLen[0] > 0) {
                    outBuf.copyOf(outLen[0])
                } else null
            } catch (_: Throwable) { null }

            Log.d("TAG", "tx[crt]: open=$chipPowerOn rf=$rfActive cmd=${cmd.contentToString()} resp=${resp?.size ?: -1}")

            // 2) Fallback (nếu cần) qua mManager.apdu
            if (resp == null || resp.size < 2) {
                try {
                    // reset RF nhẹ rồi gửi lại
                    try { crt.CrtReaderRFRelease() } catch (_: Throwable) {}
                    rfActive = false
                    ensureReady()
                    Thread.sleep(100)
                } catch (_: Throwable) {}

                resp = try { mManager.apdu(cmd) } catch (_: Throwable) { null }
                Log.d("TAG", "tx[fallback-mManager]: resp=${resp?.size ?: -1}")
            }

            val bytes = resp ?: throw CardServiceException("transceive() returned null")
            if (bytes.size < 2) throw CardServiceException("APDU response too short (${bytes.size})")

            val sw1 = bytes[bytes.size - 2].toInt() and 0xFF
            val sw2 = bytes[bytes.size - 1].toInt() and 0xFF
            if (sw1 != 0x90 || sw2 != 0x00) {
                Log.w("TAG", String.format(java.util.Locale.US,
                    "Non-OK SW: %02X %02X for cmd=%s", sw1, sw2, cmd.contentToString()))
            }

            val out = ResponseAPDU(bytes)
            notifyExchangedAPDU(APDUEvent(this, "ISODep", ++apduCount, commandAPDU, out))
            return out
        } finally {
            txLock.unlock()
        }
    }



    override fun getATR(): ByteArray? {
        return null
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