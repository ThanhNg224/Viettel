package vn.leeon.eidsdk.network

import androidx.lifecycle.Lifecycle
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url
import vn.leeon.eidsdk.network.models.EidVerifyModel
import vn.leeon.eidsdk.network.models.ModelProtocol
import vn.leeon.eidsdk.network.models.ResponseModel
import vn.leeon.eidsdk.network.models.RestCallback
import vn.leeon.eidsdk.network.rest.RestClient
import vn.leeon.eidsdk.utils.AppExecutors

object EidService {

    private val gson: Gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").setLenient().create()

    private const val EID_VERIFY_ENDPOINT = "eid_verify/sdk/verify"
    private const val HEADER_X_API_KEY = "x-api-key"

    private interface RequestService {
        @POST
        fun requestVerifyEid(@Url url: String, @Header(HEADER_X_API_KEY) apiKey: String, @Body jsonObject: JsonObject): Call<ResponseModel<EidVerifyModel>>
    }

    private class ResponseCallback<T : ModelProtocol>(private val delegate: RestCallback<ResponseModel<T>>?) :
        Callback<ResponseModel<T>> {
        override fun onResponse(call: Call<ResponseModel<T>>, response: Response<ResponseModel<T>>) {
            AppExecutors.get().mainThread().execute {
                val callback = delegate ?: return@execute

                val lifecycleOwner = callback.lifecycleOwner
                if (lifecycleOwner != null && !lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    return@execute
                }

                if (!response.isSuccessful) {
                    if (response.code() == 401) {
                        callback.Error("401: Unauthorized Request. Please check API Key and try again.")
                        return@execute
                    }
                    if (response.errorBody() == null) {
                        callback.Error("Error code ${response.code()}")
                        return@execute
                    }
                    try {
                        callback.Error(response.errorBody()!!.string())
                    } catch (e: Exception) {
                        callback.Error(e.localizedMessage ?: "")
                    }
                    return@execute
                }

                val body = response.body()
                if (body == null) {
                    callback.Error("Response object is empty!")
                    return@execute
                }

                if (body.success != true) {
                    val error = body.error
                    if (error != null) {
                        callback.Error(error.code, error.message)
                    } else {
                        callback.Error("0", "Unknown error code")
                    }
                    return@execute
                }

                if (body.data == null) {
                    callback.Success(body)
                    return@execute
                }

                if (body.data.isValidModel()) {
                    callback.Success(body)
                } else {
                    callback.Error("Invalid object!")
                }
            }
        }

        override fun onFailure(call: Call<ResponseModel<T>>, t: Throwable) {
            AppExecutors.get().mainThread().execute {
                delegate?.Error(t.message ?: "")
            }
        }
    }

    private fun getEidService(): RequestService {
        return RestClient.buildService(gson, baseUrl).create(RequestService::class.java)
    }

    @Volatile
    private var apiKey: String = ""
    @Volatile
    private var baseUrl: String = ""

    val EIDSERVICE: EidService
        get() = this

    fun init(apiKey: String) {
        this.apiKey = apiKey
    }

    fun init(apiKey: String, baseUrl: String) {
        this.apiKey = apiKey
        this.baseUrl = baseUrl
    }

    fun verifyEid(
        idCard: String,
        dsCert: String,
        province: String,
        code: String,
        delegate: RestCallback<ResponseModel<EidVerifyModel>>,
    ) {
        verifyEid(EID_VERIFY_ENDPOINT, idCard, dsCert, province, code, delegate)
    }

    fun verifyEid(
        path: String,
        idCard: String,
        dsCert: String,
        province: String,
        code: String,
        delegate: RestCallback<ResponseModel<EidVerifyModel>>,
    ) {
        val jsonObject = JsonObject().apply {
            addProperty("id_card", idCard)
            addProperty("ds_cert", dsCert)
            addProperty("device_type", "Android")
            addProperty("province", province)
            addProperty("code", code)
        }
        getEidService().requestVerifyEid(path, apiKey, jsonObject).enqueue(ResponseCallback(delegate))
    }
}
