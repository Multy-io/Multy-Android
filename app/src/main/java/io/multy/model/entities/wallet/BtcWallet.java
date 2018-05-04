/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities.wallet;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.math.BigInteger;

import io.multy.R;
import io.multy.model.entities.Output;
import io.multy.util.Constants;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

public class BtcWallet extends RealmObject {

    public final static BigDecimal DIVISOR = new BigDecimal(Math.pow(10, 8));

    @SerializedName("address")
    private String creationAddress;

    @SerializedName("addresses")
    private RealmList<WalletAddress> addresses;

    @SerializedName("addressindex")
    private int addressIndex;

    private long availableBalance;

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

    public int getAddressIndex() {
        return addressIndex;
    }

    public void setAddressIndex(int addressIndex) {
        this.addressIndex = addressIndex;
    }

    public long getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(long availableBalance) {
        this.availableBalance = availableBalance;
    }

    public long calculateBalance() {
        long calculatedBalance = 0;
        for (WalletAddress walletAddress : addresses) {
            calculatedBalance += walletAddress.getAmount();
//            if (walletAddress.getOutputs() != null) {
//                for (Output output : walletAddress.getOutputs()) {
//                    switch (output.getStatus()) {
//                        case Constants.TX_IN_BLOCK_INCOMING:
//                            calculatedBalance += Long.valueOf(output.getTxOutAmount());
//                            break;
//                        case Constants.TX_IN_BLOCK_OUTCOMING:
//                            calculatedBalance -= Long.valueOf(output.getTxOutAmount());
//                            break;
//                        case Constants.TX_CONFIRMED_INCOMING:
//                            calculatedBalance += Long.valueOf(output.getTxOutAmount());
//                            break;
//                        case Constants.TX_CONFIRMED_OUTCOMING:
//                            calculatedBalance -= Long.valueOf(output.getTxOutAmount());
//                            break;
//                    }
//                }
//            }
        }

        return calculatedBalance;
    }

    public long calculateAvailableBalance() {
        availableBalance = 0;
        for (WalletAddress walletAddress : addresses) {
            if (walletAddress.getOutputs() != null) {
                for (Output output : walletAddress.getOutputs()) {
                    if (output.getStatus() == Constants.TX_CONFIRMED_INCOMING || output.getStatus() == Constants.TX_IN_BLOCK_INCOMING) {
                        availableBalance += Long.valueOf(output.getTxOutAmount());
                    }
                }
            }
        }

        return availableBalance;
    }

    public BtcWallet asRealmObject(Realm realm) {
        BtcWallet btcWallet = realm.createObject(BtcWallet.class);
        btcWallet.setAddresses(new RealmList<>());

        for (WalletAddress walletAddress : getAddresses()) {
            btcWallet.getAddresses().add(realm.copyToRealm(walletAddress));
        }
        btcWallet.setAddressIndex(addressIndex);
        btcWallet.setAvailableBalance(availableBalance);
        btcWallet.setCreationAddress(creationAddress);
        return btcWallet;
    }
}
