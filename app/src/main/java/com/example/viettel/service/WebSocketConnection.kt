package com.example.viettel.service

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.viettel.activities.MainActivity
import com.example.viettel.commons.WebSocketActionRequest
import com.example.viettel.models.SocketRequestMessage
import com.example.viettel.models.SocketResponseMessage
import com.google.gson.Gson
import io.reactivex.CompletableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.StompHeader
import ua.naiksoftware.stomp.dto.LifecycleEvent
import java.util.concurrent.TimeUnit

class WebSocketConnection(private val context: Context) : Runnable {

    private val stompClient: StompClient
    private var compositeDisposable = CompositeDisposable()

    override fun run() {
        connect()
    }

    private fun connect() {
        resetSubscriptions()
        connectStomp()
    }

    private fun connectStomp() {
        val webDomain = "http://192.168.1.150:42055"
        if (webDomain.isBlank()) {
            return
        }

        val headers = mutableListOf(
            StompHeader(LOGIN, "guest"),
            StompHeader(PASSCODE, "guest"),
            StompHeader("username", "123456789"),
        )

        stompClient.withClientHeartbeat(1000).withServerHeartbeat(1000)
        resetSubscriptions()

        val lifecycleDisposable: Disposable = stompClient.lifecycle()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({ lifecycleEvent ->
                when (lifecycleEvent.type) {
                    LifecycleEvent.Type.OPENED -> Log.d(TAG, "Stomp connection opened")
                    LifecycleEvent.Type.ERROR -> {
                        Log.e(TAG, "Stomp connection error ${lifecycleEvent.exception}")
                        reconnect()
                    }
                    LifecycleEvent.Type.CLOSED -> {
                        Log.d(TAG, "Stomp connection closed")
                        disconnectStomp()
                        resetSubscriptions()
                        reconnect()
                    }
                    LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> Log.e(TAG, "Stomp failed server heartbeat")
                }
            }, { error ->
                Log.e(TAG, "Lifecycle error", error)
            })

        compositeDisposable.add(lifecycleDisposable)

        val topicDisposable = stompClient.topic("/user/queue/messages")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ topicMessage ->
                Log.d(TAG, "Received ${topicMessage.payload}")
                processServerResponse(topicMessage.payload)
                //sendToServer("/app/clientResponse", msg);
            }, { throwable ->
                Log.e(TAG, "Error on subscribe topic", throwable)
            })

        compositeDisposable.add(topicDisposable)
        stompClient.connect(headers)
    }

    private fun resetSubscriptions() {
        compositeDisposable.dispose()
        compositeDisposable = CompositeDisposable()
    }

    private fun disconnectStomp() {
        stompClient.disconnect()
    }

    private fun reconnect() {
        Log.d(TAG, "Try to reconnect stomp server")
        try {
            TimeUnit.SECONDS.sleep(5)
        } catch (e: InterruptedException) {
            Log.e(TAG, "Reconnect interrupted", e)
        }
        connectStomp()
    }

    private fun applySchedulers(): CompletableTransformer {
        return CompletableTransformer { upstream ->
            upstream.unsubscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    @Suppress("unused")
    fun sendToServer(destination: String, message: String) {
        compositeDisposable.add(
            stompClient.send(destination, message)
                .compose(applySchedulers())
                .subscribe({
                    Log.d(TAG, "STOMP echo send successfully")
                }, { throwable ->
                    Log.e(TAG, "Error send STOMP echo", throwable)
                }),
        )
    }

    private fun processServerResponse(request: String): String {
        var responseMessage: SocketResponseMessage<String>? = null
        val socketRequest = gson.fromJson(request, SocketRequestMessage::class.java)

        when (socketRequest.actionType) {
            WebSocketActionRequest.CHANGE_SIM -> {
                responseMessage = try {
                    val intent = Intent(context.applicationContext, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.applicationContext.startActivity(intent)
                    SocketResponseMessage(socketRequest.actionType, "SUCCESS", null, socketRequest.requestId)
                } catch (e: Exception) {
                    SocketResponseMessage(socketRequest.actionType, "ERROR", e.message, socketRequest.requestId)
                }
            }
        }

        return gson.toJson(responseMessage)
    }

    companion object {
        private const val TAG = "WebSocketConnection"
        private const val LOGIN = "login"
        private const val PASSCODE = "passcode"
        private val gson = Gson()
    }

    init {
        val webDomain = "http://192.168.1.150:42055"
        val webSocketUrl = if (webDomain.contains("https")) {
            webDomain.replace("https://", "")
        } else {
            webDomain.replace("http://", "")
        }
        val protocol = if (webDomain.contains("https")) "wss://" else "ws://"
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "$protocol$webSocketUrl/register")
    }
}
