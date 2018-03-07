/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.storage;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import java.io.File;

import javax.annotation.Nullable;

import io.multy.Multy;
import io.multy.util.Constants;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmManager {

    private final static String TAG = RealmManager.class.getSimpleName();

    private static Realm realm;

    public static Realm open(Context context) {
        if (realm == null || realm.isClosed()) {
            try {
                realm = Realm.getInstance(getConfiguration(context));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return realm;
    }

    /**
     * TODO set config to DEFAULT ONLY ONCE SOMEWHERE, for some reason generatekey isn't working in onCreate of Multy.class
     *
     * @return
     */
    @Deprecated
    @Nullable
    public static RealmConfiguration getConfiguration(Context context) {
        String key = SecurePreferencesHelper.getString(context, Constants.PREF_KEY);
        RealmConfiguration realmConfiguration = null;
        try {
            realmConfiguration = new RealmConfiguration.Builder()
                    .encryptionKey(Base64.decode(key, Base64.NO_WRAP))
                    .schemaVersion(1)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return realmConfiguration;
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
            open(Multy.getContext());
        }
        realm.executeTransaction(realm -> realm.deleteAll());
    }

    private static void isRealmAvailable() {
        if (realm == null || realm.isClosed()) {
            Log.e(TAG, "ERROR DB IS CLOSED OR NULL");
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
