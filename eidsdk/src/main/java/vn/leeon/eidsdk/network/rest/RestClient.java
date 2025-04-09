package vn.leeon.eidsdk.network.rest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.leeon.eidsdk.BuildConfig;

public class RestClient {

    @NonNull
    public static Retrofit buildService(Gson gson, String baseUrl) {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(getUnsafeHttpClient().build())
                .baseUrl(baseUrl)
                .build();
    }

    @NonNull
    public static OkHttpClient.Builder getUnsafeHttpClient() {
        return getUnsafeHttpClient(null);
    }

    @NonNull
    public static OkHttpClient.Builder getUnsafeHttpClient(@Nullable HttpHeaderInterceptor headers) {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @NonNull
                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            // DO NOT SET LOG LEVEL TO Level.BODY SINCE IT WILL NOT WORK WITH SSE
            if (BuildConfig.DEBUG) {
                interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            }
            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.connectTimeout(7000, TimeUnit.SECONDS);
            builder.readTimeout(600, TimeUnit.SECONDS);
            if (headers != null) {
                builder.addInterceptor(headers);
            }

            builder.addInterceptor(interceptor);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return builder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // endregion
}
