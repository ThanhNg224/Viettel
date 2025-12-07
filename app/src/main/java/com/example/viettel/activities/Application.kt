package com.example.viettel.activities

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import com.example.viettel.service.WebSocketConnection
import com.example.viettel.stringee.StringeeHelper

class Application : android.app.Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        //Start stringee service
        StringeeHelper.initAndConnectStringee(this)

        //Connect to web socket server
        WebSocketConnection(this).run()

        registerActivityLifecycleCallbacks(
            object : ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
                override fun onActivityStarted(activity: Activity) = Unit
                override fun onActivityResumed(activity: Activity) = Unit
                override fun onActivityPaused(activity: Activity) = Unit
                override fun onActivityStopped(activity: Activity) = Unit
                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
                override fun onActivityDestroyed(activity: Activity) = Unit
            },
        )
    }

    fun isAppOnForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return false
        val packageName = context.packageName
        val appProcesses = activityManager.runningAppProcesses ?: return false
        return appProcesses.any { process ->
            process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                process.processName == packageName
        }
    }

    companion object {
        @Volatile
        lateinit var instance: Application
            private set
    }
}
