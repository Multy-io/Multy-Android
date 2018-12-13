/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.samwolfand.oneprefs.Prefs;

public class DeviceInfoHelper {

//    private static String getSystemProperty(String name) throws Exception {
//        Class systemPropertyClazz = Class.forName("android.os.SystemProperties");
//        return (String) systemPropertyClazz.getMethod("get", new Class[]{String.class})
//                .invoke(systemPropertyClazz, new Object[]{name});
//    }
//
//    public static boolean checkEmulator() {
//        try {
//            boolean goldfish = getSystemProperty("ro.hardware").contains("goldfish");
//            boolean sdk = getSystemProperty("ro.product.model").equals("sdk");
//            boolean qemu = getSystemProperty("ro.kernel.qemu").length() > 0;
//            return qemu || goldfish || sdk;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return false;
//    }

    public static boolean isVersionDebug(Context context) {
        return (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    public static int getSeedPhraseType() {
        return Prefs.getInt(Constants.SEED_TYPE, Constants.SEED_TYPE_MULTY);
    }
}
