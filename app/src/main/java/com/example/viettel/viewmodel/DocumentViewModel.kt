package com.example.viettel.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import org.jmrtd.lds.icao.MRZInfo
import vn.leeon.eidsdk.data.Eid
import vn.leeon.eidsdk.data.PersonOptionalDetails

class DocumentViewModel : ViewModel() {
    var frontImage: Bitmap? = null
    var backImage: Bitmap? = null
    var chipPortrait: Bitmap? = null
    var mrzInfo: MRZInfo? = null
    var eid: Eid? = null
    val portraitActions = mutableMapOf<PortraitAction, Bitmap?>()
    var userInfo: PersonOptionalDetails? = null
    var signatureBitmap: Bitmap? = null


}
