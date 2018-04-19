/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Base64;

import com.samwolfand.oneprefs.Prefs;
import com.tozny.crypto.android.AesCbcWithIntegrity;

import net.khirr.library.foreground.Foreground;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

import javax.annotation.Nullable;

import io.branch.referral.Branch;
import io.multy.storage.RealmManager;
import io.multy.storage.SecurePreferencesHelper;
import io.multy.ui.activities.SplashActivity;
import io.multy.util.Constants;
import io.multy.util.EntropyProvider;
import io.multy.util.RealmMigrations;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import timber.log.Timber;

public class Multy extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        Branch.getAutoInstance(this);
        Timber.plant(new Timber.DebugTree());

        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .setDefaultIntValue(-1)
                .setDefaultBooleanValue(false)
                .setDefaultStringValue("")
                .build();

        context = getApplicationContext();

        if (!Prefs.contains(Constants.PREF_VERSION)) {
            PackageInfo pInfo = null;
            try {
                pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                int versionCode = pInfo.versionCode;
                Prefs.putInt(Constants.PREF_VERSION, versionCode);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        Foreground.Companion.init(this);
    }

    public static Context getContext() {
        return context;
    }

    /**
     * This method is extra dangerous and useful
     * Generates unique new key for our DATABASE and writes it to our secure encrypted preferences
     * only after generating key we can access the DB
     */
    public static void makeInitialized() {
        if (!Prefs.contains(Constants.PREF_IV)) {
            try {
                byte[] iv = AesCbcWithIntegrity.generateIv();
                String vector = new String(Base64.encode(iv, Base64.NO_WRAP));
                Prefs.putString(Constants.PREF_IV, vector);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
        }

        Prefs.putBoolean(Constants.PREF_APP_INITIALIZED, true);
        try {
            String key = new String(Base64.encode(EntropyProvider.generateKey(512), Base64.NO_WRAP));
            SecurePreferencesHelper.putString(getContext(), Constants.PREF_KEY, key);
//            if (RealmManager.open(getContext()) == null) { //TODO review this.
//                systemClear(getContext());
//            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public static RealmConfiguration getRealmConfiguration() {
        String key = SecurePreferencesHelper.getString(context, Constants.PREF_KEY);
        RealmConfiguration realmConfiguration = null;
        try {
            return realmConfiguration = new RealmConfiguration.Builder()
                    .encryptionKey(Base64.decode(key, Base64.NO_WRAP))
                    .schemaVersion(2)
                    .migration(new RealmMigrations())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void systemClear(Context context) {
        try {
            RealmManager.clear();
            Prefs.clear();
//            Multy.makeInitialized();
//            Realm.init(context);
        } catch (Exception exc) {
//            System.exit(0);
            exc.printStackTrace();
        }

        context.startActivity(new Intent(context, SplashActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }
}