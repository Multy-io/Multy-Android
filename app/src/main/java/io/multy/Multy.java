package io.multy;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;

import com.samwolfand.oneprefs.Prefs;

import io.branch.referral.Branch;
import io.realm.Realm;
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

//        try {
//            FirstLaunchHelper.setCredentials(null, this);
//        } catch (JniException e) {
//            e.printStackTrace();
//        }

        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
