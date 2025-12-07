package com.example.viettel.stringee

import com.stringee.StringeeClient
import com.stringee.call.StringeeCall2
import java.lang.ref.WeakReference

object Common {
    private var clientRef: WeakReference<StringeeClient>? = null

    var client: StringeeClient?
        get() = clientRef?.get()
        set(value) {
            clientRef = if (value != null) WeakReference(value) else null
        }

    @Suppress("unused")
    val callMap: MutableMap<String, StringeeCall2> = mutableMapOf()
    var isInCall: Boolean = false
    @Suppress("unused")
    const val REQUEST_PERMISSION_CALL = 1
}
