/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class Analytics {

    private FirebaseAnalytics analytics;
    private static Analytics instance;

    private Analytics(Context context) {
        analytics = FirebaseAnalytics.getInstance(context);
    }

    public Analytics getInstance(Context context) {
        if (instance == null) {
            instance = new Analytics(context);
        }

        return instance;
    }

    public void trackEvent(String event, String argumentName, String argument) {
        Bundle bundle = new Bundle();
        bundle.putString(argumentName, argument);
        analytics.logEvent(event, bundle);
    }
}
