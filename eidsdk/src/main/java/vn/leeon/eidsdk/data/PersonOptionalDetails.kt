package vn.leeon.eidsdk.data

import android.os.Parcel
import android.os.Parcelable

class PersonOptionalDetails : Parcelable {

    var eidNumber: String? = null // CCCD
    var fullName: String? = null // Họ và tên
    var dateOfBirth: String? = null // Ngày sinh
    var gender: String? = null // Giới tính
    var nationality: String? = null // Vietnam
    var ethnicity: String? = null // Dân tộc
    var religion: String? = null // Tôn giáo
    var placeOfOrigin: String? = null // Quê quán
    var placeOfResidence: String? = null // Thường trú
    var personalIdentification: String? = null // Đặc điểm nhận dạng
    var dateOfIssue: String? = null // Ngày cấp
    var dateOfExpiry: String? = null // Ngày hết hạn
    var fatherName: String? = null // Tên bố
    var motherName: String? = null // Tên mẹ
    var oldEidNumber: String? = null // CMND
    var unkIdNumber: String? = null // UNK ID
    var unkInfo: List<String>? = null
    var wifeOrHusbandName: String? = null

    constructor()

    constructor(`in`: Parcel) {
        this.eidNumber = if (`in`.readInt() == 1) `in`.readString() else null
        this.fullName = if (`in`.readInt() == 1) `in`.readString() else null
        this.dateOfBirth = if (`in`.readInt() == 1) `in`.readString() else null
        this.nationality = if (`in`.readInt() == 1) `in`.readString() else null
        this.ethnicity = if (`in`.readInt() == 1) `in`.readString() else null
        this.religion = if (`in`.readInt() == 1) `in`.readString() else null
        this.placeOfOrigin = if (`in`.readInt() == 1) `in`.readString() else null
        this.placeOfResidence = if (`in`.readInt() == 1) `in`.readString() else null
        this.personalIdentification = if (`in`.readInt() == 1) `in`.readString() else null
        this.dateOfIssue = if (`in`.readInt() == 1) `in`.readString() else null
        this.dateOfExpiry = if (`in`.readInt() == 1) `in`.readString() else null
        this.fatherName = if (`in`.readInt() == 1) `in`.readString() else null
        this.motherName = if (`in`.readInt() == 1) `in`.readString() else null
        this.oldEidNumber = if (`in`.readInt() == 1) `in`.readString() else null
        this.unkIdNumber = if (`in`.readInt() == 1) `in`.readString() else null
        this.unkInfo = ArrayList()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(if (eidNumber != null) 1 else 0)
        if (eidNumber != null) {
            dest.writeString(eidNumber)
        }

        dest.writeInt(if (fullName != null) 1 else 0)
        if (fullName != null) {
            dest.writeString(fullName)
        }

        dest.writeInt(if (dateOfBirth != null) 1 else 0)
        if (dateOfBirth != null) {
            dest.writeString(dateOfBirth)
        }

        dest.writeInt(if (nationality != null) 1 else 0)
        if (nationality != null) {
            dest.writeString(nationality)
        }

        dest.writeInt(if (ethnicity != null) 1 else 0)
        if (ethnicity != null) {
            dest.writeString(ethnicity)
        }

        dest.writeInt(if (religion != null) 1 else 0)
        if (religion != null) {
            dest.writeString(religion)
        }

        dest.writeInt(if (placeOfOrigin != null) 1 else 0)
        if (placeOfOrigin != null) {
            dest.writeString(placeOfOrigin)
        }

        dest.writeInt(if (placeOfResidence != null) 1 else 0)
        if (placeOfResidence != null) {
            dest.writeString(placeOfResidence)
        }

        dest.writeInt(if (personalIdentification != null) 1 else 0)
        if (personalIdentification != null) {
            dest.writeString(personalIdentification)
        }

        dest.writeInt(if (dateOfIssue != null) 1 else 0)
        if (dateOfIssue != null) {
            dest.writeString(dateOfIssue)
        }

        dest.writeInt(if (dateOfExpiry != null) 1 else 0)
        if (dateOfExpiry != null) {
            dest.writeString(dateOfExpiry)
        }

        dest.writeInt(if (fatherName != null) 1 else 0)
        if (fatherName != null) {
            dest.writeString(fatherName)
        }

        dest.writeInt(if (motherName != null) 1 else 0)
        if (motherName != null) {
            dest.writeString(motherName)
        }

        dest.writeInt(if (oldEidNumber != null) 1 else 0)
        if (oldEidNumber != null) {
            dest.writeString(oldEidNumber)
        }

        dest.writeInt(if (unkIdNumber != null) 1 else 0)
        if (unkIdNumber != null) {
            dest.writeString(unkIdNumber)
        }
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<*> = object : Parcelable.Creator<PersonOptionalDetails> {
            override fun createFromParcel(pc: Parcel): PersonOptionalDetails {
                return PersonOptionalDetails(pc)
            }

            override fun newArray(size: Int): Array<PersonOptionalDetails?> {
                return arrayOfNulls(size)
            }
        }
    }
}