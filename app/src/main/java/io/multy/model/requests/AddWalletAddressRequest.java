/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.requests;

import com.google.gson.annotations.SerializedName;

import retrofit2.http.Query;


public class AddWalletAddressRequest {

    private int walletIndex;
    private String address;
    private int addressIndex;

    @SerializedName("networkID")
    private int networkId;
    @SerializedName("currencyID")
    private int currencyId;

    public AddWalletAddressRequest(int walletIndex, String address, int addressIndex, int networkId, int currencyId) {
        this.walletIndex = walletIndex;
        this.address = address;
        this.addressIndex = addressIndex;
        this.networkId = networkId;
        this.currencyId = currencyId;
    }

    public int getNetworkId() {
        return networkId;
    }

    public int getCurrencyId() {
        return currencyId;
    }

    public int getWalletIndex() {
        return walletIndex;
    }

    public String getAddress() {
        return address;
    }

    public int getAddressIndex() {
        return addressIndex;
    }

    @Override
    public String toString() {
        return "AddWalletAddressRequest{" +
                "walletIndex=" + walletIndex +
                ", address='" + address + '\'' +
                ", addressIndex=" + addressIndex +
                '}';
    }
}
