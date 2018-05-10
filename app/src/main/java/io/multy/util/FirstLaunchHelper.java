/*
 * Copyright 2018 Idealnaya rabota LLC
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

import java.io.File;

import io.multy.R;
import io.multy.model.entities.ByteSeed;
import io.multy.model.entities.DeviceId;
import io.multy.model.entities.Mnemonic;
import io.multy.model.entities.UserId;
import io.multy.storage.RealmManager;
import io.multy.storage.SettingsDao;
import io.multy.ui.fragments.dialogs.SimpleDialogFragment;


public class FirstLaunchHelper {

    public static boolean checkForBinary(String filename) {
        for (String path : Constants.rootPaths) {
            final String completePath = path + filename;
            final File f = new File(completePath);
            if (f.exists()) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkForBinaries(){
        for (String filename : Constants.rootFiles){
            if (checkForBinary(filename)){
                return true;
            }
        }
        return false;
    }

    public static boolean isRooted(AppCompatActivity activity){
        RootBeer rootBeer = new RootBeer(activity);
        return rootBeer.detectRootManagementApps(Constants.rootApplications) || rootBeer.isRootedWithoutBusyBoxCheck() || checkForBinaries();
    }

    public static boolean preventRootIfDetected(AppCompatActivity activity) {
        if (isRooted(activity)) {
            SimpleDialogFragment.newInstanceNegative(R.string.root_title, R.string.root_message, view -> {
                closeApp(activity);
            }).show(activity.getSupportFragmentManager(), "");
            return true;
        }
        return false;
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

        SettingsDao settingsDao = RealmManager.getSettingsDao();
        settingsDao.saveSeed(new ByteSeed(seed));
        settingsDao.saveUserId(new UserId(userId));
        if (!TextUtils.isEmpty(mnemonic)) {
            settingsDao.setMnemonic(new Mnemonic(mnemonic));
        }
        settingsDao.setDeviceId(new DeviceId(deviceId));
    }

    public static void closeApp(Activity activity) {
        Prefs.clear();
        activity.finish();
        System.exit(0);
    }

}
