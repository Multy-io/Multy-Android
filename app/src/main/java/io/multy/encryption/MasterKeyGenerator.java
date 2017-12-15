/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.encryption;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;

import com.google.android.gms.iid.InstanceID;

import org.spongycastle.jcajce.provider.digest.SHA3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Ihar Paliashchuk on 10.11.2017.
 * ihar.paliashchuk@gmail.com
 */

public class MasterKeyGenerator {

    private static byte[] getInstanceID(Context context) {
        return InstanceID.getInstance(context).getId().getBytes();
    }

    @SuppressLint("HardwareIds")
    private static byte[] getSecureID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID).getBytes();
    }

    /**
     * Provide PIN or password if user set it. Method will be used after alpha release
     * for better secure protection, but it returns empty value for now.
     *
     * @return zero if no password or PIN, otherwise bytes of password or PIN.
     */
    private static byte[] getUserSecret() {
        return new byte[0];
    }

    public static byte[] generateKey(Context context) {
        ByteArrayOutputStream outputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            outputStream.write(getInstanceID(context));
            outputStream.write(getUserSecret());
            outputStream.write(getSecureID(context));
            return ByteBuffer.allocate(calculateSHA3256(outputStream.toByteArray()).length * 2)
                    .put(calculateSHA3256(outputStream.toByteArray()))
                    .put(calculateSHA3256(outputStream.toByteArray()))
                    .array();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static byte[] calculateSHA3256(byte[] input){
        SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest256();
        return digestSHA3.digest(input);
    }
}
