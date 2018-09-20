/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.requests;

import com.google.gson.annotations.SerializedName;

public class ImportMultisigRequest {

    @SerializedName("currencyID")
    private int currencyId;
    @SerializedName("networkID")
    private int networkId;
    @SerializedName("address")
    private String address;
    @SerializedName("addressIndex")
    private int addressIndex;
    @SerializedName("walletIndex")
    private int walletIndex;
    @SerializedName("walletName")
    private String walletName;
    @SerializedName("multisig")
    private Multisig multisig;

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getAddressIndex() {
        return addressIndex;
    }

    public void setAddressIndex(int addressIndex) {
        this.addressIndex = addressIndex;
    }

    public int getWalletIndex() {
        return walletIndex;
    }

    public void setWalletIndex(int walletIndex) {
        this.walletIndex = walletIndex;
    }

    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }

    public Multisig getMultisig() {
        return multisig;
    }

    public void setMultisig(Multisig multisig) {
        this.multisig = multisig;
    }
}
