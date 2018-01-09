package io.multy;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;

import com.samwolfand.oneprefs.Prefs;

import io.branch.referral.Branch;
import io.multy.encryption.MasterKeyGenerator;
import io.multy.util.MyRealmMigration;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import timber.log.Timber;

public class Multy extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
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

        Realm.setDefaultConfiguration(new RealmConfiguration.Builder()
                .encryptionKey(MasterKeyGenerator.generateKey(context))
                .schemaVersion(1)
                .migration(new MyRealmMigration())
                .build());

        context = getApplicationContext();


    }

    public static Context getContext() {
        return context;
    }
}
