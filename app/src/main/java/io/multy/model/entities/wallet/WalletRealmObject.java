/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities.wallet;


import android.content.Intent;

import com.google.gson.annotations.SerializedName;

import io.multy.model.entities.Output;
import io.multy.ui.activities.DonationActivity;
import io.multy.util.Constants;
import io.realm.RealmList;
import io.realm.RealmObject;

@Deprecated
public class WalletRealmObject extends RealmObject {

    @SerializedName("walletname")
    private String name;
    @SerializedName("address")
    private String creationAddress;
    @SerializedName("addresses")
    private RealmList<WalletAddress> addresses;
    private double balance;
    @SerializedName("currencyID")
    private int currency;
    @SerializedName("addressindex")
    private int addressIndex;
    @SerializedName("walletindex")
    private int walletIndex;
    private double pendingBalance;

    public WalletRealmObject() {
    }

    public WalletRealmObject(String name, String address, double balance) {
        this.name = name;
        this.creationAddress = address;
        this.balance = balance;
    }

    public WalletRealmObject(String name, String creationAddress, RealmList<WalletAddress> addresses,
                             double balance, int currency, int addressIndex, int walletIndex) {
        this.name = name;
        this.creationAddress = creationAddress;
        this.addresses = addresses;
        this.balance = balance;
        this.currency = currency;
        this.addressIndex = addressIndex;
        this.walletIndex = walletIndex;
    }

    public double calculateBalance() {
        double calculatedBalance = 0;
        for (WalletAddress walletAddress : addresses) {
            if (walletAddress.getOutputs() != null) {
                for (Output output : walletAddress.getOutputs()) {
                    switch (output.getStatus()) {
                        case Constants.TX_IN_BLOCK_INCOMING:
                            calculatedBalance += Double.valueOf(output.getTxOutAmount());
                            break;
                        case Constants.TX_IN_BLOCK_OUTCOMING:
                            calculatedBalance -= Double.valueOf(output.getTxOutAmount());
                            break;
                        case Constants.TX_CONFIRMED_INCOMING:
                            calculatedBalance += Double.valueOf(output.getTxOutAmount());
                            break;
                        case Constants.TX_CONFIRMED_OUTCOMING:
                            calculatedBalance -= Double.valueOf(output.getTxOutAmount());
                            break;
                    }
                }
            }
        }

        return calculatedBalance;
    }

    public double calculatePendingBalance() {
        double pendingBalance = 0;
        for (WalletAddress walletAddress : addresses) {
            if (walletAddress.getOutputs() != null) {
                for (Output output : walletAddress.getOutputs()) {
                    switch (output.getStatus()) {
                        case Constants.TX_MEMPOOL_INCOMING:
                            pendingBalance += Double.valueOf(output.getTxOutAmount());
                            break;
                        case Constants.TX_MEMPOOL_OUTCOMING:
                            pendingBalance -= Double.valueOf(output.getTxOutAmount());
                            break;
                    }
                }
            }
        }

        return pendingBalance;
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

    public RealmList<WalletAddress> getAddresses() {
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

    public int getWalletIndex() {
        return walletIndex;
    }

    public void setWalletIndex(int walletIndex) {
        this.walletIndex = walletIndex;
    }

    public String getBalanceWithCode(CurrencyCode currencyCode) {
        return String.valueOf(getCurrency()).concat(Constants.SPACE).concat(currencyCode.name());
    }

    public String getBalanceFiatWithCode(Double exchangePrice, CurrencyCode currencyCode) {
        return String.valueOf(getCurrency() * exchangePrice).concat(Constants.SPACE).concat(currencyCode.name());
    }

    public double getPendingBalance() {
        return pendingBalance;
    }

    public void setPendingBalance(double pendingBalance) {
        this.pendingBalance = pendingBalance;
    }

    public long getAvailableBalance() {
        long result = 0;
        for (WalletAddress walletAddress : getAddresses()) {
            for (Output output : walletAddress.getOutputs()) {
                if (output.getStatus() == Constants.TX_CONFIRMED_INCOMING || output.getStatus() == Constants.TX_IN_BLOCK_INCOMING) {
                    result += Long.valueOf(output.getTxOutAmount());
                }
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return "WalletRealmObject{" +
                "name='" + name + '\'' +
                ", creationAddress='" + creationAddress + '\'' +
                ", addresses=" + addresses +
                ", balance=" + balance +
                ", currency=" + currency +
                ", addressIndex=" + addressIndex +
                ", walletIndex=" + walletIndex +
                ", pendingBalance=" + pendingBalance +
                '}';
    }
}
