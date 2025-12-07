package vn.leeon.eidsdk.network.models

import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class EidVerifyModel(
    @SerializedName("transaction_code")
    @Expose
    var transactionCode: String? = null,

    @SerializedName("is_valid_id_card")
    @Expose
    var isValidIdCard: Boolean? = null,

    @SerializedName("responds")
    @Expose
    var responds: JsonObject? = null,

    @SerializedName("signature")
    @Expose
    var signature: String? = null,

    @SerializedName("detail_message")
    @Expose
    var detailMessage: String? = null,
) : ModelProtocol {

    override fun isValidModel(): Boolean = true
}
