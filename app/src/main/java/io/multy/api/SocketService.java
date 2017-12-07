/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.api;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.engineio.client.Transport;
import io.socket.engineio.client.transports.WebSocket;

public class SocketService extends Service {

    public static final String TAG = SocketService.class.getSimpleName();

    private static final String SOCKET_URL = "http://192.168.0.109:6666/";
    private static final String HEADER_AUTH = "jwtToken";
    private static final String HEADER_DEVICE_TYPE = "deviceType";
    private static final String HEADER_USER_ID = "userId";
    private static final String EVENT_RECEIVE = "/newTransaction";

    private Socket socket;
    private boolean connected;

    @Override
    public void onCreate() {
        super.onCreate();

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
                    headers.put(HEADER_AUTH, Arrays.asList("SomeVerySecureJWTToken"));
                    headers.put(HEADER_DEVICE_TYPE, Arrays.asList("Android"));
                    headers.put(HEADER_USER_ID, Arrays.asList("228"));
                });
            });

            socket.on(Socket.EVENT_CONNECT_ERROR, args -> log("connection error"))
                    .on(Socket.EVENT_CONNECT_TIMEOUT, args -> log("connection timeout"))
                    .on(Socket.EVENT_CONNECT, args -> log("Connected"))
                    .on(EVENT_RECEIVE, args -> log("socket data received " + String.valueOf(args[0])))
                    .on(Socket.EVENT_DISCONNECT, args -> log("Disconnected"));

            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void log(String message) {
        Log.i(TAG, message);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("onStartCommand");
        return START_STICKY;
    }
}
