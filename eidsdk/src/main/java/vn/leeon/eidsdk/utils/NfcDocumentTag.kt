package vn.leeon.eidsdk.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.card.manager.RFCardManager
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import net.sf.scuba.smartcards.CardServiceException
import org.jmrtd.*
import org.jmrtd.lds.icao.DG1File
import org.jmrtd.lds.icao.MRZInfo
import vn.leeon.eidsdk.card.IDCardService
import vn.leeon.eidsdk.data.*
import vn.leeon.eidsdk.facade.EidCallback
import vn.leeon.eidsdk.jmrtd.MRTDTrustStore
import java.security.Security

class NfcDocumentTag {

    fun handleTag(
        context: Context,
        mrzInfo: MRZInfo,
        mrtdTrustStore: MRTDTrustStore,
        eidCallback: EidCallback
    ): Disposable {
        return Single.fromCallable {
            var eid: Eid? = null
            var cardServiceException: Exception? = null

            var passportService: PassportService? = null
            var manager = RFCardManager.getInstance()
            manager.open(context)
            manager.reset()
            manager.deviceType
            try {
                passportService = PassportService(
//                    IDCardService(context),
                    IDCardService(manager),
                    EidNfc.MAX_TRANSCEIVE_LENGTH_FOR_PACE,
                    EidNfc.MAX_TRANSCEIVE_LENGTH_FOR_SECURE_MESSAGING,
                    EidNfc.MAX_BLOCK_SIZE,
                    false,
                    true
                )
                passportService.open()

                val eidNfc = EidNfc(passportService, mrtdTrustStore, mrzInfo, EidNfc.MAX_BLOCK_SIZE)
                val verifySecurity = eidNfc.verifySecurity()
                val features = eidNfc.features

                eid = Eid()
                eid.featureStatus = eidNfc.features
                eid.verificationStatus = eidNfc.verificationStatus
                eid.sodFile = eidNfc.sodFile

                // DG1 - Basic Information
                if (eidNfc.dg1File != null) {
                    val mrzInfo = (eidNfc.dg1File as DG1File).mrzInfo
                    val personBasicInfo = PersonDetails()
                    personBasicInfo.dateOfBirth = mrzInfo.dateOfBirth
                    personBasicInfo.dateOfExpiry = mrzInfo.dateOfExpiry
                    personBasicInfo.documentCode = mrzInfo.documentCode
                    personBasicInfo.documentNumber = mrzInfo.documentNumber
                    personBasicInfo.optionalData1 = mrzInfo.optionalData1
                    personBasicInfo.optionalData2 = mrzInfo.optionalData2
                    personBasicInfo.issuingState = mrzInfo.issuingState
                    personBasicInfo.primaryIdentifier = mrzInfo.primaryIdentifier
                    personBasicInfo.secondaryIdentifier = mrzInfo.secondaryIdentifier
                    personBasicInfo.nationality = mrzInfo.nationality
                    personBasicInfo.gender = mrzInfo.gender
                    eid.personDetails = personBasicInfo
                }

                // DG2 - Face Image
                if (eidNfc.dg2File != null) {
                    try {
                        val faceImage = EidNfcUtils.retrieveFaceImage(eidNfc.dg2File!!)
                        eid.face = faceImage
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // DG3 - Fingerprint Image
                if (eidNfc.dg3File != null) {
                    try {
                        val bitmaps = EidNfcUtils.retrieveFingerPrintImage(eidNfc.dg3File!!)
                        eid.fingerprints = bitmaps
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // DG5 - Portrait Image
                if (eidNfc.dg5File != null) {
                    try {
                        val faceImage = EidNfcUtils.retrievePortraitImage(eidNfc.dg5File!!)
                        eid.portrait = faceImage
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // DG7 - Signature Image
                if (eidNfc.dg7File != null) {
                    try {
                        val bitmap = EidNfcUtils.retrieveSignatureImage(context, eidNfc.dg7File!!)
                        eid.signature = bitmap
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // DG11 - Person Additional Details
                val dg11 = eidNfc.dg11File
                if (dg11 != null) {
                    val personExtraInfo = PersonAdditionalDetails()
                    personExtraInfo.custodyInformation = dg11.custodyInformation
                    personExtraInfo.fullDateOfBirth = dg11.fullDateOfBirth
                    personExtraInfo.nameOfHolder = dg11.nameOfHolder
                    personExtraInfo.otherNames = dg11.otherNames
                    personExtraInfo.otherNames = dg11.otherNames
                    personExtraInfo.otherValidTDNumbers = dg11.otherValidTDNumbers
                    personExtraInfo.permanentAddress = dg11.permanentAddress
                    personExtraInfo.personalNumber = dg11.personalNumber
                    personExtraInfo.personalSummary = dg11.personalSummary
                    personExtraInfo.placeOfBirth = dg11.placeOfBirth
                    personExtraInfo.profession = dg11.profession
                    personExtraInfo.proofOfCitizenship = dg11.proofOfCitizenship
                    personExtraInfo.tag = dg11.tag
                    personExtraInfo.tagPresenceList = dg11.tagPresenceList
                    personExtraInfo.telephone = dg11.telephone
                    personExtraInfo.title = dg11.title
                    eid.personAdditionalDetails = personExtraInfo
                }

                // DG12 - Additional Document Details
                val dg12 = eidNfc.dg12File
                if (dg12 != null) {
                    val additionalDocumentDetails = AdditionalDocumentDetails()
                    additionalDocumentDetails.dateAndTimeOfPersonalization = dg12.dateAndTimeOfPersonalization
                    additionalDocumentDetails.dateOfIssue = dg12.dateOfIssue
                    additionalDocumentDetails.endorsementsAndObservations = dg12.endorsementsAndObservations
                    try {
                        val imageOfFront = dg12.imageOfFront
                        val bitmapImageOfFront = BitmapFactory.decodeByteArray(imageOfFront, 0, imageOfFront.size)
                        additionalDocumentDetails.imageOfFront = bitmapImageOfFront
                    } catch (e: Exception) {
                        Log.e(TAG, "Additional document image front: $e")
                    }
                    try {
                        val imageOfRear = dg12.imageOfRear
                        val bitmapImageOfRear = BitmapFactory.decodeByteArray(imageOfRear, 0, imageOfRear.size)
                        additionalDocumentDetails.imageOfRear = bitmapImageOfRear
                    } catch (e: Exception) {
                        Log.e(TAG, "Additional document image rear: $e")
                    }
                    additionalDocumentDetails.issuingAuthority = dg12.issuingAuthority
                    additionalDocumentDetails.namesOfOtherPersons = dg12.namesOfOtherPersons
                    additionalDocumentDetails.personalizationSystemSerialNumber = dg12.personalizationSystemSerialNumber
                    additionalDocumentDetails.taxOrExitRequirements = dg12.taxOrExitRequirements
                    eid.additionalDocumentDetails = additionalDocumentDetails
                }

                // DG13 - Person Optional Details
                val dg13 = eidNfc.dg13File
                if (dg13 != null) {
                    val personOptionalDetails = PersonOptionalDetails()
                    personOptionalDetails.eidNumber = dg13.eidNumber
                    personOptionalDetails.fullName = dg13.fullName
                    personOptionalDetails.dateOfBirth = dg13.dateOfBirth
                    personOptionalDetails.gender = dg13.gender
                    personOptionalDetails.nationality = dg13.nationality
                    personOptionalDetails.ethnicity = dg13.ethnicity
                    personOptionalDetails.religion = dg13.religion
                    personOptionalDetails.placeOfOrigin = dg13.placeOfOrigin
                    personOptionalDetails.placeOfResidence = dg13.placeOfResidence
                    personOptionalDetails.personalIdentification = dg13.personalIdentification
                    personOptionalDetails.dateOfIssue = dg13.dateOfIssue
                    personOptionalDetails.dateOfExpiry = dg13.dateOfExpiry
                    personOptionalDetails.fatherName = dg13.fatherName
                    personOptionalDetails.motherName = dg13.motherName
                    personOptionalDetails.wifeOrHusbandName = dg13.wifeOrHusbandName
                    personOptionalDetails.oldEidNumber = dg13.oldEidNumber
                    personOptionalDetails.unkIdNumber = dg13.unkIdNumber
                    personOptionalDetails.unkInfo = dg13.unkInfo
                    eid.personOptionalDetails = personOptionalDetails
                }

            } catch (e: Exception) {
                cardServiceException = e
            } finally {
                try {
                    passportService?.close()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
            EidDto(eid, cardServiceException)
        }.doOnSubscribe {
            eidCallback.onEidReadStart()
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe { eidDto ->
                if (eidDto.cardServiceException != null) {
                    val cardServiceException = eidDto.cardServiceException
                    if (cardServiceException is AccessDeniedException) {
                        eidCallback.onAccessDeniedException(cardServiceException)
                    } else if (cardServiceException is BACDeniedException) {
                        eidCallback.onBACDeniedException(cardServiceException)
                    } else if (cardServiceException is PACEException) {
                        eidCallback.onPACEException(cardServiceException)
                    } else if (cardServiceException is CardServiceException) {
                        eidCallback.onCardException(cardServiceException)
                    } else {
                        eidCallback.onGeneralException(cardServiceException)
                    }
                } else {
                    eidCallback.onEidRead(eidDto.eid)
                }
                eidCallback.onEidReadFinish()
            }
    }

    data class EidDto(val eid: Eid? = null, val cardServiceException: Exception? = null)

    companion object {
        private val TAG = NfcDocumentTag::class.java.simpleName

        init {
            Security.insertProviderAt(org.spongycastle.jce.provider.BouncyCastleProvider(), 1)
        }

        private val EMPTY_TRIED_BAC_ENTRY_LIST = emptyList<Any>()
        private val EMPTY_CERTIFICATE_CHAIN = emptyList<Any>()
    }
}