/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import io.multy.Multy;
import io.multy.model.DataManager;
import io.multy.model.entities.Output;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.model.entities.wallet.WalletRealmObject;

public class SendTransactionModel {

    private final static String TAG = SendTransactionModel.class.getSimpleName();

    private int[] outIds;
    private String[] hashes;
    private String[] pubKeys;
    private String[] amounts;
    private WalletRealmObject wallet;
    private ArrayList<WalletAddress> addresses;
    private int addressIndex;

    public SendTransactionModel(int walletIndex, String amount) {
        wallet = new DataManager(Multy.getContext()).getWallet(walletIndex);
        initAddresses(Long.valueOf(amount));
        Log.i(TAG, "consctuctor");
    }

    public void initAddresses(long amount) {
        long spendableAmount = 0;
        addresses = new ArrayList<>();

        for (int i = 1; i < wallet.getAddresses().size(); i++){
            addresses.add(wallet.getAddresses().get(i));
        }

//        for (WalletAddress walletAddress : wallet.getAddresses()) {
//            if (walletAddress.getAmount() != 0) {
//                spendableAmount += walletAddress.getAmount();
//                addresses.add(walletAddress);
//            }
//
//            if (spendableAmount > amount) {
//                break;
//            }
//        }

        Log.i(TAG, "init addresses " + addresses.size());
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
        Log.i(TAG, "setup fields called with index " + index);
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
        Log.i(TAG, "get addresses indexes called");
        int[] indexes = new int[addresses.size()];

        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = addresses.get(i).getIndex();
        }

        return indexes;
    }

    public int[] getOutIds() {
        for (int id : outIds){
            Log.i("wise", "id " + id);
        }
        return outIds;
    }

    public String[] getHashes() {
        Log.i("wise", "hashes " + Arrays.toString(hashes));
        return hashes;
    }

    public String[] getPubKeys() {
        Log.i("wise", "pubkeys " + Arrays.toString(pubKeys));
        return pubKeys;
    }

    public String[] getAmounts() {
        Log.i("wise", "amounts " + Arrays.toString(amounts));
        return amounts;
    }
}
