package com.example.viettel.stringee

import android.content.Context
import android.util.Log
import com.stringee.StringeeClient
import com.stringee.call.StringeeCall2
import com.stringee.common.SocketAddress
import com.stringee.exception.StringeeError
import com.stringee.listener.StatusListener
import com.stringee.listener.StringeeConnectionListener
import org.json.JSONObject

object StringeeHelper {
    private const val TAG = "StringeeHelper"
    private const val TOKEN =
        "eyJjdHkiOiJzdHJpbmdlZS1hcGk7dj0xIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJqdGkiOiJTSy4wLnV2N2JQSEI5VVpXZTNuTnd5UGJ0SjRjbUVraFZjMC0xNzU3MzEzMzI2IiwiaXNzIjoiU0suMC51djdiUEhCOVVaV2Uzbk53eVBidEo0Y21Fa2hWYzAiLCJleHAiOjE3NTk5MDUzMjYsInVzZXJJZCI6InRlc3QifQ.x9iL1gLrY5oDuYnHHl6ALfZ0ly3Q-fzVVnOCrxDOYnk"

    fun initAndConnectStringee(context: Context) {
        if (Common.client == null) {
            // Use applicationContext to prevent Activity context leak
            val appContext = context.applicationContext
            Common.client = StringeeClient(appContext).apply {
                val socketAddressList = listOf(
                    SocketAddress("v1.stringee.com", 9879),
                    SocketAddress("v2.stringee.com", 9879),
                )
                setHost(socketAddressList)
                setConnectionListener(object : StringeeConnectionListener {
                    override fun onConnectionConnected(stringeeClient: StringeeClient, isReconnecting: Boolean) {
                        Log.d(TAG, "Connected as: ${stringeeClient.userId}")
                    }

                    override fun onConnectionDisconnected(stringeeClient: StringeeClient, isReconnecting: Boolean) {
                        Log.d(TAG, "Disconnected")
                    }

                    override fun onIncomingCall2(stringeeCall2: StringeeCall2) {
                        if (Common.isInCall) {
                            stringeeCall2.reject(object : StatusListener() {
                                override fun onSuccess() = Unit
                            })
                        } else {
//                            Common.callMap[stringeeCall2.callId] = stringeeCall2
//                            val intent = Intent(StringeeHelper.this, IncomingCallActivity::class.java)
//                            intent.putExtra("call_id", stringeeCall2.callId)
//                            startActivity(intent)
                        }
                    }

                    override fun onIncomingCall(stringeeCall: com.stringee.call.StringeeCall?) = Unit

                    override fun onConnectionError(stringeeClient: StringeeClient, stringeeError: StringeeError) {
                        Log.d(TAG, "Connect error: ${stringeeError.message}")
                    }

                    override fun onRequestNewToken(stringeeClient: StringeeClient) {
                        Log.d(TAG, "Token expired. Request new token here")
                    }

                    override fun onCustomMessage(from: String, msg: JSONObject) = Unit

                    override fun onTopicMessage(from: String, msg: JSONObject) = Unit
                })
            }
        }
        Common.client?.connect(TOKEN)
    }

    fun cleanup() {
        Common.client = null
    }
}
