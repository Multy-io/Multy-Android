/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.api.socket;

import android.arch.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.samwolfand.oneprefs.Prefs;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.multy.storage.RealmManager;
import io.multy.util.Constants;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.Transport;
import io.socket.engineio.client.transports.WebSocket;
import timber.log.Timber;

public class SocketManager {

    public static final String TAG = SocketManager.class.getSimpleName();
    private static final String DEVICE_TYPE = "Android";

    private static final String SOCKET_URL = Constants.BASE_URL;
    private static final String HEADER_AUTH = "jwtToken";
    private static final String HEADER_DEVICE_TYPE = "deviceType";
    private static final String HEADER_USER_ID = "userId";
    private static final String EVENT_RECEIVE = "TransactionUpdate";
    @Deprecated
    private static final String EVENT_RECEIVE_DEPRECATED = "btcTransactionUpdate";
    private static final String EVENT_EXCHANGE_RESPONSE = "exchangePoloniex";

    public static final String EVENT_MESSAGE_SEND = "message:send";
    private static final String EVENT_MESSAGE_RECEIVE = "message:recieve:";
//    private static final String EVENT_EXCHANGE_RESPONSE = "exchangeBitfinex";

    public static final int SOCKET_JOIN = 1;
    public static final int SOCKET_LEAVE = 2;
    public static final int SOCKET_DELETE = 3; //only for creator delete wallet and leave room
    public static final int SOCKET_KICK = 4; //only creator can kick guys
    public static final int SOCKET_VALIDATE = 5;

    private Socket socket;
    private Gson gson;

    public SocketManager() throws URISyntaxException {
//                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                    .hostnameVerifier((hostname, session) -> true)
//                    .sslSocketFactory(mySSLContext.getSocketFactory(), myX509TrustManager)
//                    .build();

//            IO.setDefaultOkHttpWebSocketFactory(okHttpClient);
//            IO.setDefaultOkHttpCallFactory(okHttpClient);

//            options.callFactory = okHttpClient;
//            options.webSocketFactory = okHttpClient;
        gson = new Gson();
        IO.Options options = new IO.Options();
        options.forceNew = true;
        options.reconnectionAttempts = 3;
        options.transports = new String[]{WebSocket.NAME};
        options.path = "/socket.io";
        options.secure = false;
        socket = IO.socket(SOCKET_URL, options);
        initDefaultHeaders();
        initDefaultEvents();
    }

    private void initDefaultHeaders() {
        final String userId = RealmManager.getSettingsDao().getUserId().getUserId();
        socket.io().on(Manager.EVENT_TRANSPORT, args -> {
            Transport transport = (Transport) args[0];
            transport.on(Transport.EVENT_REQUEST_HEADERS, args1 -> {
                @SuppressWarnings("unchecked")
                Map<String, List<String>> headers = (Map<String, List<String>>) args1[0];
                headers.put(HEADER_AUTH, Arrays.asList(Prefs.getString(Constants.PREF_AUTH)));
                headers.put(HEADER_DEVICE_TYPE, Arrays.asList(DEVICE_TYPE));
                headers.put(HEADER_USER_ID, Arrays.asList(userId));
            });
        });
    }

    private void initDefaultEvents() {
        socket.on(Socket.EVENT_CONNECT_ERROR, args -> Timber.e("Error connecting to socket: " + args[0].toString()))
                .on(Socket.EVENT_CONNECT_TIMEOUT, args -> Timber.e("connection timeout"))
                .on(Socket.EVENT_CONNECT, args -> Timber.i("Connected"))
                .on(Socket.EVENT_DISCONNECT, args -> Timber.i("Disconnected"));
    }

    public void requestRates() {
//        if (socket != null) {
//            try {
//                final ExchangeRequestEntity entity = new ExchangeRequestEntity("USD", "BTC");
//                socket.emit(EVENT_EXCHANGE_REQUEST, new JSONObject(gson.toJson(entity)), (Ack) args -> log("sock exchange request delivered "));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
    }

    public void listenRatesAndTransactions(MutableLiveData<CurrenciesRate> rates, MutableLiveData<TransactionUpdateEntity> transactionUpdateEntity) {
        socket.on(EVENT_EXCHANGE_RESPONSE, args -> {
            Timber.i("received rate " + String.valueOf(args[0]));
            rates.postValue(gson.fromJson(String.valueOf(args[0]), CurrenciesRate.class));
        }).on(EVENT_RECEIVE, args -> {
            try {
                Timber.i("UPDATE " + String.valueOf(args[0]));
                transactionUpdateEntity.postValue(gson.fromJson(String.valueOf(args[0]), TransactionUpdateResponse.class).getEntity());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).on(EVENT_RECEIVE_DEPRECATED, args -> {
            try {
                Timber.i("UPDATE " + String.valueOf(args[0]));
                transactionUpdateEntity.postValue(gson.fromJson(String.valueOf(args[0]), TransactionUpdateResponse.class).getEntity());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void listenTransactionUpdates(Runnable updateCallback) {
        socket.on(EVENT_RECEIVE, args -> {
            try {
                Timber.i("UPDATE " + String.valueOf(args[0]));
                updateCallback.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void listenEvent(String event, Emitter.Listener listener) {
        socket.on(event, listener);
    }

    public void sendEvent(String sendEvent, JSONObject eventJson, Ack ack) {
        socket.emit(sendEvent, eventJson, ack);
    }

    public void sendMultisigTransactionOwnerAction(JSONObject eventJson, Ack ack) {
        socket.emit(EVENT_MESSAGE_SEND, eventJson, ack);
    }

    public void connect() {
        socket.connect();
    }

    public boolean isConnected() {
        return socket.connected();
    }

    public void disconnect() {
        socket.disconnect();
    }

    public static String getEventReceive(String userId) {
        return EVENT_MESSAGE_RECEIVE + userId;
    }
}
