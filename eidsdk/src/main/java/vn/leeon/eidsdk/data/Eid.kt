package vn.leeon.eidsdk.data

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable

import vn.leeon.eidsdk.jmrtd.FeatureStatus
import vn.leeon.eidsdk.jmrtd.VerificationStatus
import org.jmrtd.lds.SODFile

import java.util.ArrayList

class Eid : Parcelable {

    var sodFile: SODFile? = null
    var face: Bitmap? = null
    var portrait: Bitmap? = null
    var signature: Bitmap? = null
    var fingerprints: List<Bitmap>? = null
    var personDetails: PersonDetails? = null
    var personAdditionalDetails: PersonAdditionalDetails? = null
    var personOptionalDetails: PersonOptionalDetails? = null
    var additionalDocumentDetails: AdditionalDocumentDetails? = null
    var featureStatus: FeatureStatus? = null
    var verificationStatus: VerificationStatus? = null

    constructor(`in`: Parcel) {
        fingerprints = ArrayList()
        this.face = if (`in`.readInt() == 1) `in`.readParcelable(Bitmap::class.java.classLoader) else null
        this.portrait = if (`in`.readInt() == 1) `in`.readParcelable(Bitmap::class.java.classLoader) else null
        this.personDetails = if (`in`.readInt() == 1) `in`.readParcelable(PersonDetails::class.java.classLoader) else null
        this.personAdditionalDetails = if (`in`.readInt() == 1) `in`.readParcelable(
            PersonAdditionalDetails::class.java.classLoader) else null
        this.personOptionalDetails = if (`in`.readInt() == 1) `in`.readParcelable(
            PersonOptionalDetails::class.java.classLoader) else null

        if (`in`.readInt() == 1) {
            `in`.readList(fingerprints!!, Bitmap::class.java.classLoader)
        }

        this.signature = if (`in`.readInt() == 1) `in`.readParcelable(Bitmap::class.java.classLoader) else null
        this.additionalDocumentDetails = if (`in`.readInt() == 1) `in`.readParcelable(
            AdditionalDocumentDetails::class.java.classLoader) else null
        if (`in`.readInt() == 1) {
            sodFile = `in`.readSerializable() as SODFile
        }

        if (`in`.readInt() == 1) {
            featureStatus = `in`.readParcelable(FeatureStatus::class.java.classLoader)
        }

        if (`in`.readInt() == 1) {
            featureStatus = `in`.readParcelable(FeatureStatus::class.java.classLoader)
        }

        if (`in`.readInt() == 1) {
            verificationStatus = `in`.readParcelable(VerificationStatus::class.java.classLoader)
        }

    }

    constructor() {
        fingerprints = ArrayList()
        featureStatus = FeatureStatus()
        verificationStatus = VerificationStatus()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {

        dest.writeInt(if (face != null) 1 else 0)
        if (face != null) {
            dest.writeParcelable(face, flags)
        }

        dest.writeInt(if (portrait != null) 1 else 0)
        if (portrait != null) {
            dest.writeParcelable(portrait, flags)
        }

        dest.writeInt(if (personDetails != null) 1 else 0)
        if (personDetails != null) {
            dest.writeParcelable(personDetails, flags)
        }

        dest.writeInt(if (personAdditionalDetails != null) 1 else 0)
        if (personAdditionalDetails != null) {
            dest.writeParcelable(personAdditionalDetails, flags)
        }

        dest.writeInt(if (personOptionalDetails != null) 1 else 0)
        if (personOptionalDetails != null) {
            dest.writeParcelable(personOptionalDetails, flags)
        }

        dest.writeInt(if (fingerprints != null) 1 else 0)
        if (fingerprints != null) {
            dest.writeList(fingerprints)
        }

        dest.writeInt(if (signature != null) 1 else 0)
        if (signature != null) {
            dest.writeParcelable(signature, flags)
        }

        dest.writeInt(if (additionalDocumentDetails != null) 1 else 0)
        if (additionalDocumentDetails != null) {
            dest.writeParcelable(additionalDocumentDetails, flags)
        }

        dest.writeInt(if (sodFile != null) 1 else 0)
        if (sodFile != null) {
            dest.writeSerializable(sodFile)
        }

        dest.writeInt(if (featureStatus != null) 1 else 0)
        if (featureStatus != null) {
            dest.writeParcelable(featureStatus, flags)
        }

        dest.writeInt(if (verificationStatus != null) 1 else 0)
        if (verificationStatus != null) {
            dest.writeParcelable(verificationStatus, flags)
        }

    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<*> = object : Parcelable.Creator<Eid> {
            override fun createFromParcel(pc: Parcel): Eid {
                return Eid(pc)
            }

            override fun newArray(size: Int): Array<Eid?> {
                return arrayOfNulls(size)
            }
        }
    }
}
