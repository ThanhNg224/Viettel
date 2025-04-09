package vn.leeon.eidsdk.facade

import net.sf.scuba.smartcards.CardServiceException
import org.jmrtd.AccessDeniedException
import org.jmrtd.BACDeniedException
import org.jmrtd.PACEException
import vn.leeon.eidsdk.data.Eid

public interface EidCallback {
    fun onEidReadStart()
    fun onEidReadFinish()
    fun onEidRead(passport: Eid?)
    fun onAccessDeniedException(exception: AccessDeniedException)
    fun onBACDeniedException(exception: BACDeniedException)
    fun onPACEException(exception: PACEException)
    fun onCardException(exception: CardServiceException)
    fun onGeneralException(exception: Exception?)
}