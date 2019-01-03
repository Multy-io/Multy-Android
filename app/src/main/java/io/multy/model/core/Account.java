/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.core;

import com.google.gson.annotations.SerializedName;
import com.samwolfand.oneprefs.Prefs;

import io.multy.storage.RealmManager;
import io.multy.util.Constants;
import io.multy.util.JniException;
import io.multy.util.NativeDataHelper;

public class Account {

    public static final int ACCOUNT_TYPE_DEFAULT = 0;

    @SerializedName("type")
    private int type = ACCOUNT_TYPE_DEFAULT;
    @SerializedName("private_key")
    private String privateKey;

    public Account(int type, String privateKey) {
        this.type = type;
        this.privateKey = privateKey;
    }

    public Account() {
    }

    public static Account getAccount(int walletIndex, int addressIndex, int networkId) {
        try {
            final String key = NativeDataHelper.getMyPrivateKey(RealmManager.getSettingsDao().getSeed().getSeed(), walletIndex, addressIndex,
                    NativeDataHelper.Blockchain.ETH.getValue(), Prefs.getBoolean(Constants.PREF_METAMASK_MODE, false) ? NativeDataHelper.NetworkId.ETH_MAIN_NET.getValue() : networkId);
            return new Account(ACCOUNT_TYPE_DEFAULT, key);
        } catch (JniException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
