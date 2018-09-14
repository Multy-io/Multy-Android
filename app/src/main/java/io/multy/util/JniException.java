/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;

import com.crashlytics.android.Crashlytics;

public class JniException extends Exception {

    public JniException(String message) {
        super(message);
        Crashlytics.logException(new Throwable(message));
    }
}
