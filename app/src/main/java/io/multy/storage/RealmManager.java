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
import io.realm.exceptions.RealmMigrationNeededException;

public class RealmManager {

    private final static String TAG = RealmManager.class.getSimpleName();

    private static Realm realm;

    public static Realm open() {
        try {
            realm = Realm.getInstance(Multy.getRealmConfiguration());
        } catch (RealmMigrationNeededException e) {
            e.printStackTrace();
        }
        return realm;
    }

    public static void close() {
        if (realm != null && !realm.isClosed()) {
            try {
                realm.close();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                open(); //since we can't call realm from different threads, we need to reopen and close realm on the same thread
                realm.close();
            }
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

        close();
    }

    private static void isRealmAvailable() {
        try {
            if (realm == null || realm.isClosed()) {
                Log.e(TAG, "ERROR DB IS CLOSED OR NULL");
                open();
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
