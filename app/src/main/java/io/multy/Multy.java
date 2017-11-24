package io.multy;

import android.app.Application;
import android.content.ContextWrapper;
import android.util.Base64;

import com.samwolfand.oneprefs.Prefs;

import io.multy.util.Constants;
import io.multy.util.NativeDataHelper;

public class Multy extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .setDefaultIntValue(-1)
                .setDefaultBooleanValue(false)
                .setDefaultStringValue("")
                .build();

        if (Prefs.getBoolean(Constants.PREF_FIRST_START, true)) {
            Prefs.putBoolean(Constants.PREF_FIRST_START, false);
            Prefs.putString("seed", Base64.encodeToString(NativeDataHelper.makeSeed(NativeDataHelper.makeMnemonic()), Base64.DEFAULT));
            Prefs.putString("mnemonic", NativeDataHelper.makeMnemonic());
        }
    }
}
