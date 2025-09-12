package com.example.viettel.stringee;

import android.content.Context;
import android.util.Log;

import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;
import com.stringee.common.SocketAddress;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.listener.StringeeConnectionListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StringeeHelper {
    private static final String TAG = "StringeeHelper";
    private String token = "eyJjdHkiOiJzdHJpbmdlZS1hcGk7dj0xIiwidHlwIjoiSldUIiwiYWxnIjoiSFMyNTYifQ.eyJqdGkiOiJTSy4wLnV2N2JQSEI5VVpXZTNuTnd5UGJ0SjRjbUVraFZjMC0xNzU3MzEzMzI2IiwiaXNzIjoiU0suMC51djdiUEhCOVVaV2Uzbk53eVBidEo0Y21Fa2hWYzAiLCJleHAiOjE3NTk5MDUzMjYsInVzZXJJZCI6InRlc3QifQ.x9iL1gLrY5oDuYnHHl6ALfZ0ly3Q-fzVVnOCrxDOYnk";
    private static StringeeHelper instance = null;

    private Context mContext;

    public static StringeeHelper getInstance() {
        if (instance == null) {
            synchronized (StringeeHelper.class) {
                if (instance == null) {
                    instance = new StringeeHelper();
                }
            }
        }
        return instance;
    }

    public void initAndConnectStringee(Context context) {
        if (Common.client == null) {
            Common.client = new StringeeClient(context);
            List<SocketAddress> socketAddressList = new ArrayList<>();
            socketAddressList.add(new SocketAddress("v1.stringee.com", 9879));
            socketAddressList.add(new SocketAddress("v2.stringee.com", 9879));
            Common.client.setHost(socketAddressList);

            Common.client.setConnectionListener(new StringeeConnectionListener() {
                @Override
                public void onConnectionConnected(final StringeeClient stringeeClient, boolean isReconnecting) {
                    Log.d(TAG, "Connected as: " + stringeeClient.getUserId());
                }

                @Override
                public void onConnectionDisconnected(StringeeClient stringeeClient, boolean isReconnecting) {
                    Log.d(TAG, "Disconnected");
                }

                @Override
                public void onIncomingCall(final StringeeCall stringeeCall) {
                }

                @Override
                public void onIncomingCall2(StringeeCall2 stringeeCall2) {
                    if (Common.isInCall) {
                        stringeeCall2.reject(new StatusListener() {
                            @Override
                            public void onSuccess() {

                            }
                        });
                    } else {
//                            Common.callMap.put(stringeeCall2.getCallId(), stringeeCall2);
//                            Intent intent = new Intent(StringeeHelper.this, IncomingCallActivity.class);
//                            intent.putExtra("call_id", stringeeCall2.getCallId());
//                            startActivity(intent);
                    }
                }

                @Override
                public void onConnectionError(StringeeClient stringeeClient, final StringeeError stringeeError) {
                    Log.d(TAG, "Connect error: " + stringeeError.getMessage());
                }

                @Override
                public void onRequestNewToken(StringeeClient stringeeClient) {
                    // Get new token here and connect to Stringe server
                    Log.d("Stringee", "Token expired. Request new token here");
                }

                @Override
                public void onCustomMessage(String from, JSONObject msg) {
                }

                @Override
                public void onTopicMessage(String from, JSONObject msg) {

                }
            });
        }
        Common.client.connect(token);
    }
}
