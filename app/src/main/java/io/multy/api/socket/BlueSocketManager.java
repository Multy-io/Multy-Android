/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.api.socket;

import android.util.Log;

import com.google.gson.Gson;
import com.samwolfand.oneprefs.Prefs;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
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
import okhttp3.OkHttpClient;
import timber.log.Timber;

public class BlueSocketManager {

    public static final String TAG = BlueSocketManager.class.getSimpleName();
    private static final String DEVICE_TYPE = "Android";

    private static final String SOCKET_URL = Constants.BASE_URL;
    private static final String HEADER_AUTH = "jwtToken";
    private static final String HEADER_DEVICE_TYPE = "deviceType";
    private static final String HEADER_USER_ID = "userId";

    public static final String EVENT_RECEIVER_ON = "event:receiver:on";
    public static final String EVENT_SENDER_ON = "event:sender:on";
    public static final String EVENT_SENDER_CHECK = "event:sender:check";
    public static final String EVENT_FILTER = "event:filter";
    public static final String EVENT_NEW_RECEIVER = "event:new:receiver:";
    public static final String EVENT_SEND_RAW = "event:sendraw";
    public static final String EVENT_PAY_SEND = "event:payment:send";
    public static final String EVENT_PAY_RECEIVE = "event:payment:received";
    public static final String EVENT_TRANSACTION_UPDATE = "TransactionUpdate";
    public static final String EVENT_TRANSACTION_UPDATE_BTC = "TransactionUpdate";

    private Socket socket;
    private Gson gson;

    public BlueSocketManager() {
        gson = new Gson();
    }

    public void connect() {
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
                    .on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Timber.e("Error connecting to socket: " + args[0].toString());
                        }
                    })
                    .on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            BlueSocketManager.this.log("connection timeout");
                        }
                    })
//                    .on(Socket.EVENT_CONNECT, args -> log("Connected"))
                    .on(EVENT_RECEIVER_ON, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            BlueSocketManager.this.log("Receiver on " + args[0]);
                        }
                    })
                    .on(EVENT_SENDER_ON, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            BlueSocketManager.this.log("Sender on " + args[0]);
                        }
                    })
                    .on(EVENT_SENDER_CHECK, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Log.i("wise", "Sender check " + args[0].toString());
                        }
                    })
                    .on(EVENT_FILTER, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            BlueSocketManager.this.log("Filter  " + args[0]);
                        }
                    })
                    .on(EVENT_NEW_RECEIVER, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            BlueSocketManager.this.log("New receiver " + args[0]);
                        }
                    })
                    .on(EVENT_SEND_RAW, args -> log("Send raw " + args[0]))
                    .on(EVENT_PAY_SEND, args -> log("Pay send " + args[0]))
                    .on(EVENT_PAY_RECEIVE, args -> log("Payment received " + args[0]));
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void becomeReceiver(JSONObject jsonObject) {
        socket.emit(EVENT_RECEIVER_ON, jsonObject, new Ack() {
            @Override
            public void call(Object... args) {
                Log.i("wise", "become sender got ack");
            }
        });
    }

    private void log(String message) {
        Timber.i(message);
    }

    public void disconnect() {
        socket.disconnect();
    }

    public Socket getSocket() {
        return socket;
    }

}
