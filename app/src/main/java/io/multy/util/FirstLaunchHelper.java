/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.samwolfand.oneprefs.Prefs;
import com.scottyab.rootbeer.RootBeer;

import io.multy.R;
import io.multy.model.DataManager;
import io.multy.model.entities.ByteSeed;
import io.multy.model.entities.DeviceId;
import io.multy.model.entities.Mnemonic;
import io.multy.model.entities.UserId;
import io.multy.storage.DatabaseHelper;
import io.multy.ui.fragments.dialogs.SimpleDialogFragment;

public class FirstLaunchHelper {

    public static void preventRootIfDetected(AppCompatActivity activity) {
        RootBeer rootBeer = new RootBeer(activity);
        if (rootBeer.isRootedWithoutBusyBoxCheck()) {
            SimpleDialogFragment.newInstanceNegative(R.string.root_title, R.string.root_message, view -> {
                closeApp(activity);
            }).show(activity.getSupportFragmentManager(), "");
        }
    }

    public static boolean isLockModeEnabled() {
        return Prefs.contains(Constants.IS_LOCK_MODE_ENABLED);
    }

    public static boolean isFingerprintEnabled() {
        return Prefs.contains(Constants.IS_FINGERPRINT_ENABLED);
    }

    public static boolean isFingerprintAttemptsAppropriate() {
        return Prefs.getInt(Constants.FINGERPRINT_COUNTER) <= 6;
    }

    public static boolean isPinAttemptsAppropriate() {
        return Prefs.getInt(Constants.PIN_COUNTER) <= 6;
    }

    public static void setCredentials(String seedPhrase, Context context) throws JniException {
        if (Prefs.getBoolean(Constants.PREF_FIRST_SUCCESSFUL_START, true)) {
//            try {
            String mnemonic = null;
            byte[] seed;
            if (TextUtils.isEmpty(seedPhrase)) {
                mnemonic = NativeDataHelper.makeMnemonic();
                seed = NativeDataHelper.makeSeed(mnemonic);
            } else {
                seed = NativeDataHelper.makeSeed(seedPhrase);
            }

            final String userId = NativeDataHelper.makeAccountId(seed);
            final String deviceId = Settings.Secure.ANDROID_ID;

            DataManager dataManager = new DataManager(context);
            dataManager.saveSeed(new ByteSeed(seed));
            dataManager.saveUserId(new UserId(userId));
            if (!TextUtils.isEmpty(mnemonic)) {
                dataManager.setMnemonic(new Mnemonic(mnemonic));
            }
            dataManager.setDeviceId(new DeviceId(deviceId));

            Prefs.putBoolean(Constants.PREF_FIRST_SUCCESSFUL_START, false);
//            } catch (JniException e) {
//                e.printStackTrace();
//                Crashlytics.logException(e);
//                //TODO show CRITICAL EXCEPTION HERE. Can the app work without seed?
//            }
        }
    }

    public static void closeApp(Activity activity) {
        new DatabaseHelper(activity).clear();
        Prefs.clear();
        activity.finish();
        System.exit(0);
    }

}
