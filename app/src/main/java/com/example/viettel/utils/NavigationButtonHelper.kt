package com.example.viettel.utils

import android.app.Activity
import android.view.View
import android.widget.Button
import com.example.viettel.R

object NavigationButtonHelper {
    fun setContinueVisible(activity: Activity, visible: Boolean) {
        activity.findViewById<Button>(R.id.btnContinue)?.visibility =
            if (visible) View.VISIBLE else View.GONE
    }

    fun setContinueEnabled(activity: Activity, enabled: Boolean) {
        activity.findViewById<Button>(R.id.btnContinue)?.isEnabled = enabled
    }

    fun setBackVisible(activity: Activity, visible: Boolean) {
        activity.findViewById<Button>(R.id.btnBack)?.visibility =
            if (visible) View.VISIBLE else View.GONE
    }

    fun setBackEnabled(activity: Activity, enabled: Boolean) {
        activity.findViewById<Button>(R.id.btnBack)?.isEnabled = enabled
    }
}
