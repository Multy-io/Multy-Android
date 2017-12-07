package io.multy;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;

import com.crashlytics.android.Crashlytics;
import com.samwolfand.oneprefs.Prefs;

import java.util.UUID;

import io.branch.referral.Branch;
import io.multy.model.DataManager;
import io.multy.model.entities.ByteSeed;
import io.multy.model.entities.DeviceId;
import io.multy.model.entities.Mnemonic;
import io.multy.model.entities.UserId;
import io.multy.util.Constants;
import io.multy.util.JniException;
import io.multy.util.NativeDataHelper;
import io.realm.Realm;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

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

        if (Prefs.getBoolean(Constants.PREF_FIRST_SUCCESSFUL_START, true)) {
            try {
                final String mnemonic = NativeDataHelper.makeMnemonic();
                final byte[] seed = NativeDataHelper.makeSeed(mnemonic);
                final String userId = NativeDataHelper.makeAccountId(seed);
                final String deviceId = UUID.randomUUID().toString();

                DataManager dataManager = new DataManager(this);
                dataManager.saveSeed(new ByteSeed(seed));
                dataManager.saveUserId(new UserId(userId));
                dataManager.setMnemonic(new Mnemonic(mnemonic));
                dataManager.setDeviceId(new DeviceId(deviceId));

                Prefs.putBoolean(Constants.PREF_FIRST_SUCCESSFUL_START, false);
            } catch (JniException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
                //TODO show CRITICAL EXCEPTION HERE. Can the app work without seed?
            }
        }

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("montseratt_regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
