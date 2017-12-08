/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.util;


/**
 * Every method should throw JniException.
 * Also jni exception should be considered while typing new method inside JNI
 */
public class NativeDataHelper {

    static {
        System.loadLibrary("core_jnid");
    }

    public enum Currency {
        BTC(0),
        ETH(1);

        private final int value;

        Currency(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum AddressType {
        ADDRESS_EXTERNAL(0),
        ADDRESS_INTERNAL(1);

        private final int value;

        AddressType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static native String makeMnemonic() throws JniException;

    public static native byte[] makeSeed(String mnemonic) throws JniException;

    public static native String makeAccountAddress(byte[] seed, int index, int currency) throws JniException;

    public static native String makeAccountId(byte[] seed) throws JniException;

    /**
     * @param seed
     * @param txHash             hash of prev. transaction. should be given from api
     * @param txPubKey           should be given from api
     * @param txOutIndex         should be given from api
     * @param sum                all sum which is now on address
     * @param amount             amount to spend
     * @param fee
     * @param destinationAddress where money should arrive
     * @param sendAddress        from which address money should depart
     * @return hex of transaction
     * @throws JniException
     */
    public static native byte[] makeTransaction(byte[] seed, String txHash, String txPubKey, int txOutIndex, String sum, String amount, String fee, String destinationAddress, String sendAddress) throws JniException;

    public static native int runTest();

}
