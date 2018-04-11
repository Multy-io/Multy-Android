/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model.entities.wallet;

import com.google.gson.annotations.SerializedName;

import java.math.BigInteger;
import java.util.ArrayList;

import io.multy.R;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

public class EthWallet extends RealmObject {

    public final static BigInteger DIVISOR = BigInteger.valueOf((long) Math.pow(10, 18));

    @SerializedName("nonce")
    private String nonce;
    @SerializedName("balance")
    private String balance;
    @SerializedName("addresses")
    private RealmList<WalletAddress> addresses;

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public RealmList<WalletAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(RealmList<WalletAddress> addresses) {
        this.addresses = addresses;
    }

    public EthWallet asRealmObject(Realm realm) {
        EthWallet ethWallet = realm.createObject(EthWallet.class);
        ethWallet.setAddresses(new RealmList<>());

        for (WalletAddress walletAddress : getAddresses()) {
            ethWallet.getAddresses().add(realm.copyToRealm(walletAddress));
        }
        ethWallet.setNonce(nonce);
        ethWallet.setBalance(balance);
        return ethWallet;
    }
}
