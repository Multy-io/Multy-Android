/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.requests;

import retrofit2.http.Query;


public class AddWalletAddressRequest {

    private int walletIndex;
    private String address;
    private int addressIndex;

    public AddWalletAddressRequest(int walletIndex, String address, int addressIndex) {
        this.walletIndex = walletIndex;
        this.address = address;
        this.addressIndex = addressIndex;
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
