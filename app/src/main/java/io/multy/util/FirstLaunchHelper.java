/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;

import android.app.Activity;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

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
        return Prefs.contains(Constants.PREF_LOCK);
    }

    public static boolean isFingerprintEnabled() {
        return Prefs.contains(Constants.PREF_IS_FINGERPRINT_ENABLED);
    }

    public static boolean isPinAttemptsAppropriate() {
        return true;
//        return Prefs.getInt(Constants.PIN_COUNTER) <= 6;
    }

    public static void setCredentials(String seedPhrase) throws JniException {
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

        DataManager dataManager = DataManager.getInstance();
        dataManager.saveSeed(new ByteSeed(seed));
        dataManager.saveUserId(new UserId(userId));
        if (!TextUtils.isEmpty(mnemonic)) {
            dataManager.setMnemonic(new Mnemonic(mnemonic));
        }
        dataManager.setDeviceId(new DeviceId(deviceId));
    }

    public static void closeApp(Activity activity) {
        new DatabaseHelper(activity).clear();
        Prefs.clear();
        activity.finish();
        System.exit(0);
    }

}
