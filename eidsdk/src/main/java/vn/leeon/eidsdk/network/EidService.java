package vn.leeon.eidsdk.network;

import androidx.lifecycle.Lifecycle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Url;
import vn.leeon.eidsdk.network.models.EidVerifyModel;
import vn.leeon.eidsdk.network.models.ModelProtocol;
import vn.leeon.eidsdk.network.models.ResponseModel;
import vn.leeon.eidsdk.network.models.RestCallback;
import vn.leeon.eidsdk.network.rest.RestClient;
import vn.leeon.eidsdk.utils.AppExecutors;

public class EidService {

    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").setLenient().create();

    private static final String EID_VERIFY_ENDPOINT = "eid_verify/sdk/verify";

    private static final String HEADER_X_API_KEY = "x-api-key";


    // region Request & Response

    private interface IRequestService {
        @POST
        Call<ResponseModel<EidVerifyModel>> RequestVerifyEid(@Url String url, @Header(HEADER_X_API_KEY) String apiKey, @Body JsonObject jsonObject);
    }

    private class ResponseCallback<T extends ModelProtocol> implements Callback<ResponseModel<T>> {

        private final @Nullable
        RestCallback<ResponseModel<T>> delegate;

        public ResponseCallback(final RestCallback<ResponseModel<T>> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void onResponse(Call<ResponseModel<T>> call, Response<ResponseModel<T>> response) {
            AppExecutors.get().mainThread().execute(() -> {

                // Skip callback
                if (delegate == null) {
                    return;
                }

                // Invalid lifecycle owner, data response not in foreground state, so we ignore the result
                if (delegate.getLifecycleOwner() != null) {
                    if (!delegate.getLifecycleOwner().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                        return;
                    }
                }

                // Response code not in range 200-300
                if (!response.isSuccessful()) {
                    if (response.code() == 401) {
                        delegate.Error("401: Unauthorized Request. Please check API Key and try again.");
                        return;
                    }

                    // Error code
                    if (response.errorBody() == null) {
                        delegate.Error("Error code " + response.code());
                        return;
                    }

                    try {
                        delegate.Error(response.errorBody().string());
                    } catch (IOException e) {
                        delegate.Error(e.getLocalizedMessage());
                    }
                    return;
                }

                ResponseModel<T> body = response.body();
                // Empty body
                if (body == null) {
                    delegate.Error("Response object is empty!");
                    return;
                }

                // Failed request
                if (!body.success) {
                    if (body.error != null) {
                        delegate.Error(body.error.code, body.error.message);
                    } else {
                        delegate.Error("0", "Unknown error code");
                    }

                    return;
                }

                // null data so the the request is successful
                if (body.data == null) {
                    delegate.Success(body);
                    return;
                }

                // all the models of body.data should be valid
                if (body.data.isValidModel()) {
                    delegate.Success(body);
                } else {
                    delegate.Error("Invalid object!");
                }
            });
        }

        @Override
        public void onFailure(Call<ResponseModel<T>> call, Throwable t) {
            AppExecutors.get().mainThread().execute(() -> {
                if (delegate != null) {
                    delegate.Error(t.getMessage());
                }
            });
        }
    }

    private IRequestService getEidService() {
        return RestClient.buildService(GSON, this.baseUrl).create(IRequestService.class);
    }

    // endregion

    // region Singleton

    private static volatile EidService mInstance = null;
    private String apiKey = "";
    private String baseUrl = "";

    public static EidService EIDSERVICE = EidService.instance();

    public static EidService instance() {
        if (mInstance == null) {
            synchronized (EidService.class) {
                mInstance = new EidService();
            }
        }
        return mInstance;
    }

    // endregion

    // region Public

    /**
     * Initialize the service with API Key.
     * @param apiKey
     */
    public void init(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Initialize the service with API Key and base Service Url.
     * @param apiKey
     * @param baseUrl
     */
    public void init(String apiKey, String baseUrl) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    /**
     * Request to verify the eid card from the central database of public security.
     * @param idCard - EID Number (12 digits)
     * @param dsCert - SOD in the EID Card with Base 64 Encoding
     * @param province - Province of the card owner (ie Hanoi)
     * @param code - Customer code
     * @param delegate - Callback delegate
     */
    public void verifyEid(String idCard, String dsCert, String province, String code, final RestCallback<ResponseModel<EidVerifyModel>> delegate) {
        verifyEid(EID_VERIFY_ENDPOINT, idCard, dsCert, province, code, delegate);
    }

    /**
     * Request to verify the eid card from the central database of public security.
     * @param path - Proxy endpoint of 3rd parties
     * @param idCard - EID Number (12 digits)
     * @param dsCert - SOD in the EID Card with Base 64 Encoding
     * @param province - Province of the card owner (ie Hanoi)
     * @param code - Customer code
     * @param delegate - Callback delegate
     */
    public void verifyEid(String path, String idCard, String dsCert, String province, String code, final RestCallback<ResponseModel<EidVerifyModel>> delegate) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id_card", idCard);
        jsonObject.addProperty("ds_cert", dsCert);
        jsonObject.addProperty("device_type", "Android");
        jsonObject.addProperty("province", province);
        jsonObject.addProperty("code", code);
        getEidService().RequestVerifyEid(path, apiKey, jsonObject).enqueue(new ResponseCallback<>(delegate));
    }

    // endregion
}
