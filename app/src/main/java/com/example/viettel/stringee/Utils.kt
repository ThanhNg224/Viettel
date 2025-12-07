package com.example.viettel.stringee

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Toast
import java.lang.ref.WeakReference

object Utils {

    @Suppress("unused")
    fun reportMessage(contextRef: WeakReference<Context>, message: String) {
        contextRef.get()?.let { context ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).apply {
                setGravity(Gravity.CENTER, 0, 0)
            }.show()
        }
    }

    fun postDelay(runnable: Runnable, delayMillis: Long) {
        Handler(Looper.getMainLooper()).postDelayed(runnable, delayMillis)
    }
}
