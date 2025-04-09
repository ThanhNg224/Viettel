package vn.leeon.eidsdk.network.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResponseModel<T> {
    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("error")
    @Expose
    public ErrorModel error;
    @SerializedName("data")
    @Expose
    public T data;

    public ResponseModel() {
        
    }
    public ResponseModel(Boolean success, ErrorModel error, T data) {
        this.success = success;
        this.error = error;
        this.data = data;
    }
}
