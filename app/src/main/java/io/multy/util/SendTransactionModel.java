/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;

import java.util.ArrayList;

import io.multy.model.entities.Output;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.storage.RealmManager;

/**
 * Used only from JNI to init data for BTC transaction.
 */
public class SendTransactionModel {

    private final static String TAG = SendTransactionModel.class.getSimpleName();

    private int[] outIds;
    private String[] hashes;
    private String[] pubKeys;
    private String[] amounts;
    private Wallet wallet;
    private ArrayList<WalletAddress> addresses;
    private int addressIndex;

    public SendTransactionModel(long walletId, String amount) {
        wallet = RealmManager.getAssetsDao().getWalletById(walletId);
        initAddresses(Long.valueOf(amount));
    }

    public void initAddresses(long amount) {
        addresses = new ArrayList<>();

        for (WalletAddress walletAddress : wallet.getBtcWallet().getAddresses()) {
            if (walletAddress.getAmount() != 0) {
                addresses.add(walletAddress);
            }
        }
    }

    private WalletAddress getAddressByIndex(int index) {
        for (WalletAddress walletAddress : addresses) {
            if (walletAddress.getIndex() == index) {
                return walletAddress;
            }
        }

        return null;
    }

    public void setupFields(int index) {
        WalletAddress walletAddress = getAddressByIndex(index);

        final int size = walletAddress.getOutputs().size();
        ArrayList<Integer> outIds = new ArrayList<>(size);
        ArrayList<String> hashes = new ArrayList<>(size);
        ArrayList<String> amounts = new ArrayList<>(size);
        ArrayList<String> pubKeys = new ArrayList<>(size);

        for (Output output : walletAddress.getOutputs()) {
            outIds.add(output.getTxOutId());
            hashes.add(output.getTxId());
            amounts.add(output.getTxOutAmount());
            pubKeys.add(output.getTxOutScript());
        }

        this.outIds = new int[outIds.size()];

        for (int i = 0; i < outIds.size(); i++) {
            this.outIds[i] = outIds.get(i);
        }

        this.hashes = hashes.toArray(new String[hashes.size()]);
        this.amounts = amounts.toArray(new String[amounts.size()]);
        this.pubKeys = pubKeys.toArray(new String[pubKeys.size()]);
    }

    public int[] getAddressesIndexes() {
        int[] indexes = new int[addresses.size()];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = addresses.get(i).getIndex();
        }

        return indexes;
    }

    public int[] getOutIds() {
        return outIds;
    }

    public String[] getHashes() {
        return hashes;
    }

    public String[] getPubKeys() {
        return pubKeys;
    }

    public String[] getAmounts() {
        return amounts;
    }
}
