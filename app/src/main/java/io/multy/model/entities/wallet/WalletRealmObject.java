/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities.wallet;


import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;

public class WalletRealmObject extends RealmObject {

    private String name;
    @SerializedName("address")
    private String creationAddress;
    @SerializedName("addresses")
    private RealmList<WalletAddress> addresses;
    private double balance;
    @SerializedName("currency")
    private int currency;
    @SerializedName("addressID")
    private int addressIndex;
    @SerializedName("walletID")
    private String walletIndex;

    public WalletRealmObject() {
    }

    public WalletRealmObject(String name, String address, double balance) {
        this.name = name;
        this.creationAddress = address;
        this.balance = balance;
    }

    public WalletRealmObject(String name, String creationAddress, RealmList<WalletAddress> addresses,
                             double balance, int currency, int addressIndex, String walletIndex) {
        this.name = name;
        this.creationAddress = creationAddress;
        this.addresses = addresses;
        this.balance = balance;
        this.currency = currency;
        this.addressIndex = addressIndex;
        this.walletIndex = walletIndex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public String getCreationAddress() {
        return creationAddress;
    }

    public void setCreationAddress(String creationAddress) {
        this.creationAddress = creationAddress;
    }

    public List<WalletAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(RealmList<WalletAddress> addresses) {
        this.addresses = addresses;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public int getCurrency() {
        return currency;
    }

    public void setCurrency(int currency) {
        this.currency = currency;
    }

    public int getAddressIndex() {
        return addressIndex;
    }

    public void setAddressIndex(int addressIndex) {
        this.addressIndex = addressIndex;
    }

    public String getWalletIndex() {
        return walletIndex;
    }

    public void setWalletIndex(String walletIndex) {
        this.walletIndex = walletIndex;
    }

    @Override
    public String toString() {
        return "Wallet{" +
                "name='" + name + '\'' +
                ", creationAddress='" + creationAddress + '\'' +
                ", addresses=" + addresses +
                ", balance=" + balance +
                ", currency=" + currency +
                ", addressIndex='" + addressIndex + '\'' +
                ", walletIndex='" + walletIndex + '\'' +
                '}';
    }
}
