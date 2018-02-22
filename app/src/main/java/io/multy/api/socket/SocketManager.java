/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.api.socket;

import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import com.google.gson.Gson;
import com.samwolfand.oneprefs.Prefs;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.multy.storage.RealmManager;
import io.multy.util.Constants;
import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.engineio.client.Transport;
import io.socket.engineio.client.transports.WebSocket;
import okhttp3.OkHttpClient;
import timber.log.Timber;

public class SocketManager {

    public static final String TAG = SocketManager.class.getSimpleName();
    private static final String DEVICE_TYPE = "Android";

    private static final String SOCKET_URL = "https://test.multy.io/";
    private static final String HEADER_AUTH = "jwtToken";
    private static final String HEADER_DEVICE_TYPE = "deviceType";
    private static final String HEADER_USER_ID = "userId";
    private static final String EVENT_RECEIVE = "btcTransactionUpdate";
    private static final String EVENT_EXCHANGE_RESPONSE = "exchangePoloniex";

    private Socket socket;
    private Gson gson;

    public SocketManager() {
        gson = new Gson();
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

    public void connect(MutableLiveData<CurrenciesRate> rates, MutableLiveData<TransactionUpdateEntity> transactionUpdateEntity) {
        try {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .hostnameVerifier((hostname, session) -> true)
//                    .sslSocketFactory(mySSLContext.getSocketFactory(), myX509TrustManager)
                    .build();

            IO.setDefaultOkHttpWebSocketFactory(okHttpClient);
            IO.setDefaultOkHttpCallFactory(okHttpClient);

            IO.Options options = new IO.Options();
            options.forceNew = true;
            options.reconnectionAttempts = 3;
            options.transports = new String[]{WebSocket.NAME};
            options.path = "/socket.io";
            options.secure = false;
            options.callFactory = okHttpClient;
            options.webSocketFactory = okHttpClient;

            final String userId = RealmManager.getSettingsDao().getUserId().getUserId();

            Log.i(TAG, "auth header " + Prefs.getString(Constants.PREF_AUTH));
            Log.i(TAG, "userId " + userId);

            socket = IO.socket(SOCKET_URL, options);
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

            socket
                    .on(Socket.EVENT_CONNECT_ERROR, args -> ((Exception) args[0]).printStackTrace())
                    .on(Socket.EVENT_CONNECT_TIMEOUT, args -> log("connection timeout"))
                    .on(Socket.EVENT_CONNECT, args -> log("Connected"))
                    .on(EVENT_EXCHANGE_RESPONSE, args -> {
                        Timber.i("received rate " + String.valueOf(args[0]));
                        rates.postValue(gson.fromJson(String.valueOf(args[0]), CurrenciesRate.class));
                    })
                    .on(Socket.EVENT_DISCONNECT, args -> log("Disconnected"))
                    .on(EVENT_RECEIVE, args -> {
                        try {
                            Timber.i("UPDATE " + String.valueOf(args[0]));
                            transactionUpdateEntity.postValue(gson.fromJson(String.valueOf(args[0]), TransactionUpdateResponse.class).getEntity());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void log(String message) {
        Log.i(TAG, message);
    }

    public void disconnect() {
        socket.disconnect();
    }
}
