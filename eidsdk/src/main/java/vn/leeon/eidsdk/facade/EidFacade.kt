package vn.leeon.eidsdk.facade

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.text.Text
import io.fotoapparat.preview.Frame
import io.reactivex.disposables.Disposable
import org.jmrtd.lds.icao.MRZInfo
import vn.leeon.eidsdk.jmrtd.MRTDTrustStore
import vn.leeon.eidsdk.ocr.FrameMetadata
import vn.leeon.eidsdk.ocr.GraphicOverlay
import vn.leeon.eidsdk.ocr.OcrMrzDetectorProcessor
import vn.leeon.eidsdk.ocr.VisionProcessorBase
import vn.leeon.eidsdk.utils.NfcDocumentTag
import vn.leeon.eidsdk.utils.OcrUtils
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Base64


object EidFacade {
    private val mlkitCallbacks: MutableList<WeakReference<CameraOcrCallback>> = mutableListOf()
    private var frameProcessor = OcrMrzDetectorProcessor()
    private var isDecoding = false

    private fun retrievePublicKey(context: Context): PublicKey {
        // Load the public key from assets
        val inputStream: InputStream = context.assets.open("public.pem")
        val reader = BufferedReader(InputStreamReader(inputStream))
        val publicKeyPem = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            publicKeyPem.append(line)
        }
        reader.close()
        inputStream.close()

        var publicKeyStr = publicKeyPem.toString()
        publicKeyStr = publicKeyStr.replace("-----BEGIN PUBLIC KEY-----", "");
        publicKeyStr = publicKeyStr.replace("-----END PUBLIC KEY-----", "");


        val keyBytes = Base64.getDecoder().decode(publicKeyStr)
        val keySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec)
    }

    fun verifyRsaSignature(plainText: String, signature: String, publicKey: PublicKey): Boolean {
        return try {
            val publicSignature: Signature = Signature.getInstance("SHA256withRSA")
            publicSignature.initVerify(publicKey)
            publicSignature.update(plainText.toByteArray(Charsets.UTF_8))

            val signatureBytes: ByteArray = Base64.getDecoder().decode(signature)
            publicSignature.verify(signatureBytes)
        } catch (_: Exception) {
            false
        }
    }

    fun verifyRsaSignatureDefault(context: Context, plainText: String, signature: String): Boolean {
        return try {
            val publicKey = retrievePublicKey(context)
            verifyRsaSignature(plainText, signature, publicKey)
        } catch (_: Exception) {
            false
        }
    }

    fun handleDocumentNfcTag(
        context: Context, mrzInfo: MRZInfo, eidCallback: EidCallback
    ): Disposable {
        return NfcDocumentTag().handleTag(context, mrzInfo, MRTDTrustStore(), eidCallback)
    }

    fun broadcastOcrEvent(status: Boolean, mrzInfo: MRZInfo?, e: Exception?) {
        mlkitCallbacks.filter { it.get() != null }.forEach {
            if (status) {
                it.get()?.onEidRead(mrzInfo!!)
            } else {
                it.get()?.onError(e)
            }
        }
    }

    fun registerOcrListener(cb: CameraOcrCallback) {
        mlkitCallbacks.add(WeakReference(cb))
    }

    fun getCallbackFrameProcessor(rotation: Int):
            io.fotoapparat.preview.FrameProcessor = object : io.fotoapparat.preview.FrameProcessor {
        override fun process(frame: Frame) {
            if (isDecoding) return
            isDecoding = true

            frameProcessor.process(
                frame = frame,
                rotation = rotation,
                graphicOverlay = null,
                true,
                listener = object : VisionProcessorBase.Listener<Text> {
                    override fun onSuccess(
                        results: Text,
                        frameMetadata: FrameMetadata?,
                        timeRequired: Long,
                        bitmap: Bitmap?,
                        graphicOverlay: GraphicOverlay?
                    ) {
                        OcrUtils.processOcr(
                            results = results,
                            timeRequired = timeRequired,
                            callback = object : OcrUtils.MRZCallback {
                                override fun onMRZRead(mrzInfo: MRZInfo, timeRequired: Long) {
                                    broadcastOcrEvent(true, mrzInfo, null)
                                    isDecoding = false
                                }

                                override fun onMRZReadFailure(timeRequired: Long) {
                                    broadcastOcrEvent(false, null, null)
                                    isDecoding = false
                                }

                                override fun onFailure(e: Exception, timeRequired: Long) {
                                    broadcastOcrEvent(false, null, e)
                                    isDecoding = false
                                }
                            }
                        )
                    }

                    override fun onCanceled(timeRequired: Long) {
                        broadcastOcrEvent(false, null, null)
                        isDecoding = false
                    }

                    override fun onFailure(e: Exception, timeRequired: Long) {
                        broadcastOcrEvent(false, null, e)
                        isDecoding = false
                    }

                    override fun onCompleted(timeRequired: Long) {
                        broadcastOcrEvent(false, null, null)
                        isDecoding = false
                    }
                }
            )
        }
    }
}

interface CameraOcrCallback {
    fun onEidRead(mrzInfo: MRZInfo)
    fun onError(e: Exception?)
}