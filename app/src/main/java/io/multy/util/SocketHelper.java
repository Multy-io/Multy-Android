/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;

import android.util.Log;

import com.google.gson.Gson;
import com.samwolfand.oneprefs.Prefs;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.multy.Multy;
import io.multy.model.DataManager;
import io.multy.model.entities.ExchangeRequestEntity;
import io.multy.viewmodels.TransactionViewModel;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.Transport;
import io.socket.engineio.client.transports.WebSocket;

public class SocketHelper {

    public static final String TAG = SocketHelper.class.getSimpleName();

    private static final String SOCKET_URL = "http://192.168.0.111:7780/";
    private static final String HEADER_AUTH = "jwtToken";
    private static final String HEADER_DEVICE_TYPE = "deviceType";
    private static final String HEADER_USER_ID = "userId";
    private static final String EVENT_RECEIVE = "/transactionData";
    private static final String EVENT_EXCHANGE_REQUEST = "getExchangeReq";
    private static final String EVENT_EXCHANGE_RESPONSE = "getExchangeResp";

    private Socket socket;
    private boolean connected = false;
    private Gson gson;
    private TransactionViewModel viewModel;

    public SocketHelper() {
        gson = new Gson();

        try {
            IO.Options options = new IO.Options();
            options.timeout = 10000;
            options.reconnectionAttempts = 3;
            options.transports = new String[]{WebSocket.NAME};

            socket = IO.socket(SOCKET_URL, options);

            socket.io().on(Manager.EVENT_TRANSPORT, args -> {
                Transport transport = (Transport) args[0];
                transport.on(Transport.EVENT_REQUEST_HEADERS, args1 -> {
                    @SuppressWarnings("unchecked")
                    Map<String, List<String>> headers = (Map<String, List<String>>) args1[0];

                    DataManager dataManager = new DataManager(Multy.getContext());
                    headers.put(HEADER_AUTH, Arrays.asList(Prefs.getString(Constants.PREF_AUTH)));
                    headers.put(HEADER_DEVICE_TYPE, Arrays.asList("Android"));
                    headers.put(HEADER_USER_ID, Arrays.asList(dataManager.getUserId().getUserId()));
                });
            });

            socket.on(Socket.EVENT_CONNECT_ERROR, args -> SocketHelper.this.log("connection error"))
                    .on(Socket.EVENT_CONNECT_TIMEOUT, args -> log("connection timeout"))
                    .on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            SocketHelper.this.log("Connected");
                            requestRates();
                        }
                    })
                    .on(EVENT_RECEIVE, args -> {
//                            viewModel.transactionData.setValue(gson.fromJson(String.valueOf(args[0]), Transaction.class));
                        SocketHelper.this.log("socket data received " + String.valueOf(args[0]));
                    })
                    .on(Socket.EVENT_DISCONNECT, args -> log("Disconnected"))
                    .on(EVENT_EXCHANGE_RESPONSE, new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            log(String.valueOf(args[0]));
                        }
                    });

            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void requestRates() {
        if (socket != null) {
            final ExchangeRequestEntity entity = new ExchangeRequestEntity("USD", "BTC");
            log("sending " + EVENT_EXCHANGE_REQUEST + " " + gson.toJson(entity));
            socket.emit("/getExchangeReq", "[\"From\": \"USD\", \"To\": \"BTC\"]", new Ack() {
                @Override
                public void call(Object... args) {
                    log("exchange request delivered ");
                }
            });
            socket.emit("getExchangeReq", "{\"From\": \"USD\", \"To\": \"BTC\"}", new Ack() {
                @Override
                public void call(Object... args) {
                    log("exchange request delivered ");
                }
            });
        }
    }

    private void log(String message) {
        Log.i(TAG, message);
    }

    public void disconnect() {
        socket.disconnect();
    }
}
