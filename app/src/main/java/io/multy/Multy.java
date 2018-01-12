package io.multy;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;

import com.samwolfand.oneprefs.Prefs;
import com.tozny.crypto.android.AesCbcWithIntegrity;

import net.khirr.library.foreground.Foreground;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

import io.branch.referral.Branch;
import io.multy.storage.RealmManager;
import io.multy.storage.SecurePreferencesHelper;
import io.multy.ui.activities.SplashActivity;
import io.multy.util.Constants;
import io.multy.util.EntropyProvider;
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

        context = getApplicationContext();

        if (Prefs.getString(Constants.PREF_IV, "").equals("")) {
            try {
                byte[] iv = AesCbcWithIntegrity.generateIv();
                String vector = new String(Base64.encode(iv, Base64.NO_WRAP));
                Prefs.putString(Constants.PREF_IV, vector);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
        }

        if (Prefs.contains(Constants.PREF_VERSION)) {
            PackageInfo pInfo = null;
            try {
                pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                int versionCode = pInfo.versionCode;
                Prefs.putInt(Constants.PREF_VERSION, versionCode);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        final String counter = SecurePreferencesHelper.getString(this, Constants.PIN_COUNTER);
        if (counter.equals("")) {
            SecurePreferencesHelper.putString(this, Constants.PIN_COUNTER, String.valueOf(6));
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
        Prefs.putBoolean(Constants.PREF_APP_INITIALIZED, true);
        try {
            String key = new String(Base64.encode(EntropyProvider.generateKey(512), Base64.NO_WRAP));
            SecurePreferencesHelper.putString(getContext(), Constants.PREF_KEY, key);
            RealmManager.open(getContext());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static void systemClear(Activity activity) {
        try {
            RealmManager.removeDatabase(activity);
            Prefs.clear();
            Multy.makeInitialized();
            Realm.init(activity);
        } catch (Exception exc) {
//            System.exit(0);
            exc.printStackTrace();
        }

        activity.startActivity(new Intent(activity, SplashActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
        activity.finish();
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if ((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
