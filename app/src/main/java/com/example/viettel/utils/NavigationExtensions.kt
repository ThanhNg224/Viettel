package com.example.viettel.utils

import androidx.fragment.app.Fragment
import com.example.viettel.activities.MainActivity

fun Fragment.mainActivity(): MainActivity? = activity as? MainActivity

fun Fragment.updateNavigationControls(
    isBackVisible: Boolean = true,
    isContinueVisible: Boolean = true,
    isContinueEnabled: Boolean = true,
) {
    mainActivity()?.apply {
        setBackVisible(isBackVisible)
        setContinueVisible(isContinueVisible)
        setContinueEnabled(isContinueEnabled)
    }
}

fun Fragment.navigateTo(fragment: Fragment) {
    mainActivity()?.replaceFragment(fragment)
}
