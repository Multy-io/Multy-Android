/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.responses;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.multy.model.entities.wallet.WalletAddress;

public class WalletInfo {

    @SerializedName("Chain")
    private Integer chain;
    @SerializedName("WalletID")
    private Integer walletID;
    @SerializedName("Address")
    private List<WalletAddress> address;

    public Integer getChain() {
        return chain;
    }

    public void setChain(Integer chain) {
        this.chain = chain;
    }

    public Integer getWalletID() {
        return walletID;
    }

    public void setWalletID(Integer walletID) {
        this.walletID = walletID;
    }

    public List<WalletAddress> getAddress() {
        return address;
    }

    public void setAddress(List<WalletAddress> address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "WalletInfo{" +
                "chain=" + chain +
                ", walletID=" + walletID +
                ", address=" + address +
                '}';
    }
}
