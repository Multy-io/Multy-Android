/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.samwolfand.oneprefs.Prefs;

import io.multy.model.DataManager;
import io.multy.model.entities.ByteSeed;
import io.multy.model.entities.DeviceId;
import io.multy.model.entities.Mnemonic;
import io.multy.model.entities.UserId;

public class FirstLaunchHelper {

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

}
