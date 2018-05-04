/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.storage;

import android.app.Activity;
import android.content.Context;
import android.util.Base64;

import com.samwolfand.oneprefs.Prefs;

import javax.annotation.Nullable;
import javax.crypto.BadPaddingException;
import javax.crypto.SecretKey;

import io.multy.Multy;
import io.multy.encryption.CryptoUtils;
import io.multy.encryption.MasterKeyGenerator;
import io.multy.util.Constants;
import io.multy.util.EntropyProvider;

import static io.multy.encryption.CryptoUtils.sha;

public class SecurePreferencesHelper {

    public static void putString(Context context, String key, String value) {
        byte[] mk = MasterKeyGenerator.generateKey(context);
        final byte[] pass = Base64.encode(sha(sha(mk)), Base64.NO_WRAP);
        final String passPhrase = new String(pass);
        final byte[] salt = sha(mk);

        try {
            SecretKey secretKey = EntropyProvider.generateKey(passPhrase.toCharArray(), salt);
            final byte[] iv = Base64.decode(Prefs.getString(Constants.PREF_IV), Base64.NO_WRAP);
            final byte[] encryptedValue = CryptoUtils.encrypt(iv, secretKey.getEncoded(), value.getBytes());
            final String encodedString = new String(Base64.encode(encryptedValue, Base64.NO_WRAP));
            Prefs.putString(key, encodedString);
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
//            clear();
        }
    }


    @Nullable
    public static String getString(Context context, String key) {
        byte[] mk = MasterKeyGenerator.generateKey(context);
        final byte[] pass = Base64.encode(sha(sha(mk)), Base64.NO_WRAP);
        final String passPhrase = new String(pass);
        final byte[] salt = sha(mk);
        try {
            SecretKey secretKey = EntropyProvider.generateKey(passPhrase.toCharArray(), salt);
            final byte[] iv = Base64.decode(Prefs.getString(Constants.PREF_IV), Base64.NO_WRAP);
            final String encryptedString = Prefs.getString(key);
            byte[] encrypted = Base64.decode(encryptedString, Base64.NO_WRAP);
            final String result = new String(CryptoUtils.decrypt(iv, secretKey.getEncoded(), encrypted));
            return result;
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
//            clear();
        }
        return null;
    }

    private static void clear() {
        RealmManager.clear();
        Prefs.clear();
    }
}
