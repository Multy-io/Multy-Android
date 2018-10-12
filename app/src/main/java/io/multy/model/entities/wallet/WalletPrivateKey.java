/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities.wallet;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class WalletPrivateKey extends RealmObject {

    public static final String WALLET_ADDRESS = "walletAddress";
    public static final String PRIVATE_KEY = "privateKey";
    public static final String CURRENCY_ID = "currencyId";
    public static final String NETWORK_ID = "networkId";

    @PrimaryKey
    private String walletAddress;
    private String privateKey;
    private int currencyId;
    private int networkId;

    public WalletPrivateKey() {}

    public WalletPrivateKey(String walletAddress, String privateKey, int currencyId, int networkId) {
        this.walletAddress = walletAddress;
        this.privateKey = privateKey;
        this.currencyId = currencyId;
        this.networkId = networkId;
    }

    public String getWalletId() {
        return walletAddress;
    }

    public void setWalletId(String walletId) {
        this.walletAddress = walletId;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public int getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(int currencyId) {
        this.currencyId = currencyId;
    }

    public int getNetworkId() {
        return networkId;
    }

    public void setNetworkId(int networkId) {
        this.networkId = networkId;
    }
}
