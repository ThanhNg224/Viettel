import fi.iki.elonen.NanoHTTPD
import org.json.JSONObject
import kotlin.apply
import kotlin.getOrDefault
import kotlin.jvm.java
import kotlin.jvm.javaClass
import kotlin.runCatching
import kotlin.text.ifBlank

class KioskHttpServer(
    port: Int = 8088,
    private val onTrigger: (action: String) -> Unit
) : NanoHTTPD(port) {

    private fun withCors(resp: Response) = resp.apply {
        addHeader("Access-Control-Allow-Origin", "*")
        addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        addHeader("Access-Control-Allow-Headers", "Content-Type, X-Token")
        addHeader("Access-Control-Allow-Private-Network", "true")
    }

    override fun serve(session: IHTTPSession): Response {
        if (session.method == Method.OPTIONS) {
            // Preflight nên trả 204 + CORS, đóng kết nối, không gzip
            return withCors(newFixedLengthResponse(Response.Status.NO_CONTENT, "text/plain", ""))
                .noGzip()
                .closeConn()
        }

        return when {
            session.method == Method.GET && session.uri == "/api/health" -> {
                val body = """{"ok":true}"""
                withCors(newFixedLengthResponse(Response.Status.OK, "application/json", body))
                    .json()
                    .noGzip()
                    .closeConn()
            }

            session.method == Method.POST && session.uri == "/api/trigger" -> {
                val map = kotlin.collections.HashMap<String, String>()
                try { session.parseBody(map) } catch (_: Exception) {}
                val body = map["postData"] ?: "{}"
                val action = runCatching { JSONObject(body).optString("action") }
                    .getOrDefault("START_BUY_SIM")
                    .ifBlank { "START_BUY_SIM" }

                // Gọi callback KHÔNG block thread HTTP
                onTrigger(action)

                val resp = """{"ok":true,"action":"$action","flowStarted":"BUY_SIM"}"""
                withCors(newFixedLengthResponse(Response.Status.OK, "application/json", resp))
                    .json()
                    .noGzip()
                    .closeConn()
            }

            else -> withCors(newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found"))
                .noGzip()
                .closeConn()
        }
    }

    private fun Response.noGzip() = apply {
        try {
            javaClass.getMethod("setGzipEncoding", Boolean::class.java).invoke(this, false)
        } catch (_: Throwable) {}
    }
    private fun Response.closeConn() = apply { addHeader("Connection", "close") }
    private fun Response.json() = apply { mimeType = "application/json" }

}
