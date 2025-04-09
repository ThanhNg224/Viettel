package vn.leeon.eidsdk.network.models;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EidVerifyModel implements ModelProtocol {

    @SerializedName("transaction_code")
    @Expose
    public String transactionCode;

    @SerializedName("is_valid_id_card")
    @Expose
    public Boolean IsValidIdCard;

    @SerializedName("responds")
    @Expose
    public JsonObject responds;

    @SerializedName("signature")
    @Expose
    public String signature;

    @SerializedName("detail_message")
    @Expose
    public String detailMessage;

    public Boolean getValidIdCard() {
        return IsValidIdCard;
    }

    public void setValidIdCard(Boolean validIdCard) {
        IsValidIdCard = validIdCard;
    }

    @Override
    public boolean isValidModel() {
        return true;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public JsonObject getResponds() {
        return responds;
    }

    public void setResponds(JsonObject responds) {
        this.responds = responds;
    }
}
