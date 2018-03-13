/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class NetworkUtils {

    private static NetworkInfo getNetworkInfo(Context context) {
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            return connectivityManager.getActiveNetworkInfo();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isConnected(Context context) {
        if (getNetworkInfo(context) != null) {
            return (getNetworkInfo(context) != null && (getNetworkInfo(context).isConnectedOrConnecting()) && isServerOnline());
        } else {
            return (isServerOnline());
        }
    }

    private static boolean isServerOnline() {
//        try {
//            Runtime runtime = Runtime.getRuntime();
//            Process process = runtime.exec("/system/bin/ping -c 1 google.com");
//            process.waitFor();
//            int exit = process.exitValue();
//            return exit == 0;
//        } catch (InterruptedException | IOException e) {
//            e.printStackTrace();
//        }
        return true;
    }
}
