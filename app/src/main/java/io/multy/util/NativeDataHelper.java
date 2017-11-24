/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.util;


public class NativeDataHelper {

    static {
        System.loadLibrary("core_jnid");
    }

    public static native String makeMnemonic();

    public static native byte[] makeSeed(String mnemonic);

    public static native byte[] makeAccountAddress();

    public static native String makeAccountId(byte[] seed);

    public static native int runTest();

}
