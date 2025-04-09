package vn.leeon.eidsdk.network.models;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

public abstract class RestCallback<T> {

    @Nullable
    // LifecycleOwner for handling if it should call delegate only in RESUMED state
    private final LifecycleOwner lifecycleOwner;
    private String errorCode = "";

    public abstract void Success(T model);

    public abstract void Error(String error);
    public void Error(String errorCode, String error) {
        this.errorCode = errorCode;
        this.Error(error);
    }

    public RestCallback() {
        lifecycleOwner = null;
    }

    public RestCallback(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Nullable
    public LifecycleOwner getLifecycleOwner() {
        return lifecycleOwner;
    }
}