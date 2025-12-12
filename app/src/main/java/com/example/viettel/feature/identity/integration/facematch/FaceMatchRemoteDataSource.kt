package com.example.viettel.feature.identity.integration.facematch

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class FaceMatchRemoteDataSource @Inject constructor() {

    suspend fun compare(smilePortrait: ByteArray, chipPortrait: ByteArray): Result<Double> =
        withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val jsonBody = JSONObject().apply {
                    put("img1_base64", smilePortrait.toBase64())
                    put("img2_base64", chipPortrait.toBase64())
                }

                val url = URL("https://face-engine-api.atin.vn/api/v1/match")
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.connectTimeout = 25000
                connection.readTimeout = 15000
                connection.doOutput = true
                connection.doInput = true

                connection.outputStream.use { output ->
                    output.write(jsonBody.toString().toByteArray(Charsets.UTF_8))
                    output.flush()
                }

                val responseCode = connection.responseCode
                if (responseCode in 200..299) {
                    val response = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    val score = jsonResponse.optDouble("data", -1.0)
                    if (score >= 0.0) {
                        Result.success(score)
                    } else {
                        Result.failure(IllegalStateException("Invalid match score"))
                    }
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                    Result.failure(IllegalStateException("Face match failed: $responseCode ${errorResponse ?: ""}".trim()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                connection?.disconnect()
            }
        }

    private fun ByteArray.toBase64(): String = Base64.encodeToString(this, Base64.NO_WRAP)
}
