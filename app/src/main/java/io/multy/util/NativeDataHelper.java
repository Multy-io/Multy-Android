/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;


import java.util.HashMap;
import java.util.Map;

import io.multy.BuildConfig;

/**
 * Every method should throw JniException.
 * Also jni exception should be considered while typing new method inside JNI
 */
public class NativeDataHelper {

    static {
        if (BuildConfig.DEBUG) {
            System.loadLibrary("multy_core_jnid");
        } else {
            System.loadLibrary("multy_core_jni");
        }
    }

    public enum Blockchain {
        //        BTC(0x80000000),
//        ETH(0x8000003c);
        BTC(0),
        ETH(60),
        EOS(194);

        private final int value;

        Blockchain(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        private static Map map = new HashMap<>();

        static {
            for (Blockchain blockchain : Blockchain.values()) {
                map.put(blockchain.value, blockchain);
            }
        }

        public static Blockchain valueOf(int blockchainId) {
            return (Blockchain) map.get(blockchainId);
        }
    }

    //TODO make own network ids for each chain
    public enum NetworkId {
        MAIN_NET(0),
        TEST_NET(1),
        ETH_MAIN_NET(1),
        RINKEBY(4);

        private final int value;

        NetworkId(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        private static Map map = new HashMap<>();

        static {
            for (NetworkId networkId : NetworkId.values()) {
                map.put(networkId.value, networkId);
            }
        }

        public static NetworkId valueOf(int networkId) {
            return (NetworkId) map.get(networkId);
        }
    }

    public static native String makeMnemonic() throws JniException;

    public static native byte[] makeSeed(String mnemonic) throws JniException;

    public static native String makeAccountAddress(byte[] seed, int walletIndex, int addressIndex,
                                                   int blockchain, int type) throws JniException;

    public static native String makeAccountId(byte[] seed) throws JniException;

    public static native int runTest();

    public static native byte[] makeTransaction(long id, int networkId, byte[] seed, int walletIndex, String amountToSpend,
                                                String feePerByte, String donationAmount, String destinationAddress,
                                                String changeAddress, String donationAddress, boolean payFee) throws JniException;

    public static native byte[] digestSha3256(byte[] s) throws JniException;

    public static native String getMyPrivateKey(byte[] seed, int walletIndex, int addressIndex,
                                                int blockchain, int type) throws JniException;

    public static native String getDictionary() throws JniException;

    public static native void isValidAddress(String address, int blockchain, int type) throws JniException;

    public static native byte[] makeTransactionETH(byte[] seed, int walletIndex, int addressIndex, int chainId, int networkId, String balance, String amount, String destionationAddress, String gasLimit, String gasPrice, String nonce) throws JniException;

    public static native byte[] makeTransactionEthPayload(byte[] seed, int walletIndex, int addressIndex, int chainId, int networkId, String balance, String amount, String destionationAddress, String gasLimit, String gasPrice, String nonce, String payload) throws JniException;


    /**
     * @param seed
     * @param walletIndex           linked wallet
     * @param addressIndex          linked wallet
     * @param chainId               linked wallet
     * @param networkId             linked wallet
     * @param linkedBalance         linked wallet
     * @param multisigWalletAddress
     * @param amount                spendable by user
     * @param destionationAddress   selected by user
     * @param gasLimit              estiomation "submit"
     * @param gasPrice              selected by user
     * @param nonce                 linked wallet nonce
     * @return
     * @throws JniException
     */
    public static native byte[] makeTransactionMultisigETH(byte[] seed, int walletIndex, int addressIndex, int chainId, int networkId, String linkedBalance, String multisigWalletAddress, String amount, String destionationAddress, String gasLimit, String gasPrice, String nonce) throws JniException;

    /**
     * @param seed
     * @param walletIndex           linked wallet
     * @param addressIndex          linked wallet
     * @param chainId               linked wallet
     * @param networkId             linked wallet
     * @param linkedBalance         linked wallet
     * @param multisigWalletAddress
     * @param requestId             from api as "index"
     * @param gasLimit              estimation "confirm"
     * @param gasPrice              get medium from fee rate request
     * @param nonce                 linked wallet nonce
     * @return
     * @throws JniException
     */
    public static native byte[] confirmTransactionMultisigETH(byte[] seed, int walletIndex, int addressIndex, int chainId, int networkId, String linkedBalance, String multisigWalletAddress, String requestId, String gasLimit, String gasPrice, String nonce) throws JniException;

    public static native String getLibraryVersion() throws JniException;

    //    final String key = NativeDataHelper.getPublicKey(194, 0, "5KHUWnDCA7SxbCoEbLdhFZjqsqMFq3K7wXAEYoRp3P6AKQdX7BY");
    public static native String getPublicKey(int blockChain, int netType, String key);

    public static native byte[] createEthMultisigWallet(byte[] seed, int walletIndex, int addressIndex, int chainId, int netType, String balance,
                                                        String gasLimit, String gasPrice, String nonce, String factoryAddress, String ownerAddress, int confirmation, String price) throws JniException;

    public static native byte[] makeTransactionETHFromKey(String key, int chainId, int networkId, String balance, String amount, String destionationAddress, String gasLimit, String gasPrice, String nonce) throws JniException;

    public static native byte[] createEthMultisigWalletFromKey(String privateKey, int chainId, int netType, String balance,
                                                               String gasLimit, String gasPrice, String nonce, String factoryAddress, String ownerAddress, int confirmation, String price) throws JniException;

    public static native String makeAccountAddressFromKey(String privateKey, int chainId, int networkId) throws JniException;

    public static native byte[] makeTransactionMultisigETHFromKey(String privatKey, int chainId, int networkId, String linkedBalance, String multisigWalletAddress, String amount, String destionationAddress, String gasLimit, String gasPrice, String nonce) throws JniException;

    public static native byte[] confirmTransactionMultisigETHFromKey(String privateKey, int chainId, int networkId, String linkedBalance, String multisigWalletAddress, String requestId, String gasLimit, String gasPrice, String nonce) throws JniException;

    public static native String bruteForceAddress(byte[] seed, int walletIndex, int adressIndex, int blockChain, int networkType, String address);
}
