/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.storage;

import android.content.Context;
import android.util.Log;

import java.io.File;

import io.multy.Multy;
import io.realm.Realm;

public class RealmManager {

    private final static String TAG = RealmManager.class.getSimpleName();

    private static Realm realm;

    public static Realm open() {
        try {
            realm = Realm.getInstance(Multy.getRealmConfiguration());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return realm;
    }

    public static void close() {
        if (realm != null && realm.isClosed()) {
            realm.close();
        }
    }

    public static AssetsDao getAssetsDao() {
        isRealmAvailable();
        return new AssetsDao(realm);
    }

    public static SettingsDao getSettingsDao() {
        isRealmAvailable();
        return new SettingsDao(realm);
    }

    public static void clear() {
        if (realm == null || realm.isClosed()) {
            open();
        }

        if (realm != null) {
            realm.executeTransaction(realm -> realm.deleteAll());
        }
    }

    private static void isRealmAvailable() {
        try {
            if (realm == null || realm.isClosed()) {
                Log.e(TAG, "ERROR DB IS CLOSED OR NULL");
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
            open();
        }
    }

//    public static void deleteRealm() {
//        if (realm == null) {
//            open(Multy.getContext());
//        }
//
//        if (!realm.isClosed()){
//            realm.close();
//        }
//
//        Realm.deleteRealm()
//    }

    public static void removeDatabase(Context context) {
        for (File file : context.getFilesDir().listFiles()) {
            if (file.getAbsolutePath().contains("realm")) {
                if (file.isDirectory()) {
                    removeFilesFromDirectory(file);
                }
                file.delete();
            }
        }
    }

    private static void removeFilesFromDirectory(File file) {
        for (File subFile : file.listFiles()) {
            if (subFile.isDirectory()) {
                removeFilesFromDirectory(file);
            }
            subFile.delete();
        }
    }
}
