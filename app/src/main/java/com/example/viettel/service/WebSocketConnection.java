package com.example.viettel.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.viettel.activities.MainActivity;
import com.example.viettel.commons.WebSocketActionRequest;
import com.example.viettel.models.SocketRequestMessage;
import com.example.viettel.models.SocketResponseMessage;
import com.example.viettel.utils.BaseUtil;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.CompletableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

public class WebSocketConnection implements Runnable{
    private static final String TAG = "WebSocketConnection";
    private Context mContext;
    private static final String LOGIN = "login";
    private static final String PASSCODE = "passcode";
    private StompClient mStompClient;
    private CompositeDisposable compositeDisposable;
    private String phoneNumber;
    private static final Gson gson = new Gson();

    public WebSocketConnection(Context context) {
        this.mContext = context;
        phoneNumber = BaseUtil.getImeiNumber(mContext);

        String webDomain = "http://192.168.1.150:42055";
        String webSocketUrl = "";

        if(webDomain.contains("https")){
            webSocketUrl = webDomain.replaceAll("https://", "");
            mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "wss://" + webSocketUrl + "/register");
        }else{
            webSocketUrl = webDomain.replaceAll("http://", "");
            mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://" + webSocketUrl + "/register");
        }
    }

    @Override
    public void run() {
        doConnect();
    }

    private void doConnect() {
        resetSubscriptions();
        connectStomp();
    }

    private void connectStomp() {
        String webDomain = "http://192.168.1.150:42055";
        if(webDomain.equals("") || webDomain.equals("http://") || webDomain.equals("https://")){
            return;
        }

        List<StompHeader> headers = new ArrayList<>();
        headers.add(new StompHeader(LOGIN, "guest"));
        headers.add(new StompHeader(PASSCODE, "guest"));
        headers.add(new StompHeader("username", "123456789"));

        mStompClient.withClientHeartbeat(1000).withServerHeartbeat(1000);
        resetSubscriptions();

        Disposable dispLifecycle = mStompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            Log.d(TAG, "Stomp connection opened");
                            break;
                        case ERROR:
                            Log.e(TAG, "Stomp connection error " + lifecycleEvent.getException());
                            reConnectStomp();
                            break;
                        case CLOSED:
                            Log.d(TAG, "Stomp connection closed");
                            disconnectStomp();
                            resetSubscriptions();
                            reConnectStomp();
                            break;
                        case FAILED_SERVER_HEARTBEAT:
                            Log.e(TAG, "Stomp failed server heartbeat");
                            break;
                    }
                });

        compositeDisposable.add(dispLifecycle);

        // Receive greetings
        Disposable dispTopic = mStompClient.topic("/user/queue/messages")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    Log.d(TAG, "Received " + topicMessage.getPayload());
                    String msg = processServerResponse(topicMessage.getPayload());
                    //sendToServer("/app/clientResponse", msg);
                }, throwable -> {
                    Log.e(TAG, "Error on subscribe topic", throwable);
                });

        compositeDisposable.add(dispTopic);
        mStompClient.connect(headers);
    }

    private void resetSubscriptions() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
        compositeDisposable = new CompositeDisposable();
    }

    private void disconnectStomp(){
        if(mStompClient != null){
            mStompClient.disconnect();
        }
    }

    private void reConnectStomp(){
        Log.d(TAG, "Try to reconnect stomp server");
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        connectStomp();
    }

    protected CompletableTransformer applySchedulers() {
        return upstream -> upstream
                .unsubscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void sendToServer(String des, String msg){
        compositeDisposable.add(mStompClient.send(des, msg)
                .compose(applySchedulers())
                .subscribe(() -> {
                    Log.d(TAG, "STOMP echo send successfully");
                }, throwable -> {
                    Log.e(TAG, "Error send STOMP echo", throwable);
                }));
    }

    private String processServerResponse(String request){
        SocketResponseMessage responseMessage = null;
        SocketRequestMessage socketRequest = gson.fromJson(request, SocketRequestMessage.class);

        switch (socketRequest.getActionType()){
            case WebSocketActionRequest.CHANGE_SIM:
                try {
                    // Mở MainActivity khi nhận được message
                    Intent intent = new Intent(mContext.getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.getApplicationContext().startActivity(intent);

                    responseMessage = new SocketResponseMessage(socketRequest.getActionType(), "SUCCESS", null, socketRequest.getRequestId());
                } catch (Exception e) {
                    responseMessage = new SocketResponseMessage(socketRequest.getActionType(), "ERROR", e.getMessage(), socketRequest.getRequestId());
                }
                break;

            default:
                break;
        }

        return gson.toJson(responseMessage);
    }
}
