package com.example.viettel.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.viettel.service.WebSocketConnection;
import com.example.viettel.stringee.StringeeHelper;

import java.util.List;

public class Application extends android.app.Application {

    private static Application instance;

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }
    public static Application getInstance() {
        return instance;
    }

    public boolean isAppOnForeground(Context context) {
        boolean ret = false;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if(appProcesses != null){
            String packageName = context.getPackageName();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                    ret = true;
                }
            }
        }

        return ret;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        //Start stringee service
        StringeeHelper.getInstance().initAndConnectStringee(this);

        //Connect to web socket server
        WebSocketConnection webSocketConnection = new WebSocketConnection(this);
//        webSocketConnection.run();

        SharedPreferences pref = getSharedPreferences("PREF", MODE_PRIVATE);
        registerActivityLifecycleCallbacks(
                new ActivityLifecycleCallbacks() {
                    @Override
                    public void onActivityPaused(Activity activity) {
                    }

                    @Override
                    public void onActivityResumed(Activity activity) {

                    }

                    @Override
                    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                    }

                    @Override
                    public void onActivityStarted(Activity activity) {
                    }

                    @Override
                    public void onActivityStopped(Activity activity) {
                    }

                    @Override
                    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                    }

                    @Override
                    public void onActivityDestroyed(Activity activity) {
                    }
                });
    }
}