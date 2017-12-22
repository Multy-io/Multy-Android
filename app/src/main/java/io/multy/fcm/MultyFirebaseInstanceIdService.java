/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.fcm;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MultyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

//        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("wise", "Refreshed token: " + refreshedToken);
//
//        // If you want to requestRates messages to this application instance or
//        // manage this apps subscriptions on the server side, requestRates the
//        // Instance ID token to your app server.
//        sendRegistrationToServer(refreshedToken);
    }
}
