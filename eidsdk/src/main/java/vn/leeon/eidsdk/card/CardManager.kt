package vn.leeon.eidsdk.card

import android.content.Context
import android.util.Log
import com.card.manager.RFCardManager

class CardManager private constructor() {
    fun open(context: Context): Boolean {
        return mManager.open(context)
    }

    fun stop() = mManager.stop()
    fun close() = mManager.close()
    fun apdu(byteArray: ByteArray) :ByteArray {
       return mManager.apdu(byteArray)
    }

    fun reset(): Boolean {
        return mManager.reset()
    }

    private val mManager: RFCardManager by lazy {
        RFCardManager.getInstance()
    }

    companion object {
        private var mInstance: CardManager? = null
        val instance: CardManager
            get() {
                if (mInstance == null) {
                    mInstance = CardManager()
                }
                return mInstance!!
            }
    }
}