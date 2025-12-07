package com.example.viettel.feature.identity.integration.eid

import android.content.Context
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.suspendCancellableCoroutine
import net.sf.scuba.smartcards.CardServiceException
import org.jmrtd.AccessDeniedException
import org.jmrtd.BACDeniedException
import org.jmrtd.PACEException
import org.jmrtd.lds.icao.MRZInfo
import vn.leeon.eidsdk.data.Eid
import vn.leeon.eidsdk.facade.EidCallback
import vn.leeon.eidsdk.facade.EidFacade
import kotlin.coroutines.resume

class EidReaderDataSource(
    private val context: Context,
) {

    suspend fun readEid(mrzInfo: MRZInfo): Result<Eid> = suspendCancellableCoroutine { continuation ->
        var disposable: Disposable? = null

        disposable = EidFacade.handleDocumentNfcTag(context, mrzInfo, object : EidCallback {
            override fun onEidReadStart() {
                // no-op for now; progress handled in presentation layer
            }

            override fun onEidReadFinish() {
                // no-op
            }

            override fun onEidRead(eid: Eid?) {
                if (eid == null) {
                    if (continuation.isActive) continuation.resume(Result.failure(IllegalStateException("Missing eID data")))
                } else if (continuation.isActive) {
                    continuation.resume(Result.success(eid))
                }
            }

            override fun onAccessDeniedException(ex: AccessDeniedException) {
                if (continuation.isActive) continuation.resume(Result.failure(ex))
            }

            override fun onBACDeniedException(ex: BACDeniedException) {
                if (continuation.isActive) continuation.resume(Result.failure(ex))
            }

            override fun onPACEException(ex: PACEException) {
                if (continuation.isActive) continuation.resume(Result.failure(ex))
            }

            override fun onCardException(ex: CardServiceException) {
                if (continuation.isActive) continuation.resume(Result.failure(ex))
            }

            override fun onGeneralException(exception: Exception?) {
                if (continuation.isActive) continuation.resume(Result.failure(exception ?: IllegalStateException("Unknown NFC error")))
            }
        })

        continuation.invokeOnCancellation { disposable?.dispose() }
    }
}
